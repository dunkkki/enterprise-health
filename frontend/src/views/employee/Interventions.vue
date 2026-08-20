<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getInterventions } from '../../api/intervention'

const loading = ref(false)
const list = ref([])

onMounted(() => {
  loading.value = true
  getInterventions({ page: 1, size: 50 }).then(res => {
    list.value = res.data.data.records || []
  }).catch(() => {
    ElMessage.error('加载干预计划失败')
  }).finally(() => { loading.value = false })
})
</script>

<template>
  <div>
    <div class="page-header"><h2>我的干预</h2><span class="sub">查看分配给您的干预计划</span></div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无干预计划">
        <el-table-column prop="title" label="计划名称" min-width="160" />
        <el-table-column label="类型" width="90">
          <template #default="{row}">{{ {lecture:'讲座',exercise:'运动',weight:'减重',smoking:'戒烟',mental:'心理'}[row.type] || row.type }}</template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'info':''" size="small">{{ ['未开始','进行中','已结束'][row.status] }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始" width="120" />
        <el-table-column prop="endDate" label="结束" width="120" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.page-header { margin-bottom:20px }
.page-header h2 { font-size:20px;font-weight:600;margin:0 }
.sub { font-size:13px;color:#6b7280;display:block;margin-top:2px }
.card-table { background:#fff;border-radius:10px;padding:20px 24px;box-shadow:0 1px 2px rgba(0,0,0,0.04) }
</style>

<!--
  我的干预 — 所有角色可见
  显示分配给当前用户的干预计划列表（讲座/运动/减重/戒烟/心理）
  后端通过 DataScopeUtil scope=2 自动过滤为仅本人参与的记录
-->