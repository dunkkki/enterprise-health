<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecords, getRecordDetail, enterResult } from '../../api/exam'
import { getSchedules } from '../../api/exam'
import { getDeptTree } from '../../api/org'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const scheduleId = ref('')
const deptId = ref('')
const status = ref('')
const schedules = ref([])
const depts = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const saving = ref(false)
const form = ref({ items: [] })
const detail = ref(null)

function fetchList() {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (scheduleId.value) params.scheduleId = scheduleId.value
  if (deptId.value) params.deptId = deptId.value
  if (status.value !== '') params.status = status.value
  getRecords(params).then(res => {
    list.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  }).catch(() => { ElMessage.error('加载记录失败') })
    .finally(() => { loading.value = false })
}

function openEnter(row) {
  getRecordDetail(row.id).then(res => {
    const d = res.data.data
    form.value = { recordId: Number(row.id), examDate: d.examDate || '', overallResult: d.overallResult || '', items: (d.items || []).map(i => ({ packageItemId: Number(i.packageItemId), itemValue: i.value || '' })) }
    dialogVisible.value = true
  }).catch(() => { ElMessage.error('加载记录详情失败') })
}

function handleSave() {
  saving.value = true
  enterResult(form.value).then(() => { ElMessage.success('录入成功'); dialogVisible.value = false; fetchList() })
    .catch(() => { ElMessage.error('录入失败') })
    .finally(() => { saving.value = false })
}

function showDetail(row) {
  getRecordDetail(row.id).then(res => { detail.value = res.data.data; detailVisible.value = true })
    .catch(() => { ElMessage.error('加载详情失败') })
}

onMounted(() => {
  getSchedules({ page: 1, size: 50 }).then(res => { schedules.value = res.data.data.records || [] })
  getDeptTree().then(res => { depts.value = res.data.data || [] })
  fetchList()
})
</script>

<template>
  <div>
    <div class="page-header"><div><h2>体检记录</h2><span class="sub">录入与查看员工体检结果</span></div></div>
    <div style="display:flex;gap:12px;margin-bottom:16px;flex-wrap:wrap">
      <el-select v-model="scheduleId" clearable placeholder="排期" style="width:180px" @change="fetchList"><el-option v-for="s in schedules" :key="s.id" :label="s.title" :value="s.id" /></el-select>
      <el-select v-model="deptId" clearable placeholder="部门" style="width:160px" @change="fetchList"><el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" /></el-select>
      <el-select v-model="status" clearable placeholder="状态" style="width:120px" @change="fetchList"><el-option label="未检" :value="0" /><el-option label="已检" :value="1" /><el-option label="请假" :value="2" /></el-select>
      <el-button type="primary" @click="fetchList">搜索</el-button>
      <el-button @click="scheduleId=''; deptId=''; status=''; fetchList()">重置</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无体检记录">
        <el-table-column prop="userName" label="员工" width="80" />
        <el-table-column prop="scheduleTitle" label="排期" min-width="130" />
        <el-table-column label="状态" width="70"><template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'warning':''" size="small">{{ ['未检','已检','请假'][row.status] }}</el-tag></template></el-table-column>
        <el-table-column prop="examDate" label="体检日期" width="110" />
        <el-table-column prop="overallResult" label="结论" width="90" />
        <el-table-column label="操作" width="160">
          <template #default="{row}">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button size="small" type="primary" @click="openEnter(row)" v-if="row.status!==1">录入</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="fetchList" style="margin-top:16px;justify-content:flex-end" layout="total,prev,pager,next" />
    </div>

    <el-dialog v-model="dialogVisible" title="录入体检结果" width="650px">
      <el-form :model="form" label-width="80px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="体检日期"><el-date-picker v-model="form.examDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="综合结论"><el-input v-model="form.overallResult" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="检测指标">
          <div v-for="(it,i) in form.items" :key="i" style="display:flex;gap:8px;margin-bottom:6px;align-items:center">
            <span style="width:60px;font-size:13px;text-align:right;color:#6b7280">{{ it.packageItemId }}</span>
            <el-input v-model="it.itemValue" placeholder="检测值" size="small" style="width:140px" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="记录详情" width="600px">
      <div v-if="detail">
        <p><b>{{ detail.userName }}</b> · {{ detail.scheduleTitle }}</p>
        <p style="color:#6b7280;font-size:13px">体检日期: {{ detail.examDate }} · 结论: {{ detail.overallResult }}</p>
        <el-table :data="detail.items||[]" border size="small" style="margin-top:12px" empty-text="暂无指标数据">
          <el-table-column prop="itemName" label="指标" width="120" />
          <el-table-column prop="value" label="检测值" width="80" />
          <el-table-column prop="unit" label="单位" width="60" />
          <el-table-column label="参考范围" width="150"><template #default="{row}">{{ row.refMin }} ~ {{ row.refMax }}</template></el-table-column>
          <el-table-column label="异常" width="60"><template #default="{row}"><el-tag v-if="row.isAbnormal" type="danger" size="small">异常</el-tag></template></el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header { display:flex;justify-content:space-between;align-items:center;margin-bottom:20px }
.page-header h2 { font-size:20px;font-weight:600;margin:0 }
.sub { font-size:13px;color:#6b7280;display:block;margin-top:2px }
.card-table { background:#fff;border-radius:10px;padding:20px 24px;box-shadow:0 1px 2px rgba(0,0,0,0.04) }
</style>

<!--
  体检记录 — admin / hr 共享
  按排期/部门/状态筛选员工体检记录，支持"录入"和"详情"
  录入弹窗回填套餐模板指标，填写检测值后自动判异常，记录录入人
  一人一排期仅一条记录（UNIQUE 约束）
-->