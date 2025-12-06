import request from '@/utils/request'

/**
 * 文档查重与AI降重相关API
 */

// 上传Word文档并执行查重
export function uploadAndCheck(file) {
  const formData = new FormData()
  formData.append('file', file)

  return request({
    url: '/v1/doc/upload-check',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取文档查重报告
export function getDocReport(documentId) {
  return request({
    url: `/v1/doc/${documentId}/report`,
    method: 'get'
  })
}

// AI改写/降重
export function rewriteText(data) {
  // data: { documentId?, paragraphId?, text, style? }
  return request({
    url: '/v1/doc/rewrite',
    method: 'post',
    data
  })
}

// 保存文档版本
export function saveDocVersion(documentId, data) {
  // data: { content, style?, remark? }
  return request({
    url: `/v1/doc/${documentId}/versions`,
    method: 'post',
    data
  })
}

// 获取文档版本列表
export function listDocVersions(documentId) {
  return request({
    url: `/v1/doc/${documentId}/versions`,
    method: 'get'
  })
}

// 获取单个版本详情
export function getDocVersion(versionId) {
  return request({
    url: `/v1/doc/version/${versionId}`,
    method: 'get'
  })
}

// 批量更新文档段落
export function batchUpdateParagraphs(documentId, paragraphs) {
  return request({
    url: `/v1/doc/${documentId}/paragraphs`,
    method: 'put',
    data: paragraphs
  })
}

// 下载文档
export function downloadDocument(documentId) {
  return request({
    url: `/v1/doc/${documentId}/download`,
    method: 'get',
    responseType: 'blob'
  })
}

// ==================== 管理端接口 ====================

// 获取查重与降重配置（分组结构）
export function getDocConfig() {
  return request({
    url: '/v1/admin/doc/config',
    method: 'get'
  })
}

// 保存查重与降重配置
export function saveDocConfig(data) {
  return request({
    url: '/v1/admin/doc/config',
    method: 'put',
    data
  })
}

// 获取所有配置原始列表
export function listAllDocConfigs() {
  return request({
    url: '/v1/admin/doc/config/list',
    method: 'get'
  })
}

// 分页查询文档列表
export function getDocPage(params) {
  return request({
    url: '/v1/admin/doc/documents',
    method: 'get',
    params
  })
}

// 获取单个文档详情
export function getDocDetail(documentId) {
  return request({
    url: `/v1/admin/doc/documents/${documentId}`,
    method: 'get'
  })
}

// 删除文档
export function deleteDocument(documentId) {
  return request({
    url: `/v1/admin/doc/documents/${documentId}`,
    method: 'delete'
  })
}

// ==================== 敏感词管理 ====================

// 分页查询敏感词
export function getSensitiveWordPage(params) {
  return request({
    url: '/v1/admin/doc/sensitive-words/page',
    method: 'get',
    params
  })
}

// 新增敏感词
export function createSensitiveWord(data) {
  return request({
    url: '/v1/admin/doc/sensitive-words',
    method: 'post',
    data
  })
}

// 更新敏感词
export function updateSensitiveWord(id, data) {
  return request({
    url: `/v1/admin/doc/sensitive-words/${id}`,
    method: 'put',
    data
  })
}

// 删除敏感词
export function deleteSensitiveWord(id) {
  return request({
    url: `/v1/admin/doc/sensitive-words/${id}`,
    method: 'delete'
  })
}

// ==================== 文档操作日志 ====================

// 分页查询文档操作日志
export function getDocOperationLogPage(params) {
  return request({
    url: '/v1/admin/doc/logs/page',
    method: 'get',
    params
  })
}
