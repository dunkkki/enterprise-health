<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSchedules, createSchedule, updateSchedule, changeScheduleStatus } from '../../api/exam'
import { getPackages } from '../../api/exam'
import { getDeptTree } from '../../api/org'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ title: '', packageId: '', startDate: '', endDate: '', targetDeptIds: [] })
const rules = {
  title: [{ required: true, message: '请输入排期标题', trigger: 'blur' }],
  packageId: [{ required: true, message: '请选择体检套餐', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
}
const packages = ref([])
const depts = ref([])

const statusMap = ['未开始','进行中','已截止']

function fetchList() {
  loading.value = true
  getSchedules({ page: page.value, size: size.value }).then(res => {
    list.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  }).catch(() => { ElMessage.error('加载排期失败') })
    .finally(() => { loading.value = false })
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { title: '', packageId: '', startDate: '', endDate: '', targetDeptIds: [] })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row, id: Number(row.id), targetDeptIds: (row.targetDeptIds||[]).map(Number) })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleSave() {
  formRef.value?.validate().then(() => {
    saving.value = true
    const req = isEdit.value ? updateSchedule(form.id, { ...form }) : createSchedule({ ...form })
    req.then(() => { ElMessage.success(isEdit.value?'已更新':'已创建'); dialogVisible.value=false; fetchList() })
      .catch((e) => { ElMessage.error(e?.response?.data?.msg || '保存失败') })
      .finally(() => { saving.value = false })
  }).catch(() => {})
}

function handleStatus(row, status) {
  changeScheduleStatus(row.id, status).then(() => { ElMessage.success('状态已更新'); fetchList() })
    .catch(() => { ElMessage.error('状态变更失败') })
}

onMounted(() => {
  getPackages().then(res => { packages.value = res.data.data || [] })
  getDeptTree().then(res => { depts.value = res.data.data || [] })
  fetchList()
})
</script>

<template>
  <div>
    <div class="page-header"><div><h2>体检排期</h2><span class="sub">安排年度体检计划</span></div><el-button type="primary" @click="openCreate">创建排期</el-button></div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无排期数据">
        <el-table-column prop="title" label="标题" min-width="140" />
        <el-table-column prop="packageName" label="套餐" width="120" />
        <el-table-column prop="startDate" label="开始" width="110" />
        <el-table-column prop="endDate" label="结束" width="110" />
        <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'info':''" size="small">{{ statusMap[row.status] }}</el-tag></template></el-table-column>
        <el-table-column label="完成率" width="90"><template #default="{row}">{{ row.totalCount ? Math.round(row.examinedCount*100/row.totalCount)+'%' : '-' }}</template></el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.status===0" size="small" type="success" @click="handleStatus(row,1)">启动</el-button>
            <el-button v-if="row.status===1" size="small" type="warning" @click="handleStatus(row,2)">截止</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="fetchList" style="margin-top:16px;justify-content:flex-end" layout="total,prev,pager,next" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑排期':'创建排期'" width="500px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="form.title" maxlength="128" /></el-form-item>
        <el-form-item label="套餐" prop="packageId"><el-select v-model="form.packageId"><el-option v-for="p in packages" :key="p.id" :label="p.name" :value="Number(p.id)" /></el-select></el-form-item>
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="开始" prop="startDate"><el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="结束" prop="endDate"><el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="目标部门"><el-select v-model="form.targetDeptIds" multiple placeholder="空=全员"><el-option v-for="d in depts" :key="d.id" :label="d.name" :value="Number(d.id)" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header { display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:20px }
.page-header h2 { font-size:20px;font-weight:600;margin:0 }
.sub { font-size:13px;color:#6b7280;display:block;margin-top:2px }
.card-table { background:#fff;border-radius:10px;padding:20px 24px;box-shadow:0 1px 2px rgba(0,0,0,0.04) }
</style>

<!--
  体检排期 — admin / hr 共享
  创建年度体检排期：选择套餐、设定日期范围、指定目标部门（空=全员）
  创建/启动时自动为目标部门内所有启用员工生成 exam_record
  状态流转：未开始 → 进行中 → 已截止
-->