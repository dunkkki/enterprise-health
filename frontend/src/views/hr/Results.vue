<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getRiskResults, assessRecord } from '../../api/risk'
import { getDeptTree } from '../../api/org'
import { suggestPlan } from '../../api/rag'

const router = useRouter()
const route = useRoute()

const prefix = route.path.startsWith('/leader') ? '/leader' : route.path.startsWith('/hr') ? '/hr' : '/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const deptId = ref('')
const riskLevel = ref('')
const depts = ref([])
const aiVisible = ref(false)
const aiLoading = ref(false)
const aiResult = ref({ suggestion: '', sources: [], suggestedType: '', suggestedTitle: '' })
const currentRow = ref(null)

function fetchList() {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (deptId.value) params.deptId = deptId.value
  if (riskLevel.value) params.riskLevel = riskLevel.value
  getRiskResults(params).then(res => {
    list.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  }).catch(() => { ElMessage.error('加载评估结果失败') })
    .finally(() => { loading.value = false })
}

function handleSearch() { page.value = 1; fetchList() }
function onPageChange(p) { page.value = p; fetchList() }

function doAssess(row) {
  assessRecord(row.recordId).then(() => { ElMessage.success('评估完成'); fetchList() })
    .catch(() => { ElMessage.error('评估失败') })
}

function openAiSuggest(row) {
  currentRow.value = row
  aiResult.value = { suggestion: '', sources: [], suggestedType: '', suggestedTitle: '' }
  aiVisible.value = true
  aiLoading.value = true

  const hitRules = []
  try {
    const detail = typeof row.detailJson === 'string' ? JSON.parse(row.detailJson) : (row.detailJson || [])
    hitRules.push(...detail.map(d => d.ruleName).filter(Boolean))
  } catch {}

  suggestPlan({
    riskLevel: row.riskLevel,
    totalScore: row.totalScore,
    hitRules: hitRules
  }).then(res => {
    const data = res.data.data || {}
    aiResult.value = {
      suggestion: data.suggestion || data.advice || '',
      sources: data.sources || [],
      suggestedType: data.suggestedType || '',
      suggestedTitle: data.suggestedTitle || ''
    }
  }).catch(() => { ElMessage.error('AI 建议生成失败') })
    .finally(() => { aiLoading.value = false })
}

function goToPlan() {
  aiVisible.value = false
  const q = new URLSearchParams({
    title: aiResult.value.suggestedTitle || 'AI 干预计划',
    type: aiResult.value.suggestedType || 'weight',
    description: aiResult.value.suggestion.slice(0, 500)
  })
  router.push(`${prefix}/intervention/plan?${q.toString()}`)
}

onMounted(() => {
  getDeptTree().then(res => { depts.value = res.data.data || [] })
  fetchList()
})
</script>

<template>
  <div>
    <div class="page-header"><div><h2>评估结果</h2><span class="sub">查看员工健康风险评估</span></div></div>
    <div style="display:flex;gap:12px;margin-bottom:16px">
      <el-select v-model="deptId" clearable placeholder="选择部门" style="width:160px" @change="handleSearch">
        <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
      </el-select>
      <el-select v-model="riskLevel" clearable placeholder="风险等级" style="width:120px" @change="handleSearch">
        <el-option label="低风险" value="低" /><el-option label="中风险" value="中" /><el-option label="高风险" value="高" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="deptId=''; riskLevel=''; handleSearch()">重置</el-button>
    </div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无评估结果">
        <el-table-column prop="userName" label="员工" width="90" />
        <el-table-column prop="scheduleTitle" label="排期" min-width="120" />
        <el-table-column prop="totalScore" label="总分" width="80" />
        <el-table-column label="等级" width="80">
          <template #default="{row}"><el-tag :type="row.riskLevel==='高'?'danger':row.riskLevel==='中'?'warning':'success'" size="small">{{ row.riskLevel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="assessedAt" label="评估时间" width="170" />
        <el-table-column label="操作" width="180">
          <template #default="{row}">
            <el-button size="small" type="primary" @click="doAssess(row)">重新评估</el-button>
            <el-button size="small" @click="openAiSuggest(row)">AI 建议</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="onPageChange" style="margin-top:16px;justify-content:flex-end" layout="total,prev,pager,next" />
    </div>

    <el-dialog v-model="aiVisible" title="AI 干预方案建议" width="600px">
      <div v-loading="aiLoading" style="min-height:80px; font-size:var(--text-base); line-height:1.8; white-space:pre-wrap; color:var(--text-primary)">
        {{ aiResult.suggestion || '正在生成建议...' }}
      </div>
      <div v-if="aiResult.sources.length" style="margin-top:12px; padding-top:12px; border-top:1px solid var(--border); font-size:var(--text-xs); color:var(--text-tertiary)">
        参考来源：{{ aiResult.sources.join('、') }}
      </div>
      <template #footer>
        <el-button @click="aiVisible=false">关闭</el-button>
        <el-button type="primary" @click="goToPlan" :disabled="!aiResult.suggestion">转为干预计划</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px; }
.page-header h2 { font-size: var(--text-xl); font-weight: 600; color: var(--text-primary); margin: 0; }
.sub { font-size: var(--text-sm); color: var(--text-tertiary); display: block; margin-top: 2px; }

.card-table {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 20px 24px;
}
</style>

<!--
  评估结果 — admin / hr / leader 共享
  新增 AI 建议按钮：点击后根据风险等级和命中规则调用 RAG 生成干预方案
  建议内容弹窗展示，底部标注参考知识来源
-->