<template>
  <div class="ai-avatar-fallback" :class="type">
    <span v-if="type === 'doctor'">👨‍⚕️</span>
    <span v-else>🤖</span>
  </div>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'default'
  }
})
</script>

<style scoped>
.ai-avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  border-radius: 50%;
}

.doctor {
  background: linear-gradient(45deg, #cffae5, #00c39a);
}

.medical {
  background: linear-gradient(45deg, #00c6ff, #0072ff);
}

.default, .super {
  background: linear-gradient(45deg, #3f51b5, #5677fc);
}
</style> 