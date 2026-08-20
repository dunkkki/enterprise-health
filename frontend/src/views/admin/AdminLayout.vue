<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useUser } from '../../stores/user'
import { DataAnalysis, OfficeBuilding, FirstAidKit, Warning, Clock, Setting, User } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const { user, logout } = useUser()

const isCollapsed = ref(false)

function doLogout() { logout(); router.push('/login') }

import { ref } from 'vue'
</script>

<template>
  <el-container class="admin-shell">
    <!-- Sidebar -->
    <el-aside :width="isCollapsed ? '64px' : '232px'" class="sidebar">
      <div class="side-head">
        <span v-if="!isCollapsed" class="brand">企业健康管理</span>
        <span v-else class="brand-icon">健</span>
      </div>

      <el-menu
        :default-active="route.path"
        :collapse="isCollapsed"
        router
        class="side-menu"
      >
        <el-sub-menu index="dash">
          <template #title><el-icon><DataAnalysis /></el-icon><span>数据看板</span></template>
          <el-menu-item index="/admin/dashboard">数据看板</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="org">
          <template #title><el-icon><OfficeBuilding /></el-icon><span>组织架构</span></template>
          <el-menu-item index="/admin/org/dept">部门管理</el-menu-item>
          <el-menu-item index="/admin/org/user">员工管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="exam">
          <template #title><el-icon><FirstAidKit /></el-icon><span>体检管理</span></template>
          <el-menu-item index="/admin/exam/package">体检套餐</el-menu-item>
          <el-menu-item index="/admin/exam/schedule">体检排期</el-menu-item>
          <el-menu-item index="/admin/exam/record">体检记录</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="risk">
          <template #title><el-icon><Warning /></el-icon><span>风险评估</span></template>
          <el-menu-item index="/admin/risk/rules">评估规则</el-menu-item>
          <el-menu-item index="/admin/risk/results">评估结果</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="interv">
          <template #title><el-icon><Clock /></el-icon><span>干预管理</span></template>
          <el-menu-item index="/admin/intervention/plan">干预计划</el-menu-item>
          <el-menu-item index="/admin/intervention/follow">随访记录</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="sys">
          <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
          <el-menu-item index="/admin/system/role">角色管理</el-menu-item>
          <el-menu-item index="/admin/system/menu">菜单管理</el-menu-item>
          <el-menu-item index="/admin/system/log">系统日志</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="my">
          <template #title><el-icon><User /></el-icon><span>个人中心</span></template>
          <el-menu-item index="/admin/my/exams">我的体检</el-menu-item>
          <el-menu-item index="/admin/my/risks">我的风险</el-menu-item>
          <el-menu-item index="/admin/my/interventions">我的干预</el-menu-item>
          <el-menu-item index="/admin/my/profile">个人信息</el-menu-item>
        </el-sub-menu>
      </el-menu>

      <div class="side-foot">
        <div class="user-row">
          <span class="user-avatar">{{ (user?.realName || user?.username || '?')[0] }}</span>
          <span v-if="!isCollapsed" class="user-name">{{ user?.realName || user?.username }}</span>
        </div>
        <el-button text class="logout-btn" @click="doLogout">
          <template v-if="!isCollapsed">退出登录</template>
          <template v-else>&#8594;</template>
        </el-button>
      </div>
    </el-aside>

    <!-- Main -->
    <el-container>
      <el-header class="topbar">
        <div class="topbar-left">
          <el-button text class="collapse-btn" @click="isCollapsed = !isCollapsed">
            <span v-if="isCollapsed">&#9776;</span>
            <span v-else>&#9776;</span>
          </el-button>
          <div class="breadcrumb">
            <span class="bc-item">首页</span>
            <span class="bc-sep">/</span>
            <span class="bc-item active">{{ route.meta?.title || route.name || '' }}</span>
          </div>
        </div>
        <div class="topbar-right">
          <el-tag size="small" effect="plain" round>超级管理员</el-tag>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
/* ── Shell ── */
.admin-shell {
  min-height: 100vh;
  background: var(--canvas);
  font-family: var(--font);
}

/* ── Sidebar ── */
.sidebar {
  background: #F4F5F7;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border);
  transition: width 0.2s ease;
  overflow: hidden;
}

.side-head {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
}

.brand {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.brand-icon {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent-soft);
  border-radius: var(--radius-sm);
}

/* ── Menu ── */
.side-menu {
  flex: 1;
  border-right: none !important;
  background: transparent;
  padding: 0 8px;
}

.side-menu:deep(.el-menu-item),
.side-menu:deep(.el-sub-menu__title) {
  border-radius: var(--radius-sm);
  margin-bottom: 2px;
  height: 40px;
  line-height: 40px;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.side-menu:deep(.el-menu-item:hover),
.side-menu:deep(.el-sub-menu__title:hover) {
  background: var(--accent-soft);
  color: var(--text-primary);
}

.side-menu:deep(.el-menu-item.is-active) {
  background: var(--accent-soft);
  color: var(--accent);
  font-weight: 500;
}

.side-menu:deep(.el-sub-menu.is-active > .el-sub-menu__title) {
  color: var(--text-primary);
}

/* ── Sidebar footer ── */
.side-foot {
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-sm);
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-size: var(--text-sm);
  color: var(--text-primary);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.logout-btn {
  font-size: var(--text-xs);
  color: var(--text-tertiary) !important;
  padding: 0;
  justify-content: flex-start;
}

.logout-btn:hover { color: var(--accent) !important; }

/* ── Topbar ── */
.topbar {
  height: 56px;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 18px;
  color: var(--text-tertiary) !important;
  padding: 4px;
}

.collapse-btn:hover { color: var(--text-primary) !important; }

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-sm);
}

.bc-item { color: var(--text-tertiary); }
.bc-sep { color: var(--border); }
.bc-item.active { color: var(--text-primary); font-weight: 500; }

/* ── Main ── */
.main {
  padding: 24px;
  background: var(--canvas);
}

/* ── Element Plus overrides ── */
:deep(.el-sub-menu .el-menu) {
  background: transparent;
}

:deep(.el-menu--inline) {
  background: transparent !important;
}
</style>
