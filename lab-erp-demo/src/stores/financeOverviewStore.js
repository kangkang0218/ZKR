import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getFinanceOverview } from '@/api/finance/overview'
import { getFinanceErrorMessage, unwrapFinanceEnvelope } from '@/utils/financeAdapters'

export const useFinanceOverviewStore = defineStore('financeOverview', () => {
  const loading = ref(false)
  const error = ref('')
  const overview = ref(null)
  const envelope = ref(unwrapFinanceEnvelope(null))

  const hasOverview = computed(() => !!overview.value)

  const loadOverview = async params => {
    loading.value = true
    error.value = ''

    try {
      const response = await getFinanceOverview(params)
      envelope.value = unwrapFinanceEnvelope(response)
      overview.value = envelope.value.data
      return envelope.value
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
    overview,
    envelope,
    hasOverview,
    loadOverview
  }
})
