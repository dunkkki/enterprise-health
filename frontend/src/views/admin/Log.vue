<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOperationLogs, getLoginLogs } from '../../api/admin'

const activeTab = ref('operation')
const loading = ref(false)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const username = ref('')
const module = ref('')
const status = ref('')

const moduleOptions = ['部门管理','用户管理','角色管理','菜单管理','体检套餐','体检排期','体检记录','风险规则','风险评估','干预管理','随访记录']

function fetchLogs() {
  loading.value = true
  const params = { page: page.value, size: size.value }
  if (username.value) params.username = username.value

  let req
  if (activeTab.value === 'operation') {
    if (module.value) params.module = module.value
    req = getOperationLogs(params)
  } else {
    if (status.value !== '') params.status = status.value
    req = getLoginLogs(params)
  }

  req.then(res => {
    list.value = res.data.data.records || []
    total.value = res.data.data.total || 0
  }).catch(() => {
    ElMessage.error('加载日志失败')
  }).finally(() => { loading.value = false })
}

function handleSearch() { page.value = 1; fetchLogs() }
function onPageChange(p) { page.value = p; fetchLogs() }
function onTabChange() { page.value = 1; username.value = ''; module.value = ''; status.value = ''; fetchLogs() }

onMounted(fetchLogs)
</script>

<template>
  <div>
    <h2 style="font-size:18px;color:#303133;margin-bottom:16px">系统日志</h2>
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="操作日志" name="operation">
        <div style="display:flex;gap:12px;margin-bottom:16px">
          <el-input v-model="username" placeholder="用户名" clearable style="width:150px" @keyup.enter="handleSearch" @clear="handleSearch" />
          <el-select v-model="module" clearable placeholder="选择模块" style="width:150px" @change="handleSearch" @clear="handleSearch">
            <el-option v-for="m in moduleOptions" :key="m" :label="m" :value="m" />
          </el-select>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="username=''; module=''; handleSearch()">重置</el-button>
        </div>
        <el-table :data="list" v-loading="loading" border stripe empty-text="暂无操作记录">
          <el-table-column prop="username" label="用户" width="100" />
          <el-table-column prop="module" label="模块" width="100" />
          <el-table-column prop="description" label="操作描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="requestMethod" label="方法" width="70" />
          <el-table-column prop="requestUrl" label="URL" min-width="200" show-overflow-tooltip />
          <el-table-column prop="ip" label="IP" width="140" />
          <el-table-column prop="duration" label="耗时(ms)" width="90" />
          <el-table-column prop="createdAt" label="操作时间" width="170" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="登录日志" name="login">
        <div style="display:flex;gap:12px;margin-bottom:16px">
          <el-input v-model="username" placeholder="用户名" clearable style="width:150px" @keyup.enter="handleSearch" @clear="handleSearch" />
          <el-select v-model="status" clearable placeholder="登录状态" style="width:130px" @change="handleSearch" @clear="handleSearch">
            <el-option label="登录成功" :value="1" />
            <el-option label="登录失败" :value="0" />
          </el-select>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="username=''; status=''; handleSearch()">重置</el-button>
        </div>
        <el-table :data="list" v-loading="loading" border stripe empty-text="暂无登录记录">
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column label="状态" width="90">
            <template #default="{row}">
              <el-tag :type="row.status===1?'success':'danger'" size="small">{{ row.status===1?'成功':'失败' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="failReason" label="失败原因" width="130" show-overflow-tooltip />
          <el-table-column prop="ip" label="IP 地址" width="140" />
          <el-table-column prop="userAgent" label="User-Agent" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createdAt" label="登录时间" width="170" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <div style="display:flex;justify-content:flex-end;margin-top:16px">
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" @current-change="onPageChange" layout="total, prev, pager, next" background />
    </div>
  </div>
</template>

<!--
  系统日志 — 系统管理员专属功能
  操作日志 Tab：记录所有增删改操作的执行人、模块、URL、IP、耗时，支持按用户名和模块筛选
  登录日志 Tab：记录每次登录的成功/失败、用户名、IP、浏览器信息，支持按状态筛选
  两个 Tab 均支持分页和搜索条件重置
-->
