<template>
  <el-dialog
    v-model="visible"
    title="忘记密码"
    width="450px"
    :close-on-click-modal="false"
    :show-close="!loading"
    @close="handleClose"
  >
    <!-- Step 1: Input email -->
    <div v-if="currentStep === 1" class="reset-step">
      <p class="step-description">请输入您的注册邮箱，我们将发送验证码到该邮箱。</p>
      <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" @submit.prevent="sendCode">
        <el-form-item prop="email">
          <el-input
            v-model="emailForm.email"
            placeholder="请输入邮箱地址"
            prefix-icon="Message"
            :disabled="loading"
          />
        </el-form-item>
        <el-button
          type="primary"
          class="full-width-btn"
          :loading="loading"
          @click="sendCode"
        >
          发送验证码
        </el-button>
      </el-form>
    </div>

    <!-- Step 2: Verify code -->
    <div v-else-if="currentStep === 2" class="reset-step">
      <p class="step-description">
        验证码已发送至 <strong>{{ emailForm.email }}</strong>
      </p>
      <el-form :model="codeForm" :rules="codeRules" ref="codeFormRef" @submit.prevent="verifyCode">
        <el-form-item prop="code">
          <el-input
            v-model="codeForm.code"
            placeholder="请输入6位验证码"
            maxlength="6"
            prefix-icon="Message"
            :disabled="loading"
          />
        </el-form-item>
        <div class="code-actions">
          <el-button
            type="primary"
            class="full-width-btn"
            :loading="loading"
            @click="verifyCode"
          >
            验证
          </el-button>
          <div class="resend-container">
            <span v-if="cooldown > 0">{{ cooldown }}秒后可重新发送</span>
            <el-button
              v-else
              type="text"
              @click="resendCode"
              :disabled="loading"
            >
              重新发送
            </el-button>
          </div>
        </div>
      </el-form>
    </div>

    <!-- Step 3: Reset password -->
    <div v-else-if="currentStep === 3" class="reset-step">
      <p class="step-description">请输入新密码完成重置。</p>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" @submit.prevent="resetPassword">
        <el-form-item prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（8-100位）"
            prefix-icon="Lock"
            :disabled="loading"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            prefix-icon="Lock"
            :disabled="loading"
            show-password
          />
        </el-form-item>
        <el-button
          type="primary"
          class="full-width-btn"
          :loading="loading"
          @click="resetPassword"
        >
          重置密码
        </el-button>
      </el-form>
    </div>

    <!-- Step 4: Success -->
    <div v-else-if="currentStep === 4" class="reset-step success-step">
      <div class="success-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
          <polyline points="22 4 12 14.01 9 11.01"/>
        </svg>
      </div>
      <h3>密码重置成功！</h3>
      <p>您的密码已成功重置，请使用新密码登录。</p>
      <el-button type="primary" class="full-width-btn" @click="handleClose">
        返回登录
      </el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/userStore'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const userStore = useUserStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const currentStep = ref(1)
const cooldown = ref(0)
let cooldownTimer = null

const emailFormRef = ref(null)
const codeFormRef = ref(null)
const passwordFormRef = ref(null)

const emailForm = reactive({
  email: ''
})

const codeForm = reactive({
  code: ''
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const codeRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须为6位', trigger: 'blur' }
  ]
}

const passwordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 100, message: '密码长度必须在8-100位之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const sendCode = async () => {
  if (!emailFormRef.value) return

  try {
    const valid = await emailFormRef.value.validate()
    if (!valid) return

    loading.value = true
    await userStore.sendVerificationCode(emailForm.email)
    ElMessage.success('验证码已发送')
    currentStep.value = 2
    startCooldown()
  } catch (error) {
    const message = error.response?.data?.message || error.message || '发送验证码失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const verifyCode = async () => {
  if (!codeFormRef.value) return

  try {
    const valid = await codeFormRef.value.validate()
    if (!valid) return

    loading.value = true
    await userStore.verifyCode(emailForm.email, codeForm.code)
    ElMessage.success('验证码验证成功')
    currentStep.value = 3
  } catch (error) {
    const message = error.response?.data?.message || error.message || '验证码验证失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const resetPassword = async () => {
  if (!passwordFormRef.value) return

  try {
    const valid = await passwordFormRef.value.validate()
    if (!valid) return

    loading.value = true
    await userStore.resetPassword(emailForm.email, codeForm.code, passwordForm.newPassword)
    ElMessage.success('密码重置成功')
    currentStep.value = 4
    emit('success')
  } catch (error) {
    const message = error.response?.data?.message || error.message || '密码重置失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const resendCode = async () => {
  if (cooldown.value > 0) return

  try {
    loading.value = true
    await userStore.sendVerificationCode(emailForm.email)
    ElMessage.success('验证码已重新发送')
    startCooldown()
  } catch (error) {
    const message = error.response?.data?.message || error.message || '发送验证码失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const startCooldown = () => {
  cooldown.value = 60
  cooldownTimer = setInterval(() => {
    cooldown.value--
    if (cooldown.value <= 0) {
      clearInterval(cooldownTimer)
      cooldownTimer = null
    }
  }, 1000)
}

const handleClose = () => {
  // Reset state
  currentStep.value = 1
  cooldown.value = 0
  if (cooldownTimer) {
    clearInterval(cooldownTimer)
    cooldownTimer = null
  }
  emailForm.email = ''
  codeForm.code = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  loading.value = false

  visible.value = false
}

onMounted(() => {
  // Cleanup timer on unmount
  return () => {
    if (cooldownTimer) {
      clearInterval(cooldownTimer)
    }
  }
})
</script>

<style scoped>
.reset-step {
  padding: 20px 0;
}

.step-description {
  margin-bottom: 20px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.full-width-btn {
  width: 100%;
  margin-top: 10px;
}

.code-actions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.resend-container {
  font-size: 12px;
  color: #909399;
}

.success-step {
  text-align: center;
  padding: 30px 0;
}

.success-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-icon svg {
  width: 32px;
  height: 32px;
  stroke: white;
}

.success-step h3 {
  margin: 0 0 8px;
  color: #303133;
}

.success-step p {
  margin: 0 0 20px;
  color: #606266;
}
</style>
