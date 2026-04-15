<template>
  <span class="finance-status-tag" :class="`finance-status-tag--${tone}`">{{ label }}</span>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    default: '--'
  },
  tone: {
    type: String,
    default: 'neutral'
  }
})
</script>

<style scoped>
.finance-status-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  white-space: nowrap;
}

.finance-status-tag--neutral {
  color: #475569;
  background: rgba(226, 232, 240, 0.72);
  border-color: rgba(148, 163, 184, 0.22);
}

.finance-status-tag--success {
  color: #047857;
  background: rgba(220, 252, 231, 0.88);
  border-color: rgba(52, 211, 153, 0.35);
}

.finance-status-tag--danger {
  color: #b91c1c;
  background: rgba(254, 226, 226, 0.92);
  border-color: rgba(248, 113, 113, 0.34);
}

.finance-status-tag--warning {
  color: #b45309;
  background: rgba(254, 243, 199, 0.92);
  border-color: rgba(251, 191, 36, 0.34);
}

.finance-status-tag--info {
  color: #0369a1;
  background: rgba(224, 242, 254, 0.92);
  border-color: rgba(56, 189, 248, 0.3);
}
</style>
