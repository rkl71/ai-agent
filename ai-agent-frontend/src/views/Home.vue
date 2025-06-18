<template>
  <div class="home-container">
    <ThemeToggle class="theme-toggle" />

    <div class="header">
      <h1 class="main-title">神农医慧库</h1>
      <p class="subtitle">/ 传承中医智慧，赋能现代健康 /</p>
    </div>

    <div class="apps-container">
      <div class="app-card" @click="navigateTo('/doctor-answer')">
        <div class="app-icon doctor-icon">👨‍⚕️</div>
        <div class="app-info">
          <div class="app-title">华佗问诊</div>
          <div class="app-desc">智能健康助手，提供专业医疗咨询</div>
        </div>
        <div class="app-button">
          <span class="btn-text">立即体验</span>
          <span class="btn-icon">→</span>
        </div>
      </div>

      <div class="app-card" @click="navigateTo('/super-agent')">
        <div class="app-icon robot-icon">🤖</div>
        <div class="app-info">
          <div class="app-title">AI超级智能体</div>
          <div class="app-desc">全能型AI助手，解决各类专业问题</div>
        </div>
        <div class="app-button">
          <span class="btn-text">立即体验</span>
          <span class="btn-icon">→</span>
        </div>
      </div>
    </div>

    <AppFooter />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import AppFooter from '../components/AppFooter.vue'
// 1. 导入 ThemeToggle 组件
import ThemeToggle from '../components/ThemeToggle.vue';

// 设置页面标题和元数据
useHead({
  title: '神农医慧库智能体平台 - 首页',
  meta: [
    {
      name: 'description',
      content: '神农医慧库智能体平台提供华佗问诊和AI超级智能体服务，满足您的各种AI对话需求'
    },
    {
      name: 'keywords',
      content: 'AI智能体,AI应用,华佗问诊,AI助手,智能对话,神农医慧库,AI超级智能体,首页'
    }
  ]
})

const router = useRouter()

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
/* 引入更适合中文的字体 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;500;700&display=swap');

.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  /* 柔和的背景色，您可以根据喜好调整 */
  background-color: #f5f5f5;
  position: relative;

  /* * 背景图片设置
   * 请将 'herbs-background.jpg' 替换为您放在 public 目录下的图片文件名
   * linear-gradient 叠加层是为了让背景图片变暗，从而实现“隐隐浮现”的效果
   * 您可以调整 rgba 的最后一个值（透明度）来控制图片的明显程度，0.9 表示非常淡
  */
  background-image:
      linear-gradient(rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0.7)),
      url('/herbs-background.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed; /* 背景固定，内容滚动 */
}

.theme-toggle {
  position: absolute;
  top: 25px;    /* 距离顶部 25px */
  right: 40px;  /* 距离右侧 40px (这是把它放在右边的核心) */
  z-index: 10;
}

.header {
  padding: 80px 20px 60px;
  text-align: center;
  background-color: transparent;
  color: #333; /* 深色文字，更显专业 */
}

.main-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 3rem;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 20px;
  text-shadow: 1px 1px 3px rgba(0,0,0,0.1); /* 添加细微阴影以增强质感 */
}

.subtitle {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.2rem;
  color: #555;
  letter-spacing: 1px;
}

.apps-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 40px;
  max-width: 900px;
  margin: 0 auto 60px;
  padding: 0 20px;
  flex: 1;
}

.app-card {
  width: 320px;
  background-color: rgba(255, 255, 255, 0.8); /* 半透明白色背景 */
  backdrop-filter: blur(10px); /* 毛玻璃效果 */
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 30px;
  cursor: pointer;
  transition: all 0.3s ease-in-out;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.app-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.app-icon {
  font-size: 3.5rem;
  margin-bottom: 25px;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

/* 图标背景可以保留，也可以换成更柔和的颜色 */
.doctor-icon {
  background: linear-gradient(135deg, #cffae5 0%, #00c39a 100%);
  box-shadow: 0 4px 15px rgba(0, 195, 154, 0.20);
  font-size: 3rem;
}

.robot-icon {
  /*background: linear-gradient(135deg, #448aff, #2962ff);
  box-shadow: 0 4px 15px rgba(41, 98, 255, 0.4);*/
  background: linear-gradient(135deg, #d5d0ff 0%, #675bff 100%);
  box-shadow: 0 4px 15px rgba(103, 91, 255, 0.22);
  font-size: 3rem;
}

.app-info {
  text-align: center;
  margin-bottom: 30px;
  width: 100%;
}

.app-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.6rem;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 12px;
}

.app-desc {
  font-size: 1rem;
  color: #606266;
  line-height: 1.6;
}

.app-button {
  background: #34495e;
  color: white;
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s;
  margin-top: auto;
  display: flex;
  align-items: center;
  border: none;
}

.app-button:hover {
  background: #2c3e50;
  transform: scale(1.05);
}

.btn-text {
  margin-right: 8px;
}

.btn-icon {
  transition: transform 0.3s;
}

.app-button:hover .btn-icon {
  transform: translateX(4px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 2.5rem;
  }
  .subtitle {
    font-size: 1.1rem;
  }
}
</style>