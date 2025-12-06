import request from '@/utils/request'

/**
 * 上传文件（通用）
 */
export function uploadFile(file, type = 'chat') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  
  return request({
    url: '/v1/upload/file',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 上传图片
 */
export function uploadImage(file) {
  return uploadFile(file, 'image')
}

/**
 * 上传视频
 */
export function uploadVideo(file) {
  return uploadFile(file, 'video')
}

/**
 * 上传头像
 */
export function uploadAvatar(file) {
  return uploadFile(file, 'avatar')
}

/**
 * 上传聊天文件
 */
export function uploadChatFile(file) {
  return uploadFile(file, 'chat')
}
