import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { chatWithFinanceAi, queryFinanceRag, rebuildFinanceRag } from '@/api/finance/ai'
import {
  getFinanceErrorMessage,
  normalizeFinanceAiResponse,
  normalizeFinanceListPayload,
  unwrapFinanceEnvelope
} from '@/utils/financeAdapters'

const firstDefined = (...values) => values.find(value => value !== undefined && value !== null && value !== '')

const wait = duration => new Promise(resolve => globalThis.setTimeout(resolve, duration))

const createConversationMessage = ({ id, role, text, pending = false, createdAt = '' }) => ({
  id,
  role,
  roleLabel: role === 'assistant' ? '助手' : role === 'system' ? '系统' : '提问者',
  title: role === 'assistant' ? (pending ? '正在生成回复' : '业务回复') : '业务问题',
  displayText: text,
  pending,
  createdAt
})

const createConversationId = (prefix, index) => `${prefix}-${Date.now()}-${index}`

const getEntryText = entry =>
  firstDefined(entry?.displayText, entry?.content, entry?.message, entry?.text, entry?.answer, entry?.summary, '')

const normalizeConversationEntries = history =>
  normalizeFinanceListPayload(history).map((entry, index) => {
    const role = firstDefined(entry?.role, entry?.sender, entry?.type, index % 2 === 0 ? 'user' : 'assistant')
    return createConversationMessage({
      id: firstDefined(entry?.id, entry?.messageId, createConversationId('history', index)),
      role,
      text: getEntryText(entry) || '未返回消息内容。',
      pending: false,
      createdAt: firstDefined(entry?.createdAt, entry?.updatedAt, '')
    })
  })

const getAssistantReplyText = payload => {
  const historyEntries = normalizeConversationEntries(payload?.history || payload?.messages || [])
  const lastAssistantEntry = [...historyEntries].reverse().find(entry => entry.role === 'assistant' && entry.displayText)
  if (lastAssistantEntry) {
    return lastAssistantEntry.displayText
  }

  return firstDefined(payload?.answer, payload?.reply, payload?.content, payload?.message, payload?.summary, '')
}

const createRagResultCard = (entry, index) => ({
  id: firstDefined(entry?.id, entry?.chunkId, entry?.docId, `rag-${index + 1}`),
  sourceLabel: firstDefined(entry?.source, entry?.documentName, entry?.docName, entry?.fileName, '业务知识库'),
  title: firstDefined(entry?.title, entry?.heading, entry?.sectionTitle, `结果 ${index + 1}`),
  snippet: firstDefined(entry?.snippet, entry?.content, entry?.summary, entry?.text, '该结果暂无摘要内容。')
})

const normalizeRagResultCards = payload => {
  const listPayload = normalizeFinanceListPayload(payload)
  const sourceList = listPayload.length ? listPayload : payload ? [payload] : []
  return sourceList.map(createRagResultCard)
}

export const ragShortcutPrompts = [
  {
    key: 'member-projects',
    label: '成员项目参与',
    prompt: '请总结每位成员发起了哪些项目、参与了哪些项目，并指出主要角色。'
  },
  {
    key: 'project-dialogue',
    label: '项目沟通重点',
    prompt: '请检索当前项目聊天里的关键讨论主题、主要参与者和最近推进重点。'
  },
  {
    key: 'repo-status',
    label: '仓库接入状态',
    prompt: '请总结各项目的 Git 仓库接入状态、分支、测试状态和关联项目。'
  }
]

export const useFinanceAiStore = defineStore('financeAi', () => {
  const loading = ref(false)
  const error = ref('')
  const contextMode = ref('global-business')
  const ragResults = ref([])
  const conversation = ref([])
  const lastEnvelope = ref(unwrapFinanceEnvelope(null))
  const fallbackMessage = ref('')
  const streaming = ref(false)

  const runAsync = async executor => {
    loading.value = true
    error.value = ''

    try {
      const response = await executor()
      lastEnvelope.value = unwrapFinanceEnvelope(response)
      return lastEnvelope.value
    } catch (requestError) {
      error.value = getFinanceErrorMessage(requestError)
      throw requestError
    } finally {
      loading.value = false
    }
  }

  const queryRag = async payload =>
    runAsync(async () => {
      const response = await queryFinanceRag(payload)
      const envelope = unwrapFinanceEnvelope(response, [])
      ragResults.value = normalizeRagResultCards(envelope.data)
      return response
    })

  const rebuildRag = async payload =>
    runAsync(async () => rebuildFinanceRag(payload))

  const sendMessage = async payload =>
    runAsync(async () => {
      const response = await chatWithFinanceAi(payload)
      const envelope = unwrapFinanceEnvelope(response)
      const aiResponse = normalizeFinanceAiResponse(envelope.data)
      const historyEntries = normalizeConversationEntries(aiResponse.history)
      const replyText = getAssistantReplyText(aiResponse)
      conversation.value = historyEntries.length
        ? historyEntries
        : [
            createConversationMessage({
              id: createConversationId('user', 0),
              role: 'user',
               text: firstDefined(payload?.message, payload?.prompt, '业务问题')
             }),
            createConversationMessage({
              id: createConversationId('assistant', 1),
              role: 'assistant',
               text: replyText || '暂未获取到助手回复。'
             })
          ]
      contextMode.value = aiResponse.contextMode || contextMode.value
      return response
    })

  const resetConversation = () => {
    conversation.value = []
    fallbackMessage.value = ''
    error.value = ''
  }

  const sendMessageProgressively = async payload => {
    const promptText = String(firstDefined(payload?.message, payload?.prompt, '')).trim()
    if (!promptText) {
      return null
    }

    fallbackMessage.value = ''
    const userMessage = createConversationMessage({
      id: createConversationId('user', conversation.value.length),
      role: 'user',
      text: promptText
    })
    const assistantMessage = createConversationMessage({
      id: createConversationId('assistant', conversation.value.length + 1),
      role: 'assistant',
      text: '正在生成业务分析，请稍候...',
      pending: true
    })

    conversation.value = [...conversation.value, userMessage, assistantMessage]
    streaming.value = true

    try {
      const requestPayload = {
        ...payload,
        message: promptText,
        prompt: promptText,
        contextMode: contextMode.value
      }
      let response
      try {
        await rebuildRag({ contextMode: contextMode.value })
        response = await sendMessage(requestPayload)
      } catch (firstError) {
        try {
          await rebuildRag({ contextMode: contextMode.value })
          response = await sendMessage(requestPayload)
        } catch {
          throw firstError
        }
      }
      const envelope = unwrapFinanceEnvelope(response)
      const aiResponse = normalizeFinanceAiResponse(envelope.data)
      const replyText = getAssistantReplyText(aiResponse)

      if (!replyText) {
        fallbackMessage.value = '未返回可读答案，请缩小问题范围或先进行业务检索后重试。'
        assistantMessage.displayText = fallbackMessage.value
        assistantMessage.pending = false
        conversation.value = [...conversation.value.slice(0, -1), assistantMessage]
        return response
      }

      assistantMessage.displayText = ''
      conversation.value = [...conversation.value.slice(0, -1), assistantMessage]

      const chunks = replyText.match(/.{1,48}(\s|$)/g) || [replyText]
      for (const chunk of chunks) {
        assistantMessage.displayText += chunk
        conversation.value = [...conversation.value.slice(0, -1), { ...assistantMessage }]
        await wait(30)
      }

      assistantMessage.pending = false
      conversation.value = [...conversation.value.slice(0, -1), { ...assistantMessage }]
      return response
    } catch (requestError) {
      fallbackMessage.value = '全局业务助手暂时不可用，本次仅查询未执行任何写入操作。'
      assistantMessage.displayText = fallbackMessage.value
      assistantMessage.pending = false
      conversation.value = [...conversation.value.slice(0, -1), assistantMessage]
      throw requestError
    } finally {
      streaming.value = false
    }
  }

  return {
    loading,
    error,
    contextMode,
    ragResults,
    conversation,
    lastEnvelope,
    fallbackMessage,
    streaming,
    queryRag,
    rebuildRag,
    sendMessage,
    resetConversation,
    sendMessageProgressively
  }
})

export const useFinanceAiChatSurface = () => {
  const store = useFinanceAiStore()
  const prompt = ref('')

  const sendPrompt = async () => {
    const nextPrompt = prompt.value.trim()
    if (!nextPrompt) {
      return null
    }

    prompt.value = ''

    try {
      return await store.sendMessageProgressively({ prompt: nextPrompt })
    } catch (error) {
      return null
    }
  }

  return {
    loading: computed(() => store.loading || store.streaming),
    error: computed(() => store.error),
    fallbackMessage: computed(() => store.fallbackMessage),
    contextModeLabel: computed(() => {
      const mode = store.contextMode || 'global-business'
      return mode === 'global-business' ? '全局业务' : mode
    }),
    messages: computed(() => store.conversation),
    prompt,
    sendPrompt,
    resetConversation: () => store.resetConversation()
  }
}

export const useFinanceRagSurface = () => {
  const store = useFinanceAiStore()
  const prompt = ref('')

  return {
    loading: computed(() => store.loading),
    error: computed(() => store.error),
    results: computed(() => store.ragResults),
    prompt,
    applyShortcut: shortcut => {
      prompt.value = shortcut?.prompt || ''
    },
    clearPrompt: () => {
      prompt.value = ''
    },
    search: async () => {
      const nextPrompt = prompt.value.trim()
      if (!nextPrompt) {
        return null
      }

      try {
        return await store.queryRag({ query: nextPrompt, prompt: nextPrompt, contextMode: 'global-business' })
      } catch (error) {
        return null
      }
    }
  }
}
