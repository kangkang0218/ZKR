<template>
  <section class="finance-view middleware-view">
    <header class="view-hero">
      <div>
        <p class="view-eyebrow">中间件仓库</p>
        <h1>ERP 中间件仓库</h1>
        <p class="view-description">展示中间件入库时间、创建人、被调用次数与调用价格。</p>
      </div>
      <div class="hero-note">
        <span class="hero-note-label">中间件数量</span>
        <strong>{{ rows.length }}</strong>
      </div>
    </header>

    <article class="surface-card">
      <div class="surface-header">
        <h2>仓库列表</h2>
        <el-button size="small" :loading="loading" @click="loadRows">刷新</el-button>
      </div>

      <div class="table-shell" v-if="rows.length">
        <table class="finance-table">
          <thead>
            <tr>
              <th>中间件</th>
              <th>入库时间</th>
              <th>入库创建人</th>
              <th>被调用次数</th>
              <th>调用价格</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.middlewareId">
              <td>{{ row.middlewareName || '-' }}</td>
              <td>{{ formatDate(row.createdAt) }}</td>
              <td>{{ row.creator || '未知' }}</td>
              <td>{{ row.invokeCount ?? 0 }}</td>
              <td>{{ formatPrice(row.callPrice, row.currency) }}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <el-empty v-else description="暂无中间件入库记录" />
    </article>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '@/utils/request'

const rows = ref([])
const loading = ref(false)

const formatDate = value => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'
  return date.toLocaleString('zh-CN', { hour12: false })
}

const formatPrice = (value, currency) => {
  if (value === null || value === undefined || value === '') return '-'
  const num = Number(value)
  if (Number.isNaN(num)) return '-'
  return `${currency || 'CNY'} ${num.toFixed(2)}`
}

const loadRows = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/middleware-hub/repository-view')
    rows.value = res?.data || res || []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRows()
})
</script>

<style scoped>
.finance-view { display: flex; flex-direction: column; gap: 20px; }
.view-hero, .surface-card { border: 1px solid rgba(148,163,184,.22); background: rgba(255,255,255,.84); border-radius: 24px; box-shadow: 0 20px 40px rgba(15,23,42,.08); }
.view-hero { display: flex; justify-content: space-between; gap: 24px; padding: 28px; background-image: linear-gradient(135deg, rgba(240,253,250,.96), rgba(239,246,255,.92)); }
.view-eyebrow, .hero-note-label { margin: 0; font-size: 12px; letter-spacing: .14em; text-transform: uppercase; color: #0f766e; }
.view-description { color: #475569; line-height: 1.6; }
.hero-note { min-width: 180px; padding: 16px; border-radius: 18px; background: rgba(255,255,255,.72); }
.surface-card { padding: 22px; }
.surface-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.table-shell { overflow-x: auto; }
.finance-table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 12px 10px; border-bottom: 1px solid rgba(226,232,240,.92); color: #475569; }
th { font-size: 12px; letter-spacing: .08em; text-transform: uppercase; color: #0f172a; }
</style>
