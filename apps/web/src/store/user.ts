import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi, LoginParams, UserInfo } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const permissions = ref<string[]>([])

  // 登录
  async function login(params: LoginParams) {
    const res = await loginApi(params)
    token.value = res.tokenValue
    localStorage.setItem('token', res.tokenValue)
    return res
  }

  // 登出
  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      userInfo.value = null
      permissions.value = []
      localStorage.removeItem('token')
      router.push('/login')
    }
  }

  // 获取用户信息
  async function getUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res
    permissions.value = res.permissions || []
    return res
  }

  // 检查权限
  function hasPermission(code: string) {
    return permissions.value.includes(code)
  }

  return {
    token,
    userInfo,
    permissions,
    login,
    logout,
    getUserInfo,
    hasPermission,
  }
})
