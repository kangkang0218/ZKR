import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// *** 核心补充：引入 Element Plus 组件库 ***
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

const savedTheme = localStorage.getItem('app-theme')
if (savedTheme === 'dark' || savedTheme === 'light') {
  document.documentElement.classList.toggle('dark', savedTheme === 'dark')
  document.documentElement.setAttribute('data-theme', savedTheme)
}

window.addEventListener('vite:preloadError', () => {
  window.location.reload()
})

const app = createApp(App)

app.use(createPinia())
app.use(router)

// *** 核心补充：注册组件库 ***
app.use(ElementPlus)

app.mount('#app')
