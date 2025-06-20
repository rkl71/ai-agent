<template>
  <div class="super-agent-container">
    <div class="header">
      <div class="back-button" @click="goBack">返回</div>
      <h1 class="title">AI超级智能体</h1>
      <div class="status-indicator" :class="connectionStatus">
        <span class="status-dot"></span>
        <span class="status-text">{{ getStatusText() }}</span>
      </div>
    </div>

    <div class="content-wrapper">
      <div class="chat-area">
        <ChatRoom
            :messages="messages"
            :connection-status="connectionStatus"
            ai-type="super"
            @send-message="sendMessage"
        />
      </div>
    </div>

    <div class="footer-container">
      <AppFooter />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useHead } from '@vueuse/head'
import ChatRoom from '../components/ChatRoom.vue'
import AppFooter from '../components/AppFooter.vue'
import { chatWithManus } from '../api'

// 设置页面标题和元数据
useHead({
  title: 'AI超级智能体 - 神农医慧库智能体平台',
  meta: [
    {
      name: 'description',
      content: 'AI超级智能体是神农医慧库智能体平台的全能助手，拥有自主规划能力，能解答各类专业问题，提供精准建议和解决方案'
    },
    {
      name: 'keywords',
      content: 'AI超级智能体,智能助手,专业问答,AI问答,专业建议,AI智能体,自主规划,Manus'
    }
  ]
})

const router = useRouter()
const messages = ref([])
const connectionStatus = ref('disconnected')
let eventSource = null

// 获取状态文本
const getStatusText = () => {
  switch (connectionStatus.value) {
    case 'connecting':
      return '思考中'
    case 'connected':
      return '已连接'
    case 'disconnected':
      return '准备就绪'
    case 'error':
      return '连接异常'
    default:
      return '准备就绪'
  }
}

// 添加消息到列表（支持步骤化显示）
const addMessage = (content, isUser, type = '') => {
  messages.value.push({
    content,
    isUser,
    type,
    time: new Date().getTime(),
    showDetails: false // 初始化为折叠状态
  })
}

// 发送消息（模拟Manus的层层递进反馈）
const sendMessage = (message) => {
  addMessage(message, true, 'user-question')

  // 连接SSE
  if (eventSource) {
    eventSource.close()
  }

  // 设置连接状态
  connectionStatus.value = 'connecting'

  // 添加智能体状态提示
  addMessage('🔍 正在分析您的需求...', false, 'ai-thinking')

  // 用于管理响应的变量
  let messageBuffer = []
  let isFirstResponse = true
  let currentStepNumber = 1
  let lastBubbleTime = Date.now()
  let isCompleted = false
  let hasGeneratedFiles = false

  const chineseEndPunctuation = ['。', '！', '？', '…', '\n\n']
  const minBubbleInterval = 1000 // 最小间隔1秒

  // 提取文件路径的函数
  const extractFilePaths = (content) => {
    const filePathRegex = /(?:生成|下载|保存|创建).*?(?:PDF|文件|图片|资源).*?(?:到|至)?[：:]?\s*([C-Z]:[\\\/][\w\s\-\\\/\.]+\.(?:pdf|txt|jpg|jpeg|png|doc|docx|xls|xlsx))/gi
    const matches = []
    let match
    while ((match = filePathRegex.exec(content)) !== null) {
      matches.push(match[1].trim())
    }
    return matches
  }

  // 创建新的响应气泡
  const createResponseBubble = (content, type = 'ai-step') => {
    if (!content.trim()) return

    const now = Date.now()
    const timeSinceLastBubble = now - lastBubbleTime

    // 移除思考状态（仅在第一次响应时）
    if (isFirstResponse) {
      messages.value = messages.value.filter(msg => msg.type !== 'ai-thinking')
      isFirstResponse = false
    }

    // 检查是否包含文件生成信息
    const filePaths = extractFilePaths(content)
    if (filePaths.length > 0) {
      hasGeneratedFiles = true
    }

    // 格式化步骤内容
    let formattedContent = content
    if (type === 'ai-step' && !content.includes('Step') && !content.includes('步骤')) {
      formattedContent = `**步骤 ${currentStepNumber}**: ${content}`
      currentStepNumber++
    }

    if (timeSinceLastBubble < minBubbleInterval) {
      setTimeout(() => {
        addMessage(formattedContent, false, type)
      }, minBubbleInterval - timeSinceLastBubble)
    } else {
      addMessage(formattedContent, false, type)
    }

    lastBubbleTime = now
    messageBuffer = []
  }

  eventSource = chatWithManus(message)

  // 监听SSE消息
  eventSource.onmessage = (event) => {
    const data = event.data

    if (data && data !== '[DONE]') {
      messageBuffer.push(data)

      const combinedText = messageBuffer.join('')

      // 检查是否应该创建新气泡
      const lastChar = data.charAt(data.length - 1)
      const hasCompleteSentence = chineseEndPunctuation.some(punct =>
          lastChar === punct || data.includes(punct)
      )
      const isLongEnough = combinedText.length > 60

      // 检测步骤标识符
      const stepIndicators = ['Step', 'step', '步骤', '第', '正在', '开始', '完成', '任务结束']
      const hasStepIndicator = stepIndicators.some(indicator =>
          combinedText.toLowerCase().includes(indicator.toLowerCase())
      )

      if (hasCompleteSentence || isLongEnough || hasStepIndicator) {
        // 判断消息类型
        let messageType = 'ai-step'
        if (combinedText.includes('任务结束') || combinedText.includes('Terminated')) {
          messageType = 'ai-completion'
          isCompleted = true
        } else if (combinedText.includes('错误') || combinedText.includes('失败') || combinedText.includes('Error')) {
          messageType = 'ai-error'
        } else if (combinedText.includes('完成') || combinedText.includes('结束') || combinedText.includes('successfully')) {
          messageType = 'ai-final'
        } else if (combinedText.includes('搜索') || combinedText.includes('查找') || combinedText.includes('search')) {
          messageType = 'ai-search'
        } else if (combinedText.includes('分析') || combinedText.includes('处理') || combinedText.includes('analysis')) {
          messageType = 'ai-analysis'
        } else if (combinedText.includes('生成') || combinedText.includes('创建') || combinedText.includes('generate')) {
          messageType = 'ai-generation'
        }

        createResponseBubble(combinedText, messageType)
      }
    }

    if (data === '[DONE]') {
      // 标记任务完成，避免错误处理
      isCompleted = true

      // 处理剩余内容
      if (messageBuffer.length > 0) {
        const remainingContent = messageBuffer.join('')
        createResponseBubble(remainingContent, 'ai-final')
      }

      // 根据是否生成了文件来显示不同的完成消息
      setTimeout(() => {
        if (hasGeneratedFiles) {
          addMessage('📁 所有文件已生成完成！您可以在指定路径查看生成的文档', false, 'ai-file-summary')
        }

        addMessage('✅ 任务执行完成！如需进一步帮助，请继续提问', false, 'ai-completion')

        // 状态恢复到初始状态
        connectionStatus.value = 'disconnected'
      }, 500)

      // 关闭连接
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
    }
  }

  // 监听SSE错误（仅在真正的网络错误时显示）
  eventSource.onerror = (error) => {
    console.error('SSE Error:', error)

    // 如果任务已完成，不显示错误信息
    if (isCompleted) {
      console.log('Task completed normally, ignoring connection error')
      connectionStatus.value = 'disconnected'
      if (eventSource) {
        eventSource.close()
        eventSource = null
      }
      return
    }

    // 只有在连接状态为connecting且任务未完成时才显示错误
    if (connectionStatus.value === 'connecting') {
      connectionStatus.value = 'error'

      // 移除思考状态，显示错误信息
      messages.value = messages.value.filter(msg => msg.type !== 'ai-thinking')
      addMessage('⚠️ 连接中断，请检查网络后重试', false, 'ai-error')
    }

    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  }
}

// 返回主页
const goBack = () => {
  router.push('/')
}

// 页面加载时添加欢迎消息
onMounted(() => {
  // 添加分步骤的欢迎消息
  addMessage('🤖 您好！我是Manus，您的AI超级智能体', false, 'ai-greeting')

  setTimeout(() => {
    addMessage('🚀 我拥有自主规划能力，可以帮您解决各类复杂问题', false, 'ai-introduction')
  }, 1000)

  setTimeout(() => {
    addMessage('🛠️ 我可以调用多种工具：网络搜索、文件处理、PDF生成、图片搜索等', false, 'ai-capabilities')
  }, 2000)

  setTimeout(() => {
    addMessage('💡 请告诉我您需要什么帮助，我会为您制定详细的执行计划', false, 'ai-instruction')
  }, 3000)
})

// 组件销毁前关闭SSE连接
onBeforeUnmount(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.super-agent-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
}

.super-agent-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
      radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
      radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.15) 0%, transparent 50%),
      radial-gradient(circle at 40% 40%, rgba(120, 119, 198, 0.2) 0%, transparent 50%);
  pointer-events: none;
}

.header {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 10;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.back-button {
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
  justify-self: start;
  padding: 8px 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(-2px);
}

.back-button:before {
  content: '←';
  margin-right: 8px;
  font-weight: bold;
}

.title {
  font-size: 20px;
  font-weight: bold;
  margin: 0;
  text-align: center;
  justify-self: center;
  background: linear-gradient(45deg, #fff, #e3f2fd);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.status-indicator {
  justify-self: end;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  font-size: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4caf50;
  animation: pulse 2s infinite;
}

.status-indicator.connecting .status-dot {
  background: #ff9800;
  animation: pulse 1s infinite;
}

.status-indicator.error .status-dot {
  background: #f44336;
  animation: none;
}

.status-text {
  font-weight: 500;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 currentColor;
    opacity: 1;
  }
  70% {
    box-shadow: 0 0 0 4px transparent;
    opacity: 0.7;
  }
  100% {
    box-shadow: 0 0 0 0 transparent;
    opacity: 1;
  }
}

.content-wrapper {
  display: flex;
  flex-direction: column;
  flex: 1;
  position: relative;
  z-index: 1;
}

.chat-area {
  flex: 1;
  padding: 16px;
  overflow: hidden;
  position: relative;
  min-height: calc(100vh - 56px - 180px);
  margin-bottom: 16px;
}

.footer-container {
  margin-top: auto;
  position: relative;
  z-index: 1;
}

/* 响应式样式 */
@media (max-width: 768px) {
  .header {
    padding: 12px 16px;
    grid-template-columns: auto 1fr auto;
    gap: 12px;
  }

  .title {
    font-size: 18px;
  }

  .status-indicator {
    font-size: 11px;
    padding: 4px 8px;
  }

  .status-text {
    display: none;
  }

  .chat-area {
    padding: 12px;
    min-height: calc(100vh - 48px - 160px);
    margin-bottom: 12px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 10px 12px;
  }

  .back-button {
    font-size: 14px;
    padding: 6px 12px;
  }

  .title {
    font-size: 16px;
  }

  .status-indicator {
    padding: 3px 6px;
  }

  .chat-area {
    padding: 8px;
    min-height: calc(100vh - 42px - 150px);
    margin-bottom: 8px;
  }
}

/* 增强的动画效果 */
.super-agent-container {
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header {
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    transform: translateY(-100%);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.chat-area {
  animation: slideUp 0.7s ease-out 0.2s both;
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>