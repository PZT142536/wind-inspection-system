import request from '@/utils/request'

export interface LoginParams {
  empNo: string
  password: string
  captcha: string
  captchaKey: string
}

export interface LoginResult {
  tokenName: string
  tokenValue: string
  isLogin: boolean
  loginId: number
  loginType: string
  tokenTimeout: number
}

export interface UserInfo {
  id: number
  empNo: string
  name: string
  dept: string
  roles: number[]
  permissions: string[]
}

// 获取验证码
export function getCaptcha() {
  return request.get<any, { key: string; image: string }>('/admin/auth/captcha')
}

// 登录
export function login(data: LoginParams) {
  return request.post<any, LoginResult>('/admin/auth/login', data)
}

// 登出
export function logout() {
  return request.post('/admin/auth/logout')
}

// 获取当前用户信息
export function getUserInfo() {
  return request.get<any, UserInfo>('/admin/auth/info')
}
