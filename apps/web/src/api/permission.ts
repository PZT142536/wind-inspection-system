import request from '@/utils/request'

export interface Permission {
  id: number
  code: string
  name: string
  type: string
  parentId: number
  path: string
  icon: string
  sortOrder: number
  children?: Permission[]
}

// 获取权限树
export function getPermissionTree() {
  return request.get<any, Permission[]>('/admin/permissions/tree')
}

// 获取所有权限
export function getAllPermissions() {
  return request.get<any, Permission[]>('/admin/permissions')
}

// 根据角色ID查询权限列表
export function getPermissionsByRoleId(roleId: number) {
  return request.get<any, Permission[]>(`/admin/permissions/role/${roleId}`)
}
