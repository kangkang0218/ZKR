export const FINANCE_RESPONSE_STATUS = Object.freeze({
  SUCCESS: 'success',
  ERROR: 'error'
})

export const FINANCE_RESPONSE_SUCCESS_ALIASES = Object.freeze({
  success: FINANCE_RESPONSE_STATUS.SUCCESS,
  true: FINANCE_RESPONSE_STATUS.SUCCESS,
  error: FINANCE_RESPONSE_STATUS.ERROR,
  false: FINANCE_RESPONSE_STATUS.ERROR
})

export const FINANCE_ADJUSTMENT_DIRECTION_OPTIONS = Object.freeze([
  { value: 'DEBIT', label: 'Debit', tone: 'danger' },
  { value: 'CREDIT', label: 'Credit', tone: 'success' }
])

export const FINANCE_DIVIDEND_STATUS_OPTIONS = Object.freeze([
  { value: 'PENDING', label: 'Pending', tone: 'warning' },
  { value: 'CONFIRMED', label: 'Confirmed', tone: 'success' }
])

export const FINANCE_WALLET_TRANSACTION_TYPE_OPTIONS = Object.freeze([
  { value: 'DIVIDEND', label: 'Dividend' },
  { value: 'ROYALTY', label: 'Royalty' },
  { value: 'WITHDRAWAL', label: 'Withdrawal' },
  { value: 'ADJUSTMENT', label: 'Adjustment' }
])

export const FINANCE_CASH_FLOW_DIRECTION_OPTIONS = Object.freeze([
  { value: 'IN', label: 'Cash In', tone: 'success' },
  { value: 'OUT', label: 'Cash Out', tone: 'danger' }
])

const toDictionary = options =>
  options.reduce((dictionary, option) => {
    dictionary[option.value] = option
    return dictionary
  }, {})

export const financeAdjustmentDirectionMap = Object.freeze(toDictionary(FINANCE_ADJUSTMENT_DIRECTION_OPTIONS))
export const financeDividendStatusMap = Object.freeze(toDictionary(FINANCE_DIVIDEND_STATUS_OPTIONS))
export const financeWalletTransactionTypeMap = Object.freeze(toDictionary(FINANCE_WALLET_TRANSACTION_TYPE_OPTIONS))
export const financeCashFlowDirectionMap = Object.freeze(toDictionary(FINANCE_CASH_FLOW_DIRECTION_OPTIONS))
