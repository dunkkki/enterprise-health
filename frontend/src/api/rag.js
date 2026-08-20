import api from './index'

export function getHealthAdvice(recordId) {
  return api.get('/api/rag/health-advice', { params: { recordId } })
}

export function suggestPlan(data) {
  return api.post('/api/rag/suggest-plan', data)
}
