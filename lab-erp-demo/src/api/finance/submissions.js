import request from '@/utils/request'

export const getFinanceSubmissionCenter = () => request.get('/api/finance/submissions')

export const downloadFinanceSubmissionInvoice = submissionId => request.get(`/api/finance/submissions/${submissionId}/invoice`, {
  responseType: 'blob'
})
