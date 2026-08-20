<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUser } from '../../stores/user'

const router = useRouter()
const { login } = useUser()

const formRef = ref(null)
const form = reactive({ username: '', password: '' })
const loading = ref(false)

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function doLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await login(form.username, form.password)
    ElMessage.success('登录成功')
    const roles = JSON.parse(localStorage.getItem('user') || '{}').roles || []
    if (roles.includes('admin'))  router.push('/admin/dashboard')
    else if (roles.includes('hr'))     router.push('/hr/dashboard')
    else if (roles.includes('leader')) router.push('/leader/dashboard')
    else router.push('/my/exams')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="card-left">
        <div class="brand-mark">健</div>
        <h1>企业健康管理平台</h1>
        <p class="brand-sub">Enterprise Health Management</p>

        <div class="feature-list">
          <div class="feature-item">
            <span class="feature-dot" />
            <span>员工健康数据统一管理</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot" />
            <span>智能体检排期与风险预警</span>
          </div>
          <div class="feature-item">
            <span class="feature-dot" />
            <span>多角色权限精细管控</span>
          </div>
        </div>
      </div>

      <div class="card-right">
        <div class="form-header">
          <h2>登录</h2>
          <p>请输入您的账号信息</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="doLogin" class="login-form">
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="用户名"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              show-password
              size="large"
              @keyup.enter="doLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="doLogin"
              class="login-btn"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <p class="login-footer">Enterprise Health &copy; 2026</p>
  </div>
</template>

<script>
export default { name: 'Login' }
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--canvas);
  font-family: var(--font);
}

.login-card {
  display: flex;
  width: 860px;
  min-height: 480px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

/* ── Left panel ── */
.card-left {
  flex: 1;
  background: linear-gradient(135deg, #FAFBFC 0%, #F1F3F5 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 40px;
  border-right: 1px solid var(--border-light);
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: var(--radius);
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 20px;
}

.card-left h1 {
  color: var(--text-primary);
  font-size: var(--text-xl);
  font-weight: 600;
  margin: 0 0 4px;
  letter-spacing: 0.5px;
}

.brand-sub {
  color: var(--text-tertiary);
  font-size: var(--text-xs);
  font-weight: 400;
  letter-spacing: 2px;
  text-transform: uppercase;
  margin: 0 0 36px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.feature-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
  opacity: 0.6;
  flex-shrink: 0;
}

/* ── Right panel ── */
.card-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 48px 40px;
}

.form-header { margin-bottom: var(--space-6); }

.form-header h2 {
  color: var(--text-primary);
  font-size: var(--text-xl);
  font-weight: 600;
  margin: 0 0 4px;
}

.form-header p {
  color: var(--text-tertiary);
  font-size: var(--text-sm);
  margin: 0;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-sm);
  font-size: var(--text-base);
  font-weight: 500;
  letter-spacing: 4px;
  transition: all 0.2s;
}

/* ── Footer ── */
.login-footer {
  margin-top: var(--space-6);
  font-size: var(--text-xs);
  color: var(--text-tertiary);
}

/* ── Responsive ── */
@media (max-width: 920px) {
  .login-card { flex-direction: column; width: 400px; }
  .card-left { padding: 32px 28px; border-right: none; border-bottom: 1px solid var(--border-light); }
  .feature-list { display: none; }
  .brand-sub { margin-bottom: 0; }
  .card-right { padding: 32px 28px; }
}
</style>
