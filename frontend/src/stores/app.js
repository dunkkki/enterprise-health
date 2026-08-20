// 导入Pinia定义仓库的核心方法
import { defineStore } from 'pinia'
// 导入Vue组合式API创建基础响应式数据
import { ref } from 'vue'

/**
 * 全局应用配置状态仓库
 * 统一管理页面布局相关全局状态，当前主要控制侧边栏展开/收起
 */
export const useApp = defineStore('app', () => {
  // 侧边栏折叠状态：false=展开，true=收起，默认初始为展开状态
  const sidebarCollapsed = ref(false)

  /**
   * 切换侧边栏状态
   * 点击侧边栏开关按钮触发，取反当前折叠布尔值
   */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // 导出状态变量与修改方法，供各个组件使用
  return { sidebarCollapsed, toggleSidebar }
})