import { FINANCE_RESPONSE_STATUS, FINANCE_RESPONSE_SUCCESS_ALIASES } from '@/utils/financeEnums'

const isPlainObject = value => Object.prototype.toString.call(value) === '[object Object]'

const firstDefined = (...values) => values.find(value => value !== undefined && value !== null)

const toCamelCase = value =>
  typeof value === 'string'
    ? value.replace(/[_-]([a-z])/gi, (_, letter) => letter.toUpperCase())
    : value

const normalizeFinanceDataShape = payload => {
  if (Array.isArray(payload)) {
    return payload.map(item => normalizeFinanceDataShape(item))
  }

  if (!isPlainObject(payload)) {
    return payload
  }

  return Object.entries(payload).reduce((normalized, [key, value]) => {
    const nextValue = normalizeFinanceDataShape(value)
    normalized[key] = nextValue

    const camelKey = toCamelCase(key)
    if (!(camelKey in normalized)) {
      normalized[camelKey] = nextValue
    }

    return normalized
  }, {})
}

const defaultMeta = () => ({
  page: null,
  size: null,
  total: null,
  totalPages: null,
  traceId: null,
  timestamp: null
})

export const normalizeFinanceMeta = envelope => ({
  ...defaultMeta(),
  ...(normalizeFinanceDataShape(envelope?.meta) || {}),
  totalPages: firstDefined(envelope?.meta?.totalPages, envelope?.meta?.total_pages, null),
  traceId: firstDefined(envelope?.traceId, envelope?.trace_id, envelope?.meta?.traceId, envelope?.meta?.trace_id, null),
  timestamp: firstDefined(envelope?.timestamp, envelope?.meta?.timestamp, null)
})

export const unwrapFinanceEnvelope = (envelope, fallbackData = null) => {
  const success =
    typeof envelope?.success === 'boolean'
      ? envelope.success
      : FINANCE_RESPONSE_SUCCESS_ALIASES[String(envelope?.status || '').toLowerCase()] === FINANCE_RESPONSE_STATUS.SUCCESS
  const status = envelope?.status || (success ? FINANCE_RESPONSE_STATUS.SUCCESS : FINANCE_RESPONSE_STATUS.ERROR)
  const data = normalizeFinanceDataShape(envelope?.data ?? fallbackData)

  return {
    status,
    success,
    ok: status === FINANCE_RESPONSE_STATUS.SUCCESS,
    message: envelope?.message || '',
    data,
    meta: normalizeFinanceMeta(envelope),
    timestamp: firstDefined(envelope?.timestamp, envelope?.meta?.timestamp, null),
    traceId: firstDefined(envelope?.traceId, envelope?.trace_id, envelope?.meta?.traceId, envelope?.meta?.trace_id, null)
  }
}

export const normalizeFinanceListPayload = payload => {
  if (Array.isArray(payload)) {
    return normalizeFinanceDataShape(payload)
  }

  if (Array.isArray(payload?.items)) {
    return normalizeFinanceDataShape(payload.items)
  }

  if (Array.isArray(payload?.rows)) {
    return normalizeFinanceDataShape(payload.rows)
  }

  if (Array.isArray(payload?.records)) {
    return normalizeFinanceDataShape(payload.records)
  }

  if (Array.isArray(payload?.content)) {
    return normalizeFinanceDataShape(payload.content)
  }

  if (Array.isArray(payload?.wallets)) {
    return normalizeFinanceDataShape(payload.wallets)
  }

  if (Array.isArray(payload?.transactions)) {
    return normalizeFinanceDataShape(payload.transactions)
  }

  if (Array.isArray(payload?.results)) {
    return normalizeFinanceDataShape(payload.results)
  }

  return []
}

export const normalizeFinanceMutationResult = (payload, envelope = null) => {
  const normalizedPayload = normalizeFinanceDataShape(payload) || {}
  const hasMutationPayload = Boolean(
    envelope || (isPlainObject(normalizedPayload) && Object.keys(normalizedPayload).length > 0)
  )

  return {
    ...normalizedPayload,
    success: firstDefined(envelope?.success, normalizedPayload.success, false),
    completed: hasMutationPayload,
    idle: !hasMutationPayload,
    operation: firstDefined(normalizedPayload.operation, normalizedPayload.status, ''),
    referenceId:
      firstDefined(
        normalizedPayload.referenceId,
        normalizedPayload.batchId,
        normalizedPayload.clearingSheetId,
        normalizedPayload.adjustment?.id,
        normalizedPayload.id
      ) ?? null,
    message: firstDefined(normalizedPayload.message, envelope?.message, '')
  }
}

export const normalizeFinanceAiResponse = payload => {
  const normalizedPayload = normalizeFinanceDataShape(payload) || {}

  return {
    ...normalizedPayload,
    history: normalizeFinanceListPayload(normalizedPayload.history || normalizedPayload.messages || []),
    contextMode: firstDefined(normalizedPayload.contextMode, 'finance')
  }
}

export const normalizeFinanceLedgerMonthPayload = payload => ({
  ledger_month: firstDefined(payload?.ledger_month, payload?.ledgerMonth, ''),
  rerun_existing_month: Boolean(
    firstDefined(payload?.rerun_existing_month, payload?.rerunExistingMonth, payload?.replace_existing_month, false)
  )
})

export const normalizeFinanceClearingPayload = payload => ({
  venture_id: firstDefined(payload?.venture_id, payload?.ventureId, null),
  final_revenue: firstDefined(payload?.final_revenue, payload?.finalRevenue, null)
})

export const normalizeFinanceDividendPayload = payload => ({
  projectId: firstDefined(payload?.projectId, payload?.ventureId, payload?.venture_id, ''),
  operator: payload?.operator || '',
  remark: payload?.remark || ''
})

export const normalizeFinanceAdjustmentPayload = payload => ({
  userId: firstDefined(payload?.userId, payload?.user_id, ''),
  subject: payload?.subject || '',
  direction: payload?.direction || '',
  amount: firstDefined(payload?.amount, null),
  remark: payload?.remark || '',
  reason: payload?.reason || '',
  sourceId: firstDefined(payload?.sourceId, payload?.source_id, null),
  refDocNo: firstDefined(payload?.refDocNo, payload?.ref_doc_no, ''),
  operator: payload?.operator || ''
})

export const normalizeFinanceBankBalancePayload = payload => ({
  balance: firstDefined(payload?.balance, null),
  operator: payload?.operator || '',
  remark: payload?.remark || ''
})

export const normalizeFinanceTransactionQuery = params => ({
  limit: firstDefined(params?.limit, null),
  userId: firstDefined(params?.userId, params?.user_id, null),
  type: firstDefined(params?.type, params?.transactionType, params?.transaction_type, null),
  direction: firstDefined(params?.direction, params?.cashFlowDirection, params?.cash_flow_direction, null),
  sourceTable: firstDefined(params?.sourceTable, params?.source_table, null)
})

export const getFinanceErrorMessage = error => {
  const status = error?.response?.status
  const backendMessage = error?.response?.data?.message

  if (backendMessage) {
    return backendMessage
  }
  if (status === 401) {
    return 'Finance session expired. Please log in again.'
  }
  if (status === 403) {
    return 'Current role cannot access this finance route.'
  }
  if (status === 404) {
    return 'Finance resource is not available yet.'
  }
  if (status === 409) {
    return 'Finance operation conflicted with the latest backend state.'
  }
  if (status === 422) {
    return 'Finance request validation failed.'
  }
  if (status === 429) {
    return 'Finance request rate limit reached. Please retry later.'
  }

  return error?.message || 'Finance request failed.'
}

export const createFinanceAsyncState = (overrides = {}) => ({
  loading: false,
  error: '',
  envelope: unwrapFinanceEnvelope(null),
  ...overrides
})
