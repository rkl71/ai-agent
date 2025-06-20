<template>
  <div class="chat-container">
    <!-- 聊天记录区域 -->
    <div class="chat-messages" ref="messagesContainer">
      <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
        <!-- AI消息 -->
        <div v-if="!msg.isUser"
             class="message ai-message"
             :class="[msg.type]">
          <div class="avatar ai-avatar">
            <AiAvatarFallback :type="aiType" />
          </div>
          <div class="message-bubble" :class="getMessageBubbleClass(msg.type)">
            <!-- 检查是否是工具调用消息 -->
            <div v-if="isToolCallMessage(msg.content)" class="tool-call-message">
              <div class="tool-summary" @click="toggleToolDetails(index)">
                <div class="tool-header">
                  <span class="tool-name">{{ getToolDisplayName(extractToolName(msg.content)) }}</span>
                  <span class="toggle-icon" :class="{ 'expanded': msg.showDetails }">▼</span>
                </div>
                <div class="tool-brief">{{ getToolBrief(msg.content) }}</div>
              </div>

              <!-- 工具详情 - 可展开/折叠 -->
              <div v-if="msg.showDetails" class="tool-details">
                <div class="tool-content" v-html="parseMarkdown(getToolDetails(msg.content))"></div>
              </div>
            </div>

            <!-- 普通消息内容 -->
            <div v-else class="message-content" v-html="parseMarkdown(msg.content)"></div>

            <!-- 特殊类型消息的图标 -->
            <div v-if="getMessageIcon(msg.type)" class="message-icon">
              {{ getMessageIcon(msg.type) }}
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>

        <!-- 用户消息 -->
        <div v-else class="message user-message" :class="[msg.type]">
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
          <div class="avatar user-avatar">
            <div class="avatar-placeholder">我</div>
          </div>
        </div>
      </div>

      <!-- 打字指示器 -->
      <div v-if="connectionStatus === 'connecting'" class="message ai-message typing-message">
        <div class="avatar ai-avatar">
          <AiAvatarFallback :type="aiType" />
        </div>
        <div class="message-bubble typing-bubble">
          <div class="typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-container">
      <div class="chat-input">
        <textarea
            v-model="inputMessage"
            @keydown.enter.prevent="sendMessage"
            placeholder="询问医疗相关的任何问题"
            class="input-box"
            :disabled="connectionStatus === 'connecting'"
        ></textarea>
        <button
            @click="sendMessage"
            class="send-button"
            :disabled="connectionStatus === 'connecting' || !inputMessage.trim()"
        >发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch, computed } from 'vue'
import AiAvatarFallback from './AiAvatarFallback.vue'
import { marked } from 'marked'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  },
  aiType: {
    type: String,
    default: 'default'
  }
})

const emit = defineEmits(['send-message'])

const inputMessage = ref('')
const messagesContainer = ref(null)

// 根据消息类型获取对应的样式类
const getMessageBubbleClass = (type) => {
  switch (type) {
    case 'ai-thinking':
      return 'thinking-bubble'
    case 'ai-greeting':
      return 'greeting-bubble'
    case 'ai-introduction':
      return 'introduction-bubble'
    case 'ai-instruction':
      return 'instruction-bubble'
    case 'ai-capabilities':
      return 'capabilities-bubble'
    case 'ai-step':
      return 'step-bubble'
    case 'ai-search':
      return 'search-bubble'
    case 'ai-analysis':
      return 'analysis-bubble'
    case 'ai-generation':
      return 'generation-bubble'
    case 'ai-final':
      return 'final-bubble'
    case 'ai-completion':
      return 'completion-bubble'
    case 'ai-file-summary':
      return 'file-summary-bubble'
    case 'ai-summary':
      return 'summary-bubble'
    case 'ai-error':
      return 'error-bubble'
    case 'ai-answer':
      return 'answer-bubble'
    default:
      return 'default-bubble'
  }
}

// 根据消息类型获取对应的图标
const getMessageIcon = (type) => {
  switch (type) {
    case 'ai-thinking':
      return '🤔'
    case 'ai-greeting':
      return '👋'
    case 'ai-introduction':
      return '🔍'
    case 'ai-instruction':
      return '📝'
    case 'ai-capabilities':
      return '🛠️'
    case 'ai-step':
      return '⚙️'
    case 'ai-search':
      return '🔍'
    case 'ai-analysis':
      return '📊'
    case 'ai-generation':
      return '✨'
    case 'ai-final':
      return '🎯'
    case 'ai-completion':
      return '✅'
    case 'ai-file-summary':
      return '📁'
    case 'ai-summary':
      return '📋'
    case 'ai-error':
      return '⚠️'
    default:
      return null
  }
}

// 解析Markdown内容
const parseMarkdown = (content) => {
  if (!content) {
    return ''
  }

  let correctedContent = content
      .replace(/^(\s*(\d+\.|[-*+]))([^\s\n])/gm, '$1 $3')
      .replace(/^(#+)([^\s#\n])/gm, '$1 $2')

  return marked.parse(correctedContent)
}

// 检查是否是工具调用消息
const isToolCallMessage = (content) => {
  const toolCallPattern = /(?:步骤\s*\d+[:：]\s*)?工具\s+(\w+)\s+返回的结果/
  return toolCallPattern.test(content)
}

// 提取工具名称
const extractToolName = (content) => {
  const toolCallPattern = /(?:步骤\s*\d+[:：]\s*)?工具\s+(\w+)\s+返回的结果/
  const match = content.match(toolCallPattern)
  return match ? match[1] : 'unknown'
}

// 获取工具名称的中文显示
const getToolDisplayName = (toolName) => {
  const toolNames = {
    'googleSearch': '🔍 Google搜索',
    'searchWeb': '🌐 网页搜索',
    'scrapeWebPage': '📄 网页抓取',
    'downloadResource': '⬇️ 资源下载',
    'generatePDF': '📄 PDF生成',
    'writeFile': '📝 文件写入',
    'readFile': '📖 文件读取',
    'executeTerminalCommand': '⚡ 终端执行',
    'doTerminate': '✅ 任务结束'
  }
  return toolNames[toolName] || `🔧 ${toolName}`
}

// 获取工具调用简要信息
const getToolBrief = (content) => {
  const toolName = extractToolName(content)

  // 根据工具类型返回不同的简要信息
  if (toolName === 'googleSearch' || toolName === 'searchWeb') {
    return '搜索完成，找到相关结果'
  } else if (toolName === 'generatePDF') {
    return 'PDF文档生成完成'
  } else if (toolName === 'downloadResource') {
    return '资源下载完成'
  } else if (toolName === 'scrapeWebPage') {
    return '网页内容抓取完成'
  } else if (toolName === 'writeFile') {
    return '文件写入完成'
  } else if (toolName === 'doTerminate') {
    return '任务执行结束'
  }

  return '执行完成'
}

// 获取工具详细内容
const getToolDetails = (content) => {
  const toolCallPattern = /(?:步骤\s*\d+[:：]\s*)?工具\s+\w+\s+返回的结果[:：]\s*"(.+?)"/s
  const match = content.match(toolCallPattern)
  return match ? match[1] : content
}

// 切换工具详情显示状态
const toggleToolDetails = (messageIndex) => {
  // 由于Vue的响应式限制，我们需要确保showDetails属性存在
  if (!props.messages[messageIndex].hasOwnProperty('showDetails')) {
    props.messages[messageIndex].showDetails = false
  }
  props.messages[messageIndex].showDetails = !props.messages[messageIndex].showDetails
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim()) return

  emit('send-message', inputMessage.value)
  inputMessage.value = ''
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 自动滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 监听消息变化，自动滚动
watch(() => props.messages.length, () => {
  scrollToBottom()
})

watch(() => props.messages.map(m => m.content).join(''), () => {
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 70vh;
  min-height: 600px;
  background-color: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  padding-bottom: 80px;
  display: flex;
  flex-direction: column;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 72px;
}

.message-wrapper {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  width: 100%;
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: 85%;
  margin-bottom: 8px;
}

.user-message {
  margin-left: auto;
  flex-direction: row;
}

.ai-message {
  margin-right: auto;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  margin-left: 8px;
}

.ai-avatar {
  margin-right: 8px;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #007bff;
  color: white;
  font-weight: bold;
}

.message-bubble {
  padding: 12px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
  min-width: 100px;
  animation: fadeIn 0.3s ease-in-out;
}

.user-message .message-bubble {
  background-color: #007bff;
  color: white;
  border-bottom-right-radius: 4px;
  text-align: left;
}

.ai-message .message-bubble {
  background-color: #e9e9eb;
  color: #333;
  border-bottom-left-radius: 4px;
  text-align: left;
}

/* 不同类型AI消息的样式 */
.thinking-bubble {
  background: linear-gradient(135deg, #ffeaa7, #fdcb6e) !important;
  border-left: 4px solid #e17055;
}

.greeting-bubble {
  background: linear-gradient(135deg, #a8e6cf, #7fcdcd) !important;
  border-left: 4px solid #00b894;
}

.introduction-bubble {
  background: linear-gradient(135deg, #ddd6fe, #c4b5fd) !important;
  border-left: 4px solid #8b5cf6;
}

.instruction-bubble {
  background: linear-gradient(135deg, #bfdbfe, #93c5fd) !important;
  border-left: 4px solid #3b82f6;
}

.capabilities-bubble {
  background: linear-gradient(135deg, #fef3c7, #fcd34d) !important;
  border-left: 4px solid #f59e0b;
}

.step-bubble {
  background: linear-gradient(135deg, #e0e7ff, #c7d2fe) !important;
  border-left: 4px solid #6366f1;
  font-weight: 500;
}

.search-bubble {
  background: linear-gradient(135deg, #ecfdf5, #bbf7d0) !important;
  border-left: 4px solid #059669;
}

.analysis-bubble {
  background: linear-gradient(135deg, #fef7ff, #f3e8ff) !important;
  border-left: 4px solid #a855f7;
}

.generation-bubble {
  background: linear-gradient(135deg, #fffbeb, #fed7aa) !important;
  border-left: 4px solid #ea580c;
}

.final-bubble {
  background: linear-gradient(135deg, #f0f9ff, #bae6fd) !important;
  border-left: 4px solid #0284c7;
  font-weight: 600;
}

.completion-bubble {
  background: linear-gradient(135deg, #f0fdf4, #bbf7d0) !important;
  border-left: 4px solid #16a34a;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(34, 197, 94, 0.2);
}

.file-summary-bubble {
  background: linear-gradient(135deg, #eff6ff, #dbeafe) !important;
  border-left: 4px solid #2563eb;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.summary-bubble {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0) !important;
  border-left: 4px solid #10b981;
  font-weight: 500;
}

.error-bubble {
  background: linear-gradient(135deg, #fecaca, #fca5a5) !important;
  border-left: 4px solid #ef4444;
}

.answer-bubble {
  background-color: #ffffff !important;
  border: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* 工具调用消息样式 */
.tool-call-message {
  width: 100%;
}

.tool-summary {
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.2);
  transition: all 0.2s ease;
}

.tool-summary:hover {
  background: rgba(59, 130, 246, 0.15);
  border-color: rgba(59, 130, 246, 0.3);
}

.tool-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.tool-name {
  font-weight: 600;
  color: #1e40af;
  font-size: 14px;
}

.toggle-icon {
  color: #6b7280;
  font-size: 12px;
  transition: transform 0.2s ease;
}

.toggle-icon.expanded {
  transform: rotate(180deg);
}

.tool-brief {
  font-size: 13px;
  color: #4b5563;
  line-height: 1.4;
}

.tool-details {
  margin-top: 8px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 6px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  animation: slideDown 0.2s ease-out;
}

.tool-content {
  font-size: 12px;
  line-height: 1.5;
  color: #374151;
  max-height: 200px;
  overflow-y: auto;
}

.tool-content::-webkit-scrollbar {
  width: 4px;
}

.tool-content::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 2px;
}

.tool-content::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

@keyframes slideDown {
  from {
    opacity: 0;
    max-height: 0;
    padding-top: 0;
    padding-bottom: 0;
  }
  to {
    opacity: 1;
    max-height: 200px;
    padding-top: 12px;
    padding-bottom: 12px;
  }
}

.message-icon {
  position: absolute;
  top: -8px;
  right: -8px;
  font-size: 14px;
  background: white;
  border-radius: 50%;
  padding: 2px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

/* 打字指示器样式 */
.typing-message {
  opacity: 0.8;
}

.typing-bubble {
  background-color: #f0f0f0 !important;
  padding: 16px !important;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #666;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Markdown 内容样式 */
.message-content :deep(h1),
.message-content :deep(h2),
.message-content :deep(h3),
.message-content :deep(h4),
.message-content :deep(h5),
.message-content :deep(h6) {
  margin-top: 16px;
  margin-bottom: 8px;
  font-weight: 600;
  line-height: 1.4;
  border-bottom: 1px solid #e0e0e0;
  padding-bottom: 4px;
}

.message-content :deep(h3) {
  font-size: 1.1em;
}

.message-content :deep(h4) {
  font-size: 1.05em;
  border-bottom: none;
}

.message-content :deep(> h1:first-child),
.message-content :deep(> h2:first-child),
.message-content :deep(> h3:first-child),
.message-content :deep(> h4:first-child) {
  margin-top: 0;
}

.message-content :deep(p) {
  line-height: 1.6;
  margin: 0 0 6px 0;
}

.message-content :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content :deep(strong) {
  font-weight: 600;
}

.message-content :deep(ul), .message-content :deep(ol) {
  margin: 0;
  padding-left: 22px;
}

.message-content :deep(li) {
  margin-bottom: 4px;
  padding-left: 2px;
}

.message-content :deep(li p) {
  margin: 0;
}

.message-time {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 4px;
  text-align: right;
}

/* 输入区域样式 */
.chat-input-container {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: white;
  border-top: 1px solid #e0e0e0;
  z-index: 100;
  height: 72px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.chat-input {
  display: flex;
  padding: 16px;
  height: 100%;
  box-sizing: border-box;
  align-items: center;
}

.input-box {
  flex-grow: 1;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 10px 16px;
  font-size: 16px;
  resize: none;
  min-height: 20px;
  max-height: 40px;
  outline: none;
  transition: border-color 0.3s;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
}

.input-box::-webkit-scrollbar {
  display: none;
}

.input-box:focus {
  border-color: #007bff;
}

.send-button {
  margin-left: 12px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0 20px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
  height: 40px;
  align-self: center;
}

.send-button:hover:not(:disabled) {
  background-color: #0069d9;
}

.input-box:disabled, .send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .message {
    max-width: 95%;
  }

  .message-content {
    font-size: 15px;
  }

  .chat-input {
    padding: 12px;
  }

  .input-box {
    padding: 8px 12px;
  }

  .send-button {
    padding: 0 15px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .avatar {
    width: 32px;
    height: 32px;
  }

  .message-bubble {
    padding: 10px;
  }

  .message-content {
    font-size: 14px;
  }

  .chat-input-container {
    height: 64px;
  }

  .chat-messages {
    bottom: 64px;
  }
}
</style>