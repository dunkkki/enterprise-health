<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyExams } from '../../api/exam'
import { getRecordDetail } from '../../api/exam'
import { getHealthAdvice } from '../../api/rag'

const loading = ref(false)
const list = ref([])
const detailVisible = ref(false)
const detail = ref(null)
const advice = ref('')
const adviceLoading = ref(false)
const sources = ref([])

onMounted(() => {
  loading.value = true
  getMyExams().then(res => {
    list.value = res.data.data.records || []
  }).catch(() => {
    ElMessage.error('加载体检记录失败')
  }).finally(() => { loading.value = false })
})

function showDetail(row) {
  advice.value = ''
  sources.value = []
  getRecordDetail(row.id).then(res => {
    detail.value = res.data.data
    detailVisible.value = true
    loadAdvice(row.id)
  }).catch(() => { ElMessage.error('加载详情失败') })
}

function loadAdvice(recordId) {
  adviceLoading.value = true
  getHealthAdvice(recordId).then(res => {
    const data = res.data.data
    advice.value = data?.advice || ''
    sources.value = data?.sources || []
  }).catch(() => {
    ElMessage.error('AI 建议生成失败')
  }).finally(() => { adviceLoading.value = false })
}
</script>

<template>
  <div>
    <div class="page-header"><h2>我的体检</h2><span class="sub">查看历年体检报告</span></div>
    <div class="card-table">
      <el-table :data="list" v-loading="loading" border stripe empty-text="暂无体检记录">
        <el-table-column prop="scheduleTitle" label="排期" min-width="160" />
        <el-table-column prop="examDate" label="体检日期" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'warning':''" size="small">{{ ['未检','已检','请假'][row.status] }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="overallResult" label="结论" min-width="100" />
        <el-table-column label="操作" width="80">
          <template #default="{row}">
            <el-button size="small" type="primary" @click="showDetail(row)" :disabled="row.status!==1">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="detailVisible" title="体检报告详情" width="680px">
      <div v-if="detail">
        <p style="margin-bottom:8px"><b>{{ detail.userName }}</b> · {{ detail.scheduleTitle }}</p>
        <p style="color:var(--text-tertiary);font-size:var(--text-sm);margin-bottom:16px">
          体检日期: {{ detail.examDate }} · 结论: {{ detail.overallResult || '未填写' }}
        </p>
        <el-table :data="detail.items||[]" border size="small" empty-text="暂无指标数据">
          <el-table-column prop="itemName" label="指标" width="130" />
          <el-table-column prop="value" label="检测值" width="80" />
          <el-table-column prop="unit" label="单位" width="60" />
          <el-table-column label="参考范围" width="140">
            <template #default="{row}">{{ row.refMin || '-' }} ~ {{ row.refMax || '-' }}</template>
          </el-table-column>
          <el-table-column label="结果" width="70">
            <template #default="{row}">
              <el-tag v-if="row.isAbnormal===1" type="warning" size="small">偏高</el-tag>
              <el-tag v-else-if="row.isAbnormal===2" type="danger" size="small">偏低</el-tag>
              <el-tag v-else type="success" size="small">正常</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="advice-card" v-if="advice">
          <div class="advice-header"><span>AI 健康建议</span><el-button size="small" text @click="loadAdvice(detail.id)">刷新</el-button></div>
          <div class="advice-body" v-loading="adviceLoading">{{ advice }}</div>
          <div class="advice-sources" v-if="sources.length">
            参考来源：{{ sources.join('、') }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: var(--text-xl); font-weight: 600; color: var(--text-primary); margin: 0; }
.sub { font-size: var(--text-sm); color: var(--text-tertiary); display: block; margin-top: 2px; }

.card-table {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 20px 24px;
}

.advice-card {
  margin-top: 20px;
  background: var(--canvas);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  overflow: hidden;
}

.advice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: var(--border-light);
  border-bottom: 1px solid var(--border);
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
}

.advice-body {
  padding: 16px;
  font-size: var(--text-base);
  line-height: 1.8;
  color: var(--text-primary);
  white-space: pre-wrap;
  min-height: 40px;
}

.advice-sources {
  padding: 8px 16px;
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  border-top: 1px solid var(--border);
}
</style>