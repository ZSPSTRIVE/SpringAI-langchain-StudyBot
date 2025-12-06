package com.qasystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qasystem.dto.DocRewriteRequest;
import com.qasystem.dto.SaveDocVersionRequest;
import com.qasystem.entity.DocDocument;
import com.qasystem.entity.DocParagraph;
import com.qasystem.entity.DocRewriteVersion;
import com.qasystem.mapper.DocDocumentMapper;
import com.qasystem.mapper.DocParagraphMapper;
import com.qasystem.mapper.DocRewriteVersionMapper;
import com.qasystem.service.DocContentFilterService;
import com.qasystem.service.DocOperationLogService;
import com.qasystem.service.DocService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档查重与AI降重-用户侧服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocServiceImpl implements DocService {

    private final DocDocumentMapper docDocumentMapper;
    private final DocParagraphMapper docParagraphMapper;
    private final DocRewriteVersionMapper docRewriteVersionMapper;
    private final DocOperationLogService docOperationLogService;
    private final DocContentFilterService docContentFilterService;
    private final ChatLanguageModel chatLanguageModel;

    /**
     * 上传目录（沿用 upload.path 配置约定，默认为 ./uploads）
     */
    private static final String DEFAULT_UPLOAD_DIR = "uploads/documents";

    @Override
    public Map<String, Object> uploadAndCheck(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择要上传的Word文件");
        }

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "文件名不能为空");
        if (!originalFilename.toLowerCase().endsWith(".doc") && !originalFilename.toLowerCase().endsWith(".docx")) {
            throw new RuntimeException("仅支持上传 .doc / .docx 文件");
        }

        try {
            // 1. 先解析Word段落（必须在 transferTo 之前，否则临时文件会被移走）
            List<String> paragraphs = parseWordParagraphs(file);

            // 2. 保存原始文件到本地（后续可替换为COS存储）
            String fileUrl = saveFile(file);
            if (paragraphs.isEmpty()) {
                throw new RuntimeException("未解析到有效段落");
            }

            // 3. 构建段落实体（相似度稍后统一计算）
            List<DocParagraph> paragraphEntities = new ArrayList<>();
            for (int i = 0; i < paragraphs.size(); i++) {
                String text = paragraphs.get(i);
                DocParagraph p = new DocParagraph();
                p.setParagraphIndex(i);
                p.setOriginalText(text);
                p.setSimilarSource(null);
                p.setSimilarSpans(null);
                LocalDateTime now = LocalDateTime.now();
                p.setCreatedAt(now);
                p.setUpdatedAt(now);
                paragraphEntities.add(p);
            }

            // 4. 基于 MinHash + LCS + 语义相似度，对文档内部段落两两比较，计算每个段落的最高相似度。
            //    段落相似度取与同一文档其他段落的最大组合相似度，范围 0-100。
            computeParagraphSimilarities(paragraphEntities);

            // 5. 计算整体查重率（段落相似度的平均值）
            double overallSimilarity = paragraphEntities.stream()
                    .map(DocParagraph::getSimilarity)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            // 6. 保存文档记录
            DocDocument document = new DocDocument();
            document.setUserId(userId);
            document.setTitle(originalFilename);
            document.setFileUrl(fileUrl);
            document.setStatus("CHECKED");
            document.setOverallSimilarity(overallSimilarity);
            document.setAlgorithmDetail(null);
            LocalDateTime now = LocalDateTime.now();
            document.setCreatedAt(now);
            document.setUpdatedAt(now);
            docDocumentMapper.insert(document);

            // 回填 documentId 保存段落
            Long documentId = document.getId();
            for (DocParagraph p : paragraphEntities) {
                p.setDocumentId(documentId);
                docParagraphMapper.insert(p);
            }

            // 记录敏感词命中情况
            StringBuilder detailBuilder = new StringBuilder();
            detailBuilder.append("上传并查重文档: title=").append(originalFilename)
                    .append(", overallSimilarity=").append(String.format("%.1f", overallSimilarity));
            int hitCount = 0;
            for (DocParagraph p : paragraphEntities) {
                List<String> hits = docContentFilterService.findSensitiveWords(p.getOriginalText());
                if (!hits.isEmpty()) {
                    hitCount += hits.size();
                }
            }
            if (hitCount > 0) {
                detailBuilder.append(", 命中敏感词数量=").append(hitCount);
            }
            docOperationLogService.log(userId, null, "UPLOAD_AND_CHECK", documentId, null, detailBuilder.toString());

            Map<String, Object> result = new HashMap<>();
            result.put("documentId", documentId);
            result.put("title", document.getTitle());
            result.put("overallSimilarity", overallSimilarity);
            result.put("paragraphs", paragraphEntities);
            result.put("fileUrl", fileUrl); // 添加文件保存路径
            result.put("savedPath", System.getProperty("user.dir") + fileUrl.replace("/", "\\")); // 绝对路径
            return result;
        } catch (IOException e) {
            log.error("上传并查重失败", e);
            throw new RuntimeException("文档处理失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getReport(Long documentId) {
        DocDocument document = docDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        List<DocParagraph> paragraphs = docParagraphMapper.selectList(
                new LambdaQueryWrapper<DocParagraph>()
                        .eq(DocParagraph::getDocumentId, documentId)
                        .orderByAsc(DocParagraph::getParagraphIndex)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("documentId", document.getId());
        result.put("title", document.getTitle());
        result.put("status", document.getStatus());
        result.put("overallSimilarity", document.getOverallSimilarity());
        result.put("algorithmDetail", document.getAlgorithmDetail());
        result.put("paragraphs", paragraphs);
        return result;
    }

    @Override
    public Map<String, Object> rewriteText(Long userId, DocRewriteRequest request) {
        String text = request.getText();
        if (text == null || text.isBlank()) {
            throw new RuntimeException("改写内容不能为空");
        }
        String style = request.getStyle();

        // 构造系统提示词，后续可以从配置表/模板中加载
        String systemPrompt = buildRewriteSystemPrompt(style);

        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(text));

        log.info("调用AI进行文档降重，userId={}, documentId={}, paragraphId={}, style={}",
                userId, request.getDocumentId(), request.getParagraphId(), style);

        Response<AiMessage> response = chatLanguageModel.generate(messages);
        String rewritten = response.content().text();

        Map<String, Object> result = new HashMap<>();
        result.put("rewrittenText", rewritten);
        result.put("style", style);
        result.put("tokensUsed", response.tokenUsage() != null ? response.tokenUsage().totalTokenCount() : 0);

        // 记录敏感词和操作日志
        List<String> hits = docContentFilterService.findSensitiveWords(rewritten);
        StringBuilder detailBuilder = new StringBuilder("AI降重");
        if (request.getDocumentId() != null) {
            detailBuilder.append(" docId=").append(request.getDocumentId());
        }
        if (request.getParagraphId() != null) {
            detailBuilder.append(", paragraphId=").append(request.getParagraphId());
        }
        if (!hits.isEmpty()) {
            detailBuilder.append(", 命中敏感词: ").append(String.join("/", hits));
        }
        docOperationLogService.log(userId, null, "REWRITE", request.getDocumentId(), request.getParagraphId(), detailBuilder.toString());

        return result;
    }

    @Override
    public DocRewriteVersion saveVersion(Long userId, Long documentId, SaveDocVersionRequest request) {
        if (request.getContent() == null) {
            throw new RuntimeException("版本内容不能为空");
        }

        // 查询当前最大版本号
        Integer maxVersion = docRewriteVersionMapper.selectList(
                        new LambdaQueryWrapper<DocRewriteVersion>()
                                .eq(DocRewriteVersion::getDocumentId, documentId)
                ).stream()
                .map(DocRewriteVersion::getVersionNo)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);

        DocRewriteVersion version = new DocRewriteVersion();
        version.setDocumentId(documentId);
        version.setVersionNo(maxVersion + 1);
        version.setStyle(request.getStyle());
        version.setContent(request.getContent());
        version.setRemark(request.getRemark());
        version.setCreatedBy(userId);
        version.setCreatedAt(LocalDateTime.now());
        docRewriteVersionMapper.insert(version);

        String detail = "保存文档版本: docId=" + documentId + ", versionNo=" + version.getVersionNo();
        docOperationLogService.log(userId, null, "SAVE_VERSION", documentId, null, detail);
        return version;
    }

    @Override
    public List<DocRewriteVersion> listVersions(Long documentId) {
        return docRewriteVersionMapper.selectList(
                new LambdaQueryWrapper<DocRewriteVersion>()
                        .eq(DocRewriteVersion::getDocumentId, documentId)
                        .orderByDesc(DocRewriteVersion::getVersionNo)
        );
    }

    @Override
    public DocRewriteVersion getVersion(Long versionId) {
        return docRewriteVersionMapper.selectById(versionId);
    }

    /**
     * 保存上传文件到本地目录，并返回相对访问路径
     */
    private String saveFile(MultipartFile file) throws IOException {
        String baseDir = System.getProperty("user.dir");
        Path uploadDir = Paths.get(baseDir, DEFAULT_UPLOAD_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String ext = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : "";
        String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = uploadDir.resolve(newFileName);
        file.transferTo(target.toFile());

        // 返回相对路径，前端可根据需要拼接访问地址
        return "/" + DEFAULT_UPLOAD_DIR + "/" + newFileName;
    }

    /**
     * 解析Word文档为段落列表（忽略空白段落）
     */
    private List<String> parseWordParagraphs(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream(); XWPFDocument document = new XWPFDocument(in)) {
            return document.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .map(text -> text == null ? "" : text.trim())
                    .filter(text -> !text.isEmpty())
                    .collect(Collectors.toList());
        }
    }

    /**
     * 计算文档内部所有段落的相似度：
     * - 先对每一对段落 (i, j) 计算 MinHash/LCS/语义向量三种相似度
     * - 使用加权组合得到最终相似度 combined
     * - 对于每个段落，取与其他段落的最大 combined，相似度范围 0-100
     */
    private void computeParagraphSimilarities(List<DocParagraph> paragraphs) {
        int n = paragraphs.size();
        if (n <= 1) {
            // 单段落文档，视为无重复
            paragraphs.forEach(p -> p.setSimilarity(0.0));
            return;
        }

        double[] maxSim = new double[n];
        Arrays.fill(maxSim, 0.0);

        // 预先取出文本，避免重复访问字段
        String[] texts = new String[n];
        for (int i = 0; i < n; i++) {
            texts[i] = Optional.ofNullable(paragraphs.get(i).getOriginalText()).orElse("");
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                String a = texts[i];
                String b = texts[j];
                if (a.isEmpty() || b.isEmpty()) {
                    continue;
                }

                double minhash = minhashSimilarity(a, b);   // 0-1
                double lcs = lcsSimilarity(a, b);           // 0-1
                double semantic = semanticSimilarity(a, b); // 0-1

                // 加权组合，可根据需要调整权重
                double combined = 0.4 * minhash + 0.3 * lcs + 0.3 * semantic;

                if (combined > maxSim[i]) {
                    maxSim[i] = combined;
                }
                if (combined > maxSim[j]) {
                    maxSim[j] = combined;
                }
            }
        }

        for (int i = 0; i < n; i++) {
            // 转为 0-100 的百分比
            paragraphs.get(i).setSimilarity(maxSim[i] * 100.0);
        }
    }

    // ==================== MinHash Jaccard 相似度 ====================

    private static final int MINHASH_SIZE = 64;
    private static final int MINHASH_PRIME = 2_147_483_647;
    private static final int[] MINHASH_A = new int[MINHASH_SIZE];
    private static final int[] MINHASH_B = new int[MINHASH_SIZE];

    static {
        Random random = new Random(42);
        for (int i = 0; i < MINHASH_SIZE; i++) {
            MINHASH_A[i] = random.nextInt(MINHASH_PRIME - 1) + 1;
            MINHASH_B[i] = random.nextInt(MINHASH_PRIME - 1) + 1;
        }
    }

    private double minhashSimilarity(String a, String b) {
        int[] sigA = buildMinhashSignature(a);
        int[] sigB = buildMinhashSignature(b);
        int match = 0;
        for (int i = 0; i < MINHASH_SIZE; i++) {
            if (sigA[i] == sigB[i]) {
                match++;
            }
        }
        return match / (double) MINHASH_SIZE;
    }

    private int[] buildMinhashSignature(String text) {
        Set<Integer> shingles = buildShingles(text);
        int[] signature = new int[MINHASH_SIZE];
        Arrays.fill(signature, Integer.MAX_VALUE);

        for (int shingle : shingles) {
            for (int i = 0; i < MINHASH_SIZE; i++) {
                long hash = (MINHASH_A[i] * (long) shingle + MINHASH_B[i]) % MINHASH_PRIME;
                int h = (int) hash;
                if (h < signature[i]) {
                    signature[i] = h;
                }
            }
        }
        return signature;
    }

    private Set<Integer> buildShingles(String text) {
        String normalized = text.replaceAll("\\s+", "");
        Set<Integer> set = new HashSet<>();
        int k = 3; // 3-gram
        if (normalized.length() <= k) {
            set.add(normalized.hashCode());
            return set;
        }
        for (int i = 0; i <= normalized.length() - k; i++) {
            String shingle = normalized.substring(i, i + k);
            set.add(shingle.hashCode());
        }
        return set;
    }

    // ==================== LCS 相似度 ====================

    private double lcsSimilarity(String a, String b) {
        // 为避免极长文本导致内存占用过大，对长度做简单裁剪
        int maxLen = 500;
        if (a.length() > maxLen) {
            a = a.substring(0, maxLen);
        }
        if (b.length() > maxLen) {
            b = b.substring(0, maxLen);
        }

        int n = a.length();
        int m = b.length();
        if (n == 0 || m == 0) {
            return 0.0;
        }

        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                char cb = b.charAt(j - 1);
                if (ca == cb) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        int lcsLen = dp[n][m];
        return (2.0 * lcsLen) / (n + m);
    }

    // ==================== 语义向量（简易字符级余弦相似度） ====================

    private double semanticSimilarity(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        Map<Integer, Integer> va = buildCharVector(a);
        Map<Integer, Integer> vb = buildCharVector(b);

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (Integer key : va.keySet()) {
            int ca = va.getOrDefault(key, 0);
            int cb = vb.getOrDefault(key, 0);
            dot += (double) ca * cb;
        }
        for (int v : va.values()) {
            normA += v * (double) v;
        }
        for (int v : vb.values()) {
            normB += v * (double) v;
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private Map<Integer, Integer> buildCharVector(String text) {
        Map<Integer, Integer> map = new HashMap<>();
        text.codePoints()
                .filter(cp -> !Character.isWhitespace(cp))
                .forEach(cp -> map.merge(cp, 1, Integer::sum));
        return map;
    }

    @Override
    public void batchUpdateParagraphs(Long documentId, List<Map<String, Object>> paragraphs) {
        for (Map<String, Object> p : paragraphs) {
            Integer index = (Integer) p.get("paragraphIndex");
            String text = (String) p.get("originalText");
            if (index == null || text == null) continue;

            DocParagraph paragraph = docParagraphMapper.selectOne(
                    new LambdaQueryWrapper<DocParagraph>()
                            .eq(DocParagraph::getDocumentId, documentId)
                            .eq(DocParagraph::getParagraphIndex, index)
            );
            if (paragraph != null) {
                paragraph.setOriginalText(text);
                paragraph.setUpdatedAt(LocalDateTime.now());
                docParagraphMapper.updateById(paragraph);
            }
        }
    }

    @Override
    public byte[] downloadDocument(Long documentId) throws Exception {
        DocDocument document = docDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        List<DocParagraph> paragraphs = docParagraphMapper.selectList(
                new LambdaQueryWrapper<DocParagraph>()
                        .eq(DocParagraph::getDocumentId, documentId)
                        .orderByAsc(DocParagraph::getParagraphIndex)
        );

        // 尝试读取原始文件并修改
        String fileUrl = document.getFileUrl();
        if (fileUrl != null && !fileUrl.isEmpty()) {
            try {
                Path originalPath = Paths.get(System.getProperty("user.dir"), fileUrl.replace("/", "\\"));
                if (Files.exists(originalPath)) {
                    // 读取原始文档
                    try (InputStream inputStream = Files.newInputStream(originalPath);
                         XWPFDocument wordDoc = new XWPFDocument(inputStream)) {
                        
                        // 替换段落内容，保留格式
                        List<XWPFParagraph> docParagraphs = wordDoc.getParagraphs();
                        int paraIndex = 0;
                        
                        for (int i = 0; i < docParagraphs.size() && paraIndex < paragraphs.size(); i++) {
                            XWPFParagraph docPara = docParagraphs.get(i);
                            String originalText = docPara.getText();
                            
                            // 跳过空段落
                            if (originalText == null || originalText.trim().isEmpty()) {
                                continue;
                            }
                            
                            // 替换为修改后的内容
                            DocParagraph newPara = paragraphs.get(paraIndex);
                            String newText = newPara.getOriginalText();
                            
                            // 清空段落内容但保留格式
                            for (int j = docPara.getRuns().size() - 1; j >= 0; j--) {
                                docPara.removeRun(j);
                            }
                            
                            // 添加新内容，使用原有段落格式
                            if (newText != null && !newText.isEmpty()) {
                                XWPFRun run = docPara.createRun();
                                run.setText(newText);
                            }
                            
                            paraIndex++;
                        }
                        
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        wordDoc.write(out);
                        return out.toByteArray();
                    }
                }
            } catch (Exception e) {
                log.warn("无法读取原始文件，将创建新文档: {}", e.getMessage());
            }
        }
        
        // 如果原始文件不存在，创建新文档
        XWPFDocument wordDoc = new XWPFDocument();
        for (DocParagraph p : paragraphs) {
            XWPFParagraph para = wordDoc.createParagraph();
            para.createRun().setText(p.getOriginalText());
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wordDoc.write(out);
        wordDoc.close();
        return out.toByteArray();
    }

    /**
     * 根据风格构造系统Prompt
     */
    private String buildRewriteSystemPrompt(String style) {
        String base = "你是一个学术写作助手，负责对用户提供的中文文本进行改写，以降低查重率，同时保持原意不变。" +
                "请用正式、流畅的中文输出结果，不要包含解释说明，只返回改写后的文本。";
        if (style == null) {
            return base;
        }
        return switch (style.toUpperCase()) {
            case "ACADEMIC" -> base + "风格要求：偏学术论文风格，注意用词严谨、句式多样。";
            case "FLUENCY" -> base + "风格要求：重点提升语句通顺和可读性，可适当简化复杂表达。";
            case "EXPAND" -> base + "风格要求：在保持原意的前提下适度扩写，丰富内容和论证。";
            case "LOGIC_ENHANCE" -> base + "风格要求：增强段落的逻辑性和结构清晰度，可调整语序和增删少量信息。";
            default -> base;
        };
    }
}
