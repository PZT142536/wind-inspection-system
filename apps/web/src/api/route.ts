import request from '@/utils/request'

export interface FlightRoute {
  id: number
  taskId: number
  name: string
  surface: string
  altitude: number
  speed: number
  configJson: string
  waypointsJson: string
  djiMissionJson: string
  createdAt: string
}

export interface GenerateRouteDTO {
  taskId: number
  baseLat: number
  baseLng: number
  hubHeight?: number
  bladeLength?: number
  altitude?: number
  component?: string
  radius?: number
  towerHeight?: number
  bladeNo?: string
}

// 根据任务ID查询航线列表
export function getRoutesByTask(taskId: number) {
  return request.get<any, FlightRoute[]>(`/inspection/routes/task/${taskId}`)
}

// 获取航线详情
export function getRouteDetail(id: number) {
  return request.get<any, FlightRoute>(`/inspection/routes/${id}`)
}

// 生成叶片到货检查航线
export function generateBladeArrivalRoutes(data: GenerateRouteDTO) {
  return request.post<any, FlightRoute[]>('/inspection/routes/generate/blade-arrival', data)
}

// 生成部套到货检查航线(360度环绕)
export function generateOrbitalArrivalRoute(data: GenerateRouteDTO) {
  return request.post<any, FlightRoute>('/inspection/routes/generate/orbital-arrival', data)
}

// 生成叶片吊装监督航线
export function generateBladeInstallRoute(data: GenerateRouteDTO) {
  return request.post<any, FlightRoute>('/inspection/routes/generate/blade-install', data)
}

// 生成风机整体验收航线
export function generateAcceptanceRoute(data: GenerateRouteDTO) {
  return request.post<any, FlightRoute>('/inspection/routes/generate/acceptance', data)
}

// 删除航线
export function deleteRoute(id: number) {
  return request.delete(`/inspection/routes/${id}`)
}
