<template>
  <section class="finance-view dividend-center-view">
    <header class="view-hero">
      <div>
        <p class="view-eyebrow">分红</p>
        <div class="hero-title-row">
          <h1>分红中心</h1>
          <FinanceStatusTag :label="selectionStatusLabel" :tone="selectionStatusTone" />
        </div>
        <p class="view-description">
          生成分红单、查看后端最新列表，并在提交确认前执行二次确认。
        </p>
      </div>
      <div class="hero-note-grid">
        <article class="hero-note">
          <span class="hero-note-label">列表</span>
          <strong>{{ listCountLabel }}</strong>
        </article>
        <article class="hero-note">
          <span class="hero-note-label">最新结果</span>
          <strong>{{ latestResultLabel }}</strong>
        </article>
      </div>
    </header>

    <FinanceErrorState
      v-if="showBlockingError"
      title="分红单加载失败"
      :message="surface.error"
      action-label="重试"
      @action="loadDividendSheets"
    />

    <template v-else>
      <section class="content-grid content-grid--two">
        <article class="surface-card">
          <header class="surface-header">
            <div>
              <p class="surface-eyebrow">生成</p>
              <h2>生成分红单</h2>
            </div>
            <FinanceStatusTag :label="prepareStatusLabel" :tone="prepareStatusTone" />
          </header>

          <p class="surface-footnote">
            可按项目生成分红单，再刷新列表查看后端返回状态。
          </p>

          <div v-if="showInlineError" class="feedback-banner feedback-banner--error" role="alert">
            <strong>后端消息</strong>
            <p>{{ surface.error }}</p>
          </div>

          <div v-if="showPrepareSuccess" class="feedback-banner feedback-banner--success">
            <strong>生成完成</strong>
            <p>{{ prepareBannerMessage }}</p>
          </div>

          <form class="prepare-form" @submit.prevent="submitPrepare">
            <label class="field">
              <span>项目ID</span>
              <input v-model.trim="form.projectId" type="text" maxlength="64" placeholder="project-001" />
            </label>

            <label class="field">
              <span>操作人</span>
              <input v-model.trim="form.operator" type="text" maxlength="50" placeholder="finance.operator" />
            </label>

            <label class="field field--full">
              <span>备注</span>
              <textarea
                v-model.trim="form.remark"
                rows="3"
                maxlength="200"
                placeholder="可选备注，将用于生成与确认动作"
              ></textarea>
            </label>

            <div class="form-actions">
              <button class="primary-button" type="submit" :disabled="preparing || !canPrepare">
                {{ preparing ? '生成中...' : '生成分红单' }}
              </button>
              <button class="secondary-button" type="button" :disabled="surface.loading" @click="resetForm">重置</button>
              <span class="form-hint">确认时将使用已选分红单、当前操作人与备注。</span>
            </div>
          </form>
        </article>

        <article class="surface-card">
          <header class="surface-header">
            <div>
              <p class="surface-eyebrow">结果</p>
              <h2>后端状态与输出</h2>
            </div>
            <FinanceStatusTag :label="confirmStatusLabel" :tone="confirmStatusTone" />
          </header>

          <div v-if="showConfirmSuccess" class="feedback-banner feedback-banner--success">
            <strong>确认完成</strong>
            <p>{{ confirmBannerMessage }}</p>
          </div>

          <div class="result-stack">
            <article class="result-block">
              <span class="result-label">生成结果</span>
              <strong>{{ prepareResultSummary }}</strong>
              <p>{{ prepareBannerMessage }}</p>
            </article>
            <article class="result-block">
              <span class="result-label">确认结果</span>
              <strong>{{ confirmResultSummary }}</strong>
              <p>{{ confirmBannerMessage }}</p>
            </article>
          </div>

          <div v-if="surface.latestResultCards.length" class="result-grid">
            <article v-for="card in surface.latestResultCards" :key="card.key" class="result-card">
              <span>{{ card.label }}</span>
              <strong>{{ formatResultValue(card) }}</strong>
              <small>{{ card.hint }}</small>
            </article>
          </div>
          <FinanceEmptyState
            v-else
            title="暂无分红结果"
            description="生成或确认分红单后将在此显示最新结果。"
            compact
          />
        </article>
      </section>

      <section class="surface-card surface-card--full">
        <header class="surface-header">
          <div>
            <p class="surface-eyebrow">列表</p>
            <h2>分红单列表</h2>
          </div>
          <div class="surface-header-actions">
            <FinanceStatusTag :label="selectedSheetLabel" :tone="selectionStatusTone" />
            <button class="secondary-button" type="button" :disabled="surface.loading" @click="loadDividendSheets">刷新</button>
          </div>
        </header>

        <div v-if="rows.length" class="table-shell">
          <table class="finance-table">
            <thead>
              <tr>
                <th>单号</th>
                <th>项目</th>
                <th>状态</th>
                <th>操作人</th>
                <th>备注</th>
                <th>更新时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in rows" :key="describeSheet(row, index).rowKey" :class="{ 'is-selected': isSelectedRow(row, index) }">
                <td>{{ describeSheet(row, index).referenceLabel }}</td>
                <td>{{ formatFinanceText(describeSheet(row, index).projectId) }}</td>
                <td>
                  <FinanceStatusTag
                    :label="formatFinanceEnumLabel(dividendStatusMap, describeSheet(row, index).status, describeSheet(row, index).status)"
                    :tone="dividendToneFor(describeSheet(row, index).status)"
                  />
                </td>
                <td>{{ formatFinanceText(describeSheet(row, index).operator) }}</td>
                <td>{{ formatFinanceText(describeSheet(row, index).remark) }}</td>
                <td>{{ formatFinanceDateTime(describeSheet(row, index).updatedAt) }}</td>
                <td>
                  <div class="row-actions">
                    <button class="row-link" type="button" @click="selectSheet(row, index)">选择</button>
                    <button
                      class="row-link row-link--primary"
                      type="button"
                      :disabled="surface.loading || !canConfirmRow(row)"
                      @click="openConfirmDialog(row)"
                    >
                      确认
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <FinanceEmptyState
          v-else
            title="未返回分红单"
            description="请先生成分红单或刷新页面后重试。"
            compact
          />
      </section>
    </template>

    <FinanceConfirmDialog
      v-model:open="confirmOpen"
      title="确认分红提交"
      message="调用后端确认接口前，请再次核对所选分红单。"
      :items="confirmItems"
      :error-message="showInlineError ? surface.error : ''"
      :loading="confirming"
      confirm-label="确认本次操作"
      double-confirm
      double-confirm-title="最终确认"
      double-confirm-message="该操作将向后端提交确认请求，请确认分红单状态无误。"
      double-confirm-label="提交分红确认"
      @confirm="submitConfirm"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import FinanceConfirmDialog from '@/components/finance/FinanceConfirmDialog.vue'
import FinanceEmptyState from '@/components/finance/FinanceEmptyState.vue'
import FinanceErrorState from '@/components/finance/FinanceErrorState.vue'
import FinanceStatusTag from '@/components/finance/FinanceStatusTag.vue'
import { useFinanceDividendSurface } from '@/stores/financeWorkbenchStore'
import { financeDividendStatusMap } from '@/utils/financeEnums'
import { formatFinanceCount, formatFinanceDateTime, formatFinanceEnumLabel, formatFinanceText } from '@/utils/financeFormatters'

const surface = reactive(useFinanceDividendSurface())
const confirmOpen = ref(false)
const selectedSheetKey = ref('')
const lastAction = ref('')

const form = reactive({
  projectId: '',
  operator: '',
  remark: ''
})

const dividendStatusMap = financeDividendStatusMap

const firstDefined = (...values) => values.find(value => value !== undefined && value !== null && value !== '')
const rows = computed(() => surface.rows || [])
const describeSheet = (row, index = 0) =>
  surface.describeSheet(row, index)
const preparing = computed(() => surface.loading && lastAction.value === 'prepare')
const confirming = computed(() => surface.loading && lastAction.value === 'confirm')
const showBlockingError = computed(() => Boolean(surface.error) && lastAction.value === 'load' && !rows.value.length)
const showInlineError = computed(() => Boolean(surface.error) && !showBlockingError.value)
const canPrepare = computed(() => Boolean(form.projectId) && Boolean(form.operator))
const selectedSheet = computed(() => rows.value.find((row, index) => describeSheet(row, index).rowKey === selectedSheetKey.value) || null)
const latestResultLabel = computed(() => {
  if (confirming.value) return '确认中'
  if (preparing.value) return '生成中'
  if (surface.confirmResult?.completed) return surface.confirmResult.success ? '确认成功' : '确认需重试'
  if (surface.prepareResult?.completed) return surface.prepareResult.success ? '生成成功' : '生成需重试'
  return '等待中'
})
const listCountLabel = computed(() => `${formatFinanceCount(rows.value.length)} 张分红单`)
const selectionStatusLabel = computed(() => {
  if (!selectedSheet.value) {
    return '未选择分红单'
  }

  const descriptor = describeSheet(selectedSheet.value)
  return formatFinanceEnumLabel(dividendStatusMap, descriptor.status, descriptor.status)
})
const selectionStatusTone = computed(() => {
  if (!selectedSheet.value) {
    return 'neutral'
  }

  return dividendToneFor(describeSheet(selectedSheet.value).status)
})
const selectedSheetLabel = computed(() => {
  if (!selectedSheet.value) {
    return '请选择分红单'
  }

  return describeSheet(selectedSheet.value).referenceLabel
})
const prepareStatusLabel = computed(() => {
  if (preparing.value) return '生成中'
  if (!surface.prepareResult?.completed) return '待生成'
  return surface.prepareResult.success ? '已生成' : '需重试'
})
const prepareStatusTone = computed(() => {
  if (preparing.value) return 'info'
  if (!surface.prepareResult?.completed) return 'neutral'
  return surface.prepareResult.success ? 'success' : 'danger'
})
const confirmStatusLabel = computed(() => {
  if (confirming.value) return '确认中'
  if (!surface.confirmResult?.completed) return '待确认'
  return surface.confirmResult.success ? '已确认' : '需重试'
})
const confirmStatusTone = computed(() => {
  if (confirming.value) return 'info'
  if (!surface.confirmResult?.completed) return 'neutral'
  return surface.confirmResult.success ? 'success' : 'danger'
})
const showPrepareSuccess = computed(() => Boolean(surface.prepareResult?.completed && surface.prepareResult?.success))
const showConfirmSuccess = computed(() => Boolean(surface.confirmResult?.completed && surface.confirmResult?.success))
const prepareBannerMessage = computed(() => mutationMessage(surface.prepareResult, '后端已返回生成结果。'))
const confirmBannerMessage = computed(() => mutationMessage(surface.confirmResult, '后端已返回确认结果。'))
const prepareResultSummary = computed(() => mutationSummary(surface.prepareResult, '尚未发起生成'))
const confirmResultSummary = computed(() => mutationSummary(surface.confirmResult, '尚未发起确认'))
const confirmItems = computed(() => {
  const row = selectedSheet.value

  return [
    { label: '分红单', value: row ? describeSheet(row).referenceLabel : '--' },
    { label: '项目', value: formatFinanceText(row ? describeSheet(row).projectId : form.projectId, form.projectId || '--') },
    { label: '状态', value: formatFinanceEnumLabel(dividendStatusMap, row ? describeSheet(row).status : '', '待确认') },
    { label: '操作人', value: formatFinanceText(form.operator) },
    { label: '备注', value: formatFinanceText(form.remark || (row ? describeSheet(row).remark : '')) }
  ]
})

watch(
  rows,
  nextRows => {
    if (!nextRows.length) {
      selectedSheetKey.value = ''
      return
    }

    const hasSelected = nextRows.some((row, index) => describeSheet(row, index).rowKey === selectedSheetKey.value)
    if (!hasSelected) {
      selectedSheetKey.value = describeSheet(nextRows[0], 0).rowKey
    }
  },
  { immediate: true }
)

const loadDividendSheets = async () => {
  lastAction.value = 'load'

  try {
    await surface.reloadSheets()
  } catch {
    return null
  }

  return surface.rows
}

const submitPrepare = async () => {
  if (!canPrepare.value) {
    return null
  }

  lastAction.value = 'prepare'

  try {
    await surface.prepareSheet({
      projectId: form.projectId,
      operator: form.operator,
      remark: form.remark
    })
    await loadDividendSheets()

    const preparedReferenceId = surface.prepareResult?.referenceId
    const preparedRow = rows.value.find((row, index) => {
      const descriptor = describeSheet(row, index)

      if (preparedReferenceId) {
        return String(descriptor.referenceId) === String(preparedReferenceId)
      }

      return descriptor.projectId === form.projectId
    })

    if (preparedRow) {
      selectedSheetKey.value = describeSheet(preparedRow).rowKey
    }
  } catch {
    return null
  }

  return surface.prepareResult
}

const openConfirmDialog = row => {
  const targetRow = row || selectedSheet.value
  if (!canConfirmRow(targetRow)) {
    return
  }

  selectedSheetKey.value = describeSheet(targetRow).rowKey
  confirmOpen.value = true
}

const submitConfirm = async () => {
  if (!canConfirmRow(selectedSheet.value)) {
    return null
  }

  lastAction.value = 'confirm'

  try {
    await surface.confirmSheet({
      projectId: describeSheet(selectedSheet.value).projectId,
      operator: form.operator,
      remark: form.remark || describeSheet(selectedSheet.value).remark
    })
    confirmOpen.value = false
    await loadDividendSheets()
  } catch {
    confirmOpen.value = true
    return null
  }

  return surface.confirmResult
}

const resetForm = () => {
  form.projectId = ''
  form.operator = ''
  form.remark = ''
}

const selectSheet = (row, index = 0) => {
  const descriptor = describeSheet(row, index)
  selectedSheetKey.value = descriptor.rowKey
  form.projectId = descriptor.projectId
  form.remark = descriptor.remark
}

const canConfirmRow = row => {
  const descriptor = describeSheet(row)
  return Boolean(row && descriptor.projectId && form.operator && String(descriptor.status).toUpperCase() !== 'CONFIRMED')
}
const isSelectedRow = (row, index = 0) => describeSheet(row, index).rowKey === selectedSheetKey.value
const dividendToneFor = status => {
  if (status === 'CONFIRMED') return 'success'
  if (status === 'PENDING') return 'warning'
  if (!status || status === '--') return 'neutral'
  return 'info'
}
const mutationSummary = (result, fallback) => {
  if (!result?.completed) {
    return fallback
  }

  return result.success ? '成功' : '后端返回异常'
}
const mutationMessage = (result, fallback) =>
  firstDefined(result?.message, result?.remark, result?.referenceId ? `参考号 ${result.referenceId}` : '', fallback)
const formatResultValue = card => {
  if (card.kind === 'datetime') {
    return formatFinanceDateTime(card.value)
  }

  return formatFinanceText(card.value)
}

onMounted(() => {
  loadDividendSheets()
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
.result-block,
.result-card,
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

.hero-title-row,
.surface-header,
.surface-header-actions,
.form-actions,
.row-actions {
  display: flex;
  gap: 12px;
}

.hero-title-row {
  flex-wrap: wrap;
  align-items: center;
}

.hero-note-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 180px));
  gap: 12px;
}

.view-eyebrow,
.surface-eyebrow,
.hero-note-label,
.field span,
.form-hint,
.result-label,
th {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

h1,
h2,
p,
strong,
small {
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
.field textarea,
td,
.feedback-banner p,
.result-block p,
.result-card small,
.row-link {
  color: #475569;
}

.view-description {
  max-width: 72ch;
  line-height: 1.6;
}

.hero-note {
  padding: 18px;
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

.surface-card--full {
  width: 100%;
}

.surface-header {
  justify-content: space-between;
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

.prepare-form {
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

.form-actions {
  justify-content: space-between;
  align-items: flex-start;
}

.result-stack,
.result-grid {
  display: grid;
  gap: 12px;
}

.result-stack {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.result-block,
.result-card {
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.16);
  background: rgba(248, 250, 252, 0.88);
}

.result-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.result-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.result-card strong {
  display: block;
  font-size: 20px;
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

tr.is-selected {
  background: rgba(240, 253, 250, 0.72);
}

.row-actions {
  align-items: center;
}

.row-link {
  border: 0;
  padding: 0;
  background: transparent;
  font: inherit;
  cursor: pointer;
}

.row-link--primary {
  color: #0f766e;
  font-weight: 600;
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
.row-link:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 1120px) {
  .content-grid--two,
  .prepare-form,
  .result-stack,
  .result-grid {
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

  .hero-note-grid {
    grid-template-columns: 1fr;
    width: 100%;
  }

  th,
  td {
    white-space: normal;
  }
}
</style>
