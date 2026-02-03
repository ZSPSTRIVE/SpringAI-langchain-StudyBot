<template>
  <div class="doc-config-page">
    <PageHeader
      title="查重与降重配置"
      description="配置文档查重算法阈值与AI降重策略"
      :show-back="false"
      :show-home="true"
      :breadcrumbs="[{ label: '查重与降重配置' }]"
    />

    <el-row
      :gutter="24"
      class="config-row"
    >
      <el-col
        :xs="24"
        :md="12"
      >
        <el-card class="config-card">
          <template #header>
            <div class="card-header">
              <el-icon><DataAnalysis /></el-icon>
              <span>查重算法阈值</span>
            </div>
          </template>

          <el-form
            :model="similarity"
            label-width="140px"
          >
            <el-form-item label="MinHash 阈值">
              <el-input-number
                v-model="similarity.minhashThreshold"
                :min="0"
                :max="1"
                :step="0.01"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="LCS 阈值">
              <el-input-number
                v-model="similarity.lcsThreshold"
                :min="0"
                :max="1"
                :step="0.01"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="语义相似度阈值">
              <el-input-number
                v-model="similarity.semanticThreshold"
                :min="0"
                :max="1"
                :step="0.01"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="高风险提醒阈值">
              <el-input-number
                v-model="similarity.highRiskThreshold"
                :min="0"
                :max="100"
                :step="1"
                :precision="0"
                style="width: 100%"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col
        :xs="24"
        :md="12"
      >
        <el-card class="config-card">
          <template #header>
            <div class="card-header">
              <el-icon><Cpu /></el-icon>
              <span>AI 降重策略</span>
            </div>
          </template>

          <el-form
            :model="ai"
            label-width="140px"
          >
            <el-form-item label="模型名称">
              <el-select
                v-model="ai.model"
                placeholder="选择 AI 模型"
              >
                <el-option-group label="硅基流动 · 通义千问 / DeepSeek">
                  <el-option
                    label="Qwen2.5-7B-Instruct"
                    value="Qwen/Qwen2.5-7B-Instruct"
                  />
                  <el-option
                    label="Qwen2-72B-Instruct"
                    value="Qwen/Qwen2-72B-Instruct"
                  />
                  <el-option
                    label="DeepSeek-V2.5"
                    value="deepseek-ai/DeepSeek-V2.5"
                  />
                </el-option-group>
                <el-option-group label="阿里通义千问">
                  <el-option
                    label="qwen-turbo"
                    value="qwen-turbo"
                  />
                  <el-option
                    label="qwen-plus"
                    value="qwen-plus"
                  />
                  <el-option
                    label="qwen-max"
                    value="qwen-max"
                  />
                </el-option-group>
                <el-option-group label="智谱 GLM">
                  <el-option
                    label="GLM-4"
                    value="glm-4"
                  />
                  <el-option
                    label="GLM-4-Flash"
                    value="glm-4-flash"
                  />
                  <el-option
                    label="GLM-3-Turbo"
                    value="glm-3-turbo"
                  />
                </el-option-group>
                <el-option-group label="MiniMax">
                  <el-option
                    label="abab5.5-chat"
                    value="abab5.5-chat"
                  />
                  <el-option
                    label="abab6-chat"
                    value="abab6-chat"
                  />
                </el-option-group>
                <el-option-group label="Kimi / Moonshot">
                  <el-option
                    label="moonshot-v1-8k"
                    value="moonshot-v1-8k"
                  />
                  <el-option
                    label="moonshot-v1-32k"
                    value="moonshot-v1-32k"
                  />
                  <el-option
                    label="moonshot-v1-128k"
                    value="moonshot-v1-128k"
                  />
                </el-option-group>
              </el-select>
            </el-form-item>
            <el-form-item label="温度 (temperature)">
              <el-input-number
                v-model="ai.temperature"
                :min="0"
                :max="1"
                :step="0.05"
                :precision="2"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="最大Tokens">
              <el-input-number
                v-model="ai.maxTokens"
                :min="256"
                :max="4096"
                :step="64"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="默认降重风格">
              <el-select
                v-model="ai.defaultStyle"
                placeholder="选择默认风格"
              >
                <el-option
                  label="学术降重"
                  value="ACADEMIC"
                />
                <el-option
                  label="通顺化"
                  value="FLUENCY"
                />
                <el-option
                  label="扩写"
                  value="EXPAND"
                />
                <el-option
                  label="逻辑强化"
                  value="LOGIC_ENHANCE"
                />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="actions-card">
      <div class="actions">
        <el-button
          type="primary"
          :loading="saving"
          @click="handleSave"
        >
          保存配置
        </el-button>
        <el-button
          :loading="loading"
          @click="loadConfig"
        >
          重新加载
        </el-button>
      </div>
      <div class="tips">
        <p>提示：</p>
        <ul>
          <li>阈值改动会影响查重结果，高风险阈值建议设置在 60-80% 之间。</li>
          <li>AI 模型参数应与后端 LangChain4j / SpringAI 配置保持一致。</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Cpu } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { getDocConfig, saveDocConfig } from '@/api/doc'

const loading = ref(false)
const saving = ref(false)

// 查重算法配置
const similarity = reactive({
  minhashThreshold: 0.8,
  lcsThreshold: 0.75,
  semanticThreshold: 0.8,
  highRiskThreshold: 70
})

// AI 配置
const ai = reactive({
  model: 'qwen-turbo',
  temperature: 0.3,
  maxTokens: 2048,
  defaultStyle: 'ACADEMIC'
})

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await getDocConfig()
    const config = res.data || {}

    const similarityCfg = config.similarity || {}
    const aiCfg = config.ai || {}

    similarity.minhashThreshold = parseFloat(similarityCfg['minhashThreshold'] ?? similarity.minhashThreshold)
    similarity.lcsThreshold = parseFloat(similarityCfg['lcsThreshold'] ?? similarity.lcsThreshold)
    similarity.semanticThreshold = parseFloat(similarityCfg['semanticThreshold'] ?? similarity.semanticThreshold)
    similarity.highRiskThreshold = parseFloat(similarityCfg['highRiskThreshold'] ?? similarity.highRiskThreshold)

    ai.model = aiCfg['model'] ?? ai.model
    ai.temperature = parseFloat(aiCfg['temperature'] ?? ai.temperature)
    ai.maxTokens = parseInt(aiCfg['maxTokens'] ?? ai.maxTokens)
    ai.defaultStyle = aiCfg['defaultStyle'] ?? ai.defaultStyle
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    const payload = {
      similarity: {
        minhashThreshold: similarity.minhashThreshold,
        lcsThreshold: similarity.lcsThreshold,
        semanticThreshold: similarity.semanticThreshold,
        highRiskThreshold: similarity.highRiskThreshold
      },
      ai: {
        model: ai.model,
        temperature: ai.temperature,
        maxTokens: ai.maxTokens,
        defaultStyle: ai.defaultStyle
      }
    }

    await saveDocConfig(payload)
    ElMessage.success('配置保存成功')
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadConfig()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.doc-config-page {
  padding: $spacing-xl;
  animation: fadeIn 0.4s ease;
}

.config-row {
  margin-top: $spacing-xl;
}

.config-card {
  margin-bottom: $spacing-lg;

  .card-header {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;

    .el-icon {
      color: $primary-color;
    }
  }
}

.actions-card {
  margin-top: $spacing-lg;

  .actions {
    display: flex;
    gap: $spacing-md;
    margin-bottom: $spacing-md;
  }

  .tips {
    font-size: $font-size-sm;
    color: $text-secondary;

    ul {
      margin: $spacing-xs 0 0;
      padding-left: 1.2em;
    }
  }
}

@media (max-width: $breakpoint-md) {
  .doc-config-page {
    padding: $spacing-md;
  }
}
</style>
