<template>
  <section class="procurement-root">
    <div class="hero-card">
      <p class="eyebrow">ERP Personal Procurement</p>
      <h1>个人采购申请</h1>
      <p class="hero-copy">提交采购物品、发票与用途说明，财务系统会自动汇总这笔申请。</p>
      <div class="meta-row">
        <span>申请人：{{ currentUser?.name || currentUser?.username || '未登录用户' }}</span>
        <span>账号：{{ currentUser?.username || '-' }}</span>
      </div>
    </div>

    <div class="form-card">
      <ExpenseSubmissionForm submission-type="PERSONAL_PROCUREMENT" @submitted="handleSubmitted" />
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/userStore'
import ExpenseSubmissionForm from '@/components/finance/ExpenseSubmissionForm.vue'

const userStore = useUserStore()
const currentUser = computed(() => userStore.activeUserInfo || {})

const handleSubmitted = () => {
  window.dispatchEvent(new Event('finance-global-refresh'))
}
</script>

<style scoped>
.procurement-root {
  --procurement-card-bg: linear-gradient(150deg, rgba(255, 255, 255, 0.9), rgba(240, 249, 255, 0.82));
  --procurement-card-border: rgba(148, 163, 184, 0.18);
  --procurement-card-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
  --procurement-eyebrow: #0f766e;
  max-width: 1080px;
  margin: 0 auto;
  padding: 32px 20px 40px;
  display: grid;
  gap: 20px;
}

.hero-card,
.form-card {
  border-radius: 26px;
  padding: 24px;
  background: var(--procurement-card-bg);
  border: 1px solid var(--procurement-card-border);
  box-shadow: var(--procurement-card-shadow);
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 11px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: var(--procurement-eyebrow);
}

.hero-card h1 {
  margin: 0;
  color: var(--text-main);
  font-size: 34px;
}

.hero-copy {
  margin: 12px 0 0;
  color: var(--text-sub);
  max-width: 680px;
}

.meta-row {
  margin-top: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--text-sub);
  font-size: 13px;
}

:global(.dark) .procurement-root {
  --procurement-card-bg: linear-gradient(150deg, rgba(15, 23, 42, 0.96), rgba(30, 41, 59, 0.9));
  --procurement-card-border: rgba(148, 163, 184, 0.24);
  --procurement-card-shadow: 0 22px 42px rgba(2, 6, 23, 0.42);
  --procurement-eyebrow: #5eead4;
}
</style>
