<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole, getRoleMenus, assignRoleMenus, getMenuTree } from '../../api/admin'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const menuVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', code: '', description: '', dataScope: 2 })
const rules = {
  name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}
const menuTree = ref([])
const checkedMenus = ref([])
const currentRole = ref(null)

function fetchList() {
  loading.value = true
  getRoles().then(res => {
    list.value = res.data.data || []
  }).catch(() => {
    ElMessage.error('加载角色列表失败')
  }).finally(() => { loading.value = false })
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { name: '', code: '', description: '', dataScope: 2 })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { id: Number(row.id), name: row.name, code: row.code, description: row.description, dataScope: Number(row.dataScope) })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleSave() {
  formRef.value?.validate().then(() => {
    saving.value = true
    const req = isEdit.value
      ? updateRole(form.id, { name: form.name, code: form.code, description: form.description, dataScope: form.dataScope })
      : createRole({ name: form.name, code: form.code, description: form.description, dataScope: form.dataScope })
    req.then(() => {
      ElMessage.success(isEdit.value ? '已更新' : '已创建')
      dialogVisible.value = false
      fetchList()
    }).catch(() => {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    }).finally(() => { saving.value = false })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`删除角色"${row.name}"？删除后该角色的用户将失去对应权限`, '确认删除', { type: 'warning', confirmButtonText: '删除' }).then(() => {
    deleteRole(row.id).then(() => {
      ElMessage.success('已删除')
      fetchList()
    }).catch((e) => {
      ElMessage.error(e?.response?.data?.msg || e?.message || '删除失败')
    })
  }).catch(() => {})
}

function openMenu(row) {
  currentRole.value = row
  Promise.all([getMenuTree(), getRoleMenus(row.id)]).then(([treeRes, menuRes]) => {
    menuTree.value = treeRes.data.data || []
    checkedMenus.value = (menuRes.data.data?.menuIds || []).map(String)
  }).catch(() => {
    ElMessage.error('加载菜单失败')
  })
  menuVisible.value = true
}

function saveMenu() {
  assignRoleMenus(currentRole.value.id, checkedMenus.value.map(Number)).then(() => {
    ElMessage.success('菜单已分配')
    menuVisible.value = false
  }).catch(() => {
    ElMessage.error('保存菜单失败')
  })
}

onMounted(fetchList)
</script>

<template>
  <div>
    <div class="page-header"><div><h2>角色管理</h2><span class="sub">管理角色与权限分配</span></div><el-button type="primary" @click="openCreate">新增角色</el-button></div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无角色数据">
        <el-table-column prop="name" label="名称" width="100" />
        <el-table-column prop="code" label="编码" width="90" />
        <el-table-column prop="description" label="描述" min-width="160" />
        <el-table-column label="数据范围" width="100">
          <template #default="{row}"><el-tag size="small">{{ {0:'全部',1:'本部门',2:'仅本人'}[row.dataScope] }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="240">
          <template #default="{row}">
            <el-button size="small" @click="openMenu(row)">分配菜单</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑角色':'新增角色'" width="480px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="名称" prop="name"><el-input v-model="form.name" maxlength="32" show-word-limit /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="编码" prop="code"><el-input v-model="form.code" maxlength="32" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="描述"><el-input v-model="form.description" maxlength="128" /></el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope">
            <el-option label="全部数据" :value="0" /><el-option label="本部门数据" :value="1" /><el-option label="仅本人数据" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="menuVisible" :title="`分配菜单 - ${currentRole?.name}`" width="500px">
      <el-tree :data="menuTree" show-checkbox node-key="id" :props="{ label:'name', children:'children' }" v-model:checked-keys="checkedMenus" default-expand-all empty-text="暂无菜单数据" />
      <template #footer><el-button @click="menuVisible=false">取消</el-button><el-button type="primary" @click="saveMenu">保存</el-button></template>
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
  角色管理 — 系统管理员专属功能
  可创建/编辑/删除角色，设置角色的数据权限范围（全部/本部门/仅本人）
  通过"分配菜单"弹窗为角色绑定可访问的菜单和按钮权限
  有用户的角色无法删除，需先移除关联用户
-->
