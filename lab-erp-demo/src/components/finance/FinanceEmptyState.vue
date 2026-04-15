<template>
  <div class="finance-empty-state" :class="{ 'finance-empty-state--compact': compact }">
    <strong>{{ title }}</strong>
    <p>{{ description }}</p>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: 'No data'
  },
  description: {
    type: String,
    default: 'There is nothing to show yet.'
  },
  compact: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.finance-empty-state {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-start;
  justify-content: center;
  padding: 28px;
  border: 1px dashed rgba(148, 163, 184, 0.42);
  border-radius: 24px;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.96), rgba(241, 245, 249, 0.9));
}

.finance-empty-state--compact {
  padding: 20px;
}

strong,
p {
  margin: 0;
}

strong {
  color: #0f172a;
}

p {
  color: #475569;
  line-height: 1.5;
}
</style>
