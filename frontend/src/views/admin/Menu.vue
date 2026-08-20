<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMenuTree, createMenu, updateMenu, deleteMenu } from '../../api/admin'

const tree = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', parentId: 0, path: '', icon: '', sortOrder: 0, type: 1, permission: '', visible: 1 })
const rules = computed(() => ({
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  path: form.type !== 0 ? [{ required: true, message: '菜单/按钮必须填写路由路径', trigger: 'blur' }] : [],
}))

function fetchTree() {
  loading.value = true
  getMenuTree().then(res => {
    tree.value = res.data.data || []
  }).catch(() => {
    ElMessage.error('加载菜单失败')
  }).finally(() => { loading.value = false })
}

function openCreate(parent) {
  isEdit.value = false
  Object.assign(form, { name: '', parentId: parent ? Number(parent.id) : 0, path: '', icon: '', sortOrder: 0, type: 1, permission: '', visible: 1 })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: Number(row.id), name: row.name,
    parentId: Number(row.parentId ?? 0), path: row.path || '', icon: row.icon || '',
    sortOrder: Number(row.sortOrder ?? 0), type: Number(row.type),
    permission: row.permission || '', visible: Number(row.visible)
  })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleSave() {
  formRef.value?.validate().then(() => {
    saving.value = true
    const req = isEdit.value
      ? updateMenu(form.id, { ...form })
      : createMenu({ ...form })
    req.then(() => {
      ElMessage.success(isEdit.value ? '已更新' : '已创建')
      dialogVisible.value = false
      fetchTree()
    }).catch(() => {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }).finally(() => { saving.value = false })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`删除菜单"${row.name}"？有子菜单时无法删除`, '确认删除', { type: 'warning', confirmButtonText: '删除' }).then(() => {
    deleteMenu(row.id).then(() => {
      ElMessage.success('已删除')
      fetchTree()
    }).catch(() => {
      ElMessage.error('删除失败')
    })
  }).catch(() => {})
}

onMounted(fetchTree)
</script>

<template>
  <div>
    <div class="page-header"><div><h2>菜单管理</h2><span class="sub">管理系统菜单与权限标识</span></div><el-button type="primary" @click="openCreate(null)">新增菜单</el-button></div>
    <div class="card-table">
      <el-table :data="tree" v-loading="loading" row-key="id" default-expand-all border stripe empty-text="暂无菜单数据">
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="path" label="路径" width="160" />
        <el-table-column prop="permission" label="权限标识" width="140" />
        <el-table-column label="类型" width="70"><template #default="{row}"><el-tag size="small">{{ ['目录','菜单','按钮'][row.type] }}</el-tag></template></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="60" />
        <el-table-column label="操作" width="240">
          <template #default="{row}">
            <el-button size="small" @click="openCreate(row)" v-if="row.type!=2">添加子项</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑菜单':'新增菜单'" width="480px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="名称" prop="name"><el-input v-model="form.name" maxlength="32" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="类型"><el-select v-model="form.type"><el-option label="目录" :value="0" /><el-option label="菜单" :value="1" /><el-option label="按钮" :value="2" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="路径" prop="path"><el-input v-model="form.path" maxlength="128" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="图标"><el-input v-model="form.icon" placeholder="Element Plus 图标名" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="标识"><el-input v-model="form.permission" placeholder="如 dept:create" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item></el-col>
        </el-row>
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
  菜单管理 — 系统管理员专属功能
  管理三级菜单树：目录（分组）→ 菜单（页面）→ 按钮（操作权限）
  每级可配置名称、路径、图标、权限标识、排序号
  目录类型不强制填写路径，菜单/按钮类型必须填写
  有子菜单的父级菜单无法删除
-->
