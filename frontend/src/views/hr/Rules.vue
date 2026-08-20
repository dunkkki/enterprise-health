<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRiskRules, createRiskRule, updateRiskRule, deleteRiskRule } from '../../api/risk'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ ruleName: '', packageItemId: '', riskLevel: 1, conditionType: 'gt', thresholdValue: '', score: 0, weight: 1.0, status: 1 })
const rules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  packageItemId: [{ required: true, message: '请输入指标ID', trigger: 'blur' }],
}
const condOpts = ['gt','lt','out_of_range','equals']
const levelOpts = [{ label:'低', value:1 },{ label:'中', value:2 },{ label:'高', value:3 }]

function fetchList() {
  loading.value = true
  getRiskRules().then(res => { list.value = res.data.data || [] })
    .catch(() => { ElMessage.error('加载规则失败') })
    .finally(() => { loading.value = false })
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { ruleName: '', packageItemId: '', riskLevel: 1, conditionType: 'gt', thresholdValue: '', score: 0, weight: 1.0, status: 1 })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { ...row, id: Number(row.id) })
  formRef.value?.resetFields()
  dialogVisible.value = true
}

function handleSave() {
  formRef.value?.validate().then(() => {
    saving.value = true
    const req = isEdit.value ? updateRiskRule(form.id, { ...form }) : createRiskRule({ ...form })
    req.then(() => { ElMessage.success(isEdit.value ? '已更新' : '已创建'); dialogVisible.value = false; fetchList() })
      .catch((e) => { ElMessage.error(e?.response?.data?.msg || '保存失败') })
      .finally(() => { saving.value = false })
  }).catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(`删除规则"${row.ruleName}"？`, '确认删除', { type: 'warning', confirmButtonText: '删除' }).then(() => {
    deleteRiskRule(row.id).then(() => { ElMessage.success('已删除'); fetchList() })
      .catch(() => { ElMessage.error('删除失败') })
  }).catch(() => {})
}

onMounted(fetchList)
</script>

<template>
  <div>
    <div class="page-header"><div><h2>评估规则</h2><span class="sub">配置体检指标的风险评估规则</span></div><el-button type="primary" @click="openCreate">新增规则</el-button></div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无规则数据">
        <el-table-column prop="ruleName" label="规则名称" min-width="140" />
        <el-table-column prop="packageItemId" label="指标ID" width="80" />
        <el-table-column label="条件" width="110"><template #default="{row}">{{ {gt:'大于',lt:'小于',out_of_range:'超出范围',equals:'等于'}[row.conditionType] || row.conditionType }}</template></el-table-column>
        <el-table-column prop="thresholdValue" label="阈值" width="70" />
        <el-table-column prop="score" label="分数" width="60" />
        <el-table-column prop="weight" label="权重" width="60" />
        <el-table-column label="等级" width="60"><template #default="{row}"><el-tag :type="row.riskLevel===3?'danger':row.riskLevel===2?'warning':''" size="small">{{ ['','低','中','高'][row.riskLevel] }}</el-tag></template></el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{row}"><el-button size="small" @click="openEdit(row)">编辑</el-button><el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button></template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑规则':'新增规则'" width="500px" @closed="formRef?.resetFields()">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="10">
          <el-col :span="14"><el-form-item label="名称" prop="ruleName"><el-input v-model="form.ruleName" maxlength="64" /></el-form-item></el-col>
          <el-col :span="10"><el-form-item label="指标ID" prop="packageItemId"><el-input-number v-model="form.packageItemId" :min="1" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="10"><el-form-item label="条件类型"><el-select v-model="form.conditionType"><el-option v-for="c in condOpts" :key="c" :label="{gt:'大于',lt:'小于',out_of_range:'超出范围',equals:'等于'}[c]" :value="c" /></el-select></el-form-item></el-col>
          <el-col :span="7"><el-form-item label="阈值"><el-input v-model="form.thresholdValue" /></el-form-item></el-col>
          <el-col :span="7"><el-form-item label="等级"><el-select v-model="form.riskLevel"><el-option v-for="l in levelOpts" :key="l.value" :label="l.label" :value="l.value" /></el-select></el-form-item></el-col>
        </el-row>
        <el-row :gutter="10">
          <el-col :span="8"><el-form-item label="分数"><el-input-number v-model="form.score" :min="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="权重"><el-input-number v-model="form.weight" :min="0" :precision="2" :step="0.1" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
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
  评估规则 — admin / hr 共享
  配置健康风险评估规则引擎：选择指标、设定条件类型（大于/小于/超出范围/等于）、阈值、分数、权重
  规则触发后加权计分，总分判定风险等级（低<20 / 中20-50 / 高>50）
-->