<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUser } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const { user, logout } = useUser()

function doLogout() { logout(); router.push('/login') }
</script>

<template>
  <el-container class="leader-shell">
    <el-header class="topbar">
      <div class="topbar-left">
        <span class="brand">部门主管</span>
        <div class="nav-tabs">
          <router-link to="/leader/dashboard"     class="nav-tab" :class="{ active: route.path.startsWith('/leader/dashboard') }">数据看板</router-link>
          <router-link to="/leader/risk/results"  class="nav-tab" :class="{ active: route.path.startsWith('/leader/risk') }">评估结果</router-link>
          <router-link to="/leader/my/exams"      class="nav-tab" :class="{ active: route.path.startsWith('/leader/my') }">个人中心</router-link>
        </div>
      </div>
      <div class="topbar-right">
        <span class="user-avatar-sm">{{ (user?.realName || user?.username || '?')[0] }}</span>
        <span class="user-name">{{ user?.realName || user?.username }}</span>
        <el-tag size="small" effect="plain" round>部门主管</el-tag>
        <el-button text class="logout-link" @click="doLogout">退出</el-button>
      </div>
    </el-header>

    <el-main class="main">
      <div class="scope-bar">当前查看范围：<b>本部门</b> — 仅显示您部门下的员工数据</div>
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.leader-shell {
  min-height: 100vh;
  background: var(--canvas);
  flex-direction: column;
  font-family: var(--font);
}

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  padding: 0 24px;
  flex-shrink: 0;
}

.topbar-left { display: flex; align-items: center; gap: 32px; }

.brand {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 1px;
}

.nav-tabs { display: flex; gap: 4px; }

.nav-tab {
  text-decoration: none;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  transition: all 0.15s;
}

.nav-tab:hover {
  color: var(--accent);
  background: var(--accent-soft);
}

.nav-tab.active {
  color: var(--accent);
  background: var(--accent-soft);
  font-weight: 500;
}

.topbar-right { display: flex; align-items: center; gap: 12px; }

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

.logout-link { color: var(--text-tertiary) !important; font-size: var(--text-sm); }
.logout-link:hover { color: var(--accent) !important; }

.main {
  padding: 24px;
  max-width: 1100px;
  margin: 0 auto;
  width: 100%;
}

.scope-bar {
  padding: 8px 14px;
  background: var(--accent-soft);
  border: 1px solid var(--accent-ring);
  border-radius: var(--radius-sm);
  margin-bottom: 20px;
  font-size: var(--text-sm);
  color: var(--text-primary);
}
</style>
