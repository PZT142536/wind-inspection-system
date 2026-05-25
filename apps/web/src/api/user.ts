import request from '@/utils/request'

export interface User {
  id: number
  empNo: string
  name: string
  dept: string
  source: string
  status: number
  lockedUntil: string
  lastLoginTime: string
  lastLoginIp: string
  createdAt: string
  updatedAt: string
}

export interface UserQuery {
  keyword?: string
  status?: number
  pageNum: number
  pageSize: number
}

export interface UserDTO {
  empNo: string
  name: string
  dept?: string
  password?: string
}

// 分页查询用户
export function getUserPage(params: UserQuery) {
  return request.get<any, { records: User[]; total: number }>('/admin/users', { params })
}

// 获取用户详情
export function getUserDetail(id: number) {
  return request.get<any, User>(`/admin/users/${id}`)
}

// 创建用户
export function createUser(data: UserDTO) {
  return request.post<any, User>('/admin/users', data)
}

// 更新用户
export function updateUser(id: number, data: UserDTO) {
  return request.put<any, User>(`/admin/users/${id}`, data)
}

// 删除用户
export function deleteUser(id: number) {
  return request.delete(`/admin/users/${id}`)
}

// 修改用户角色
export function changeUserRoles(id: number, roleIds: number[]) {
  return request.put(`/admin/users/${id}/roles`, roleIds)
}

// 获取用户角色ID列表
export function getUserRoleIds(id: number) {
  return request.get<any, number[]>(`/admin/users/${id}/roles`)
}

// 锁定用户
export function lockUser(id: number, minutes: number = 10) {
  return request.put(`/admin/users/${id}/lock`, null, { params: { minutes } })
}

// 解锁用户
export function unlockUser(id: number) {
  return request.put(`/admin/users/${id}/unlock`)
}

// 重置密码
export function resetPassword(id: number, newPassword: string) {
  return request.put(`/admin/users/${id}/reset-password`, null, { params: { newPassword } })
}
