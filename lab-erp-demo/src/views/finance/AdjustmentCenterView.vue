<template>
  <section class="finance-view adjustment-center-view">
    <header class="view-hero">
      <div>
        <p class="view-eyebrow">调账</p>
        <div class="hero-title-row">
          <h1>调账中心</h1>
          <FinanceStatusTag :label="logStatusLabel" :tone="logStatusTone" />
        </div>
        <p class="view-description">
          创建借方或贷方调账，并在页面内查看后端审计记录。
        </p>
      </div>
      <div class="hero-note">
        <span class="hero-note-label">最新结果</span>
        <strong>{{ mutationStatusLabel }}</strong>
      </div>
    </header>

    <FinanceErrorState
      v-if="showBlockingError"
      title="调账数据加载失败"
      :message="workbenchStore.error"
      action-label="重试"
      @action="loadAdjustmentLogs"
    />

    <template v-else>
      <section class="content-grid content-grid--two">
        <article class="surface-card">
          <header class="surface-header">
            <div>
              <p class="surface-eyebrow">创建</p>
              <h2>新增调账</h2>
            </div>
            <FinanceStatusTag :label="directionLabel" :tone="directionTone" />
          </header>

          <p class="surface-footnote">
            提交前请明确选择借方或贷方方向，避免误记账。
          </p>

          <div v-if="showInlineError" class="feedback-banner feedback-banner--error" role="alert">
            <strong>后端消息</strong>
            <p>{{ workbenchStore.error }}</p>
          </div>

          <div v-if="showSuccessBanner" class="feedback-banner feedback-banner--success">
            <strong>调账已提交</strong>
            <p>{{ successMessage }}</p>
          </div>

          <form class="adjustment-form" @submit.prevent="openConfirmDialog">
            <label class="field">
              <span>用户ID</span>
              <input v-model.trim="form.userId" type="text" maxlength="64" placeholder="wallet-user-001" />
            </label>

            <label class="field">
              <span>主题</span>
              <input v-model.trim="form.subject" type="text" maxlength="100" placeholder="Manual balance correction" />
            </label>

            <label class="field">
              <span>方向</span>
              <select v-model="form.direction">
                  <option disabled value="">请选择方向</option>
                <option v-for="option in directionOptions" :key="option.value" :value="option.value">
                  {{ option.label }} ({{ option.value }})
                </option>
              </select>
            </label>

            <label class="field">
              <span>金额</span>
              <input v-model.number="form.amount" type="number" min="0.01" step="0.01" placeholder="0.00" />
            </label>

            <label class="field">
              <span>操作人</span>
              <input v-model.trim="form.operator" type="text" maxlength="50" placeholder="finance.auditor" />
            </label>

            <label class="field">
              <span>参考单号</span>
              <input v-model.trim="form.refDocNo" type="text" maxlength="80" placeholder="ADJ-2026-03-001" />
            </label>

            <label class="field field--full">
              <span>原因</span>
              <textarea v-model.trim="form.reason" rows="3" maxlength="200" placeholder="Why this debit or credit is needed"></textarea>
            </label>

            <label class="field field--full">
              <span>备注</span>
              <textarea v-model.trim="form.remark" rows="3" maxlength="200" placeholder="Optional operator note for the audit trail"></textarea>
            </label>

            <div class="form-actions">
              <button class="primary-button" type="submit" :disabled="submitting || !canReviewAdjustment">
                {{ submitting ? '提交中...' : '提交调账' }}
              </button>
              <button class="secondary-button" type="button" :disabled="submitting" @click="resetForm">重置</button>
              <span class="form-hint">提交前会先弹出确认框，再调用后端接口。</span>
            </div>
          </form>
        </article>

        <article class="surface-card">
          <header class="surface-header">
            <div>
              <p class="surface-eyebrow">审计</p>
              <h2>调账列表</h2>
            </div>
            <div class="surface-header-actions">
              <FinanceStatusTag :label="listCountLabel" tone="info" />
              <button class="secondary-button" type="button" :disabled="submitting" @click="loadAdjustmentLogs">刷新</button>
            </div>
          </header>

          <div v-if="adjustmentRows.length" class="table-shell">
            <table class="finance-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>用户ID</th>
                  <th>主题</th>
                  <th>方向</th>
                  <th>Amount</th>
                  <th>原因</th>
                  <th>操作人</th>
                  <th>创建时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in adjustmentRows" :key="adjustmentRowKey(row, index)">
                  <td>{{ adjustmentReference(row, index) }}</td>
                  <td>{{ formatFinanceText(row.userId ?? row.user_id) }}</td>
                  <td>{{ formatFinanceText(row.subject) }}</td>
                  <td>
                    <FinanceStatusTag
                       :label="formatFinanceEnumLabel(directionMap, row.direction, '未指定')"
                      :tone="directionToneFor(row.direction)"
                    />
                  </td>
                  <td :class="amountClass(signedAmount(row))">{{ formatFinanceDelta(signedAmount(row)) }}</td>
                  <td>{{ formatFinanceText(row.reason || row.remark) }}</td>
                  <td>{{ formatFinanceText(row.operator) }}</td>
                  <td>{{ formatFinanceDateTime(row.createdAt ?? row.created_at) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <FinanceEmptyState
            v-else
            title="未返回调账记录"
            description="当前财务工作台状态下，后端未返回调账列表。"
            compact
          />
        </article>
      </section>
    </template>

    <FinanceConfirmDialog
      v-model:open="confirmOpen"
      title="确认调账提交"
      message="提交到后端前，请再次核对调账信息。"
      :items="confirmItems"
      :error-message="showInlineError ? workbenchStore.error : ''"
      :loading="submitting"
      confirm-label="确认提交"
      @confirm="submitAdjustment"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import FinanceConfirmDialog from '@/components/finance/FinanceConfirmDialog.vue'
import FinanceEmptyState from '@/components/finance/FinanceEmptyState.vue'
import FinanceErrorState from '@/components/finance/FinanceErrorState.vue'
import FinanceStatusTag from '@/components/finance/FinanceStatusTag.vue'
import { useFinanceWorkbenchStore } from '@/stores/financeWorkbenchStore'
import { FINANCE_ADJUSTMENT_DIRECTION_OPTIONS, financeAdjustmentDirectionMap } from '@/utils/financeEnums'
import {
  formatFinanceCount,
  formatFinanceDateTime,
  formatFinanceDelta,
  formatFinanceEnumLabel,
  formatFinanceText
} from '@/utils/financeFormatters'

const workbenchStore = useFinanceWorkbenchStore()
const confirmOpen = ref(false)
const lastAction = ref('')

const form = reactive({
  userId: '',
  subject: '',
  direction: '',
  amount: null,
  operator: '',
  refDocNo: '',
  reason: '',
  remark: ''
})

const directionOptions = FINANCE_ADJUSTMENT_DIRECTION_OPTIONS
const directionMap = financeAdjustmentDirectionMap

const adjustmentRows = computed(() => workbenchStore.adjustmentLogs || [])
const submitting = computed(() => workbenchStore.loading && lastAction.value === 'submit')
const showBlockingError = computed(() => Boolean(workbenchStore.error) && lastAction.value === 'load' && !adjustmentRows.value.length)
const showInlineError = computed(() => Boolean(workbenchStore.error) && !showBlockingError.value)
const canReviewAdjustment = computed(
  () =>
    Boolean(form.userId) &&
    Boolean(form.subject) &&
    Boolean(form.direction) &&
    Number(form.amount) > 0 &&
    Boolean(form.operator) &&
    Boolean(form.reason)
)
const logStatusLabel = computed(() => (adjustmentRows.value.length ? `${formatFinanceCount(adjustmentRows.value.length)} 条` : '无记录'))
const logStatusTone = computed(() => (adjustmentRows.value.length ? 'info' : 'neutral'))
const listCountLabel = computed(() => `最近 ${formatFinanceCount(adjustmentRows.value.length)} 条`)
const directionLabel = computed(() => formatFinanceEnumLabel(directionMap, form.direction, '请选择方向'))
const directionTone = computed(() => directionToneFor(form.direction))
const mutationStatusLabel = computed(() => {
  if (submitting.value) return '提交中'
  if (!workbenchStore.adjustmentResult?.completed) return '待提交'
  return workbenchStore.adjustmentResult.success ? '已提交' : '需重试'
})
const showSuccessBanner = computed(() => Boolean(workbenchStore.adjustmentResult?.completed && workbenchStore.adjustmentResult?.success))
const successMessage = computed(
  () => workbenchStore.adjustmentResult?.message || `参考号 ${formatFinanceText(workbenchStore.adjustmentResult?.referenceId)}`
)
const confirmItems = computed(() => [
  { label: '用户ID', value: formatFinanceText(form.userId) },
  { label: '主题', value: formatFinanceText(form.subject) },
  { label: '方向', value: formatFinanceEnumLabel(directionMap, form.direction, '请选择方向') },
  { label: '金额', value: formatFinanceDelta(signedFormAmount.value) },
  { label: '操作人', value: formatFinanceText(form.operator) },
  { label: '参考单号', value: formatFinanceText(form.refDocNo) },
  { label: '原因', value: formatFinanceText(form.reason) },
  { label: '备注', value: formatFinanceText(form.remark) }
])
const signedFormAmount = computed(() => {
  const amount = Number(form.amount ?? 0)
  if (form.direction === 'DEBIT') return -Math.abs(amount)
  return Math.abs(amount)
})

const loadAdjustmentLogs = async () => {
  lastAction.value = 'load'
  try {
    await workbenchStore.loadAdjustmentLogs()
  } catch {
    return null
  }

  return workbenchStore.adjustmentLogs
}

const openConfirmDialog = () => {
  if (!canReviewAdjustment.value) {
    return
  }

  confirmOpen.value = true
}

const resetForm = () => {
  form.userId = ''
  form.subject = ''
  form.direction = ''
  form.amount = null
  form.operator = ''
  form.refDocNo = ''
  form.reason = ''
  form.remark = ''
}

const submitAdjustment = async () => {
  if (!canReviewAdjustment.value) {
    return null
  }

  lastAction.value = 'submit'

  try {
    await workbenchStore.submitAdjustment({
      userId: form.userId,
      subject: form.subject,
      direction: form.direction,
      amount: Math.abs(Number(form.amount)),
      operator: form.operator,
      refDocNo: form.refDocNo,
      reason: form.reason,
      remark: form.remark
    })
    confirmOpen.value = false
    await loadAdjustmentLogs()
    resetForm()
  } catch {
    confirmOpen.value = true
    return null
  }

  return workbenchStore.adjustmentResult
}

const adjustmentRowKey = (row, index) => row?.id || row?.adjustmentId || row?.referenceId || `${row?.userId || row?.user_id || 'adjustment'}-${index}`
const adjustmentReference = (row, index) => {
  const value = row?.id || row?.adjustmentId || row?.referenceId || row?.refDocNo || row?.ref_doc_no
  return value ? `#${value}` : `#${index + 1}`
}
const directionToneFor = direction => {
  if (direction === 'CREDIT') return 'success'
  if (direction === 'DEBIT') return 'danger'
  return 'neutral'
}
const signedAmount = row => {
  const amount = Number(row?.amount ?? 0)
  if (row?.direction === 'DEBIT') return -Math.abs(amount)
  if (row?.direction === 'CREDIT') return Math.abs(amount)
  return amount
}
const amountClass = value => {
  const amount = Number(value ?? 0)
  if (amount > 0) return 'amount amount--positive'
  if (amount < 0) return 'amount amount--negative'
  return 'amount'
}

onMounted(() => {
  loadAdjustmentLogs()
})
</script>

<style scoped>
.finance-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.view-hero,
.surface-card,
.hero-note,
.feedback-banner,
.primary-button,
.secondary-button {
  border-radius: 24px;
}

.view-hero,
.surface-card {
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: rgba(255, 255, 255, 0.84);
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
}

.view-hero {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  background-image: linear-gradient(135deg, rgba(240, 253, 250, 0.96), rgba(239, 246, 255, 0.92));
}

.hero-title-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
}

.view-eyebrow,
.surface-eyebrow,
.hero-note-label,
.field span,
.form-hint {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

h1,
h2,
p,
strong {
  margin: 0;
}

h1,
h2,
strong {
  color: #0f172a;
}

h1 {
  font-size: clamp(28px, 3vw, 40px);
}

h2 {
  font-size: 22px;
}

.view-description,
.surface-footnote,
.hero-note,
.field input,
.field select,
.field textarea,
th,
td,
.feedback-banner p {
  color: #475569;
}

.view-description {
  max-width: 68ch;
  line-height: 1.6;
}

.hero-note {
  min-width: 190px;
  padding: 18px;
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.72);
}

.content-grid {
  display: grid;
  gap: 16px;
}

.content-grid--two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.surface-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
}

.surface-header,
.surface-header-actions,
.form-actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.surface-header-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.feedback-banner {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px 18px;
}

.feedback-banner--error {
  border: 1px solid rgba(248, 113, 113, 0.28);
  background: linear-gradient(160deg, rgba(254, 242, 242, 0.96), rgba(254, 226, 226, 0.92));
}

.feedback-banner--success {
  border: 1px solid rgba(16, 185, 129, 0.24);
  background: linear-gradient(160deg, rgba(236, 253, 245, 0.96), rgba(220, 252, 231, 0.92));
}

.adjustment-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field--full,
.form-actions {
  grid-column: 1 / -1;
}

.field input,
.field select,
.field textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.92);
  font: inherit;
}

.field textarea {
  resize: vertical;
}

.table-shell {
  overflow-x: auto;
}

.finance-table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px 10px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.92);
  text-align: left;
  white-space: nowrap;
}

th {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
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
.secondary-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.amount {
  font-variant-numeric: tabular-nums;
}

.amount--positive {
  color: #047857;
}

.amount--negative {
  color: #b91c1c;
}

@media (max-width: 1120px) {
  .content-grid--two,
  .adjustment-form {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .view-hero,
  .surface-header,
  .surface-header-actions,
  .form-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero-note {
    min-width: 0;
    width: 100%;
  }

  th,
  td {
    white-space: normal;
  }
}
</style>
