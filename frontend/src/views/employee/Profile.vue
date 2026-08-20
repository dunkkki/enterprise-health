<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMe, changePassword } from '../../api/auth'

const user = ref({})
const loading = ref(false)
const saving = ref(false)
const formRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (_, v, cb) => v !== pwdForm.newPassword ? cb('两次密码不一致') : cb(), trigger: 'blur' }
  ],
}

function loadUser() {
  loading.value = true
  fetchMe().then(res => {
    user.value = res.data.data.user || {}
  }).catch(() => {
    ElMessage.error('加载用户信息失败')
  }).finally(() => { loading.value = false })
}

function handleChangePwd() {
  formRef.value?.validate().then(() => {
    saving.value = true
    changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword }).then(() => {
      ElMessage.success('密码修改成功，请重新登录')
      localStorage.clear()
      location.reload()
    }).catch(() => {
      ElMessage.error('密码修改失败，请检查原密码是否正确')
    }).finally(() => { saving.value = false })
  }).catch(() => {})
}

onMounted(loadUser)
</script>

<template>
  <div>
    <div class="page-header"><h2>个人信息</h2></div>
    <div class="profile-grid">
      <div class="card-table" v-loading="loading">
        <h3 class="section-title">基本信息</h3>
        <div class="info-grid">
          <div class="info-item"><span class="info-label">用户名</span><span>{{ user.username }}</span></div>
          <div class="info-item"><span class="info-label">姓名</span><span>{{ user.realName }}</span></div>
          <div class="info-item"><span class="info-label">工号</span><span>{{ user.employeeNo }}</span></div>
          <div class="info-item"><span class="info-label">性别</span><span>{{ user.gender===1?'男':'女' }}</span></div>
          <div class="info-item"><span class="info-label">手机</span><span>{{ user.phone }}</span></div>
          <div class="info-item"><span class="info-label">邮箱</span><span>{{ user.email }}</span></div>
          <div class="info-item"><span class="info-label">岗位</span><span>{{ user.position }}</span></div>
        </div>
      </div>
      <div class="card-table">
        <h3 class="section-title">修改密码</h3>
        <el-form ref="formRef" :model="pwdForm" :rules="pwdRules" label-width="80px" style="max-width:320px">
          <el-form-item label="原密码" prop="oldPassword"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
          <el-form-item label="新密码" prop="newPassword"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
          <el-form-item label="确认密码" prop="confirm"><el-input v-model="pwdForm.confirm" type="password" show-password /></el-form-item>
          <el-button type="primary" @click="handleChangePwd" :loading="saving">修改密码</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-header h2 { font-size:20px;font-weight:600;margin:0 0 20px }
.profile-grid { display:grid;grid-template-columns:1fr 1fr;gap:16px }
.card-table { background:#fff;border-radius:10px;padding:24px;box-shadow:0 1px 2px rgba(0,0,0,0.04) }
.section-title { font-size:15px;font-weight:600;margin-bottom:16px;color:#1f2937 }
.info-grid { display:grid;grid-template-columns:1fr 1fr;gap:12px }
.info-item { display:flex;flex-direction:column }
.info-label { font-size:12px;color:#9ca3af;margin-bottom:2px }
@media (max-width:768px) { .profile-grid { grid-template-columns:1fr } }
</style>

<!--
  个人信息 — 所有角色可见
  显示当前登录用户的基本信息（只读）+ 修改密码表单
  密码修改需验证原密码正确性，新密码 BCrypt 加密存储，修改后强制重新登录
-->