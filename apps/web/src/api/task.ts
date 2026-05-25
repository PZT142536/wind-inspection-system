import request from '@/utils/request'

export interface InspectionTask {
  id: number
  projectId: number
  turbineId: number
  type: string
  component: string
  status: string
  inspectorId: number
  plannedAt: string
  startedAt: string
  completedAt: string
  progress: number
  remark: string
  createdAt: string
  updatedAt: string
}

export interface TaskDTO {
  projectId: number
  turbineId?: number
  type: string
  component?: string
  inspectorId?: number
  plannedAt?: string
  remark?: string
}

// 分页查询任务
export function getTaskPage(params: {
  pageNum: number
  pageSize: number
  projectId?: number
  type?: string
  status?: string
}) {
  return request.get<any, { records: InspectionTask[]; total: number }>('/inspection/tasks', { params })
}

// 获取任务详情
export function getTaskDetail(id: number) {
  return request.get<any, InspectionTask>(`/inspection/tasks/${id}`)
}

// 创建任务
export function createTask(data: TaskDTO) {
  return request.post<any, InspectionTask>('/inspection/tasks', data)
}

// 更新任务
export function updateTask(id: number, data: TaskDTO) {
  return request.put<any, InspectionTask>(`/inspection/tasks/${id}`, data)
}

// 删除任务
export function deleteTask(id: number) {
  return request.delete(`/inspection/tasks/${id}`)
}

// 开始任务
export function startTask(id: number) {
  return request.put(`/inspection/tasks/${id}/start`)
}

// 完成任务
export function completeTask(id: number) {
  return request.put(`/inspection/tasks/${id}/complete`)
}

// 更新进度
export function updateTaskProgress(id: number, progress: number) {
  return request.put(`/inspection/tasks/${id}/progress`, null, { params: { progress } })
}
