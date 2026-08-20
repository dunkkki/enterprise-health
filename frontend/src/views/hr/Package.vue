<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPackages, getPackageDetail, createPackage, updatePackage, deletePackage } from '../../api/exam'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ name: '', description: '', applicableGender: 2, price: 0, status: 1, items: [] })
const rules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
}
const detail = ref(null)

function fetchList() {
  loading.value = true
  getPackages().then(res => { list.value = res.data.data || [] })
    .catch(() => { ElMessage.error('加载套餐失败') })
    .finally(() => { loading.value = false })
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { name: '', description: '', applicableGender: 2, price: 0, status: 1, items: [] })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  getPackageDetail(row.id).then(res => {
    const d = res.data.data
    Object.assign(form, { id: Number(row.id), name: d.name, description: d.description||'', applicableGender: d.applicableGender??2, price: d.price||0, status: d.status??1, items: (d.items||[]).map(i=>({...i})) })
    dialogVisible.value = true
  }).catch(() => { ElMessage.error('加载套餐详情失败') })
}

function addItem() { form.items.push({ itemName:'', itemCategory:'', unit:'', refMin:null, refMax:null }) }
function removeItem(i) { form.items.splice(i,1) }

function handleSave() {
  formRef.value?.validate().then(() => {
    saving.value = true
    const req = isEdit.value ? updatePackage(form.id, { ...form }) : createPackage({ ...form })
    req.then(() => { ElMessage.success(isEdit.value?'已更新':'已创建'); dialogVisible.value=false; fetchList() })
      .catch((e) => { ElMessage.error(e?.response?.data?.msg || '保存失败') })
      .finally(() => { saving.value = false })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`删除套餐"${row.name}"？有关联排期时无法删除`, '确认删除', { type: 'warning', confirmButtonText: '删除' }).then(() => {
    deletePackage(row.id).then(() => { ElMessage.success('已删除'); fetchList() })
      .catch((e) => { ElMessage.error(e?.response?.data?.msg || '删除失败') })
  }).catch(() => {})
}

function showDetail(row) {
  getPackageDetail(row.id).then(res => { detail.value=res.data.data; detailVisible.value=true })
    .catch(() => { ElMessage.error('加载详情失败') })
}

onMounted(fetchList)
</script>

<template>
  <div>
    <div class="page-header"><div><h2>体检套餐</h2><span class="sub">管理体检套餐与检查项目</span></div><el-button type="primary" @click="openCreate">新增套餐</el-button></div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无套餐数据">
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column label="性别" width="70"><template #default="{row}">{{ row.applicableGender===1?'男':row.applicableGender===0?'女':'不限' }}</template></el-table-column>
        <el-table-column prop="price" label="价格" width="90" />
        <el-table-column label="状态" width="70"><template #default="{row}"><el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'启用':'停用' }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{row}">
            <el-button size="small" @click="showDetail(row)">详情</el-button>
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑套餐':'新增套餐'" width="720px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="名称" prop="name"><el-input v-model="form.name" maxlength="64" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="适用性别"><el-select v-model="form.applicableGender"><el-option label="不限" :value="2" /><el-option label="男" :value="1" /><el-option label="女" :value="0" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="12"><el-form-item label="价格"><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="描述"><el-input v-model="form.description" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="体检项目">
          <div v-for="(it,i) in form.items" :key="i" style="display:flex;gap:6px;margin-bottom:6px;align-items:center">
            <el-input v-model="it.itemName" placeholder="名称" size="small" style="width:100px" />
            <el-input v-model="it.itemCategory" placeholder="类别" size="small" style="width:70px" />
            <el-input v-model="it.unit" placeholder="单位" size="small" style="width:60px" />
            <el-input-number v-model="it.refMin" placeholder="下限" size="small" style="width:85px" controls-position="right" />
            <el-input-number v-model="it.refMax" placeholder="上限" size="small" style="width:85px" controls-position="right" />
            <el-button size="small" type="danger" circle @click="removeItem(i)">×</el-button>
          </div>
          <el-button size="small" @click="addItem">+ 添加项目</el-button>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="handleSave" :loading="saving">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="套餐详情" width="600px">
      <div v-if="detail">
        <p style="margin-bottom:4px"><b>{{ detail.name }}</b>  ¥{{ detail.price }}</p>
        <p style="color:#6b7280;font-size:13px;margin-bottom:12px">{{ detail.description }}</p>
        <el-table :data="detail.items||[]" border size="small" empty-text="暂无检查项目">
          <el-table-column prop="itemName" label="项目" width="120" />
          <el-table-column prop="itemCategory" label="类别" width="80" />
          <el-table-column prop="unit" label="单位" width="70" />
          <el-table-column prop="refMin" label="下限" width="80" />
          <el-table-column prop="refMax" label="上限" width="80" />
        </el-table>
      </div>
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
  体检套餐 — admin / hr 共享
  管理体检套餐模板（名称/性别/价格/状态）和套餐内的检查项目列表
  每个项目包含名称、类别、单位、参考上下限，录入结果时据此自动判异常
  有关联排期的套餐不可删除
-->