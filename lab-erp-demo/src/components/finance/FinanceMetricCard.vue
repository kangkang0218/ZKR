<template>
  <article class="finance-metric-card" :class="`finance-metric-card--${tone}`">
    <p class="metric-title">{{ title }}</p>
    <strong class="metric-value">{{ value }}</strong>
    <p v-if="subtitle" class="metric-subtitle">{{ subtitle }}</p>
    <p v-if="caption" class="metric-caption">{{ caption }}</p>
  </article>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  value: {
    type: String,
    default: '--'
  },
  subtitle: {
    type: String,
    default: ''
  },
  caption: {
    type: String,
    default: ''
  },
  tone: {
    type: String,
    default: 'neutral'
  }
})
</script>

<style scoped>
.finance-metric-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 176px;
  padding: 20px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.9), rgba(248, 250, 252, 0.96));
  box-shadow: 0 18px 36px rgba(15, 23, 42, 0.08);
}

.finance-metric-card--success {
  background-image: linear-gradient(160deg, rgba(240, 253, 244, 0.96), rgba(220, 252, 231, 0.9));
}

.finance-metric-card--danger {
  background-image: linear-gradient(160deg, rgba(254, 242, 242, 0.96), rgba(254, 226, 226, 0.9));
}

.finance-metric-card--warning {
  background-image: linear-gradient(160deg, rgba(255, 251, 235, 0.96), rgba(254, 243, 199, 0.9));
}

.finance-metric-card--info {
  background-image: linear-gradient(160deg, rgba(239, 246, 255, 0.96), rgba(224, 242, 254, 0.9));
}

.metric-title,
.metric-subtitle,
.metric-caption {
  margin: 0;
}

.metric-title {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

.metric-value {
  font-size: clamp(24px, 2.4vw, 34px);
  color: #0f172a;
  line-height: 1.1;
}

.metric-subtitle {
  color: #1e293b;
  font-weight: 600;
}

.metric-caption {
  color: #475569;
  line-height: 1.5;
}
</style>
