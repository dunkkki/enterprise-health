import api from './index'

export function getSummary() {
  return api.get('/api/dashboard/summary')
}

export function getRiskDistribution() {
  return api.get('/api/dashboard/risk-distribution')
}

export function getDeptRanking() {
  return api.get('/api/dashboard/dept-ranking')
}

export function getExamTrend() {
  return api.get('/api/dashboard/exam-trend')
}

export function getInterventionStats() {
  return api.get('/api/dashboard/intervention-stats')
}
