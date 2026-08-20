<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getInterventions, createIntervention, updateIntervention, deleteIntervention, changeInterventionStatus, getParticipants, addParticipants } from '../../api/intervention'
import { getUsers } from '../../api/org'

const route = useRoute()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const typeOpts = [
  { label: '健康讲座', value: 'lecture' },
  { label: '运动计划', value: 'exercise' },
  { label: '减重管理', value: 'weight' },
  { label: '戒烟计划', value: 'smoking' },
  { label: '心理辅导', value: 'mental' }
]
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ title: '', type: 'lecture', description: '', startDate: '', endDate: '', executorId: null })
const rules = {
  title: [{ required: true, message: '请输入计划标题', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
}
const users = ref([])
const selectedUsers = ref([])

function fetchList() {
  loading.value = true
  getInterventions({ page: page.value, size: size.value })
    .then(res => { list.value = res.data.data.records || []; total.value = res.data.data.total || 0 })
    .catch(() => { ElMessage.error('加载计划失败') })
    .finally(() => { loading.value = false })
}

function loadUsers() {
  getUsers({ page: 1, size: 100 }).then(res => { users.value = res.data.data.records || [] })
}

function openCreate() {
  isEdit.value = false
  const q = route.query
  Object.assign(form, {
    title: q.title || '',
    type: q.type || 'lecture',
    description: q.description || '',
    startDate: '',
    endDate: '',
    executorId: null
  })
  selectedUsers.value = []
  formRef.value?.resetFields()
  dialogVisible.value = true
  loadUsers()
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row })
  formRef.value?.resetFields()
  dialogVisible.value = true
  loadUsers()
  getParticipants(row.id).then(res => { selectedUsers.value = (res.data.data || []).map(p => p.userId) })
}

function handleSave() {
  formRef.value?.validate().then(() => {
    saving.value = true
    const req = isEdit.value ? updateIntervention(form.id, { ...form }) : createIntervention({ ...form })
    req.then(res => {
      const planId = isEdit.value ? form.id : res.data.data.id
      if (selectedUsers.value.length) addParticipants(planId, selectedUsers.value)
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      fetchList()
    }).catch((e) => { ElMessage.error(e?.response?.data?.msg || '保存失败') })
      .finally(() => { saving.value = false })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm('确定删除该干预计划？将级联删除参与者和随访记录', '确认删除', { type: 'warning', confirmButtonText: '删除' }).then(() => {
    deleteIntervention(row.id).then(() => { ElMessage.success('已删除'); fetchList() })
      .catch(() => { ElMessage.error('删除失败') })
  }).catch(() => {})
}

function handleStatus(row, status) {
  changeInterventionStatus(row.id, status).then(() => { ElMessage.success('状态已更新'); fetchList() })
    .catch(() => { ElMessage.error('状态变更失败') })
}

function typeName(type) { return typeOpts.find(t => t.value === type)?.label || type }

onMounted(() => {
  fetchList()
  if (route.query.title) {
    openCreate()
  }
})
</script>

<template>
  <div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
      <h2 style="font-size:var(--text-xl);font-weight:600;color:var(--text-primary);margin:0">干预计划</h2>
      <el-button type="primary" @click="openCreate">新增计划</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe empty-text="暂无干预计划">
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column label="类型" width="100"><template #default="{row}">{{ typeName(row.type) }}</template></el-table-column>
      <el-table-column label="状态" width="90"><template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'info':''" size="small">{{ ['未开始','进行中','已结束'][row.status] }}</el-tag></template></el-table-column>
      <el-table-column prop="executorName" label="执行人" width="90" />
      <el-table-column prop="participantCount" label="参与人数" width="90" />
      <el-table-column prop="startDate" label="开始" width="110" />
      <el-table-column prop="endDate" label="结束" width="110" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status===0" size="small" type="success" @click="handleStatus(row,1)">启动</el-button>
          <el-button v-if="row.status===1" size="small" type="warning" @click="handleStatus(row,2)">结束</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="fetchList" style="margin-top:16px;justify-content:flex-end" layout="total, prev, pager, next" />

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑计划':'新增计划'" width="560px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" maxlength="128" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type"><el-option v-for="t in typeOpts" :key="t.value" :label="t.label" :value="t.value" /></el-select></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="开始日期" prop="startDate"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束日期" prop="endDate"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="执行人"><el-select v-model="form.executorId" clearable><el-option v-for="u in users" :key="u.id" :label="u.realName||u.username" :value="Number(u.id)" /></el-select></el-form-item>
        <el-form-item label="参与人员"><el-select v-model="selectedUsers" multiple placeholder="选择员工"><el-option v-for="u in users" :key="u.id" :label="`${u.realName||u.username} (${u.employeeNo})`" :value="Number(u.id)" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 使用全局样式 */
</style>

<!--
  干预计划 — admin / hr 共享
  管理健康干预计划：创建/编辑/删除，分配参与员工，状态流转（未开始→进行中→已结束）
  5 种干预类型：讲座/运动/减重/戒烟/心理辅导
  删除计划会级联删除参与者和随访记录
-->