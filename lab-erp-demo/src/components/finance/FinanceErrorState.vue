<template>
  <section class="finance-error-state">
    <div>
      <p class="error-eyebrow">Finance Error</p>
      <h2>{{ title }}</h2>
      <p class="error-message">{{ message }}</p>
    </div>
    <button v-if="actionLabel" class="error-action" type="button" @click="$emit('action')">{{ actionLabel }}</button>
  </section>
</template>

<script setup>
defineEmits(['action'])

defineProps({
  title: {
    type: String,
    default: 'Something went wrong'
  },
  message: {
    type: String,
    default: 'Please retry the request.'
  },
  actionLabel: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.finance-error-state {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 24px;
  border: 1px solid rgba(248, 113, 113, 0.3);
  border-radius: 24px;
  background: linear-gradient(160deg, rgba(254, 242, 242, 0.96), rgba(254, 226, 226, 0.92));
}

.error-eyebrow,
.error-message,
h2 {
  margin: 0;
}

.error-eyebrow {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #b91c1c;
}

h2 {
  color: #7f1d1d;
}

.error-message {
  color: #991b1b;
  line-height: 1.5;
}

.error-action {
  align-self: flex-start;
  border: 0;
  border-radius: 999px;
  padding: 10px 16px;
  background: #b91c1c;
  color: #fff;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

@media (max-width: 760px) {
  .finance-error-state {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
