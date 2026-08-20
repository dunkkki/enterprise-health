import api from './index'

// 体检套餐
export function getPackages() {
  return api.get('/api/packages')
}

export function getPackageDetail(id) {
  return api.get(`/api/packages/${id}`)
}

export function createPackage(data) {
  return api.post('/api/packages', data)
}

export function updatePackage(id, data) {
  return api.put(`/api/packages/${id}`, data)
}

export function deletePackage(id) {
  return api.delete(`/api/packages/${id}`)
}

// 体检排期
export function getSchedules(params) {
  return api.get('/api/schedules', { params })
}

export function createSchedule(data) {
  return api.post('/api/schedules', data)
}

export function updateSchedule(id, data) {
  return api.put(`/api/schedules/${id}`, data)
}

export function changeScheduleStatus(id, status) {
  return api.put(`/api/schedules/${id}/status?status=${status}`)
}

// 体检记录
export function getRecords(params) {
  return api.get('/api/records', { params })
}

export function getRecordDetail(id) {
  return api.get(`/api/records/${id}`)
}

export function enterResult(data) {
  return api.post('/api/records', data)
}

export function getMyExams() {
  return api.get('/api/records/mine')
}
