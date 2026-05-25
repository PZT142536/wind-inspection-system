import request from '@/utils/request'

export interface Role {
  id: number
  code: string
  name: string
  description: string
  createdBy: number
  createdAt: string
  updatedAt: string
}

export interface RoleDTO {
  code: string
  name: string
  description?: string
}

// 分页查询角色
export function getRolePage(params: { pageNum: number; pageSize: number; keyword?: string }) {
  return request.get<any, { records: Role[]; total: number }>('/admin/roles', { params })
}

// 获取所有角色
export function getAllRoles() {
  return request.get<any, Role[]>('/admin/roles/all')
}

// 获取角色详情
export function getRoleDetail(id: number) {
  return request.get<any, Role>(`/admin/roles/${id}`)
}

// 创建角色
export function createRole(data: RoleDTO) {
  return request.post<any, Role>('/admin/roles', data)
}

// 更新角色
export function updateRole(id: number, data: RoleDTO) {
  return request.put<any, Role>(`/admin/roles/${id}`, data)
}

// 删除角色
export function deleteRole(id: number) {
  return request.delete(`/admin/roles/${id}`)
}

// 修改角色权限
export function changeRolePermissions(id: number, permissionIds: number[]) {
  return request.put(`/admin/roles/${id}/permissions`, permissionIds)
}

// 获取角色权限ID列表
export function getRolePermissionIds(id: number) {
  return request.get<any, number[]>(`/admin/roles/${id}/permissions`)
}
