<template>
  <div class="finance-shell" :class="{ 'finance-shell-fullscreen': isOverviewFullscreen }">
    <aside v-if="!isOverviewFullscreen" class="finance-sidebar">
      <div class="brand-card">
        <h1>AI智能财务中心</h1>
      </div>

      <nav class="finance-nav">
        <router-link
          v-for="item in financeNavigationItems"
          :key="item.routeName"
          :to="item.path"
          class="nav-card"
          active-class="is-active"
        >
          <strong>{{ item.label }}</strong>
        </router-link>
      </nav>
    </aside>

    <main class="finance-main" :class="{ 'finance-main-fullscreen': isOverviewFullscreen }">
      <div v-if="!isOverviewFullscreen" class="finance-topline">
        <div class="topline-right">
          <button type="button" class="trace-card trace-card-btn" :disabled="isSyncing" @click="triggerGlobalRefresh">
            <span>财务数据状态</span>
            <strong>{{ isSyncing ? '同步中...' : `最后更新时间 ${lastSyncedLabel}` }}</strong>
          </button>
          <el-dropdown trigger="click" @command="handleAvatarCommand">
            <div class="user-trigger">
              <img :src="displayAvatar" class="avatar-hex">
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="switch-view">切换视角</el-dropdown-item>
                <el-dropdown-item command="logout" style="color:#FF3B30">退出</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div class="finance-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { financeNavigationItems } from '@/router/financeRoutes'
import { rebuildFinanceRag } from '@/api/finance/ai'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const lastSyncedAt = ref(localStorage.getItem('finance-dashboard-last-updated') || '')
const isSyncing = ref(false)

const displayAvatar = computed(() => userStore.activeUserInfo?.hiddenAvatar
  ? 'https://api.dicebear.com/7.x/shapes/svg?seed=masked'
  : (userStore.activeUserInfo?.avatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'))
const isOverviewFullscreen = computed(() => route.path === '/finance/overview' && route.query?.fullscreen === '1')

const lastSyncedLabel = computed(() => {
  if (!lastSyncedAt.value) return '未同步'
  const date = new Date(lastSyncedAt.value)
  if (Number.isNaN(date.getTime())) return '未同步'
  return date.toLocaleString('zh-CN', { hour12: false })
})

const syncDashboardTimestamp = () => {
  lastSyncedAt.value = localStorage.getItem('finance-dashboard-last-updated') || ''
}

const triggerGlobalRefresh = async () => {
  if (isSyncing.value) return
  isSyncing.value = true
  try {
    await rebuildFinanceRag()
    const timestamp = new Date().toISOString()
    localStorage.setItem('finance-dashboard-last-updated', timestamp)
    window.dispatchEvent(new Event('finance-dashboard-updated'))
    window.dispatchEvent(new Event('finance-global-refresh'))
    syncDashboardTimestamp()
    ElMessage.success('财务系统已更新')
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || error?.message || '财务系统更新失败')
  } finally {
    isSyncing.value = false
  }
}

const requestBrowserFullscreen = async () => {
  const isWindows = /windows/i.test(navigator.userAgent)
  if (!isWindows) return
  const element = document.documentElement
  const enter = element.requestFullscreen || element.webkitRequestFullscreen || element.msRequestFullscreen
  if (typeof enter === 'function') {
    try {
      await enter.call(element)
    } catch {
      // browser policy may block fullscreen
    }
  }
}

const handleAvatarCommand = async command => {
  if (command === 'switch-view') {
    if (route.path.startsWith('/finance/overview')) {
      await router.push('/finance/classic')
    } else {
      await requestBrowserFullscreen()
      await router.push({ path: '/finance/overview', query: { fullscreen: '1' } })
    }
    return
  }
  if (command === 'logout') {
    userStore.logout()
    await router.push('/login')
  }
}

onMounted(() => {
  syncDashboardTimestamp()
  window.addEventListener('finance-dashboard-updated', syncDashboardTimestamp)
  window.addEventListener('storage', syncDashboardTimestamp)
})

onBeforeUnmount(() => {
  window.removeEventListener('finance-dashboard-updated', syncDashboardTimestamp)
  window.removeEventListener('storage', syncDashboardTimestamp)
})
</script>

<style scoped>
.finance-shell {
  min-height: calc(100vh - var(--nav-height));
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  background:
    radial-gradient(circle at 12% 8%, rgba(56, 189, 248, 0.22), transparent 32%),
    radial-gradient(circle at 88% 14%, rgba(34, 197, 94, 0.2), transparent 34%),
    radial-gradient(circle at 50% 86%, rgba(244, 114, 182, 0.12), transparent 36%),
    linear-gradient(150deg, #f7fffd 0%, #edf8ff 45%, #f9f4ff 100%);
  color-scheme: light;
}

.finance-sidebar {
  padding: 28px;
  border-right: 1px solid rgba(148, 163, 184, 0.2);
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(14px);
}

.brand-card {
  padding: 24px;
  border-radius: 28px;
  background: linear-gradient(155deg, rgba(6, 24, 44, 0.92), rgba(6, 95, 107, 0.9) 52%, rgba(8, 145, 178, 0.84) 100%);
  color: #e2e8f0;
  box-shadow: 0 24px 48px rgba(15, 23, 42, 0.22);
}

.section-kicker,
.nav-kicker,
.trace-card span {
  margin: 0 0 10px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  opacity: 0.8;
}

.brand-card h1 {
  margin: 0;
  font-size: 30px;
  line-height: 1.1;
}

.finance-nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 20px;
}

.nav-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0;
  padding: 18px;
  min-height: 108px;
  border-radius: 22px;
  text-decoration: none;
  text-align: center;
  color: #0f172a;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(148, 163, 184, 0.18);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.nav-card strong {
  font-size: 20px;
  line-height: 1.2;
  font-weight: 800;
}

.nav-card:hover,
.nav-card.is-active {
  transform: translateX(4px);
  border-color: rgba(13, 148, 136, 0.35);
  box-shadow: 0 16px 28px rgba(15, 23, 42, 0.08);
}

.finance-main {
  padding: 28px;
}

.finance-main-fullscreen {
  padding: 0;
}

.finance-content {
  max-width: 1320px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.finance-shell-fullscreen {
  min-height: 100vh;
  display: block;
}

.finance-shell-fullscreen .finance-content {
  max-width: none;
  display: block;
  gap: 0;
}

.finance-topline {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  align-items: center;
  margin-bottom: 20px;
}

.topline-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-trigger {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.avatar-hex {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.finance-topline strong,
.trace-card strong {
  color: #0f172a;
}

.trace-card {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.64);
  border: 1px solid rgba(148, 163, 184, 0.18);
}

.trace-card-btn {
  cursor: pointer;
  text-align: left;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.trace-card-btn:hover {
  border-color: rgba(13, 148, 136, 0.35);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.08);
}

.trace-card span {
  display: block;
}

@media (max-width: 1080px) {
  .finance-shell {
    grid-template-columns: 1fr;
  }

  .finance-sidebar {
    border-right: none;
    border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  }
}

@media (max-width: 640px) {
  .finance-sidebar,
  .finance-main {
    padding: 18px;
  }

  .finance-topline {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
