import request from '@/utils/request'

export interface MediaFile {
  id: number
  taskId: number
  type: string
  name: string
  originalName: string
  path: string
  bucket: string
  sizeBytes: number
  surface: string
  keyPoint: string
  durationSec: number
  checksum: string
  uploadedBy: number
  uploadedAt: string
}

export interface InitUploadParams {
  fileMd5: string
  fileName: string
  fileSize: number
  totalChunks: number
  taskId: number
  fileType: string
}

export interface InitUploadResult {
  newFile: boolean
  uploadTaskId: number | null
  uploadedChunks: number[] | null
}

export interface MergeChunksParams {
  fileMd5: string
  surface?: string
  keyPoint?: string
}

// 初始化上传（秒传检测 + 断点续传）
export function initUpload(data: InitUploadParams) {
  return request.post<any, InitUploadResult>('/media/upload/init', data)
}

// 上传分片
export function uploadChunk(formData: FormData) {
  return request.post('/media/upload/chunk', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 合并分片
export function mergeChunks(data: MergeChunksParams) {
  return request.post<any, MediaFile>('/media/upload/merge', data)
}

// 获取已上传分片列表
export function getUploadedChunks(fileMd5: string) {
  return request.get<any, number[]>(`/media/upload/chunks/${fileMd5}`)
}

// 分页查询媒体文件
export function getMediaFilePage(params: {
  pageNum: number
  pageSize: number
  taskId?: number
  type?: string
}) {
  return request.get<any, { records: MediaFile[]; total: number }>('/media/files', { params })
}

// 获取媒体文件详情
export function getMediaFileDetail(id: number) {
  return request.get<any, MediaFile>(`/media/files/${id}`)
}

// 删除媒体文件
export function deleteMediaFile(id: number) {
  return request.delete(`/media/files/${id}`)
}

// 获取下载URL
export function getDownloadUrl(id: number) {
  return request.get<any, string>(`/media/files/${id}/download`)
}

// 获取预览URL
export function getPreviewUrl(id: number) {
  return request.get<any, string>(`/media/files/${id}/preview`)
}
