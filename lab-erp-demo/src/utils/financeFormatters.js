const moneyFormatter = new Intl.NumberFormat('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const compactMoneyFormatter = new Intl.NumberFormat('zh-CN', {
  notation: 'compact',
  minimumFractionDigits: 0,
  maximumFractionDigits: 1
})

const countFormatter = new Intl.NumberFormat('zh-CN', {
  maximumFractionDigits: 0
})

const signedMoneyFormatter = new Intl.NumberFormat('zh-CN', {
  signDisplay: 'exceptZero',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})

const percentFormatter = new Intl.NumberFormat('zh-CN', {
  minimumFractionDigits: 1,
  maximumFractionDigits: 1
})

export const formatFinanceCurrency = value => {
  const amount = Number(value ?? 0)
  if (Number.isNaN(amount)) {
    return '¥0.00'
  }
  return `¥${moneyFormatter.format(amount)}`
}

export const formatFinanceCompactCurrency = value => {
  const amount = Number(value ?? 0)
  if (Number.isNaN(amount)) {
    return '¥0'
  }
  return `¥${compactMoneyFormatter.format(amount)}`
}

export const formatFinancePercent = value => {
  const amount = Number(value ?? 0)
  if (Number.isNaN(amount)) {
    return '0.0%'
  }
  return `${percentFormatter.format(amount)}%`
}

export const formatFinanceCount = value => {
  const amount = Number(value ?? 0)
  if (Number.isNaN(amount)) {
    return '0'
  }
  return countFormatter.format(amount)
}

export const formatFinanceDelta = value => {
  const amount = Number(value ?? 0)
  if (Number.isNaN(amount)) {
    return '¥0.00'
  }
  return `¥${signedMoneyFormatter.format(amount)}`
}

export const formatFinanceMetricValue = (value, unit = 'CNY') => {
  if (unit === 'COUNT') {
    return formatFinanceCount(value)
  }

  if (unit === 'PERCENT') {
    return formatFinancePercent(value)
  }

  return formatFinanceCurrency(value)
}

export const formatFinanceText = (value, fallback = '--') => {
  if (value === undefined || value === null) {
    return fallback
  }

  const text = String(value).trim()
  return text ? text : fallback
}

export const formatFinanceEnumLabel = (dictionary, value, fallback = '--') => {
  const key = formatFinanceText(value, '')
  if (!key) {
    return fallback
  }

  return dictionary?.[key]?.label || key || fallback
}

export const formatFinanceDateTime = value => {
  if (!value) {
    return '--'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '--'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date)
}

export const formatFinanceLedgerMonth = value => {
  if (!value) {
    return '--'
  }

  const text = String(value)
  return /^\d{4}-\d{2}$/.test(text) ? text : '--'
}
