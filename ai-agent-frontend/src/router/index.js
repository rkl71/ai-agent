import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: {
      title: '首页 - 神农医慧库',
      description: '神农医慧库提供华佗问诊和AI超级智能体服务，满足您的各种AI对话需求'
    }
  },
  {
    path: '/doctor-answer',
    name: 'Doctor',
    component: () => import('../views/DoctorAnswer.vue'),
    meta: {
      title: '华佗问诊 - 神农医慧库',
      description: '智能健康助手，基于中医理论提供专业的健康咨询和调理建议。'
    }
  },
  {
    path: '/super-agent',
    name: 'SuperAgent',
    component: () => import('../views/SuperAgent.vue'),
    meta: {
      title: 'AI超级智能体 - 神农医慧库',
      description: 'AI超级智能体是神农医慧库的全能助手，能解答各类专业问题，提供精准建议和解决方案'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局导航守卫，设置文档标题
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title
  }
  next()
})

export default router 