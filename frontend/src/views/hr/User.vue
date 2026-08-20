<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, createUser, updateUser, deleteUser, toggleUserStatus, importUsers, getDeptTree } from '../../api/org'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')
const deptId = ref('')
const depts = ref([])
const dialogVisible = ref(false)
const importVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', realName: '', employeeNo: '', gender: 1, phone: '', email: '', deptId: '', position: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
}
const importFile = ref(null)

function fetchList() {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (keyword.value) params.keyword = keyword.value
  if (deptId.value) params.deptId = deptId.value
  getUsers(params).then(res => {
    list.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  }).catch(() => { ElMessage.error('加载员工列表失败') })
    .finally(() => { loading.value = false })
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { username: '', realName: '', employeeNo: '', gender: 1, phone: '', email: '', deptId: '', position: '' })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row, id: Number(row.id), deptId: row.deptId ? Number(row.deptId) : '' })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleSave() {
  formRef.value?.validate().then(() => {
    saving.value = true
    const p = { ...form, deptId: form.deptId || null }
    const req = isEdit.value ? updateUser(form.id, p) : createUser(p)
    req.then(() => {
      ElMessage.success(isEdit.value ? '已更新' : '已创建')
      dialogVisible.value = false
      fetchList()
    }).catch((e) => { ElMessage.error(e?.response?.data?.msg || '保存失败') })
      .finally(() => { saving.value = false })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`删除员工"${row.realName||row.username}"？关联的体检记录、评估结果将级联删除`, '确认删除', { type: 'warning', confirmButtonText: '删除' }).then(() => {
    deleteUser(row.id).then(() => { ElMessage.success('已删除'); fetchList() })
      .catch((e) => { ElMessage.error(e?.response?.data?.msg || '删除失败') })
  }).catch(() => {})
}

function handleStatus(row) {
  const s = row.status === 1 ? 0 : 1
  toggleUserStatus(row.id, s).then(() => fetchList()).catch(() => { ElMessage.error('操作失败') })
}

function onFileChange(f) { importFile.value = f.raw }

function handleImport() {
  if (!importFile.value) return
  const fd = new FormData(); fd.append('file', importFile.value)
  importUsers(fd).then(() => {
    ElMessage.success('导入成功'); importVisible.value = false; fetchList()
  }).catch(() => { ElMessage.error('导入失败') })
}

onMounted(() => {
  getDeptTree().then(res => { depts.value = res.data.data || [] })
  fetchList()
})
</script>

<template>
  <div>
    <div class="page-header"><div><h2>员工管理</h2><span class="sub">管理员工档案</span></div><div style="display:flex;gap:8px"><el-button @click="importVisible=true">Excel导入</el-button><el-button type="primary" @click="openCreate">新增员工</el-button></div></div>
    <div style="display:flex;gap:12px;margin-bottom:16px">
      <el-input v-model="keyword" placeholder="搜索姓名/工号" clearable style="width:200px" @keyup.enter="fetchList" />
      <el-select v-model="deptId" clearable placeholder="部门" style="width:160px" @change="fetchList"><el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" /></el-select>
      <el-button type="primary" @click="fetchList">搜索</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无员工数据">
        <el-table-column prop="employeeNo" label="工号" width="90" />
        <el-table-column prop="realName" label="姓名" width="90" />
        <el-table-column prop="username" label="用户名" width="90" />
        <el-table-column label="性别" width="60"><template #default="{row}">{{ row.gender===1?'男':'女' }}</template></el-table-column>
        <el-table-column prop="phone" label="手机" width="120" />
        <el-table-column prop="position" label="岗位" width="100" />
        <el-table-column label="状态" width="70"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.status===1?'warning':'success'" @click="handleStatus(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="fetchList" style="margin-top:16px;justify-content:flex-end" layout="total,prev,pager,next" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑员工':'新增员工'" width="520px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="用户名" prop="username"><el-input v-model="form.username" maxlength="32" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" maxlength="32" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="工号"><el-input v-model="form.employeeNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别"><el-select v-model="form.gender"><el-option label="男" :value="1" /><el-option label="女" :value="0" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="部门"><el-select v-model="form.deptId" clearable><el-option v-for="d in depts" :key="d.id" :label="d.name" :value="Number(d.id)" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="岗位"><el-input v-model="form.position" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="importVisible" title="Excel导入员工" width="400px">
      <el-upload :auto-upload="false" :on-change="onFileChange" accept=".xlsx,.xls" drag>
        <div style="padding:20px;text-align:center;color:#909399">拖拽或点击上传 Excel 文件</div>
      </el-upload>
      <template #footer><el-button @click="importVisible=false">取消</el-button><el-button type="primary" @click="handleImport" :disabled="!importFile">开始导入</el-button></template>
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
  员工管理 — admin / hr 共享
  分页列表支持按姓名/工号模糊搜索 + 部门筛选
  新增/编辑弹窗含表单校验，支持 Excel 批量导入
  删除员工会级联删除其体检记录和风险评估结果
-->