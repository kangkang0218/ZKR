<template>
  <div ref="rootRef" class="command-launcher" :class="{ expanded: isExpanded, busy: interpreting || executing }">
    <button
      type="button"
      class="command-trigger"
      :title="isExpanded ? '收起业务命令窗口' : '打开业务命令窗口'"
      @click="toggleLauncher"
    >
      ✦
    </button>

    <transition name="command-pill">
      <form v-if="isExpanded" class="command-input-shell" @submit.prevent="submitInterpret">
        <input
          ref="inputRef"
          v-model.trim="prompt"
          class="command-input"
          type="text"
          placeholder="例如：把蒋之骏加入项目最终测试"
          :disabled="interpreting || executing"
          @keydown.esc.prevent="collapseLauncher"
        >
        <button type="submit" class="command-submit" :disabled="!prompt || interpreting || executing">
          {{ interpreting ? '解析中' : '发送' }}
        </button>
      </form>
    </transition>

    <transition name="command-panel-fade">
      <div v-if="isExpanded" class="command-panel">
        <div v-if="errorMessage" class="command-banner error">{{ errorMessage }}</div>

        <template v-else-if="resultSummary">
          <div class="command-badge success">执行完成</div>
          <h4>{{ resultSummary }}</h4>
          <div class="command-actions compact">
            <button v-if="navigateTo" type="button" class="panel-btn primary" @click="openResult">查看结果</button>
            <button type="button" class="panel-btn" @click="resetPanel">继续输入</button>
          </div>
        </template>

        <template v-else-if="preview">
          <div class="command-badge">命令预览</div>
          <h4>{{ preview.title }}</h4>
          <p class="command-summary">{{ preview.summary }}</p>

          <div v-if="preview.previewLines?.length" class="command-section">
            <strong>执行内容</strong>
            <ul>
              <li v-for="(line, index) in preview.previewLines" :key="`line-${index}`">{{ line }}</li>
            </ul>
          </div>

          <div v-if="preview.missingFields?.length" class="command-section warning">
            <strong>仍需补充</strong>
            <ul>
              <li v-for="(field, index) in preview.missingFields" :key="`missing-${index}`">{{ field }}</li>
            </ul>
          </div>

          <div v-if="preview.warnings?.length" class="command-section muted">
            <strong>执行提醒</strong>
            <ul>
              <li v-for="(warning, index) in preview.warnings" :key="`warning-${index}`">{{ warning }}</li>
            </ul>
          </div>

          <div class="command-actions">
            <button type="button" class="panel-btn" @click="submitInterpret">重新解析</button>
            <button type="button" class="panel-btn primary" :disabled="!preview.canExecute || executing" @click="confirmExecute">
              {{ executing ? '执行中' : '确认执行' }}
            </button>
          </div>
        </template>

        <template v-else>
          <div class="command-badge">业务命令</div>
          <h4>一句话执行 ERP 操作</h4>
          <p class="command-summary">先输入一句自然语言，系统会先给出执行预览，再由你确认落库。</p>
          <div class="command-section muted">
            <strong>当前支持</strong>
            <ul>
              <li>发起产品 / 交付 / 科研项目</li>
              <li>增加项目成员</li>
              <li>推进项目流程</li>
            </ul>
          </div>
        </template>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

const rootRef = ref(null)
const inputRef = ref(null)
const isExpanded = ref(false)
const prompt = ref('')
const preview = ref(null)
const interpreting = ref(false)
const executing = ref(false)
const errorMessage = ref('')
const resultSummary = ref('')
const navigateTo = ref('')

const currentProjectId = computed(() => String(route.params?.id || ''))

const focusInput = async () => {
  await nextTick()
  inputRef.value?.focus()
}

const toggleLauncher = async () => {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value) {
    await focusInput()
  }
}

const collapseLauncher = () => {
  isExpanded.value = false
}

const resetPanel = async () => {
  preview.value = null
  errorMessage.value = ''
  resultSummary.value = ''
  navigateTo.value = ''
  await focusInput()
}

const submitInterpret = async () => {
  if (!prompt.value || interpreting.value) return
  interpreting.value = true
  errorMessage.value = ''
  resultSummary.value = ''
  navigateTo.value = ''
  try {
    preview.value = await request.post('/api/command/interpret', {
      text: prompt.value,
      currentRoute: route.fullPath,
      currentProjectId: currentProjectId.value
    })
  } catch (error) {
    preview.value = null
    errorMessage.value = error.response?.data?.message || error.message || '命令解析失败'
  } finally {
    interpreting.value = false
  }
}

const confirmExecute = async () => {
  if (!preview.value?.canExecute || executing.value) return
  executing.value = true
  errorMessage.value = ''
  try {
    const result = await request.post('/api/command/execute', {
      actionType: preview.value.actionType,
      payload: preview.value.payload
    })
    resultSummary.value = result?.resultSummary || '命令已执行'
    navigateTo.value = result?.navigateTo || ''
    ElMessage.success(resultSummary.value)
  } catch (error) {
    errorMessage.value = error.response?.data?.message || error.message || '命令执行失败'
  } finally {
    executing.value = false
  }
}

const openResult = () => {
  if (!navigateTo.value) return
  router.push(navigateTo.value)
  collapseLauncher()
}

const handleDocumentClick = event => {
  if (!isExpanded.value || !rootRef.value) return
  if (rootRef.value.contains(event.target)) return
  collapseLauncher()
}

const handleShortcut = event => {
  if ((event.metaKey || event.ctrlKey) && String(event.key || '').toLowerCase() === 'k') {
    event.preventDefault()
    if (!isExpanded.value) {
      isExpanded.value = true
      focusInput()
      return
    }
    collapseLauncher()
  }
}

onMounted(() => {
  document.addEventListener('mousedown', handleDocumentClick)
  window.addEventListener('keydown', handleShortcut)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentClick)
  window.removeEventListener('keydown', handleShortcut)
})
</script>

<style scoped>
.command-launcher {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  min-width: 40px;
}

.command-trigger,
.command-submit,
.panel-btn {
  border: none;
  cursor: pointer;
}

.command-trigger {
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(96, 165, 250, 0.26), rgba(163, 230, 53, 0.2));
  color: #f8fafc;
  font-size: 16px;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.28);
}

.command-input-shell {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 360px;
  margin-left: 10px;
  padding: 4px 6px 4px 14px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.92);
  border: 1px solid rgba(148, 163, 184, 0.2);
  box-shadow: 0 12px 28px rgba(2, 6, 23, 0.36);
}

.command-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  color: #f8fafc;
  font-size: 14px;
}

.command-input::placeholder {
  color: rgba(226, 232, 240, 0.56);
}

.command-submit {
  min-width: 66px;
  height: 30px;
  border-radius: 999px;
  background: linear-gradient(135deg, #38bdf8, #22c55e);
  color: #020617;
  font-weight: 700;
  padding: 0 12px;
}

.command-submit:disabled,
.panel-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.command-panel {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  width: 420px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.96);
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: 0 24px 44px rgba(2, 6, 23, 0.42);
  color: #e2e8f0;
}

.command-badge {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #c4f1e7;
  background: rgba(13, 148, 136, 0.18);
}

.command-badge.success {
  color: #dcfce7;
  background: rgba(34, 197, 94, 0.18);
}

.command-panel h4 {
  margin: 12px 0 8px;
  font-size: 18px;
  color: #f8fafc;
}

.command-summary {
  margin: 0;
  color: #cbd5e1;
  line-height: 1.6;
}

.command-section {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(148, 163, 184, 0.12);
}

.command-section strong {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: #f8fafc;
}

.command-section ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 8px;
  color: #cbd5e1;
}

.command-section.warning strong {
  color: #fbbf24;
}

.command-section.muted strong {
  color: #93c5fd;
}

.command-banner {
  padding: 12px 14px;
  border-radius: 14px;
  font-size: 13px;
  line-height: 1.5;
}

.command-banner.error {
  background: rgba(127, 29, 29, 0.4);
  color: #fecaca;
}

.command-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

.command-actions.compact {
  justify-content: flex-start;
}

.panel-btn {
  min-height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.14);
  color: #e2e8f0;
  font-weight: 600;
}

.panel-btn.primary {
  background: linear-gradient(135deg, #38bdf8, #22c55e);
  color: #020617;
}

.command-panel-fade-enter-active,
.command-panel-fade-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.command-panel-fade-enter-from,
.command-panel-fade-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}

.command-pill-enter-active,
.command-pill-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease, width 0.18s ease;
  transform-origin: right center;
}

.command-pill-enter-from,
.command-pill-leave-to {
  opacity: 0;
  transform: translateX(10px) scaleX(0.88);
}
</style>
