import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, fetchMe } from '../api/auth'

function safeJSON(raw, fallback) {
  try {
    const v = raw ? JSON.parse(raw) : null
    return v != null ? v : fallback
  } catch {
    return fallback
  }
}

export const useUser = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(safeJSON(localStorage.getItem('user'), null))
  const menus = ref(safeJSON(localStorage.getItem('menus'), []))

  async function login(username, password) {
    const res = await loginApi(username, password)
    if (res.data.code !== 200) {
      throw new Error(res.data.msg || '登录失败')
    }
    const d = res.data.data
    token.value = d.token
    user.value = d.user
    menus.value = d.menus || []
    localStorage.setItem('token', token.value)
    localStorage.setItem('user', JSON.stringify(user.value))
    localStorage.setItem('menus', JSON.stringify(menus.value))
  }

  async function fetchUser() {
    const res = await fetchMe()
    const d = res.data.data
    user.value = d.user
    menus.value = d.menus || []
    localStorage.setItem('user', JSON.stringify(user.value))
    localStorage.setItem('menus', JSON.stringify(menus.value))
  }

  function logout() {
    token.value = ''
    user.value = null
    menus.value = []
    localStorage.clear()
  }

  return { token, user, menus, login, fetchUser, logout }
})
