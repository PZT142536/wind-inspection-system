import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' },
      },
    ],
  },
  {
    path: '/system',
    component: () => import('@/layout/index.vue'),
    redirect: '/system/user',
    meta: { title: '系统管理', icon: 'Setting' },
    children: [
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' },
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' },
      },
      {
        path: 'log',
        name: 'Log',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '日志管理', icon: 'Document' },
      },
    ],
  },
  {
    path: '/project',
    component: () => import('@/layout/index.vue'),
    redirect: '/project/list',
    meta: { title: '项目管理', icon: 'Folder' },
    children: [
      {
        path: 'list',
        name: 'ProjectList',
        component: () => import('@/views/project/list/index.vue'),
        meta: { title: '项目列表', icon: 'List' },
      },
    ],
  },
  {
    path: '/inspection',
    component: () => import('@/layout/index.vue'),
    redirect: '/inspection/task',
    meta: { title: '巡检任务', icon: 'Monitor' },
    children: [
      {
        path: 'task',
        name: 'InspectionTask',
        component: () => import('@/views/inspection/task/index.vue'),
        meta: { title: '任务管理', icon: 'Tickets' },
      },
      {
        path: 'route',
        name: 'FlightRoute',
        component: () => import('@/views/inspection/route/index.vue'),
        meta: { title: '航线管理', icon: 'MapLocation' },
      },
    ],
  },
  {
    path: '/media',
    component: () => import('@/layout/index.vue'),
    redirect: '/media/list',
    meta: { title: '媒体管理', icon: 'VideoCamera' },
    children: [
      {
        path: 'list',
        name: 'MediaList',
        component: () => import('@/views/media/list/index.vue'),
        meta: { title: '媒体列表', icon: 'Film' },
      },
    ],
  },
  {
    path: '/ai',
    component: () => import('@/layout/index.vue'),
    redirect: '/ai/finding',
    meta: { title: 'AI分析', icon: 'Cpu' },
    children: [
      {
        path: 'finding',
        name: 'AIFinding',
        component: () => import('@/views/ai/finding/index.vue'),
        meta: { title: '缺陷发现', icon: 'Warning' },
      },
      {
        path: 'alert',
        name: 'Alert',
        component: () => import('@/views/ai/alert/index.vue'),
        meta: { title: '预警管理', icon: 'Bell' },
      },
    ],
  },
  {
    path: '/report',
    component: () => import('@/layout/index.vue'),
    redirect: '/report/list',
    meta: { title: '报告管理', icon: 'DataAnalysis' },
    children: [
      {
        path: 'list',
        name: 'ReportList',
        component: () => import('@/views/report/list/index.vue'),
        meta: { title: '报告列表', icon: 'Document' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
