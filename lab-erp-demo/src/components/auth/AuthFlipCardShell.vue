<template>
  <div class="flip-wrap" :class="{ flipped }">
    <div class="flip-face face-login">
      <slot name="login" />
    </div>

    <div class="flip-face face-register">
      <slot name="register" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  flipped: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.flip-wrap {
  position: relative;
  width: 100%;
  max-width: 460px;
  min-height: 720px;
  display: flex;
  align-items: center;
  justify-content: center;
  perspective: 1600px;
  transform-style: preserve-3d;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.flip-wrap.flipped {
  transform: rotateY(180deg);
}

.flip-face {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  backface-visibility: hidden;
  -webkit-backface-visibility: hidden;
}

.face-register {
  transform: rotateY(180deg);
}

.flip-face :deep(.form-shell) {
  width: 100%;
  min-height: 560px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 40px 32px;
  border-radius: 28px;
  border: 1px solid var(--auth-card-border, rgba(17, 24, 39, 0.08));
  background: var(--auth-card-bg, rgba(255, 255, 255, 0.94));
  box-shadow: var(--auth-card-shadow, 0 28px 80px rgba(15, 23, 42, 0.14));
  backdrop-filter: blur(12px);
}

.flip-face :deep(h1) {
  margin: 0;
  font-size: 36px;
  font-weight: 700;
  text-align: center;
  letter-spacing: -0.5px;
}

.flip-face :deep(.subtitle) {
  text-align: center;
  color: var(--auth-muted, #6b7280);
  font-size: 14px;
  margin-bottom: 8px;
}

.flip-face :deep(.field-label) {
  font-size: 14px;
  font-weight: 500;
  color: var(--ink);
}

.flip-face :deep(.pill-input .el-input__wrapper) {
  min-height: 48px;
  border-radius: 12px;
  background: var(--auth-input-bg, var(--page-bg)) !important;
  border: 1px solid var(--auth-input-border, #e5e7eb);
  box-shadow: none !important;
  padding: 0 14px;
}

.flip-face :deep(.pill-input .el-input__wrapper.is-focus) {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(108, 63, 245, 0.1) !important;
}

.flip-face :deep(.pill-input .el-input__inner) {
  color: var(--ink);
  font-size: 15px;
}

.flip-face :deep(.pill-input .el-input__inner::placeholder) {
  color: var(--auth-placeholder, #9ca3af);
}

.flip-face :deep(.pill-input.el-select) {
  width: 100%;
}

.flip-face :deep(.toggle-eye) {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: transparent;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
  color: var(--auth-muted, #6b7280);
}

.flip-face :deep(.toggle-eye:hover) {
  color: var(--ink);
}

.flip-face :deep(.toggle-eye svg) {
  width: 20px;
  height: 20px;
}

.flip-face :deep(.options-row) {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.flip-face :deep(.entry-hint) {
  margin: -2px 0 0;
  color: var(--auth-muted, #6b7280);
  font-size: 13px;
  line-height: 1.5;
}

.flip-face :deep(.login-error) {
  margin: -2px 0 0;
  color: #dc2626;
  font-size: 13px;
  line-height: 1.5;
}

.flip-face :deep(.remember-me) {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--auth-muted, #6b7280);
  cursor: pointer;
}

.flip-face :deep(.remember-me input) {
  width: 16px;
  height: 16px;
  accent-color: var(--primary);
}

.flip-face :deep(.forgot-link) {
  font-size: 14px;
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.flip-face :deep(.forgot-link:hover) {
  text-decoration: underline;
}

.flip-face :deep(.pill-btn) {
  min-height: 48px;
  border-radius: 12px;
  border: none;
  background: var(--primary);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 8px;
}

.flip-face :deep(.pill-btn:hover:not(:disabled)) {
  background: #5a2fd4;
}

.flip-face :deep(.pill-btn:disabled) {
  opacity: 0.65;
  cursor: not-allowed;
}

.flip-face :deep(.register-link) {
  text-align: center;
  color: var(--auth-muted, #6b7280);
  font-size: 14px;
  text-decoration: none;
}

.flip-face :deep(.register-link:hover) {
  color: var(--ink);
  text-decoration: underline;
}

:global(.theme-dark) .flip-face :deep(.form-shell) {
  --auth-card-bg: linear-gradient(180deg, rgba(20, 27, 45, 0.96) 0%, rgba(11, 17, 31, 0.94) 100%);
  --auth-card-border: rgba(148, 163, 184, 0.2);
  --auth-card-shadow: 0 28px 80px rgba(0, 0, 0, 0.48);
  --auth-input-bg: rgba(15, 23, 42, 0.78);
  --auth-input-border: rgba(148, 163, 184, 0.18);
  --auth-muted: #94a3b8;
  --auth-placeholder: #64748b;
}

@media (max-width: 1024px) {
  .flip-wrap {
    min-height: 640px;
  }

  .flip-face :deep(.form-shell) {
    padding: 32px 24px;
  }
}

@media (max-width: 640px) {
  .flip-wrap {
    min-height: 600px;
  }

  .flip-face :deep(h1) {
    font-size: 30px;
  }
}
</style>
