import request from '@/utils/request'

export interface Turbine {
  id: number
  projectId: number
  code: string
  model: string
  lat: number
  lng: number
  altitude: number
  hubHeight: number
  status: string
  createdAt: string
  updatedAt: string
}

export interface TurbineDTO {
  projectId: number
  code: string
  model?: string
  lat: number
  lng: number
  altitude?: number
  hubHeight?: number
}

// 根据项目ID查询机位列表
export function getTurbinesByProject(projectId: number) {
  return request.get<any, Turbine[]>(`/inspection/turbines/project/${projectId}`)
}

// 获取机位详情
export function getTurbineDetail(id: number) {
  return request.get<any, Turbine>(`/inspection/turbines/${id}`)
}

// 创建机位
export function createTurbine(data: TurbineDTO) {
  return request.post<any, Turbine>('/inspection/turbines', data)
}

// 更新机位
export function updateTurbine(id: number, data: TurbineDTO) {
  return request.put<any, Turbine>(`/inspection/turbines/${id}`, data)
}

// 删除机位
export function deleteTurbine(id: number) {
  return request.delete(`/inspection/turbines/${id}`)
}
