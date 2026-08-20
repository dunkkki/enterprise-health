<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeptTree, createDept, updateDept, deleteDept } from '../../api/org'

const loading = ref(false)
const tree = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', parentId: 0, sortOrder: 0, status: 1 })
const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
}

function fetchTree() {
  loading.value = true
  getDeptTree().then(res => { tree.value = res.data.data || [] })
    .catch(() => { ElMessage.error('加载部门失败') })
    .finally(() => { loading.value = false })
}

function openCreate(parent) {
  isEdit.value = false
  Object.assign(form, { name: '', parentId: parent ? Number(parent.id) : 0, sortOrder: 0, status: 1 })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { id: Number(row.id), name: row.name, parentId: Number(row.parentId ?? 0), sortOrder: Number(row.sortOrder), status: Number(row.status) })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleSave() {
  formRef.value?.validate().then(() => {
    saving.value = true
    const req = isEdit.value ? updateDept(form.id, { ...form }) : createDept({ ...form })
    req.then(() => {
      ElMessage.success(isEdit.value ? '已更新' : '已创建')
      dialogVisible.value = false
      fetchTree()
    }).catch((e) => {
      ElMessage.error(e?.response?.data?.msg || '保存失败')
    }).finally(() => { saving.value = false })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`删除部门"${row.name}"？有子部门或员工时无法删除`, '确认删除', { type: 'warning', confirmButtonText: '删除' }).then(() => {
    deleteDept(row.id).then(() => { ElMessage.success('已删除'); fetchTree() })
      .catch((e) => { ElMessage.error(e?.response?.data?.msg || '删除失败') })
  }).catch(() => {})
}

onMounted(fetchTree)
</script>

<template>
  <div>
    <div class="page-header"><div><h2>部门管理</h2><span class="sub">管理组织架构</span></div><el-button type="primary" @click="openCreate(null)">新增根部门</el-button></div>
    <div class="card-table">
      <el-table :data="tree" v-loading="loading" row-key="id" default-expand-all border stripe empty-text="暂无部门数据">
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'禁用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{row}">
            <el-button size="small" @click="openCreate(row)">添加子部门</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑部门':'新增部门'" width="420px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" maxlength="64" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
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
  部门管理 — admin / hr 共享
  树形展示组织架构，支持新增根部门 / 添加子部门 / 编辑 / 删除
  有子部门或员工的部门后端拒绝删除
-->