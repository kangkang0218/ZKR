<template>
  <section class="finance-ai-chat">
    <div class="hero-card">
      <div>
        <p class="eyebrow">智能助手</p>
        <h1>全局业务智能助手</h1>
      </div>
      <div class="hero-metrics">
        <div class="metric-pill">
          <span class="metric-label">上下文模式</span>
          <strong>{{ aiSurface.contextModeLabel }}</strong>
        </div>
        <div class="metric-pill">
          <span class="metric-label">会话条数</span>
          <strong>{{ aiSurface.messages.length }}</strong>
        </div>
      </div>
    </div>

    <div v-if="aiSurface.fallbackMessage" class="feedback-banner warning">{{ aiSurface.fallbackMessage }}</div>
    <div v-if="aiSurface.error" class="feedback-banner error">{{ aiSurface.error }}</div>

    <div class="content-grid">
      <article class="panel-card chat-panel">
        <header class="section-header">
          <div>
            <span>会话窗口</span>
            <h2>业务问答记录</h2>
          </div>
        </header>

        <div class="message-list">
          <article v-for="message in aiSurface.messages" :key="message.id" class="message-card" :class="message.role">
            <span>{{ message.roleLabel }}</span>
            <strong>{{ message.title }}</strong>
            <div class="message-body" :class="{ 'message-body-structured': message.role === 'assistant' }">
              <template v-for="(block, blockIndex) in formatMessageBlocks(message)" :key="`${message.id}-${blockIndex}`">
                <h3 v-if="block.type === 'heading'" class="message-heading">{{ block.text }}</h3>
                <p v-else-if="block.type === 'paragraph'" class="message-paragraph">{{ block.text }}</p>
                <ul v-else-if="block.type === 'list'" class="message-list-block">
                  <li v-for="(item, itemIndex) in block.items" :key="`${message.id}-${blockIndex}-${itemIndex}`">{{ item }}</li>
                </ul>
              </template>
            </div>
          </article>
        </div>

        <el-empty v-if="!aiSurface.messages.length" description="请输入业务问题，开始对话" />
      </article>

      <article class="panel-card composer-panel">
        <header class="section-header">
          <div>
            <span>输入区</span>
            <h2>提交业务问题</h2>
          </div>
        </header>

        <el-input
          v-model="aiSurface.prompt"
          type="textarea"
          :rows="8"
          resize="none"
          placeholder="例如：谁发起了这个项目？最近项目群在讨论什么？哪个仓库测试状态异常？"
        />

        <div class="action-row">
          <el-button type="primary" :loading="aiSurface.loading" @click="aiSurface.sendPrompt">发送</el-button>
          <el-button :disabled="!aiSurface.messages.length" @click="aiSurface.resetConversation">清空会话</el-button>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { reactive } from 'vue'
import { useFinanceAiChatSurface } from '@/stores/financeAiStore'

const aiSurface = reactive(useFinanceAiChatSurface())

const STRUCTURED_HEADINGS = new Set(['结论', '依据', '建议'])

const formatMessageBlocks = message => {
  const text = String(message?.displayText || '').replace(/\r\n/g, '\n')
  if (!text) {
    return [{ type: 'paragraph', text: '' }]
  }

  const lines = text.split('\n')
  const blocks = []
  let listItems = []

  const flushList = () => {
    if (listItems.length) {
      blocks.push({ type: 'list', items: [...listItems] })
      listItems = []
    }
  }

  for (const rawLine of lines) {
    const line = rawLine.trim()
    if (!line) {
      flushList()
      continue
    }

    if (STRUCTURED_HEADINGS.has(line)) {
      flushList()
      blocks.push({ type: 'heading', text: line })
      continue
    }

    if (line.startsWith('- ')) {
      listItems.push(line.slice(2).trim())
      continue
    }

    flushList()
    blocks.push({ type: 'paragraph', text: line })
  }

  flushList()
  return blocks.length ? blocks : [{ type: 'paragraph', text }]
}
</script>

<style scoped>
.finance-ai-chat {
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
.message-card span {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

h1,
h2,
.metric-pill strong,
.message-card strong {
  margin: 0;
  color: #0f172a;
}

h1 {
  font-size: clamp(28px, 3vw, 40px);
}

h2 {
  font-size: 22px;
}

.message-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 10px;
}

.message-heading {
  margin: 4px 0 0;
  font-size: 16px;
  line-height: 1.3;
  color: #0f172a;
}

.message-paragraph {
  margin: 0;
  color: #475569;
  line-height: 1.6;
  font-size: 15px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-list-block {
  margin: 0;
  padding-left: 18px;
  color: #334155;
  display: grid;
  gap: 8px;
}

.message-list-block li {
  line-height: 1.7;
}

.message-body-structured .message-heading:first-child {
  margin-top: 0;
}


.hero-metrics,
.content-grid,
.message-list {
  display: grid;
  gap: 12px;
}

.hero-metrics {
  min-width: 180px;
}

.metric-pill,
.feedback-banner,
.message-card {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.metric-pill,
.message-card {
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

.feedback-banner.warning {
  border: 1px solid rgba(217, 119, 6, 0.18);
  color: #b45309;
}

.feedback-banner.error {
  border: 1px solid rgba(220, 38, 38, 0.18);
  color: #b91c1c;
}

.content-grid {
  grid-template-columns: minmax(0, 1.35fr) minmax(300px, 380px);
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

.chat-panel,
.composer-panel {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-card.user {
  background: linear-gradient(160deg, rgba(226, 232, 240, 0.95), rgba(241, 245, 249, 0.82));
}

.message-card.assistant {
  background: linear-gradient(160deg, rgba(236, 253, 245, 0.96), rgba(240, 253, 250, 0.88));
}

.message-card.system {
  background: linear-gradient(160deg, rgba(255, 247, 237, 0.96), rgba(254, 249, 195, 0.88));
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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
