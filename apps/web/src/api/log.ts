import request from '@/utils/request'

export interface LoginLog {
  id: number
  userId: number
  empNo: string
  name: string
  ip: string
  source: string
  userAgent: string
  loginTime: string
  status: number
  failReason: string
}

export interface OperationLog {
  id: number
  userId: number
  empNo: string
  name: string
  module: string
  action: string
  detail: string
  ip: string
  time: string
  result: string
  errorMsg: string
}

// 分页查询登录日志
export function getLoginLogPage(params: {
  pageNum: number
  pageSize: number
  empNo?: string
  status?: number
  source?: string
}) {
  return request.get<any, { records: LoginLog[]; total: number }>('/admin/login-logs', { params })
}

// 分页查询操作日志
export function getOperationLogPage(params: {
  pageNum: number
  pageSize: number
  empNo?: string
  module?: string
  result?: string
  startTime?: string
  endTime?: string
}) {
  return request.get<any, { records: OperationLog[]; total: number }>('/admin/operation-logs', { params })
}
