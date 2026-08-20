import { createRouter, createWebHistory } from 'vue-router'

function getUser() {
  try {
    const u = localStorage.getItem('user')
    return u && u !== 'undefined' ? JSON.parse(u) : null
  } catch { return null }
}

function hasRole(user, role) {
  return user?.roles?.includes(role)
}

function getDefaultPath(user) {
  if (!user) return '/login'
  if (hasRole(user, 'admin'))  return '/admin/dashboard'
  if (hasRole(user, 'hr'))     return '/hr/dashboard'
  if (hasRole(user, 'leader')) return '/leader/dashboard'
  return '/my/exams'
}

function rolePrefix(user) {
  if (hasRole(user, 'admin'))  return '/admin'
  if (hasRole(user, 'hr'))     return '/hr'
  if (hasRole(user, 'leader')) return '/leader'
  return '/my'
}

// 共享页面组件引用
const pages = {
  Dashboard:       () => import('../views/hr/Dashboard.vue'),
  Dept:            () => import('../views/hr/Dept.vue'),
  User:            () => import('../views/hr/User.vue'),
  Package:         () => import('../views/hr/Package.vue'),
  Schedule:        () => import('../views/hr/Schedule.vue'),
  Record:          () => import('../views/hr/Record.vue'),
  Rules:           () => import('../views/hr/Rules.vue'),
  Results:         () => import('../views/hr/Results.vue'),
  Plan:            () => import('../views/hr/Plan.vue'),
  Follow:          () => import('../views/hr/Follow.vue'),
  Role:            () => import('../views/admin/Role.vue'),
  Menu:            () => import('../views/admin/Menu.vue'),
  Log:             () => import('../views/admin/Log.vue'),
  MyExams:         () => import('../views/employee/Exams.vue'),
  MyRisks:         () => import('../views/employee/Risks.vue'),
  MyInterventions: () => import('../views/employee/Interventions.vue'),
  Profile:         () => import('../views/employee/Profile.vue'),
}

function makeAdminRoutes(path) {
  return [
    { path: '', redirect: `${path}/dashboard` },
    { path: 'dashboard',          component: pages.Dashboard },
    { path: 'org/dept',           component: pages.Dept },
    { path: 'org/user',           component: pages.User },
    { path: 'exam/package',       component: pages.Package },
    { path: 'exam/schedule',      component: pages.Schedule },
    { path: 'exam/record',        component: pages.Record },
    { path: 'risk/rules',         component: pages.Rules },
    { path: 'risk/results',       component: pages.Results },
    { path: 'intervention/plan',  component: pages.Plan },
    { path: 'intervention/follow',component: pages.Follow },
    { path: 'system/role',        component: pages.Role },
    { path: 'system/menu',        component: pages.Menu },
    { path: 'system/log',         component: pages.Log },
    { path: 'my/exams',           component: pages.MyExams },
    { path: 'my/risks',           component: pages.MyRisks },
    { path: 'my/interventions',   component: pages.MyInterventions },
    { path: 'my/profile',         component: pages.Profile },
  ]
}

function makeHrRoutes(path) {
  return [
    { path: '', redirect: `${path}/dashboard` },
    { path: 'dashboard',          component: pages.Dashboard },
    { path: 'org/dept',           component: pages.Dept },
    { path: 'org/user',           component: pages.User },
    { path: 'exam/package',       component: pages.Package },
    { path: 'exam/schedule',      component: pages.Schedule },
    { path: 'exam/record',        component: pages.Record },
    { path: 'risk/rules',         component: pages.Rules },
    { path: 'risk/results',       component: pages.Results },
    { path: 'intervention/plan',  component: pages.Plan },
    { path: 'intervention/follow',component: pages.Follow },
    { path: 'my/exams',           component: pages.MyExams },
    { path: 'my/risks',           component: pages.MyRisks },
    { path: 'my/interventions',   component: pages.MyInterventions },
    { path: 'my/profile',         component: pages.Profile },
  ]
}

function makeLeaderRoutes(path) {
  return [
    { path: '', redirect: `${path}/dashboard` },
    { path: 'dashboard',        component: pages.Dashboard },
    { path: 'risk/results',     component: pages.Results },
    { path: 'my/exams',         component: pages.MyExams },
    { path: 'my/risks',         component: pages.MyRisks },
    { path: 'my/interventions', component: pages.MyInterventions },
    { path: 'my/profile',       component: pages.Profile },
  ]
}

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/login/Login.vue') },

  {
    path: '/my',
    component: () => import('../views/employee/EmployeeLayout.vue'),
    children: [
      { path: 'exams',         component: pages.MyExams },
      { path: 'risks',         component: pages.MyRisks },
      { path: 'interventions', component: pages.MyInterventions },
      { path: 'profile',       component: pages.Profile },
    ]
  },

  {
    path: '/admin',
    component: () => import('../views/admin/AdminLayout.vue'),
    children: makeAdminRoutes('/admin'),
  },

  {
    path: '/hr',
    component: () => import('../views/hr/HrLayout.vue'),
    children: makeHrRoutes('/hr'),
  },

  {
    path: '/leader',
    component: () => import('../views/leader/LeaderLayout.vue'),
    children: makeLeaderRoutes('/leader'),
  },

  { path: '/:pathMatch(.*)*', redirect: () => getDefaultPath(getUser()) }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to) => {
  const user = getUser()

  if (!user && to.path !== '/login') return '/login'
  if (user && to.path === '/login') return getDefaultPath(user)
  if (!user) return true

  const prefix = rolePrefix(user)
  const isMyPath = to.path.startsWith('/my')
  // /my/* 员工端页面只允许 employee/leader 访问，admin/hr 没有这些页面
  if (isMyPath && hasRole(user, 'admin')) return getDefaultPath(user)
  if (isMyPath && hasRole(user, 'hr')) return getDefaultPath(user)
  if (!to.path.startsWith(prefix) && !isMyPath && to.path !== '/') {
    return getDefaultPath(user)
  }

  return true
})

export default router
