<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUser } from '../../stores/user'
import { DataAnalysis, OfficeBuilding, FirstAidKit, Warning, Clock, User } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const { user, logout } = useUser()

function doLogout() { logout(); router.push('/login') }
</script>

<template>
  <el-container class="hr-shell">
    <el-header class="topbar">
      <div class="topbar-left">
        <span class="brand">HR 工作台</span>
        <el-menu
          :default-active="route.path"
          mode="horizontal"
          router
          class="top-menu"
        >
          <el-sub-menu index="dash">
            <template #title><el-icon><DataAnalysis /></el-icon>数据看板</template>
            <el-menu-item index="/hr/dashboard">数据看板</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="org">
            <template #title><el-icon><OfficeBuilding /></el-icon>组织架构</template>
            <el-menu-item index="/hr/org/dept">部门管理</el-menu-item>
            <el-menu-item index="/hr/org/user">员工管理</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="exam">
            <template #title><el-icon><FirstAidKit /></el-icon>体检管理</template>
            <el-menu-item index="/hr/exam/package">体检套餐</el-menu-item>
            <el-menu-item index="/hr/exam/schedule">体检排期</el-menu-item>
            <el-menu-item index="/hr/exam/record">体检记录</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="risk">
            <template #title><el-icon><Warning /></el-icon>风险评估</template>
            <el-menu-item index="/hr/risk/rules">评估规则</el-menu-item>
            <el-menu-item index="/hr/risk/results">评估结果</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="interv">
            <template #title><el-icon><Clock /></el-icon>干预管理</template>
            <el-menu-item index="/hr/intervention/plan">干预计划</el-menu-item>
            <el-menu-item index="/hr/intervention/follow">随访记录</el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="my">
            <template #title><el-icon><User /></el-icon>个人中心</template>
            <el-menu-item index="/hr/my/exams">我的体检</el-menu-item>
            <el-menu-item index="/hr/my/risks">我的风险</el-menu-item>
            <el-menu-item index="/hr/my/interventions">我的干预</el-menu-item>
            <el-menu-item index="/hr/my/profile">个人信息</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>
      <div class="topbar-right">
        <span class="user-avatar-sm">{{ (user?.realName || user?.username || '?')[0] }}</span>
        <span class="user-name">{{ user?.realName || user?.username }}</span>
        <el-tag size="small" effect="plain" round>HR 经理</el-tag>
        <el-button text class="logout-link" @click="doLogout">退出</el-button>
      </div>
    </el-header>

    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.hr-shell {
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

.topbar-left { display: flex; align-items: center; gap: 24px; }

.brand {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 1px;
  white-space: nowrap;
}

.top-menu {
  border-bottom: none !important;
  background: transparent;
}

.top-menu:deep(.el-menu-item),
.top-menu:deep(.el-sub-menu__title) {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  border-bottom: none !important;
}

.top-menu:deep(.el-menu-item:hover),
.top-menu:deep(.el-sub-menu__title:hover) {
  color: var(--accent);
  background: var(--accent-soft);
  border-radius: var(--radius-sm);
}

.top-menu:deep(.el-menu-item.is-active) {
  color: var(--accent) !important;
  border-bottom: 2px solid var(--accent) !important;
  font-weight: 500;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
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

.main { padding: 24px; }

:deep(.el-header) { padding: 0 24px; }
</style>
