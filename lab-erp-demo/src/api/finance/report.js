import request from '@/utils/request'

export const generateFinanceReport = payload => request.post('/api/finance/report/generate', payload)

export const listFinanceReportPrompts = () => request.get('/api/finance/report/prompts')

export const deleteFinanceReportPrompt = id => request.delete(`/api/finance/report/prompts/${id}`)
