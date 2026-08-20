import api from './index'

export function login(username, password) {
  return api.post('/api/auth/login', { username, password })
}

export function fetchMe() {
  return api.get('/api/auth/me')
}

export function changePassword(data) {
  return api.put('/api/auth/password', data)
}
