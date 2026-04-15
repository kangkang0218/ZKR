import request from '@/utils/request'
import { normalizeFinanceBankBalancePayload, normalizeFinanceTransactionQuery } from '@/utils/financeAdapters'

export const getFinanceWallets = params => request.get('/api/finance/wallets', { params })

export const getFinanceTransactions = params => request.get('/api/finance/transactions', {
  params: normalizeFinanceTransactionQuery(params)
})

export const saveBankBalance = payload => request.post('/api/finance/bank_balance', normalizeFinanceBankBalancePayload(payload))
