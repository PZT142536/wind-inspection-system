import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const device = ref<'desktop' | 'mobile'>('desktop')

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function setDevice(val: 'desktop' | 'mobile') {
    device.value = val
  }

  return {
    sidebarCollapsed,
    device,
    toggleSidebar,
    setDevice,
  }
})
