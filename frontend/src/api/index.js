import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 10000,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

api.interceptors.response.use(
  res => res,
  err => {
    const code = err.response?.data?.code
    const msg = err.response?.data?.msg || '网络错误'
    if (code === 401) {
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      localStorage.removeItem('menus')
      router.push('/login')
    }
    ElMessage.error(msg)
    return Promise.reject(err)
  }
)

export default api
