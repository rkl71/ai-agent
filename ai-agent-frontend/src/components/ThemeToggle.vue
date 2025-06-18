<template>
  <label class="theme-toggle" title="切换亮/暗模式">
    <input type="checkbox" v-model="isDark" @change="toggleTheme" />
    <span class="slider">
      <span class="icon sun">☀️</span>
      <span class="icon moon">🌙</span>
    </span>
  </label>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const isDark = ref(false);

// 切换主题的函数
const toggleTheme = () => {
  if (isDark.value) {
    document.documentElement.classList.add('dark');
    localStorage.setItem('theme', 'dark');
  } else {
    document.documentElement.classList.remove('dark');
    localStorage.setItem('theme', 'light');
  }
};

// 组件加载时，检查本地存储或系统偏好来设置初始主题
onMounted(() => {
  const savedTheme = localStorage.getItem('theme');
  const prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;

  if (savedTheme === 'dark' || (!savedTheme && prefersDark)) {
    isDark.value = true;
    toggleTheme();
  }
});
</script>

<style scoped>
.theme-toggle {
  display: inline-block;
  position: relative;
  width: 60px;
  height: 34px;
  cursor: pointer;
}

.theme-toggle input {
  opacity: 0;
  width: 0;
  height: 0;
}

.slider {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #ccc;
  border-radius: 34px;
  transition: .4s;
}

.slider:before {
  position: absolute;
  content: "";
  height: 26px;
  width: 26px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  border-radius: 50%;
  transition: .4s;
}

input:checked + .slider {
  background-color: #2c3e50;
}

input:checked + .slider:before {
  transform: translateX(26px);
}

.icon {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 16px;
  transition: opacity 0.4s;
}

.sun {
  left: 7px;
  opacity: 1;
}

.moon {
  right: 7px;
  opacity: 0;
}

input:checked + .slider .sun {
  opacity: 0;
}

input:checked + .slider .moon {
  opacity: 1;
}
</style>