<template>
  <section class="rag-search-view">
    <div class="hero-card">
      <div>
        <p class="eyebrow">业务问答</p>
        <h1>全局业务检索</h1>
      </div>
      <div class="hero-metrics">
        <div class="metric-pill">
          <span class="metric-label">快捷问题</span>
          <strong>{{ shortcutPrompts.length }}</strong>
        </div>
        <div class="metric-pill">
          <span class="metric-label">命中结果</span>
          <strong>{{ ragSurface.results.length }}</strong>
        </div>
      </div>
    </div>

    <div v-if="ragSurface.error" class="feedback-banner error">{{ ragSurface.error }}</div>

    <div class="content-grid">
      <article class="panel-card">
        <header class="section-header">
          <div>
            <span>快捷问题</span>
            <h2>从全局业务问题开始</h2>
          </div>
        </header>

        <div class="shortcut-list">
          <button v-for="prompt in shortcutPrompts" :key="prompt.key" type="button" class="shortcut-chip" @click="ragSurface.applyShortcut(prompt)">
            {{ prompt.label }}
          </button>
        </div>

        <el-input
          v-model="ragSurface.prompt"
          type="textarea"
          :rows="7"
          resize="none"
          placeholder="请输入检索内容，例如：谁发起了哪些项目、最近项目聊天在讨论什么、哪个项目绑定了 Git 仓库"
        />

        <div class="action-row">
          <el-button type="primary" :loading="ragSurface.loading" @click="ragSurface.search">开始检索</el-button>
          <el-button :disabled="!ragSurface.prompt" @click="ragSurface.clearPrompt">清空</el-button>
        </div>
      </article>

      <article class="panel-card">
        <header class="section-header">
          <div>
            <span>检索结果</span>
            <h2>引用与摘要</h2>
          </div>
        </header>

        <div class="result-list">
          <article v-for="result in ragSurface.results" :key="result.id" class="result-card">
            <span>{{ result.sourceLabel }}</span>
            <strong>{{ result.title }}</strong>
            <p>{{ result.snippet }}</p>
          </article>
        </div>

        <el-empty v-if="!ragSurface.results.length" description="执行快捷问题或自定义检索后，将在此展示全局业务上下文结果" />
      </article>
    </div>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { ragShortcutPrompts, useFinanceRagSurface } from '@/stores/financeAiStore'

const ragSurface = reactive(useFinanceRagSurface())
const shortcutPrompts = ragShortcutPrompts
</script>

<style scoped>
.rag-search-view {
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
.metric-pill strong,
.result-card strong {
  margin: 0;
  color: #0f172a;
}

h1 {
  font-size: clamp(28px, 3vw, 40px);
}

h2 {
  font-size: 22px;
}

.result-card p {
  color: #475569;
  line-height: 1.6;
}


.hero-metrics,
.content-grid,
.shortcut-list,
.result-list {
  display: grid;
  gap: 12px;
}

.hero-metrics {
  min-width: 180px;
}

.metric-pill,
.feedback-banner,
.shortcut-chip,
.result-card {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.metric-pill,
.result-card {
  padding: 16px;
}

.metric-pill {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feedback-banner {
  padding: 14px 18px;
}

.feedback-banner.info {
  border: 1px solid rgba(8, 145, 178, 0.18);
  color: #0f766e;
}

.feedback-banner.error {
  border: 1px solid rgba(220, 38, 38, 0.18);
  color: #b91c1c;
}

.content-grid {
  grid-template-columns: minmax(300px, 380px) minmax(0, 1fr);
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

.shortcut-list {
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  margin-bottom: 16px;
}

.shortcut-chip {
  border: 1px solid rgba(15, 118, 110, 0.14);
  padding: 12px 14px;
  text-align: left;
  cursor: pointer;
  color: #0f172a;
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.shortcut-chip:hover {
  border-color: rgba(15, 118, 110, 0.36);
  transform: translateY(-1px);
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 16px;
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
