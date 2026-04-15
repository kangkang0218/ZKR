<template>
  <teleport to="body">
    <div v-if="open" class="finance-confirm-dialog" @click.self="handleCancel">
      <section class="dialog-card" role="dialog" aria-modal="true" :aria-labelledby="titleId">
        <header class="dialog-header">
          <div>
            <p class="dialog-eyebrow">Finance Confirm</p>
            <h2 :id="titleId">{{ title }}</h2>
            <p class="dialog-message">{{ message }}</p>
          </div>
          <button class="dialog-close" type="button" :disabled="loading" aria-label="Close dialog" @click="handleCancel">x</button>
        </header>

        <div v-if="items.length" class="dialog-summary">
          <div v-for="item in items" :key="item.label" class="summary-row">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>

        <div v-if="doubleConfirm && armedConfirm" class="dialog-warning" role="alert">
          <strong>{{ doubleConfirmTitle }}</strong>
          <p>{{ doubleConfirmMessage }}</p>
        </div>

        <div v-if="errorMessage" class="dialog-error" role="alert">
          <strong>Backend message</strong>
          <p>{{ errorMessage }}</p>
        </div>

        <footer class="dialog-actions">
          <button class="secondary-button" type="button" :disabled="loading" @click="handleCancel">{{ cancelLabel }}</button>
          <button class="primary-button" type="button" :disabled="loading" @click="handleConfirm">
            {{ loading ? loadingLabel : activeConfirmLabel }}
          </button>
        </footer>
      </section>
    </div>
  </teleport>
</template>

<script setup>
import { computed, ref, watch } from 'vue'

const emit = defineEmits(['update:open', 'confirm', 'cancel'])

const props = defineProps({
  open: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'Confirm finance action'
  },
  message: {
    type: String,
    default: 'Please review the details before continuing.'
  },
  items: {
    type: Array,
    default: () => []
  },
  errorMessage: {
    type: String,
    default: ''
  },
  loading: {
    type: Boolean,
    default: false
  },
  confirmLabel: {
    type: String,
    default: 'Confirm'
  },
  cancelLabel: {
    type: String,
    default: 'Cancel'
  },
  loadingLabel: {
    type: String,
    default: 'Submitting...'
  },
  doubleConfirm: {
    type: Boolean,
    default: false
  },
  doubleConfirmTitle: {
    type: String,
    default: 'Second confirmation required'
  },
  doubleConfirmMessage: {
    type: String,
    default: 'Click confirm again to continue with this finance action.'
  },
  doubleConfirmLabel: {
    type: String,
    default: 'Confirm Again'
  }
})

const titleId = computed(() => `finance-confirm-dialog-${props.title.replace(/\s+/g, '-').toLowerCase()}`)
const armedConfirm = ref(false)
const activeConfirmLabel = computed(() => {
  if (props.doubleConfirm && armedConfirm.value) {
    return props.doubleConfirmLabel
  }

  return props.confirmLabel
})

watch(
  () => props.open,
  open => {
    if (!open) {
      armedConfirm.value = false
    }
  }
)

const handleConfirm = () => {
  if (props.doubleConfirm && !armedConfirm.value) {
    armedConfirm.value = true
    return
  }

  emit('confirm')
}

const handleCancel = () => {
  armedConfirm.value = false
  emit('update:open', false)
  emit('cancel')
}
</script>

<style scoped>
.finance-confirm-dialog {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.58);
  backdrop-filter: blur(6px);
}

.dialog-card,
.primary-button,
.secondary-button,
.dialog-warning,
.dialog-error {
  border-radius: 24px;
}

.dialog-card {
  width: min(100%, 560px);
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 24px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(248, 250, 252, 0.98));
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.28);
}

.dialog-header,
.dialog-actions,
.summary-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.dialog-header {
  align-items: flex-start;
}

.dialog-eyebrow,
.dialog-message,
h2,
.summary-row span,
.summary-row strong,
.dialog-warning p,
.dialog-error p {
  margin: 0;
}

.dialog-eyebrow {
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

h2,
.summary-row strong,
.dialog-warning strong,
.dialog-error strong {
  color: #0f172a;
}

.dialog-message,
.summary-row span,
.dialog-close {
  color: #475569;
}

.dialog-close {
  border: 0;
  background: transparent;
  font: inherit;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

.dialog-summary {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-row {
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(241, 245, 249, 0.8);
  align-items: center;
}

.dialog-warning {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 18px;
  border: 1px solid rgba(245, 158, 11, 0.28);
  background: linear-gradient(160deg, rgba(255, 251, 235, 0.96), rgba(254, 243, 199, 0.92));
}

.dialog-error {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 18px;
  border: 1px solid rgba(248, 113, 113, 0.28);
  background: linear-gradient(160deg, rgba(254, 242, 242, 0.96), rgba(254, 226, 226, 0.92));
}

.dialog-actions {
  align-items: center;
}

.primary-button,
.secondary-button {
  border: 0;
  padding: 12px 18px;
  color: #fff;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
}

.primary-button {
  background: linear-gradient(135deg, #0f766e, #0f766e 45%, #0284c7);
}

.secondary-button {
  background: linear-gradient(135deg, #0f172a, #1e293b 55%, #0f766e);
}

.primary-button:disabled,
.secondary-button:disabled,
.dialog-close:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 760px) {
  .finance-confirm-dialog {
    padding: 16px;
  }

  .dialog-header,
  .dialog-actions,
  .summary-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .dialog-card {
    padding: 20px;
  }

  .primary-button,
  .secondary-button {
    width: 100%;
  }
}
</style>
