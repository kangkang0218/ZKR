import request from '@/utils/request'

export const getFinanceOverview = params => request.get('/api/finance/statements', { params: params || {} })

const toDashboardShape = statementsEnvelope => {
  const payload = statementsEnvelope?.data || statementsEnvelope || {}
  const active = Array.isArray(payload.activeProjectAccounting) ? payload.activeProjectAccounting : []
  const buckets = {
    projects: [],
    products: [],
    research: []
  }

  active.forEach(item => {
    const flowType = String(item.flowType || '').toUpperCase()
    const normalized = {
      projectId: item.projectId,
      name: item.name,
      status: item.status,
      projectTier: item.projectTier || 'N',
      projectType: item.projectType || 'BUSINESS',
      budget: item.estimatedAsset,
      cost: item.estimatedLiability,
      members: item.members || [],
      managerId: item.primaryOwnerId || item.managerId || '',
      managerName: item.primaryOwnerName || item.managerName || '',
      primaryOwnerId: item.primaryOwnerId || item.managerId || '',
      primaryOwnerName: item.primaryOwnerName || item.managerName || '',
      description: item.description || ''
    }

    if (flowType === 'PROJECT') {
      buckets.projects.push(normalized)
      return
    }
    if (flowType === 'PRODUCT') {
      buckets.products.push(normalized)
      return
    }
    if (flowType === 'RESEARCH') {
      buckets.research.push(normalized)
    }
  })

  return buckets
}

export const getFinanceDashboard = async () => {
  try {
    const dashboard = await request.get('/api/projects/dashboard')
    if (dashboard?.projects || dashboard?.products || dashboard?.research) {
      return dashboard
    }
  } catch (error) {
    if (error?.response?.status !== 403) {
      throw error
    }
  }

  const statements = await getFinanceOverview()
  return toDashboardShape(statements)
}
