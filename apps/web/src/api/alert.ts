import request from '@/utils/request'

export interface Alert {
  id: number
  findingId: number
  taskId: number
  targetUserId: number
  type: string
  message: string
  sentAt: string
  readAt: string
  createdAt: string
}

// 分页查询预警
export function getAlertPage(params: {
  pageNum: number
  pageSize: number
  unreadOnly?: boolean
}) {
  return request.get<any, { records: Alert[]; total: number }>('/inspection/alerts', { params })
}

// 标记单条预警已读
export function markAlertRead(id: number) {
  return request.put(`/inspection/alerts/${id}/read`)
}

// 全部标记已读
export function markAllAlertsRead() {
  return request.put('/inspection/alerts/read-all')
}

// 获取未读预警数量
export function getUnreadAlertCount() {
  return request.get<any, number>('/inspection/alerts/unread-count')
}
