<template>
  <section class="cost-batch-view">
    <div class="hero-card">
      <div>
        <p class="eyebrow">核算</p>
        <h1>成本跑批</h1>
        <p class="description">先预览会计期间数据，再执行批量核算，可选择允许重跑。</p>
      </div>
      <div class="hero-metrics">
        <div class="metric-pill">
          <span class="metric-label">会计期间</span>
          <strong>{{ costSurface.ledgerMonthLabel }}</strong>
        </div>
        <div class="metric-pill">
          <span class="metric-label">预览条数</span>
          <strong>{{ costSurface.previewCards.length }}</strong>
        </div>
      </div>
    </div>

    <div v-if="costSurface.error" class="feedback-banner error">{{ costSurface.error }}</div>

    <div class="content-grid">
      <article class="panel-card">
        <header class="section-header">
          <div>
            <span>批处理控制</span>
            <h2>期间执行</h2>
          </div>
        </header>

        <el-form label-position="top" class="batch-form">
          <el-form-item label="会计期间">
            <el-date-picker
              v-model="costSurface.form.ledgerMonth"
              type="month"
              value-format="YYYY-MM"
              placeholder="选择会计期间"
            />
          </el-form-item>

          <el-form-item label="预览项目ID">
            <el-input v-model="costSurface.form.ventureId" placeholder="输入用于预览的项目ID" />
          </el-form-item>

          <el-checkbox v-model="costSurface.form.rerunExistingMonth">允许重跑所选期间</el-checkbox>

          <div class="action-row">
            <el-button :loading="costSurface.loading" @click="costSurface.previewLedgerMonth">预览期间</el-button>
            <el-button type="primary" :loading="costSurface.loading" @click="costSurface.runLedgerMonth">执行跑批</el-button>
          </div>
        </el-form>
      </article>

      <article class="panel-card">
        <header class="section-header">
          <div>
            <span>预览与结果</span>
            <h2>后端返回概览</h2>
          </div>
        </header>

        <div class="result-grid">
          <article v-for="card in costSurface.previewCards" :key="card.key" class="result-card">
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}</strong>
            <small>{{ card.hint }}</small>
          </article>
        </div>

        <el-empty v-if="!costSurface.previewCards.length" description="请先执行预览查看当前期间数据" />

        <div v-if="costSurface.executionSummary" class="execution-banner">
          <strong>{{ costSurface.executionSummary.title }}</strong>
          <p>{{ costSurface.executionSummary.message }}</p>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { normalizeFinanceListPayload } from '@/utils/financeAdapters'
import { formatFinanceCurrency, formatFinanceLedgerMonth, formatFinanceText } from '@/utils/financeFormatters'
import { useFinanceWorkbenchStore } from '@/stores/financeWorkbenchStore'

const firstDefined = (...values) => values.find(value => value !== undefined && value !== null && value !== '')

const store = useFinanceWorkbenchStore()
const form = reactive({
  ledgerMonth: store.ledgerMonth || '',
  ventureId: '',
  rerunExistingMonth: false
})

const getPreviewRowCount = payload => {
  const rows = normalizeFinanceListPayload(payload)
  if (rows.length) {
    return rows.length
  }

  return firstDefined(payload?.count, payload?.total, payload?.previewCount, payload?.lineCount, 0)
}

const previewCards = computed(() => {
  if (!store.costPreview) {
    return []
  }

  const payload = store.costPreview
  return [
    {
      key: 'ledgerMonth',
      label: '会计期间',
      value: formatFinanceLedgerMonth(firstDefined(payload?.ledgerMonth, form.ledgerMonth, store.ledgerMonth)),
      hint: '目标核算期间'
    },
    {
      key: 'venture',
      label: '预览目标',
      value: formatFinanceText(firstDefined(payload?.ventureId, payload?.projectId, form.ventureId)),
      hint: '用于预览查询的项目'
    },
    {
      key: 'rows',
      label: '返回条数',
      value: String(getPreviewRowCount(payload)),
      hint: '归一化后的预览条目数'
    },
    {
      key: 'totalCost',
      label: '预览总成本',
      value:
        firstDefined(payload?.totalCost, payload?.cost, payload?.amount) !== undefined
          ? formatFinanceCurrency(firstDefined(payload?.totalCost, payload?.cost, payload?.amount))
          : '--',
      hint: '预览结果聚合金额'
    }
  ]
})

const executionSummary = computed(() => {
  if (!store.costRunResult?.completed) {
    return null
  }

  return {
    title: store.costRunResult.success ? '跑批已提交' : '跑批返回告警',
    message:
      store.costRunResult.message ||
      `会计期间 ${formatFinanceLedgerMonth(firstDefined(store.costRunResult.ledgerMonth, form.ledgerMonth))} 已完成，后端参考号 ${formatFinanceText(store.costRunResult.referenceId)}。`
  }
})

const previewLedgerMonth = async () => {
  if (!form.ledgerMonth || !form.ventureId) {
    return null
  }

  try {
    return await store.loadCostPreview(form.ventureId, form.ledgerMonth)
  } catch (error) {
    return null
  }
}

const runLedgerMonth = async () => {
  if (!form.ledgerMonth) {
    return null
  }

  try {
    return await store.submitCostBatch({
      ledgerMonth: form.ledgerMonth,
      rerunExistingMonth: form.rerunExistingMonth
    })
  } catch (error) {
    return null
  }
}

const costSurface = reactive({
  loading: computed(() => store.loading),
  error: computed(() => store.error),
  form,
  ledgerMonthLabel: computed(() => formatFinanceLedgerMonth(firstDefined(form.ledgerMonth, store.ledgerMonth))),
  previewCards,
  executionSummary,
  previewLedgerMonth,
  runLedgerMonth
})
</script>

<style scoped>
.cost-batch-view {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card,
.panel-card {
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(15, 23, 42, 0.08);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  background-image: linear-gradient(135deg, rgba(12, 74, 110, 0.08), rgba(217, 249, 157, 0.22));
}

.eyebrow,
.section-header span,
.metric-label,
.result-card span {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

h1,
h2,
.result-card strong,
.execution-banner strong {
  margin: 0;
  color: #0f172a;
}

h1 {
  font-size: clamp(28px, 3vw, 40px);
}

h2 {
  font-size: 22px;
}

.description,
.result-card small,
.execution-banner p {
  color: #475569;
  line-height: 1.6;
}

.description {
  max-width: 60ch;
}

.hero-metrics,
.result-grid {
  display: grid;
  gap: 12px;
}

.hero-metrics {
  min-width: 180px;
}

.metric-pill,
.feedback-banner,
.result-card,
.execution-banner {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.metric-pill,
.result-card,
.execution-banner {
  padding: 16px;
}

.metric-pill {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feedback-banner {
  padding: 14px 18px;
  border: 1px solid rgba(220, 38, 38, 0.18);
}

.feedback-banner.error {
  color: #b91c1c;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  gap: 20px;
}

.panel-card {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.batch-form {
  display: grid;
  gap: 12px;
  margin-top: 20px;
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.result-grid {
  margin-bottom: 16px;
}

@media (max-width: 900px) {
  .hero-card,
  .content-grid {
    display: grid;
    grid-template-columns: 1fr;
  }

  .hero-metrics {
    min-width: 0;
  }
}
</style>
