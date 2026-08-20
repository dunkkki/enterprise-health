import api from './index'

// 部门管理
export function getDeptTree() {
  return api.get('/api/depts/tree')
}

export function createDept(data) {
  return api.post('/api/depts', data)
}

export function updateDept(id, data) {
  return api.put(`/api/depts/${id}`, data)
}

export function deleteDept(id) {
  return api.delete(`/api/depts/${id}`)
}

// 员工管理
export function getUsers(params) {
  return api.get('/api/users', { params })
}

export function createUser(data) {
  return api.post('/api/users', data)
}

export function updateUser(id, data) {
  return api.put(`/api/users/${id}`, data)
}

export function deleteUser(id) {
  return api.delete(`/api/users/${id}`)
}

export function toggleUserStatus(id, status) {
  return api.put(`/api/users/${id}/status?status=${status}`)
}

export function importUsers(formData) {
  return api.post('/api/users/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
