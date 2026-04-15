<template>
  <div class="form-shell">
    <h1>{{ title }}</h1>
    <p class="subtitle">{{ subtitle }}</p>

    <label class="field-label" for="account">{{ accountLabel }}</label>
    <el-input
      id="account"
      :model-value="account"
      :placeholder="accountPlaceholder"
      class="pill-input"
      @update:model-value="emit('update:account', $event)"
      @keyup.enter="emit('submit')"
      @focus="emit('focus-input')"
      @blur="emit('blur-input')"
    />

    <label class="field-label" for="password">{{ passwordLabel }}</label>
    <el-input
      id="password"
      :model-value="password"
      :type="passwordVisible ? 'text' : 'password'"
      :placeholder="passwordPlaceholder"
      class="pill-input"
      @update:model-value="emit('update:password', $event)"
      @focus="emit('focus-password')"
      @blur="emit('blur-password')"
      @keyup.enter="emit('submit')"
    >
      <template #suffix>
        <button
          type="button"
          class="toggle-eye"
          :aria-label="passwordVisible ? '隐藏密码' : '显示密码'"
          @click.prevent="emit('toggle-password')"
        >
          <svg v-if="passwordVisible" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
            <line x1="1" y1="1" x2="23" y2="23"/>
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
            <circle cx="12" cy="12" r="3"/>
          </svg>
        </button>
      </template>
    </el-input>

    <div class="options-row">
      <label v-if="showRememberMe" class="remember-me">
        <input
          type="checkbox"
          :checked="rememberMe"
          @change="emit('update:rememberMe', $event.target.checked)"
        />
        <span>记住30天</span>
      </label>
      <a v-if="showForgotPassword" href="#" class="forgot-link" @click.prevent="emit('go-forgot-password')">{{ forgotText }}</a>
    </div>

    <p class="entry-hint">{{ hintText }}</p>

    <p v-if="loginError" class="login-error">{{ loginError }}</p>

    <button class="pill-btn" :disabled="loading" @click="emit('submit')">
      <span v-if="loading">{{ loadingText }}</span>
      <span v-else>{{ submitText }}</span>
    </button>

    <a v-if="showSwitchAction" class="register-link" href="#" @click.prevent="emit('go-register')">{{ switchText }}</a>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '财务系统登录'
  },
  subtitle: {
    type: String,
    default: '请输入财务账号信息'
  },
  account: {
    type: String,
    default: ''
  },
  accountLabel: {
    type: String,
    default: '账号'
  },
  accountPlaceholder: {
    type: String,
    default: '请输入账号'
  },
  password: {
    type: String,
    default: ''
  },
  passwordLabel: {
    type: String,
    default: '密码'
  },
  passwordPlaceholder: {
    type: String,
    default: '请输入密码'
  },
  passwordVisible: {
    type: Boolean,
    default: false
  },
  rememberMe: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: '登录中...'
  },
  submitText: {
    type: String,
    default: '登录'
  },
  switchText: {
    type: String,
    default: '没有账号？去注册'
  },
  hintText: {
    type: String,
    default: '登录后将直接进入财务总览。'
  },
  forgotText: {
    type: String,
    default: '忘记密码？'
  },
  showRememberMe: {
    type: Boolean,
    default: true
  },
  showForgotPassword: {
    type: Boolean,
    default: true
  },
  showSwitchAction: {
    type: Boolean,
    default: true
  },
  loginError: {
    type: String,
    default: ''
  }
})

const emit = defineEmits([
  'update:account',
  'update:password',
  'update:rememberMe',
  'focus-input',
  'blur-input',
  'focus-password',
  'blur-password',
  'toggle-password',
  'submit',
  'go-register',
  'go-forgot-password'
])

</script>
