<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyRisks } from '../../api/risk'

const loading = ref(false)
const list = ref([])

onMounted(() => {
  loading.value = true
  getMyRisks().then(res => {
    list.value = res.data.data.records || []
  }).catch(() => {
    ElMessage.error('加载风险评估失败')
  }).finally(() => { loading.value = false })
})
</script>

<template>
  <div>
    <div class="page-header"><h2>我的风险</h2><span class="sub">查看个人健康风险评估</span></div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无风险评估记录">
        <el-table-column prop="scheduleTitle" label="排期" min-width="160" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="等级" width="80">
          <template #default="{row}"><el-tag :type="row.riskLevel==='高'?'danger':row.riskLevel==='中'?'warning':'success'" size="small">{{ row.riskLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="assessedAt" label="评估时间" width="170" />
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
  我的风险 — 所有角色可见
  显示当前用户历次体检的风险评估结果（总分 + 风险等级），后端自动过滤仅本人数据
-->