import request from '@/utils/request'

export interface Project {
  id: number
  name: string
  code: string
  owner: string
  location: string
  lat: number
  lng: number
  status: string
  managerId: number
  description: string
  createdAt: string
  updatedAt: string
}

export interface ProjectDTO {
  name: string
  code?: string
  owner?: string
  location?: string
  lat?: number
  lng?: number
  managerId?: number
  description?: string
}

// 分页查询项目
export function getProjectPage(params: {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: string
}) {
  return request.get<any, { records: Project[]; total: number }>('/inspection/projects', { params })
}

// 获取所有项目
export function getAllProjects() {
  return request.get<any, Project[]>('/inspection/projects/all')
}

// 获取项目详情
export function getProjectDetail(id: number) {
  return request.get<any, Project>(`/inspection/projects/${id}`)
}

// 创建项目
export function createProject(data: ProjectDTO) {
  return request.post<any, Project>('/inspection/projects', data)
}

// 更新项目
export function updateProject(id: number, data: ProjectDTO) {
  return request.put<any, Project>(`/inspection/projects/${id}`, data)
}

// 删除项目
export function deleteProject(id: number) {
  return request.delete(`/inspection/projects/${id}`)
}
