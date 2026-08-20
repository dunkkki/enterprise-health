<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getSummary, getRiskDistribution, getDeptRanking, getExamTrend, getInterventionStats } from '../../api/dashboard'

const loading = ref(true)
const error = ref(null)

const summary = ref({ totalUsers: 0, examCompletionRate: '0%', highRiskCount: 0, activeInterventions: 0 })
const riskDist = ref({ low: 0, medium: 0, high: 0 })
const deptRanking = ref([])
const examTrend = ref([])
const interventionStats = ref({})

let pieChart = null, barChart = null, trendChart = null

/* ── KPI cards with deltas (mock deltas until API provides them) ── */
const kpiCards = computed(() => [
  {
    label: '总员工',
    value: summary.value.totalUsers,
    format: n => String(n),
    delta: '+3',
    deltaOk: true,
    hint: '较上月'
  },
  {
    label: '体检完成率',
    value: summary.value.examCompletionRate,
    format: v => v,
    delta: '+5.2%',
    deltaOk: true,
    hint: '较上季度'
  },
  {
    label: '高风险人数',
    value: summary.value.highRiskCount,
    format: n => String(n),
    delta: '-2',
    deltaOk: true,
    hint: '较上月'
  },
  {
    label: '干预进行中',
    value: summary.value.activeInterventions,
    format: n => String(n),
    delta: '+1',
    deltaOk: false,
    hint: '新增'
  }
])

/* ── Data fetch ── */
onMounted(async () => {
  try {
    const [s, r, d, t, i] = await Promise.all([
      getSummary(), getRiskDistribution(), getDeptRanking(), getExamTrend(), getInterventionStats()
    ])
    summary.value = s.data.data || summary.value
    riskDist.value = r.data.data || riskDist.value
    deptRanking.value = d.data.data?.ranking || []
    examTrend.value = t.data.data?.months || []
    interventionStats.value = i.data.data || {}
  } catch (e) {
    error.value = '加载看板数据失败'
    ElMessage.error('加载看板数据失败')
  } finally {
    loading.value = false
  }
  await nextTick()
  if (!error.value) {
    renderPie()
    renderBar()
    renderTrend()
  }
})

/* ── Charts ── */
function renderPie() {
  const el = document.getElementById('risk-pie')
  if (!el || el.clientWidth === 0) return
  if (pieChart) pieChart.dispose()
  pieChart = echarts.init(el)
  pieChart.setOption({
    tooltip: { trigger: 'item', backgroundColor: '#fff', borderColor: '#E5E8EC', textStyle: { color: '#1A1D23', fontSize: 13 } },
    legend: { bottom: 0, textStyle: { color: '#5F6B7A', fontSize: 12 }, itemWidth: 8, itemHeight: 8 },
    series: [{
      type: 'pie',
      radius: ['58%', '80%'],
      center: ['50%', '46%'],
      emphasis: { scale: false },
      label: { show: false },
      data: [
        { name: '低风险', value: riskDist.value.low || 0, itemStyle: { color: '#009E73' } },
        { name: '中风险', value: riskDist.value.medium || 0, itemStyle: { color: '#E69F00' } },
        { name: '高风险', value: riskDist.value.high || 0, itemStyle: { color: '#D55E00' } }
      ]
    }]
  })
}

function renderBar() {
  const el = document.getElementById('dept-bar')
  if (!el || el.clientWidth === 0) return
  if (barChart) barChart.dispose()
  const sorted = [...deptRanking.value].sort((a, b) => b.avgScore - a.avgScore)
  barChart = echarts.init(el)
  barChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#E5E8EC', textStyle: { color: '#1A1D23', fontSize: 13 } },
    grid: { left: 4, right: 20, top: 8, bottom: 24 },
    xAxis: {
      type: 'category',
      data: sorted.map(d => d.deptName),
      axisLine: { lineStyle: { color: '#E5E8EC' } },
      axisLabel: { color: '#5F6B7A', fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '平均分',
      nameTextStyle: { color: '#8E97A4', fontSize: 11 },
      splitLine: { lineStyle: { color: '#EEF0F3' } },
      axisLabel: { color: '#8E97A4', fontSize: 11 }
    },
    series: [{
      type: 'bar',
      barWidth: 28,
      data: sorted.map(d => ({
        value: d.avgScore,
        itemStyle: {
          color: d.avgScore >= 80 ? '#009E73' : d.avgScore >= 60 ? '#E69F00' : '#D55E00',
          borderRadius: [6, 6, 0, 0]
        }
      }))
    }]
  })
}

function renderTrend() {
  const el = document.getElementById('exam-trend')
  if (!el || el.clientWidth === 0) return
  const months = examTrend.value
  if (!months.length) return
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(el)
  trendChart.setOption({
    tooltip: { trigger: 'axis', backgroundColor: '#fff', borderColor: '#E5E8EC', textStyle: { color: '#1A1D23', fontSize: 13 } },
    grid: { left: 4, right: 16, top: 8, bottom: 24 },
    xAxis: {
      type: 'category',
      data: months.map(m => m.month),
      axisLine: { lineStyle: { color: '#E5E8EC' } },
      axisLabel: { color: '#5F6B7A', fontSize: 11 },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '完成率 %',
      nameTextStyle: { color: '#8E97A4', fontSize: 11 },
      splitLine: { lineStyle: { color: '#EEF0F3' } },
      axisLabel: { color: '#8E97A4', fontSize: 11 },
      min: 0, max: 100
    },
    series: [{
      type: 'line',
      data: months.map(m => m.rate),
      smooth: true,
      symbol: 'circle',
      symbolSize: 4,
      lineStyle: { color: '#E8950A', width: 2 },
      itemStyle: { color: '#E8950A' },
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: 'rgba(232,149,10,0.12)' },
        { offset: 1, color: 'rgba(232,149,10,0.01)' }
      ])}
    }]
  })
}

/* ── Table fallback visibility ── */
const showPieTable = ref(false)
const showBarTable = ref(false)
</script>

<template>
  <div class="dashboard">
    <!-- Header -->
    <div class="page-head">
      <div>
        <h2>数据看板</h2>
        <p class="page-desc">企业健康数据概览</p>
      </div>
      <span class="freshness">更新于 {{ new Date().toLocaleTimeString('zh-CN', { hour:'2-digit', minute:'2-digit' }) }}</span>
    </div>

    <!-- KPI Row -->
    <div v-if="!error" class="kpi-grid">
      <div v-for="card in kpiCards" :key="card.label" class="kpi-card">
        <div v-if="loading" class="skeleton-block" style="height:80px" />
        <template v-else>
          <span class="kpi-label">{{ card.label }}</span>
          <span class="kpi-value">{{ card.format(card.value) }}</span>
          <div class="kpi-delta">
            <span class="kpi-delta-val" :class="card.deltaOk ? 'delta-up' : 'delta-warn'">
              <span class="delta-icon">{{ card.deltaOk ? '&#8593;' : '&#8595;' }}</span>
              {{ card.delta }}
            </span>
            <span class="kpi-hint">{{ card.hint }}</span>
          </div>
        </template>
      </div>
    </div>

    <!-- Error state -->
    <div v-if="error" class="state-panel state-error">
      <span>{{ error }}</span>
      <el-button size="small" text type="primary" @click="loading=true; error=null; onMounted()">重试</el-button>
    </div>

    <!-- Charts row -->
    <div v-if="!error" class="chart-grid">
      <!-- Pie -->
      <div class="chart-card">
        <div class="chart-head">
          <h3>风险等级分布</h3>
          <el-button text size="small" @click="showPieTable = !showPieTable">
            {{ showPieTable ? '图表' : '表格' }}
          </el-button>
        </div>
        <div v-if="loading" class="skeleton-block" style="height:280px" />
        <div v-else-if="riskDist.low===0 && riskDist.medium===0 && riskDist.high===0" class="chart-empty">
          <p>暂无评估数据</p>
          <span>创建评估规则后可查看风险分布</span>
        </div>
        <template v-else>
          <div v-show="!showPieTable" id="risk-pie" style="height:300px" />
          <table v-show="showPieTable" class="data-table" aria-label="风险分布数据表">
            <thead><tr><th>等级</th><th>人数</th><th>占比</th></tr></thead>
            <tbody>
              <tr><td><span class="dot" style="background:#009E73" /> 低风险</td><td class="num">{{ riskDist.low }}</td><td class="num">{{ riskDist.low + riskDist.medium + riskDist.high > 0 ? Math.round(riskDist.low / (riskDist.low + riskDist.medium + riskDist.high) * 100) : 0 }}%</td></tr>
              <tr><td><span class="dot" style="background:#E69F00" /> 中风险</td><td class="num">{{ riskDist.medium }}</td><td class="num">{{ riskDist.low + riskDist.medium + riskDist.high > 0 ? Math.round(riskDist.medium / (riskDist.low + riskDist.medium + riskDist.high) * 100) : 0 }}%</td></tr>
              <tr><td><span class="dot" style="background:#D55E00" /> 高风险</td><td class="num">{{ riskDist.high }}</td><td class="num">{{ riskDist.low + riskDist.medium + riskDist.high > 0 ? Math.round(riskDist.high / (riskDist.low + riskDist.medium + riskDist.high) * 100) : 0 }}%</td></tr>
            </tbody>
          </table>
        </template>
      </div>

      <!-- Bar -->
      <div class="chart-card">
        <div class="chart-head">
          <h3>部门健康评分排行</h3>
          <el-button text size="small" @click="showBarTable = !showBarTable">
            {{ showBarTable ? '图表' : '表格' }}
          </el-button>
        </div>
        <div v-if="loading" class="skeleton-block" style="height:280px" />
        <div v-else-if="deptRanking.length===0" class="chart-empty">
          <p>暂无部门数据</p>
          <span>添加部门后可查看排行</span>
        </div>
        <template v-else>
          <div v-show="!showBarTable" id="dept-bar" style="height:300px" />
          <table v-show="showBarTable" class="data-table" aria-label="部门排行数据表">
            <thead><tr><th>部门</th><th class="num">平均分</th><th>等级</th></tr></thead>
            <tbody>
              <tr v-for="d in [...deptRanking].sort((a,b) => b.avgScore - a.avgScore)" :key="d.deptName">
                <td>{{ d.deptName }}</td>
                <td class="num">{{ d.avgScore }}</td>
                <td><span class="badge" :class="d.avgScore >= 80 ? 'badge-ok' : d.avgScore >= 60 ? 'badge-warn' : 'badge-danger'">{{ d.avgScore >= 80 ? '良好' : d.avgScore >= 60 ? '一般' : '关注' }}</span></td>
              </tr>
            </tbody>
          </table>
        </template>
      </div>
    </div>

    <!-- Trend row -->
    <div v-if="!error" class="trend-row">
      <div class="chart-card chart-wide">
        <div class="chart-head">
          <h3>体检完成率趋势</h3>
        </div>
        <div v-if="loading" class="skeleton-block" style="height:240px" />
        <div v-else-if="examTrend.length===0" class="chart-empty">
          <p>暂无趋势数据</p>
        </div>
        <div v-else id="exam-trend" style="height:260px" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { font-family: var(--font); }

/* ── Header ── */
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-head h2 {
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 2px;
}

.page-desc {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

.freshness {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-family: var(--font-mono);
}

/* ── KPI Grid ── */
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.kpi-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.kpi-label {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  font-weight: 400;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.kpi-value {
  font-size: var(--text-3xl);
  font-weight: 600;
  color: var(--text-primary);
  font-family: var(--font-mono);
  line-height: 1;
}

.kpi-delta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 2px;
}

.kpi-delta-val {
  font-size: var(--text-xs);
  font-weight: 500;
  font-family: var(--font-mono);
}

.delta-up { color: var(--status-success); }
.delta-warn { color: var(--status-warning); }

.delta-icon { font-style: normal; }

.kpi-hint {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* ── Charts ── */
.chart-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.trend-row { margin-bottom: 20px; }

.chart-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 18px 20px;
}

.chart-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.chart-head h3 {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
  margin: 0;
}

/* ── Data table fallback ── */
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: var(--text-sm);
  margin-top: 8px;
}

.data-table th {
  text-align: left;
  color: var(--text-tertiary);
  font-weight: 400;
  font-size: var(--text-xs);
  padding: 8px 12px;
  border-bottom: 1px solid var(--border);
}

.data-table td {
  padding: 8px 12px;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light);
}

.data-table th.num, .data-table td.num {
  text-align: right;
  font-family: var(--font-mono);
}

.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}

.badge {
  font-size: var(--text-xs);
  padding: 1px 8px;
  border-radius: 999px;
  font-weight: 500;
}

.badge-ok { background: rgba(0,158,115,0.1); color: var(--status-success); }
.badge-warn { background: rgba(230,159,0,0.1); color: var(--status-warning); }
.badge-danger { background: rgba(213,94,0,0.1); color: var(--status-danger); }

/* ── States ── */
.skeleton-block {
  background: linear-gradient(90deg, #EEF0F3 25%, #F8F9FB 50%, #EEF0F3 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.chart-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 280px;
  color: var(--text-tertiary);
}

.chart-empty p { margin: 0; font-size: var(--text-sm); font-weight: 500; }
.chart-empty span { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 4px; }

.state-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  margin-bottom: 20px;
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

/* ── Responsive ── */
@media (max-width: 960px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .chart-grid { grid-template-columns: 1fr; }
}
</style>
