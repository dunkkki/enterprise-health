import api from './index'

// 风险规则
export function getRiskRules() {
  return api.get('/api/risk-rules')
}

export function createRiskRule(data) {
  return api.post('/api/risk-rules', data)
}

export function updateRiskRule(id, data) {
  return api.put(`/api/risk-rules/${id}`, data)
}

export function deleteRiskRule(id) {
  return api.delete(`/api/risk-rules/${id}`)
}

// 评估结果
export function getRiskResults(params) {
  return api.get('/api/risk-results', { params })
}

export function assessRecord(recordId) {
  return api.post(`/api/risk-results/assess/${recordId}`)
}

export function getMyRisks() {
  return api.get('/api/risk-results/mine')
}
