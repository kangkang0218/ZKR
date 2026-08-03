<template>
  <section class="finance-report-view">
    <div class="hero-card">
      <div>
        <p class="eyebrow">一句话报表</p>
        <h1>AI 可视化报表</h1>
        <p class="description">用一句话描述想要的报表，AI 实时生成图表与数据。每次生成均由大模型实时渲染最新数据，不缓存结果。</p>
      </div>
    </div>

    <div class="panel-card composer-card">
      <el-input
        v-model="prompt"
        type="textarea"
        :rows="3"
        resize="none"
        placeholder="例如：近三个月各项目的费用柱状图"
      />
      <div class="example-chips">
        <span class="chips-label">示例：</span>
        <el-tag
          v-for="example in EXAMPLES"
          :key="example"
          class="chip"
          effect="plain"
          @click="prompt = example"
        >{{ example }}</el-tag>
      </div>
      <div class="action-row">
        <el-button type="primary" :loading="loading" @click="generateReport">📊 生成报表</el-button>
        <el-switch v-model="savePrompt" inline-prompt active-text="保存" inactive-text="不保存" />
        <span class="save-hint">开启后保存描述，可在历史记录一键复跑</span>
      </div>
      <div v-if="errorMessage" class="feedback-banner error">{{ errorMessage }}</div>
    </div>

    <div class="content-grid">
      <aside class="panel-card history-panel">
        <header class="section-header">
          <div>
            <span>历史记录</span>
            <h2>保存的报表描述</h2>
          </div>
        </header>
        <el-empty v-if="!prompts.length" description="暂无保存记录" :image-size="80" />
        <ul v-else class="prompt-list">
          <li v-for="promptItem in prompts" :key="promptItem.id" class="prompt-item">
            <div class="prompt-main">
              <p class="prompt-text">{{ promptItem.promptText }}</p>
              <span class="prompt-meta">{{ promptItem.creatorName || '未知' }} · {{ formatTime(promptItem.createdAt) }}</span>
            </div>
            <div class="prompt-actions">
              <el-button size="small" @click="reRun(promptItem.promptText)">复跑</el-button>
              <el-button size="small" type="danger" plain @click="removePrompt(promptItem.id)">删除</el-button>
            </div>
          </li>
        </ul>
      </aside>

      <article class="panel-card canvas-panel">
        <header class="section-header">
          <div>
            <span>报表画布</span>
            <h2>{{ report ? report.spec.title : '等待生成' }}</h2>
          </div>
          <div v-if="report" class="canvas-tools">
            <el-radio-group v-model="renderType" size="small">
              <el-radio-button label="bar">柱状</el-radio-button>
              <el-radio-button label="line">折线</el-radio-button>
              <el-radio-button label="pie">饼图</el-radio-button>
              <el-radio-button label="number">数字</el-radio-button>
              <el-radio-button label="table">表格</el-radio-button>
            </el-radio-group>
            <el-button size="small" @click="exportCsv">导出 CSV</el-button>
          </div>
        </header>

        <template v-if="report">
          <div v-if="renderType === 'number'" class="kpi-number">
            <span class="kpi-label">{{ metricLabel(report.spec.metrics[0]) }}</span>
            <strong>{{ formatNumber(primaryMetricValue) }}</strong>
          </div>
          <div v-else-if="renderType !== 'table'" ref="chartRef" class="chart-canvas"></div>
          <el-table v-else :data="tableRows" border size="small" class="data-table">
            <el-table-column v-for="dim in report.spec.dimensions" :key="dim" :prop="dim" :label="dimLabel(dim)" min-width="140" />
            <el-table-column
              v-for="metric in report.spec.metrics"
              :key="metric.field"
              :prop="`m:${metric.field}`"
              :label="`${metricLabel(metric)}`"
              min-width="130"
              align="right"
            />
          </el-table>
          <div class="report-meta">
            <span>provider: {{ report.provider }}</span>
            <span>行数: {{ report.total }}</span>
            <span>{{ formatTime(report.generatedAt) }}</span>
          </div>
        </template>
        <el-empty v-else description="输入描述并点击「生成报表」" :image-size="100" />
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import {
  deleteFinanceReportPrompt,
  generateFinanceReport,
  listFinanceReportPrompts
} from '@/api/finance/report'

const EXAMPLES = [
  '近三个月各项目的费用柱状图',
  '本月费用类别分布饼图',
  '各项目人力成本排行',
  '近六个月人力成本趋势折线图',
  '银行余额月度变化',
  '供应商费用明细表格'
]

const DIM_LABELS = {
  projectName: '项目名称',
  itemCategory: '费用类别',
  submissionType: '报销类型',
  status: '状态',
  submitterName: '提交人',
  supplierName: '供应商',
  month: '月份',
  date: '日期',
  ledgerMonth: '账期月份',
  userName: '成员姓名',
  role: '角色',
  operator: '操作人'
}

const METRIC_LABELS = {
  totalAmount: '总金额',
  quantity: '数量',
  unitPrice: '单价',
  workHours: '工时',
  laborCost: '人力成本',
  middlewareRoyaltyFee: '中间件分成',
  finalSettlementCost: '结算成本',
  balance: '余额'
}

const prompt = ref('')
const savePrompt = ref(false)
const loading = ref(false)
const errorMessage = ref('')
const report = ref(null)
const renderType = ref('bar')
const prompts = ref([])
const chartRef = ref(null)
let chartInstance = null

const metricLabel = metric => `${METRIC_LABELS[metric.field] || metric.field}（${metric.agg}）`
const dimLabel = dim => DIM_LABELS[dim] || dim

const primaryMetricValue = computed(() => {
  if (!report.value || !report.value.rows?.length) return 0
  const metric = report.value.spec.metrics[0]
  return Number(report.value.rows[0].values[metric.field] || 0)
})

const tableRows = computed(() => {
  if (!report.value) return []
  const spec = report.value.spec
  return report.value.rows.map(row => {
    const flat = {}
    for (const dim of spec.dimensions) {
      flat[dim] = row.dimensions[dim] || row.key
    }
    for (const metric of spec.metrics) {
      flat[`m:${metric.field}`] = Number(row.values[metric.field] || 0)
    }
    return flat
  })
})

const generateReport = async () => {
  const text = prompt.value.trim()
  if (!text) {
    errorMessage.value = '请输入报表描述'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const { data } = await generateFinanceReport({ text, save: savePrompt.value })
    if (data?.status === 'error') {
      errorMessage.value = data.message || '生成失败'
      return
    }
    report.value = data.data
    renderType.value = report.value.spec.chartType === 'number' ? 'number' : report.value.spec.chartType
    await refreshPrompts()
    renderChart()
  } catch (err) {
    errorMessage.value = err?.response?.data?.message || err?.message || '生成失败，请重试'
  } finally {
    loading.value = false
  }
}

const reRun = text => {
  prompt.value = text
  savePrompt.value = false
  generateReport()
}

const removePrompt = async id => {
  try {
    await deleteFinanceReportPrompt(id)
    ElMessage.success('已删除')
    refreshPrompts()
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || '删除失败')
  }
}

const refreshPrompts = async () => {
  try {
    const { data } = await listFinanceReportPrompts()
    prompts.value = data?.data || []
  } catch {
    prompts.value = []
  }
}

const buildOption = () => {
  const spec = report.value.spec
  const labels = report.value.rows.map(row => row.key)
  const colors = ['#6366f1', '#0ea5e9', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6']
  if (renderType.value === 'pie') {
    const metric = spec.metrics[0]
    return {
      tooltip: { trigger: 'item' },
      legend: { bottom: 0, type: 'scroll' },
      series: [{
        type: 'pie',
        radius: ['32%', '66%'],
        center: ['50%', '45%'],
        data: report.value.rows.map((row, index) => ({
          name: row.key,
          value: Number(row.values[metric.field] || 0),
          itemStyle: { color: colors[index % colors.length] }
        })),
        label: { formatter: '{b}\n{c}' }
      }]
    }
  }
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 56, right: 24, top: 40, bottom: 56 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { interval: 0, rotate: labels.length > 8 ? 30 : 0 }
    },
    yAxis: { type: 'value' },
    series: spec.metrics.map((metric, index) => ({
      name: metricLabel(metric),
      type: renderType.value,
      smooth: renderType.value === 'line',
      data: report.value.rows.map(row => Number(row.values[metric.field] || 0)),
      itemStyle: { color: colors[index % colors.length] }
    }))
  }
}

const renderChart = () => {
  nextTick(() => {
    if (chartInstance) {
      chartInstance.dispose()
      chartInstance = null
    }
    if (!chartRef.value) return
    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption(buildOption())
  })
}

watch(renderType, renderChart)

const exportCsv = () => {
  if (!report.value) return
  const headers = []
  for (const dim of report.value.spec.dimensions) headers.push(dimLabel(dim))
  for (const metric of report.value.spec.metrics) headers.push(metricLabel(metric))
  const lines = [headers.join(',')]
  for (const row of tableRows.value) {
    lines.push(headers.map((_, index) => {
      const keys = [...report.value.spec.dimensions, ...report.value.spec.metrics.map(m => `m:${m.field}`)]
      return row[keys[index]] ?? ''
    }).join(','))
  }
  const blob = new Blob(['\ufeff' + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${report.value.spec.title || '报表'}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

const formatNumber = value => Number(value || 0).toLocaleString('zh-CN', { maximumFractionDigits: 2 })
const formatTime = value => (value ? new Date(value).toLocaleString('zh-CN') : '')

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})

refreshPrompts()
</script>

<style scoped>
.finance-report-view {
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
  background-image: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(217, 249, 157, 0.22));
}

.eyebrow,
.section-header span,
.chips-label {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #6366f1;
}

h1,
h2 {
  margin: 0;
  color: #0f172a;
}

h1 {
  font-size: clamp(28px, 3vw, 40px);
}

h2 {
  font-size: 22px;
}

.description {
  color: #475569;
  line-height: 1.6;
  max-width: 60ch;
  margin: 8px 0 0;
}

.composer-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 24px;
}

.example-chips {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.chip {
  cursor: pointer;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.save-hint {
  color: #94a3b8;
  font-size: 13px;
}

.feedback-banner {
  padding: 12px 16px;
  border-radius: 14px;
  border: 1px solid rgba(220, 38, 38, 0.18);
  color: #b91c1c;
  background: rgba(254, 226, 226, 0.6);
  white-space: pre-wrap;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(280px, 340px) minmax(0, 1fr);
  gap: 20px;
  align-items: start;
}

.panel-card {
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.history-panel,
.canvas-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.prompt-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 10px;
}

.prompt-item {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(241, 245, 249, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.16);
}

.prompt-main {
  min-width: 0;
}

.prompt-text {
  margin: 0 0 6px;
  color: #334155;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.prompt-meta {
  color: #94a3b8;
  font-size: 12px;
}

.prompt-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
}

.canvas-tools {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.chart-canvas {
  width: 100%;
  height: 460px;
}

.kpi-number {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 0;
}

.kpi-label {
  color: #64748b;
  font-size: 15px;
}

.kpi-number strong {
  font-size: clamp(44px, 6vw, 72px);
  color: #0f172a;
}

.data-table {
  width: 100%;
}

.report-meta {
  display: flex;
  gap: 16px;
  color: #94a3b8;
  font-size: 12px;
  flex-wrap: wrap;
}

@media (max-width: 900px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}
</style>
