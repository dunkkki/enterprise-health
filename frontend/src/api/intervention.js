import api from './index'

// 干预计划
export function getInterventions(params) {
  return api.get('/api/interventions', { params })
}

export function getInterventionDetail(id) {
  return api.get(`/api/interventions/${id}`)
}

export function createIntervention(data) {
  return api.post('/api/interventions', data)
}

export function updateIntervention(id, data) {
  return api.put(`/api/interventions/${id}`, data)
}

export function deleteIntervention(id) {
  return api.delete(`/api/interventions/${id}`)
}

export function changeInterventionStatus(id, status) {
  return api.put(`/api/interventions/${id}/status?status=${status}`)
}

// 参与人员
export function getParticipants(id) {
  return api.get(`/api/interventions/${id}/participants`)
}

export function addParticipants(id, userIds) {
  return api.post(`/api/interventions/${id}/participants`, userIds)
}

export function updateParticipantStatus(id, uid, status) {
  return api.put(`/api/interventions/${id}/participants/${uid}?status=${status}`)
}

// 随访记录
export function getFollowUps(params) {
  return api.get('/api/follow-ups', { params })
}

export function createFollowUp(data) {
  return api.post('/api/follow-ups', data)
}

export function updateFollowUp(id, data) {
  return api.put(`/api/follow-ups/${id}`, data)
}
