import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getFinanceTransactions, getFinanceWallets, saveBankBalance } from '@/api/finance/wallets'
import {
  getFinanceErrorMessage,
  normalizeFinanceListPayload,
  normalizeFinanceMutationResult,
  unwrapFinanceEnvelope
} from '@/utils/financeAdapters'

export const useFinanceWalletStore = defineStore('financeWallet', () => {
  const loading = ref(false)
  const error = ref('')
  const walletFilters = ref({})
  const transactionFilters = ref({})
  const wallets = ref([])
  const transactions = ref([])
  const walletEnvelope = ref(unwrapFinanceEnvelope(null, []))
  const transactionEnvelope = ref(unwrapFinanceEnvelope(null, []))
  const bankMutation = ref(normalizeFinanceMutationResult(null))

  const walletCount = computed(() => wallets.value.length)

  const loadWallets = async params => {
    loading.value = true
    error.value = ''
    walletFilters.value = { ...(params || {}) }

    try {
      const response = await getFinanceWallets(params)
      walletEnvelope.value = unwrapFinanceEnvelope(response, [])
      wallets.value = normalizeFinanceListPayload(walletEnvelope.value.data)
      return walletEnvelope.value
    } catch (requestError) {
      error.value = getFinanceErrorMessage(requestError)
      throw requestError
    } finally {
      loading.value = false
    }
  }

  const loadTransactions = async params => {
    loading.value = true
    error.value = ''
    transactionFilters.value = { ...(params || {}) }

    try {
      const response = await getFinanceTransactions(params)
      transactionEnvelope.value = unwrapFinanceEnvelope(response, [])
      transactions.value = normalizeFinanceListPayload(transactionEnvelope.value.data)
      return transactionEnvelope.value
    } catch (requestError) {
      error.value = getFinanceErrorMessage(requestError)
      throw requestError
    } finally {
      loading.value = false
    }
  }

  const submitBankBalance = async payload => {
    loading.value = true
    error.value = ''

    try {
      const response = await saveBankBalance(payload)
      const envelope = unwrapFinanceEnvelope(response)
      bankMutation.value = normalizeFinanceMutationResult(envelope.data, envelope)
      return envelope
    } catch (requestError) {
      error.value = getFinanceErrorMessage(requestError)
      throw requestError
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error,
    walletFilters,
    transactionFilters,
    wallets,
    transactions,
    walletEnvelope,
    transactionEnvelope,
    bankMutation,
    walletCount,
    loadWallets,
    loadTransactions,
    submitBankBalance
  }
})
