import request from '@/utils/request'

export const rebuildFinanceRag = payload => request.post('/api/rag/push', payload)

export const queryFinanceRag = payload => request.post('/api/rag/query', payload)

export const chatWithFinanceAi = payload => request.post('/api/ai/chat', payload)
