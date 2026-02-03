
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const backgroundMode = ref('dynamic')
  const paletteIndex = ref(0)
  const palettes = [
    { glow1: 'rgba(0, 122, 255, 0.25)', glow2: 'rgba(139, 128, 249, 0.25)', pure1: '#f2f4f8', pure2: '#e5ebf3' },
    { glow1: 'rgba(120, 94, 255, 0.28)', glow2: 'rgba(255, 186, 130, 0.24)', pure1: '#f6f0ff', pure2: '#efe6ff' },
    { glow1: 'rgba(90, 200, 250, 0.28)', glow2: 'rgba(94, 92, 230, 0.2)', pure1: '#edf7ff', pure2: '#e6f1ff' },
    { glow1: 'rgba(255, 99, 132, 0.22)', glow2: 'rgba(255, 206, 86, 0.22)', pure1: '#fff2f3', pure2: '#ffe9ee' },
    { glow1: 'rgba(52, 199, 89, 0.24)', glow2: 'rgba(0, 122, 255, 0.2)', pure1: '#eefaf4', pure2: '#e6f5ef' },
    { glow1: 'rgba(255, 149, 0, 0.24)', glow2: 'rgba(255, 59, 48, 0.2)', pure1: '#fff5ea', pure2: '#ffede2' },
    { glow1: 'rgba(175, 82, 222, 0.26)', glow2: 'rgba(255, 45, 85, 0.18)', pure1: '#f6efff', pure2: '#efe5ff' },
    { glow1: 'rgba(100, 210, 255, 0.26)', glow2: 'rgba(90, 200, 250, 0.18)', pure1: '#eefbff', pure2: '#e2f4ff' },
    { glow1: 'rgba(255, 214, 10, 0.22)', glow2: 'rgba(255, 159, 10, 0.2)', pure1: '#fff8e6', pure2: '#fff0d9' },
    { glow1: 'rgba(94, 92, 230, 0.24)', glow2: 'rgba(48, 209, 88, 0.18)', pure1: '#eef0ff', pure2: '#e7eaff' },
    { glow1: 'rgba(255, 105, 180, 0.22)', glow2: 'rgba(88, 86, 214, 0.2)', pure1: '#fff0f7', pure2: '#f7e9ff' },
    { glow1: 'rgba(64, 156, 255, 0.24)', glow2: 'rgba(255, 153, 102, 0.22)', pure1: '#eef5ff', pure2: '#e6edff' }
  ]

  const applyPalette = (palette) => {
    if (typeof document === 'undefined' || !palette) return
    const root = document.documentElement
    root.style.setProperty('--theme-glow-1', palette.glow1)
    root.style.setProperty('--theme-glow-2', palette.glow2)
    root.style.setProperty('--theme-pure-1', palette.pure1)
    root.style.setProperty('--theme-pure-2', palette.pure2)
  }

  const setPaletteIndex = (index) => {
    const nextIndex = ((index % palettes.length) + palettes.length) % palettes.length
    paletteIndex.value = nextIndex
  }

  const nextPalette = () => {
    setPaletteIndex(paletteIndex.value + 1)
  }

  const toggleBackground = () => {
    backgroundMode.value = backgroundMode.value === 'dynamic' ? 'pure' : 'dynamic'
  }

  watch(paletteIndex, (value) => {
    applyPalette(palettes[value])
  }, { immediate: true })

  return {
    backgroundMode,
    paletteIndex,
    palettes,
    setPaletteIndex,
    nextPalette,
    toggleBackground
  }
}, {
  persist: {
    key: 'qa-system-theme',
    storage: localStorage,
    paths: ['backgroundMode', 'paletteIndex']
  }
})
