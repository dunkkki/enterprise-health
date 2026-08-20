<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUser } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const { user, logout } = useUser()

function doLogout() { logout(); router.push('/login') }
</script>

<template>
  <div class="shell">
    <header class="topbar">
      <span class="brand">企业健康管理</span>
      <nav class="tabs">
        <router-link to="/my/exams"          class="tab" :class="{ active: route.path === '/my/exams' }">我的体检</router-link>
        <router-link to="/my/risks"          class="tab" :class="{ active: route.path === '/my/risks' }">我的风险</router-link>
        <router-link to="/my/interventions"  class="tab" :class="{ active: route.path === '/my/interventions' }">我的干预</router-link>
        <router-link to="/my/profile"        class="tab" :class="{ active: route.path === '/my/profile' }">个人信息</router-link>
      </nav>
      <div class="topbar-right">
        <span class="user-avatar-sm">{{ (user?.realName || user?.username || '?')[0] }}</span>
        <span class="user-name">{{ user?.realName || user?.username }}</span>
        <el-tag size="small" effect="plain" round>员工</el-tag>
        <el-button text class="logout-link" @click="doLogout">退出</el-button>
      </div>
    </header>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--canvas);
  font-family: var(--font);
}

.topbar {
  height: 56px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 32px;
  flex-shrink: 0;
}

.brand {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 1px;
  white-space: nowrap;
}

.tabs { display: flex; gap: 4px; }

.tab {
  text-decoration: none;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  transition: all 0.15s;
}

.tab:hover {
  color: var(--accent);
  background: var(--accent-soft);
}

.tab.active {
  color: var(--accent);
  background: var(--accent-soft);
  font-weight: 500;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.user-avatar-sm {
  width: 28px;
  height: 28px;
  border-radius: var(--radius-sm);
  background: var(--accent-soft);
  color: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
}

.user-name { font-size: var(--text-sm); color: var(--text-primary); }

.logout-link {
  color: var(--text-tertiary) !important;
  font-size: var(--text-sm);
}

.logout-link:hover { color: var(--accent) !important; }

.content { padding: 24px; flex: 1; }
</style>
