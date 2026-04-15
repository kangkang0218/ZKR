<template>
  <div class="form-shell">
    <h1>{{ title }}</h1>
    <p class="subtitle">{{ subtitle }}</p>

    <template v-if="mode === 'erp'">
      <label class="field-label" for="register-role">{{ roleLabel }}</label>
      <el-select
        id="register-role"
        :model-value="role"
        :placeholder="rolePlaceholder"
        class="pill-input"
        @update:model-value="emit('update:role', $event)"
      >
        <el-option
          v-for="option in roleOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
    </template>

    <label class="field-label" for="register-name">{{ nameLabel }}</label>
    <el-input
      id="register-name"
      :model-value="name"
      :placeholder="namePlaceholder"
      class="pill-input"
      @update:model-value="emit('update:name', $event)"
      @focus="emit('focus-input')"
      @blur="emit('blur-input')"
    />

    <label class="field-label" for="register-username">{{ usernameLabel }}</label>
    <el-input
      id="register-username"
      :model-value="username"
      :placeholder="usernamePlaceholder"
      class="pill-input"
      @update:model-value="emit('update:username', $event)"
      @focus="emit('focus-input')"
      @blur="emit('blur-input')"
    />

    <label class="field-label" for="register-email">{{ emailLabel }}</label>
    <el-input
      id="register-email"
      :model-value="email"
      type="email"
      :placeholder="emailPlaceholder"
      class="pill-input"
      @update:model-value="emit('update:email', $event)"
      @focus="emit('focus-input')"
      @blur="emit('blur-input')"
    />

    <label class="field-label" for="register-password">{{ passwordLabel }}</label>
    <el-input
      id="register-password"
      :model-value="password"
      :type="passwordVisible ? 'text' : 'password'"
      :placeholder="passwordPlaceholder"
      class="pill-input"
      @update:model-value="emit('update:password', $event)"
      @focus="emit('focus-input')"
      @blur="emit('blur-input')"
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

    <label class="field-label" for="register-confirm-password">确认密码</label>
    <el-input
      id="register-confirm-password"
      :model-value="confirmPassword"
      :type="passwordVisible ? 'text' : 'password'"
      placeholder="请再次输入密码"
      class="pill-input"
      @update:model-value="emit('update:confirmPassword', $event)"
      @focus="emit('focus-input')"
      @blur="emit('blur-input')"
    />

    <p v-if="submitError" class="login-error">{{ submitError }}</p>

    <button class="pill-btn" :disabled="loading" @click="emit('submit')">
      <span v-if="loading">{{ loadingText }}</span>
      <span v-else>{{ submitText }}</span>
    </button>

    <a class="register-link" href="#" @click.prevent="emit('go-login')">{{ switchText }}</a>
  </div>
</template>

<script setup>
defineProps({
  mode: {
    type: String,
    default: 'finance'
  },
  title: {
    type: String,
    default: '财务系统注册'
  },
  subtitle: {
    type: String,
    default: '创建财务系统账号'
  },
  role: {
    type: String,
    default: ''
  },
  roleLabel: {
    type: String,
    default: '财务岗位'
  },
  rolePlaceholder: {
    type: String,
    default: '请选择财务岗位'
  },
  username: {
    type: String,
    default: ''
  },
  usernameLabel: {
    type: String,
    default: '账号'
  },
  usernamePlaceholder: {
    type: String,
    default: '请输入账号'
  },
  name: {
    type: String,
    default: ''
  },
  nameLabel: {
    type: String,
    default: '真实姓名'
  },
  namePlaceholder: {
    type: String,
    default: '请输入真实姓名'
  },
  email: {
    type: String,
    default: ''
  },
  emailLabel: {
    type: String,
    default: '邮箱'
  },
  emailPlaceholder: {
    type: String,
    default: '请输入邮箱'
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
  confirmPassword: {
    type: String,
    default: ''
  },
  passwordVisible: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: '注册中...'
  },
  submitText: {
    type: String,
    default: '注册财务账号'
  },
  switchText: {
    type: String,
    default: '已有账号？去登录'
  },
  submitError: {
    type: String,
    default: ''
  },
  roleOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits([
  'update:role',
  'update:username',
  'update:name',
  'update:email',
  'update:password',
  'update:confirmPassword',
  'focus-input',
  'blur-input',
  'toggle-password',
  'submit',
  'go-login'
])
</script>
