import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
    // 'dynamic' = Glowing blobs, 'pure' = Solid/Gradient minimal
    const backgroundMode = ref(localStorage.getItem('theme_bg_mode') || 'dynamic')

    const toggleBackground = () => {
        backgroundMode.value = backgroundMode.value === 'dynamic' ? 'pure' : 'dynamic'
        localStorage.setItem('theme_bg_mode', backgroundMode.value)
    }

    const setBackground = (mode) => {
        if (['dynamic', 'pure'].includes(mode)) {
            backgroundMode.value = mode
            localStorage.setItem('theme_bg_mode', mode)
        }
    }

    return {
        backgroundMode,
        toggleBackground,
        setBackground
    }
})
