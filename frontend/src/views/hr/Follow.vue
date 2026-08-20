<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFollowUps, createFollowUp } from '../../api/intervention'
import { getInterventions } from '../../api/intervention'
import { getUsers } from '../../api/org'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const planId = ref('')
const plans = ref([])
const users = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ planId: '', userId: '', followDate: '', content: '', result: '好转', nextDate: '' })
const rules = {
  planId: [{ required: true, message: '请选择计划', trigger: 'change' }],
  userId: [{ required: true, message: '请选择员工', trigger: 'change' }],
  followDate: [{ required: true, message: '请选择随访日期', trigger: 'change' }],
  content: [{ required: true, message: '请输入随访内容', trigger: 'blur' }],
}

function fetchList() {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (planId.value) params.planId = planId.value
  getFollowUps(params).then(res => {
    list.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  }).catch(() => { ElMessage.error('加载随访记录失败') })
    .finally(() => { loading.value = false })
}

function openCreate() {
  Object.assign(form, { planId: planId.value || '', userId: '', followDate: '', content: '', result: '好转', nextDate: '' })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleSave() {
  formRef.value?.validate().then(() => {
    saving.value = true
    createFollowUp({ ...form }).then(() => { ElMessage.success('新增成功'); dialogVisible.value = false; fetchList() })
      .catch(() => { ElMessage.error('新增失败') })
      .finally(() => { saving.value = false })
  }).catch(() => {})
}

function resultTag(r) {
  return { '好转':'success', '稳定':'', '恶化':'danger', '未联系上':'warning' }[r] || ''
}

onMounted(() => {
  getInterventions({ page: 1, size: 100 }).then(res => { plans.value = res.data.data.records || [] })
  getUsers({ page: 1, size: 200 }).then(res => { users.value = res.data.data.records || [] })
  fetchList()
})
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="font-size:18px;color:#303133;margin:0">随访记录</h2>
      <el-button type="primary" @click="openCreate">新增随访</el-button>
    </div>
    <div style="margin-bottom:16px;display:flex;gap:12px">
      <el-select v-model="planId" clearable placeholder="按计划筛选" @change="fetchList" style="width:220px"><el-option v-for="p in plans" :key="p.id" :label="p.title" :value="p.id" /></el-select>
      <el-button @click="planId=''; fetchList()">重置</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe empty-text="暂无随访记录">
      <el-table-column prop="planTitle" label="所属计划" min-width="140" />
      <el-table-column prop="userName" label="员工" width="80" />
      <el-table-column prop="followDate" label="随访日期" width="110" />
      <el-table-column prop="content" label="随访内容" min-width="180" show-overflow-tooltip />
      <el-table-column label="结果" width="90"><template #default="{row}"><el-tag :type="resultTag(row.result)" size="small">{{ row.result }}</el-tag></template></el-table-column>
      <el-table-column prop="recordedByName" label="记录人" width="80" />
      <el-table-column prop="nextDate" label="下次随访" width="110" />
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="fetchList" style="margin-top:16px;justify-content:flex-end" layout="total, prev, pager, next" />

    <el-dialog v-model="dialogVisible" title="新增随访" width="500px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="所属计划" prop="planId"><el-select v-model="form.planId"><el-option v-for="p in plans" :key="p.id" :label="p.title" :value="Number(p.id)" /></el-select></el-form-item>
        <el-form-item label="随访员工" prop="userId"><el-select v-model="form.userId"><el-option v-for="u in users" :key="u.id" :label="`${u.realName||u.username} (${u.employeeNo})`" :value="Number(u.id)" /></el-select></el-form-item>
        <el-form-item label="随访日期" prop="followDate"><el-date-picker v-model="form.followDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="随访内容" prop="content"><el-input v-model="form.content" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="随访结果"><el-select v-model="form.result"><el-option label="好转" value="好转" /><el-option label="稳定" value="稳定" /><el-option label="恶化" value="恶化" /><el-option label="未联系上" value="未联系上" /></el-select></el-form-item>
        <el-form-item label="下次日期"><el-date-picker v-model="form.nextDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 使用全局样式 */
</style>

<!--
  随访记录 — admin / hr 共享
  管理干预计划的随访记录：按计划筛选，新增随访（选计划+员工+日期+内容+结果）
  随访结果 4 种：好转 / 稳定 / 恶化 / 未联系上
-->