<template>
  <div class="password-page">
    <div class="password-card">
      <div class="eyebrow">SECURITY REQUIRED</div>
      <h1>首次登录请修改密码</h1>
      <p class="subtitle">为了保护账号安全，请先更新临时密码，再继续进入系统。</p>

      <label class="field-label" for="current-password">当前密码</label>
      <el-input
        id="current-password"
        v-model="form.currentPassword"
        :type="showCurrentPassword ? 'text' : 'password'"
        placeholder="请输入当前临时密码"
      >
        <template #suffix>
          <button class="icon-btn" type="button" @click="showCurrentPassword = !showCurrentPassword">
            {{ showCurrentPassword ? '隐藏' : '显示' }}
          </button>
        </template>
      </el-input>

      <label class="field-label" for="new-password">新密码</label>
      <el-input
        id="new-password"
        v-model="form.newPassword"
        :type="showNewPassword ? 'text' : 'password'"
        placeholder="请输入至少 8 位的新密码"
      >
        <template #suffix>
          <button class="icon-btn" type="button" @click="showNewPassword = !showNewPassword">
            {{ showNewPassword ? '隐藏' : '显示' }}
          </button>
        </template>
      </el-input>

      <label class="field-label" for="confirm-password">确认新密码</label>
      <el-input
        id="confirm-password"
        v-model="confirmPassword"
        :type="showConfirmPassword ? 'text' : 'password'"
        placeholder="请再次输入新密码"
      >
        <template #suffix>
          <button class="icon-btn" type="button" @click="showConfirmPassword = !showConfirmPassword">
            {{ showConfirmPassword ? '隐藏' : '显示' }}
          </button>
        </template>
      </el-input>

      <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>

      <div class="action-row">
        <el-button text @click="handleLogout">退出登录</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认修改</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { getErpLandingRoute } from '@/router/domainAccess'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  currentPassword: '',
  newPassword: ''
})

const confirmPassword = ref('')
const showCurrentPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)
const submitting = ref(false)
const errorMessage = ref('')

const handleLogout = () => {
  if (userStore.isErpLoggedIn) {
    userStore.logoutErp()
    router.push('/erp-login')
    return
  }
  userStore.logout()
  router.push('/login')
}

const handleSubmit = async () => {
  if (!form.currentPassword) {
    errorMessage.value = '请输入当前密码'
    return
  }
  if (!form.newPassword || form.newPassword.length < 8) {
    errorMessage.value = '新密码长度不能少于 8 位'
    return
  }
  if (form.newPassword !== confirmPassword.value) {
    errorMessage.value = '两次输入的新密码不一致'
    return
  }

  errorMessage.value = ''
  submitting.value = true

  try {
    await userStore.changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword
    })
    ElMessage.success('密码修改成功，请使用新密码继续工作')
    router.push(userStore.isErpLoggedIn ? getErpLandingRoute(userStore.activeUserInfo?.role) : '/finance/overview')
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || '密码修改失败'
    ElMessage.error(errorMessage.value)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.password-page {
  min-height: calc(100vh - var(--nav-height));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  background: radial-gradient(circle at top, rgba(37, 99, 235, 0.12), transparent 40%), var(--science-canvas);
}

.password-card {
  width: min(100%, 520px);
  padding: 32px;
  border-radius: 24px;
  border: 1px solid var(--border-soft);
  background: var(--science-surface);
  box-shadow: var(--shadow-md);
  display: grid;
  gap: 14px;
}

.eyebrow {
  font-size: 12px;
  letter-spacing: 0.2em;
  color: var(--science-blue);
  font-weight: 700;
}

h1 {
  margin: 0;
  color: var(--text-main);
}

.subtitle {
  margin: 0 0 4px;
  color: var(--text-sub);
  line-height: 1.6;
}

.field-label {
  font-size: 14px;
  color: var(--text-main);
  font-weight: 600;
}

.icon-btn {
  border: none;
  background: transparent;
  color: var(--science-blue);
  cursor: pointer;
}

.error-text {
  margin: 0;
  color: #dc2626;
}

.action-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
</style>
