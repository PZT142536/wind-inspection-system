import request from '@/utils/request'

export interface AiFinding {
  id: number
  taskId: number
  mediaId: number
  type: string
  severity: string
  confidence: number
  frameTimeSec: number
  frameImagePath: string
  bboxJson: string
  description: string
  status: string
  reviewerId: number
  reviewedAt: string
  createdAt: string
}

export interface AiTaskStats {
  total: number
  pending: number
  highSeverity: number
  mediumSeverity: number
  lowSeverity: number
}

// 分页查询AI发现
export function getAiFindingPage(params: {
  pageNum: number
  pageSize: number
  taskId?: number
  type?: string
  severity?: string
  status?: string
}) {
  return request.get<any, { records: AiFinding[]; total: number }>('/inspection/ai/findings', { params })
}

// 获取AI发现详情
export function getAiFindingDetail(id: number) {
  return request.get<any, AiFinding>(`/inspection/ai/findings/${id}`)
}

// 审核AI发现
export function reviewAiFinding(id: number, status: string) {
  return request.put(`/inspection/ai/findings/${id}/review`, { status })
}

// 获取任务的AI发现统计
export function getAiTaskStats(taskId: number) {
  return request.get<any, AiTaskStats>(`/inspection/ai/findings/stats/${taskId}`)
}
