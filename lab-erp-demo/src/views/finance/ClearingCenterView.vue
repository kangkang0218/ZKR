<template>
  <section class="clearing-center">
    <div class="hero-card">
      <div>
        <p class="eyebrow">结算</p>
        <h1>清算中心</h1>
        <p class="description">
          选择项目后查看后端计算的清算指标，并执行清算流程。
        </p>
      </div>
      <div class="hero-metrics">
        <div class="metric-pill">
          <span class="metric-label">项目数</span>
          <strong>{{ surface.ventureOptions.length }}</strong>
        </div>
        <div class="metric-pill">
          <span class="metric-label">结果来源</span>
          <strong>{{ surface.resultSourceLabel }}</strong>
        </div>
      </div>
    </div>

    <div v-if="surface.error" class="feedback-banner error">
      {{ surface.error }}
    </div>

    <div class="content-grid">
      <article class="panel-card selection-card">
        <header class="section-header">
          <div>
            <span>项目选择</span>
            <h2>选择清算目标</h2>
          </div>
          <el-button text :loading="surface.loading" @click="surface.reloadVentures">刷新</el-button>
        </header>

        <el-select
          v-model="surface.selectedVentureId"
          class="venture-select"
          clearable
          filterable
          placeholder="选择项目"
        >
          <el-option
            v-for="venture in surface.ventureOptions"
            :key="venture.value"
            :label="venture.label"
            :value="venture.value"
          />
        </el-select>

        <div v-if="surface.ventureOptions.length" class="venture-list">
          <button
            v-for="venture in surface.ventureOptions"
            :key="venture.value"
            type="button"
            class="venture-row"
            :class="{ active: venture.value === surface.selectedVentureId }"
            @click="surface.selectVenture(venture.value)"
          >
            <strong>{{ venture.title }}</strong>
            <span>{{ venture.caption }}</span>
          </button>
        </div>
        <el-empty v-else description="暂无可清算项目" />
      </article>

      <article class="panel-card result-card">
        <header class="section-header">
          <div>
            <span>清算结果</span>
            <h2>后端结算输出</h2>
          </div>
          <el-button
            type="primary"
            :disabled="!surface.canExecute"
            :loading="surface.loading"
            @click="surface.executeSelected"
          >
            执行清算
          </el-button>
        </header>

        <div class="result-grid">
          <article v-for="card in surface.resultCards" :key="card.key" class="result-metric">
            <span>{{ card.label }}</span>
            <strong>{{ formatResultValue(card) }}</strong>
            <small>{{ card.hint }}</small>
          </article>
        </div>

        <el-empty
          v-if="!surface.resultCards.length"
          description="请选择项目查看清算输出"
        />
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useFinanceClearingSurface } from '@/stores/financeWorkbenchStore'
import {
  formatFinanceCurrency,
  formatFinanceDateTime,
  formatFinanceText
} from '@/utils/financeFormatters'

const surface = reactive(useFinanceClearingSurface())

const formatResultValue = card => {
  if (card.kind === 'currency') {
    if (card.value === undefined || card.value === null || card.value === '') {
      return '--'
    }
    return formatFinanceCurrency(card.value)
  }

  if (card.kind === 'datetime') {
    return formatFinanceDateTime(card.value)
  }

  return formatFinanceText(card.value)
}

onMounted(() => {
  surface.ensureVenturesLoaded()
})
</script>

<style scoped>
.clearing-center {
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
.result-metric span {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

h1,
h2,
.venture-row strong,
.result-metric strong {
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
.venture-row span,
.result-metric small {
  color: #475569;
  line-height: 1.6;
}

.description {
  max-width: 60ch;
}

.hero-metrics {
  display: grid;
  gap: 12px;
  min-width: 180px;
}

.metric-pill,
.feedback-banner,
.venture-row,
.result-metric {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.metric-pill {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 16px;
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
  grid-template-columns: minmax(280px, 360px) minmax(0, 1fr);
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

.venture-select {
  width: 100%;
  margin: 20px 0 16px;
}

.venture-list,
.result-grid {
  display: grid;
  gap: 12px;
}

.venture-row {
  border: 1px solid transparent;
  padding: 16px;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.venture-row.active {
  border-color: rgba(15, 118, 110, 0.3);
  transform: translateY(-1px);
}

.result-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 20px;
}

.result-metric {
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.result-metric strong {
  display: block;
  font-size: 24px;
}

@media (max-width: 900px) {
  .hero-card,
  .content-grid,
  .result-grid {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero-metrics {
    min-width: 0;
  }
}
</style>
