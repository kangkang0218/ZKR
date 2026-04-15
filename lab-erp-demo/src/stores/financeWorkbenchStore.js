import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  confirmDividendSheet,
  createAdjustment,
  executeClearing,
  getAdjustmentLogs,
  getClearingVentures,
  getCostBatchPreview,
  getDividendSheets,
  prepareDividendSheet,
  runCostBatch
} from '@/api/finance/workbench'
import {
  getFinanceErrorMessage,
  normalizeFinanceListPayload,
  normalizeFinanceMutationResult,
  unwrapFinanceEnvelope
} from '@/utils/financeAdapters'

const firstDefined = (...values) => values.find(value => value !== undefined && value !== null && value !== '')

const getClearingVentureRef = venture => venture?.venture || venture || {}

const getClearingVentureIdentity = venture => {
  const ventureRef = getClearingVentureRef(venture)
  const legacyVentureId = firstDefined(ventureRef.legacyVentureId, venture?.legacyVentureId)
  if (legacyVentureId !== undefined) {
    return {
      value: `legacy:${legacyVentureId}`,
      executeId: legacyVentureId,
      title: firstDefined(ventureRef.displayName, venture?.displayName, `Venture ${legacyVentureId}`)
    }
  }

  const projectId = firstDefined(ventureRef.projectId, venture?.projectId)
  if (projectId) {
    return {
      value: `project:${projectId}`,
      executeId: projectId,
      title: firstDefined(ventureRef.displayName, venture?.displayName, projectId)
    }
  }

  return {
    value: '',
    executeId: null,
    title: firstDefined(ventureRef.displayName, venture?.displayName, 'Venture --')
  }
}

const createClearingVentureOption = venture => {
  const identity = getClearingVentureIdentity(venture)
  return {
    ...identity,
    label: `${identity.title} · ${venture.ledgerMonth || '--'}`,
    caption: `${venture.status || 'PENDING'} · Cost ${venture.totalCost ?? '--'}`
  }
}

const getDividendSheetProjectId = sheet =>
  firstDefined(sheet?.projectId, sheet?.project_id, sheet?.ventureId, sheet?.venture_id, sheet?.sheetId, sheet?.id, '')

const getDividendSheetStatus = sheet => firstDefined(sheet?.status, sheet?.sheetStatus, sheet?.sheet_status, 'PENDING')

const getDividendSheetReference = sheet =>
  firstDefined(sheet?.referenceId, sheet?.sheetId, sheet?.sheet_id, sheet?.batchId, sheet?.id, null)

const getDividendSheetOperator = sheet =>
  firstDefined(sheet?.operator, sheet?.confirmedBy, sheet?.preparedBy, sheet?.createdBy, '')

const getDividendSheetRemark = sheet => firstDefined(sheet?.remark, sheet?.message, sheet?.notes, '')

const getDividendSheetTimestamp = sheet =>
  firstDefined(sheet?.confirmedAt, sheet?.confirmed_at, sheet?.updatedAt, sheet?.updated_at, sheet?.createdAt, sheet?.created_at, null)

const createDividendSheetDescriptor = (sheet, fallbackIndex = 0) => {
  const projectId = getDividendSheetProjectId(sheet)
  const referenceId = getDividendSheetReference(sheet)
  const rowKey = firstDefined(referenceId, projectId, `dividend-sheet-${fallbackIndex + 1}`)

  return {
    rowKey,
    referenceId,
    referenceLabel: referenceId ? `#${referenceId}` : `#${fallbackIndex + 1}`,
    projectId,
    status: getDividendSheetStatus(sheet),
    operator: getDividendSheetOperator(sheet),
    remark: getDividendSheetRemark(sheet),
    updatedAt: getDividendSheetTimestamp(sheet)
  }
}

export const useFinanceWorkbenchStore = defineStore('financeWorkbench', () => {
  const createClearingResultCards = payload => {
    if (!payload) {
      return []
    }

    return [
      {
        key: 'ledgerMonth',
        label: 'Ledger month',
        value: payload.ledgerMonth,
        kind: 'text',
        hint: 'Backend ledger period'
      },
      {
        key: 'status',
        label: 'Status',
        value: payload.status,
        kind: 'text',
        hint: 'Finance clearing status'
      },
      {
        key: 'finalRevenue',
        label: 'Final revenue',
        value: payload.finalRevenue,
        kind: 'currency',
        hint: 'Submitted or listed revenue'
      },
      {
        key: 'totalCost',
        label: 'Total cost',
        value: payload.totalCost,
        kind: 'currency',
        hint: 'Cost batch output'
      },
      {
        key: 'middlewareFee',
        label: 'Middleware fee',
        value: payload.middlewareFee,
        kind: 'currency',
        hint: 'Royalty and middleware charge'
      },
      {
        key: 'netProfit',
        label: 'Net profit',
        value: payload.netProfit,
        kind: 'currency',
        hint: 'Backend-computed result'
      },
      {
        key: 'lossTransferredToCompany',
        label: 'Loss transfer',
        value: firstDefined(payload.lossTransferredToCompany, payload.carryForwardLoss),
        kind: 'currency',
        hint: 'Company carry-over amount'
      },
      {
        key: 'clearedAt',
        label: 'Cleared at',
        value: payload.clearedAt,
        kind: 'datetime',
        hint: 'Execution timestamp'
      }
    ]
  }

  const loading = ref(false)
  const error = ref('')
  const ledgerMonth = ref('')
  const costPreview = ref(null)
  const costRunResult = ref(normalizeFinanceMutationResult(null))
  const clearingVentures = ref([])
  const selectedClearingVentureId = ref('')
  const clearingResult = ref(normalizeFinanceMutationResult(null))
  const dividendSheets = ref([])
  const dividendPrepareResult = ref(normalizeFinanceMutationResult(null))
  const dividendConfirmResult = ref(normalizeFinanceMutationResult(null))
  const dividendResult = ref(normalizeFinanceMutationResult(null))
  const adjustmentLogs = ref([])
  const adjustmentResult = ref(normalizeFinanceMutationResult(null))
  const selectedClearingVenture = computed(
    () => clearingVentures.value.find(venture => getClearingVentureIdentity(venture).value === selectedClearingVentureId.value) || null
  )
  const activeClearingPayload = computed(() =>
    clearingResult.value.completed ? clearingResult.value : selectedClearingVenture.value
  )
  const clearingResultCards = computed(() => createClearingResultCards(activeClearingPayload.value))

  const syncSelectedClearingVenture = () => {
    const hasSelected = clearingVentures.value.some(venture => getClearingVentureIdentity(venture).value === selectedClearingVentureId.value)
    if (hasSelected) {
      return
    }

    selectedClearingVentureId.value = getClearingVentureIdentity(clearingVentures.value[0]).value
  }

  const runAsync = async executor => {
    loading.value = true
    error.value = ''

    try {
      return await executor()
    } catch (requestError) {
      error.value = getFinanceErrorMessage(requestError)
      throw requestError
    } finally {
      loading.value = false
    }
  }

  const loadCostPreview = async (ventureId, targetLedgerMonth) => {
    ledgerMonth.value = targetLedgerMonth || ''
    return runAsync(async () => {
      const response = await getCostBatchPreview(ventureId, targetLedgerMonth)
      const envelope = unwrapFinanceEnvelope(response)
      costPreview.value = envelope.data
      return envelope
    })
  }

  const submitCostBatch = async payload => {
    ledgerMonth.value = payload?.ledgerMonth || ledgerMonth.value
    return runAsync(async () => {
      const response = await runCostBatch(payload)
      const envelope = unwrapFinanceEnvelope(response)
      costRunResult.value = normalizeFinanceMutationResult(envelope.data, envelope)
      return envelope
    })
  }

  const loadClearingVentures = async params =>
    runAsync(async () => {
      const response = await getClearingVentures(params)
      const envelope = unwrapFinanceEnvelope(response, [])
      clearingVentures.value = normalizeFinanceListPayload(envelope.data)
      syncSelectedClearingVenture()
      return envelope
    })

  const selectClearingVenture = ventureId => {
    selectedClearingVentureId.value = ventureId || ''
    clearingResult.value = normalizeFinanceMutationResult(null)
  }

  const submitClearing = async payload =>
    runAsync(async () => {
      const response = await executeClearing(payload)
      const envelope = unwrapFinanceEnvelope(response)
      clearingResult.value = normalizeFinanceMutationResult(envelope.data, envelope)
      return envelope
    })

  const loadDividendSheets = async params =>
    runAsync(async () => {
      const response = await getDividendSheets(params)
      const envelope = unwrapFinanceEnvelope(response, [])
      dividendSheets.value = normalizeFinanceListPayload(envelope.data)
      return envelope
    })

  const prepareDividend = async payload =>
    runAsync(async () => {
      const response = await prepareDividendSheet(payload)
      const envelope = unwrapFinanceEnvelope(response)
      dividendPrepareResult.value = normalizeFinanceMutationResult(envelope.data, envelope)
      dividendResult.value = dividendPrepareResult.value
      return envelope
    })

  const confirmDividend = async payload =>
    runAsync(async () => {
      const response = await confirmDividendSheet(payload)
      const envelope = unwrapFinanceEnvelope(response)
      dividendConfirmResult.value = normalizeFinanceMutationResult(envelope.data, envelope)
      dividendResult.value = dividendConfirmResult.value
      return envelope
    })

  const loadAdjustmentLogs = async params =>
    runAsync(async () => {
      const response = await getAdjustmentLogs(params)
      const envelope = unwrapFinanceEnvelope(response, [])
      adjustmentLogs.value = normalizeFinanceListPayload(envelope.data)
      return envelope
    })

  const submitAdjustment = async payload =>
    runAsync(async () => {
      const response = await createAdjustment(payload)
      const envelope = unwrapFinanceEnvelope(response)
      adjustmentResult.value = normalizeFinanceMutationResult(envelope.data, envelope)
      return envelope
    })

  return {
    loading,
    error,
    ledgerMonth,
    costPreview,
    costRunResult,
    clearingVentures,
    selectedClearingVentureId,
    selectedClearingVenture,
    clearingResult,
    clearingResultCards,
    dividendSheets,
    dividendPrepareResult,
    dividendConfirmResult,
    dividendResult,
    adjustmentLogs,
    adjustmentResult,
    loadCostPreview,
    submitCostBatch,
    loadClearingVentures,
    selectClearingVenture,
    submitClearing,
    loadDividendSheets,
    prepareDividend,
    confirmDividend,
    loadAdjustmentLogs,
    submitAdjustment
  }
})

export const useFinanceClearingSurface = () => {
  const store = useFinanceWorkbenchStore()

  const ventureOptions = computed(() => store.clearingVentures.map(createClearingVentureOption))

  const selectedVentureId = computed({
    get: () => store.selectedClearingVentureId,
    set: value => store.selectClearingVenture(value)
  })

  const ensureVenturesLoaded = async () => {
    if (!store.clearingVentures.length) {
      await store.loadClearingVentures()
    }
  }

  const executeSelected = async () => {
    const venture = store.selectedClearingVenture
    const ventureId = getClearingVentureIdentity(venture).executeId

    if (ventureId === undefined || ventureId === null) {
      return null
    }

    return store.submitClearing({
      ventureId,
      finalRevenue: venture.finalRevenue
    })
  }

  return {
    loading: computed(() => store.loading),
    error: computed(() => store.error),
    selectedVentureId,
    ventureOptions,
    resultCards: computed(() => store.clearingResultCards),
    resultSourceLabel: computed(() => {
      if (store.clearingResult.completed) {
        return 'Execution result'
      }

      if (store.selectedClearingVenture) {
        return 'Venture snapshot'
      }

      return 'Waiting for selection'
    }),
    canExecute: computed(() => {
      const venture = store.selectedClearingVenture
      return Boolean(getClearingVentureIdentity(venture).executeId !== null)
    }),
    ensureVenturesLoaded,
    reloadVentures: () => store.loadClearingVentures(),
    selectVenture: value => store.selectClearingVenture(value),
    executeSelected
  }
}

export const useFinanceDividendSurface = () => {
  const store = useFinanceWorkbenchStore()

  const createDividendResultCards = payload => {
    if (!payload?.completed) {
      return []
    }

    return [
      {
        key: 'projectId',
        label: 'Project',
        value: getDividendSheetProjectId(payload),
        kind: 'text',
        hint: 'Dividend target from backend payload'
      },
      {
        key: 'status',
        label: 'Status',
        value: getDividendSheetStatus(payload),
        kind: 'text',
        hint: 'Latest backend lifecycle status'
      },
      {
        key: 'referenceId',
        label: 'Reference',
        value: getDividendSheetReference(payload),
        kind: 'text',
        hint: 'Returned sheet or operation reference'
      },
      {
        key: 'operator',
        label: 'Operator',
        value: firstDefined(payload?.operator, payload?.confirmedBy, payload?.preparedBy, ''),
        kind: 'text',
        hint: 'Backend operator attribution'
      },
      {
        key: 'remark',
        label: 'Remark',
        value: firstDefined(payload?.remark, payload?.message, ''),
        kind: 'text',
        hint: 'Backend remark or success note'
      },
      {
        key: 'timestamp',
        label: 'Updated',
        value: getDividendSheetTimestamp(payload),
        kind: 'datetime',
        hint: 'Last dividend mutation timestamp'
      }
    ].filter(card => card.value !== undefined && card.value !== null && card.value !== '')
  }

  const ensureSheetsLoaded = async () => {
    if (!store.dividendSheets.length) {
      await store.loadDividendSheets()
    }
  }

  return {
    loading: computed(() => store.loading),
    error: computed(() => store.error),
    rows: computed(() => store.dividendSheets),
    prepareResult: computed(() => store.dividendPrepareResult),
    confirmResult: computed(() => store.dividendConfirmResult),
    latestResult: computed(() => {
      if (store.dividendConfirmResult?.completed) {
        return store.dividendConfirmResult
      }

      return store.dividendPrepareResult
    }),
    latestResultCards: computed(() => createDividendResultCards(store.dividendResult)),
    describeSheet: (sheet, index = 0) => createDividendSheetDescriptor(sheet, index),
    ensureSheetsLoaded,
    reloadSheets: params => store.loadDividendSheets(params),
    prepareSheet: payload => store.prepareDividend(payload),
    confirmSheet: payload => store.confirmDividend(payload)
  }
}
