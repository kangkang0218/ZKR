<template>
  <div class="profile-container">
    <div class="profile-card">
      <!-- 头部 -->
      <div class="profile-header">
        <div class="avatar-wrapper">
          <img :src="displayAvatar" class="profile-avatar">
          <button class="avatar-edit-btn" @click="openAvatarDialog" title="更换头像">
            📷
          </button>
        </div>
        <div class="profile-info">
          <h2 class="profile-name">{{ currentUser?.name || '用户' }}</h2>
          <p class="profile-role">{{ currentUser?.role || '-' }}</p>
        </div>
      </div>

      <!-- 信息列表 -->
      <div class="profile-sections">
        <div class="section">
          <h3>基本信息</h3>
          <div class="info-item">
            <span class="label">用户ID</span>
            <span class="value">{{ currentUser?.userId || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">用户名</span>
            <span class="value">{{ currentUser?.username || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ currentUser?.email || '-' }}</span>
          </div>
        </div>

        <div class="section">
          <h3>账户状态</h3>
          <div class="info-item">
            <span class="label">登录状态</span>
            <span class="value status-online">已登录</span>
          </div>
          <div class="info-item">
            <span class="label">角色权限</span>
            <span class="value">{{ userStore.isManager ? '管理员' : '普通用户' }}</span>
          </div>
        </div>

        <div class="section">
          <h3>快速操作</h3>
          <div class="action-buttons">
            <button v-if="userStore.isErpLoggedIn" class="action-btn" @click="router.push('/erp/personal-procurement')">
              <span>🛒</span> 个人采购申请
            </button>
            <button class="action-btn" @click="handleSwitchView">
              <span>🔄</span> 切换视角
            </button>
            <button class="action-btn" @click="handleLogout">
              <span>🚪</span> 退出登录
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像选择对话框 -->
    <el-dialog
      v-model="showAvatarDialog"
      title="选择头像"
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="avatar-grid">
        <div
          v-for="(avatar, index) in allAvatarOptions"
          :key="index"
          class="avatar-option"
          :class="{ selected: selectedAvatar === avatar }"
          @click="selectedAvatar = avatar"
        >
          <img :src="avatar" class="avatar-preview">
        </div>
      </div>
      <div class="custom-avatar-section">
        <p class="custom-avatar-label">或输入自定义头像链接：</p>
        <el-input
          v-model="customAvatarUrl"
          placeholder="https://example.com/avatar.jpg"
          @input="selectedAvatar = customAvatarUrl"
        />
      </div>
      <template #footer>
        <el-button @click="showAvatarDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAvatarConfirm" :loading="updating">
          确认更换
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/userStore'
import { useRouter, useRoute } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()
const currentUser = computed(() => userStore.activeUserInfo || {})

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

// 头像相关状态
const showAvatarDialog = ref(false)
const selectedAvatar = ref('')
const customAvatarUrl = ref('')
const updating = ref(false)

// 预设头像选项（使用 DiceBear API 生成）
const avatarOptions = [
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Zack',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Milo',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Gizmo',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Bella',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Jack',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Cookie',
  'https://api.dicebear.com/7.x/notionists/svg?seed=1',
  'https://api.dicebear.com/7.x/notionists/svg?seed=2',
  'https://api.dicebear.com/7.x/notionists/svg?seed=3',
  'https://api.dicebear.com/7.x/thumbs/svg?seed=1',
]

// 带 wrn 勋章专属头像的条件选项
const wrnAvatarOption = '/downloads/wrn.jpg'

const allAvatarOptions = computed(() => {
  return userStore.hasWrnBadge
    ? [wrnAvatarOption, ...avatarOptions]
    : avatarOptions
})

const openAvatarDialog = async () => {
  await userStore.fetchBadges()
  showAvatarDialog.value = true
}

// 显示的头像
const displayAvatar = computed(() => {
  return userStore.activeUserInfo?.avatar || defaultAvatar
})

// 确认更换头像
const handleAvatarConfirm = async () => {
  if (!selectedAvatar.value) {
    ElMessage.warning('请选择或输入头像链接')
    return
  }

  updating.value = true
  try {
    await request.put('/api/users/avatar', {
      avatar: selectedAvatar.value
    })

    userStore.updateAvatar(selectedAvatar.value)

    ElMessage.success('头像更新成功')
    showAvatarDialog.value = false
    customAvatarUrl.value = ''
    selectedAvatar.value = ''
  } catch (error) {
    ElMessage.error('头像更新失败')
  } finally {
    updating.value = false
  }
}

const handleSwitchView = () => {
  const currentPath = route.path
  if (userStore.isErpLoggedIn && currentPath.includes('/manager')) {
    router.push('/workspace')
  } else if (userStore.isErpLoggedIn) {
    router.push('/manager/dashboard')
  } else {
    router.push('/finance/overview')
  }
}

const handleLogout = () => {
  if (userStore.isErpLoggedIn) {
    userStore.logoutErp()
    router.push('/erp-login')
  } else {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.profile-container {
  padding: 40px 20px;
  background-color: var(--science-canvas);
  min-height: 100vh;
}

.profile-card {
  max-width: 600px;
  margin: 0 auto;
  background: var(--science-surface);
  border-radius: 16px;
  padding: 30px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-soft);
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-soft);
  margin-bottom: 24px;
}

.avatar-wrapper {
  position: relative;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid var(--science-blue);
}

.avatar-edit-btn {
  position: absolute;
  bottom: -5px;
  right: -5px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  border: 2px solid white;
  background: var(--science-blue);
  color: white;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  transition: all 0.2s;
}

.avatar-edit-btn:hover {
  background: var(--science-blue-hover);
  transform: scale(1.1);
}

.profile-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-main);
  margin: 0 0 4px 0;
}

.profile-role {
  font-size: 14px;
  color: var(--text-sub);
  margin: 0;
  background: var(--science-surface-muted);
  color: var(--science-blue);
  padding: 4px 12px;
  border-radius: 12px;
  display: inline-block;
  font-weight: 600;
}

.profile-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.section h3 {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-sub);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 12px 0;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-soft);
}

.info-item:last-child {
  border-bottom: none;
}

.info-item .label {
  color: var(--text-sub);
  font-size: 14px;
}

.info-item .value {
  color: var(--text-main);
  font-weight: 500;
  font-size: 14px;
}

.status-online {
  color: #10b981;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  flex: 1;
  min-width: 140px;
  padding: 12px 16px;
  border: 1px solid var(--border-soft);
  background: var(--science-surface);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--text-main);
}

.action-btn:hover {
  background: var(--science-surface-muted);
  border-color: var(--science-blue);
  color: var(--science-blue);
}

/* 头像选择对话框样式 */
.avatar-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
  padding: 4px;
}

.avatar-option {
  width: 100%;
  aspect-ratio: 1;
  border: 2px solid var(--border-soft);
  border-radius: 8px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.2s;
  position: relative;
}

.avatar-option:hover {
  border-color: var(--science-blue);
  transform: scale(1.05);
}

.avatar-option.selected {
  border-color: var(--science-blue);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.2);
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.custom-avatar-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-soft);
}

.custom-avatar-label {
  font-size: 13px;
  color: var(--text-sub);
  margin: 0 0 8px 0;
}
</style>
