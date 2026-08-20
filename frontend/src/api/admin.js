import api from './index'

// 角色管理
export function getRoles() {
  return api.get('/api/roles')
}

export function createRole(data) {
  return api.post('/api/roles', data)
}

export function updateRole(id, data) {
  return api.put(`/api/roles/${id}`, data)
}

export function deleteRole(id) {
  return api.delete(`/api/roles/${id}`)
}

export function getRoleMenus(id) {
  return api.get(`/api/roles/${id}/menus`)
}

export function assignRoleMenus(id, menuIds) {
  return api.put(`/api/roles/${id}/menus`, { menuIds })
}

// 菜单管理
export function getMenuTree() {
  return api.get('/api/menus/tree')
}

export function createMenu(data) {
  return api.post('/api/menus', data)
}

export function updateMenu(id, data) {
  return api.put(`/api/menus/${id}`, data)
}

export function deleteMenu(id) {
  return api.delete(`/api/menus/${id}`)
}

// 系统日志
export function getOperationLogs(params) {
  return api.get('/api/logs/operation', { params })
}

export function getLoginLogs(params) {
  return api.get('/api/logs/login', { params })
}
