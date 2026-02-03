<template>
  <div class="formula-backdrop" aria-hidden="true">
    <canvas ref="canvasRef" class="formula-canvas" />
    <div class="formula-grain" />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'

const canvasRef = ref(null)

let animationFrameId = 0
let resizeTimer = 0
let ctx = null
let cssWidth = 0
let cssHeight = 0
let dpr = 1
let lastFrameTime = 0

const clamp = (n, min, max) => Math.max(min, Math.min(max, n))

const parseRgba = (value) => {
  const v = String(value || '').trim()
  const m = v.match(/rgba?\(\s*([\d.]+)\s*,\s*([\d.]+)\s*,\s*([\d.]+)(?:\s*,\s*([\d.]+)\s*)?\)/i)
  if (!m) return null
  return {
    r: clamp(Number(m[1]), 0, 255),
    g: clamp(Number(m[2]), 0, 255),
    b: clamp(Number(m[3]), 0, 255),
    a: m[4] == null ? 1 : clamp(Number(m[4]), 0, 1),
  }
}

const rgbaWithAlpha = (raw, alpha) => {
  const parsed = parseRgba(raw)
  if (!parsed) return raw
  return `rgba(${parsed.r}, ${parsed.g}, ${parsed.b}, ${clamp(alpha, 0, 1)})`
}

const getThemeColors = () => {
  if (typeof window === 'undefined') {
    return {
      glow1: 'rgba(0, 122, 255, 0.25)',
      glow2: 'rgba(139, 128, 249, 0.25)',
    }
  }
  const styles = getComputedStyle(document.documentElement)
  const glow1 = styles.getPropertyValue('--theme-glow-1')?.trim() || 'rgba(0, 122, 255, 0.25)'
  const glow2 = styles.getPropertyValue('--theme-glow-2')?.trim() || 'rgba(139, 128, 249, 0.25)'
  return { glow1, glow2 }
}

const prefersReducedMotion = () =>
  typeof window !== 'undefined' &&
  window.matchMedia &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches

const resizeCanvas = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  cssWidth = Math.max(1, window.innerWidth || 1)
  cssHeight = Math.max(1, window.innerHeight || 1)
  dpr = clamp(window.devicePixelRatio || 1, 1, 2)

  canvas.style.width = `${cssWidth}px`
  canvas.style.height = `${cssHeight}px`
  canvas.width = Math.floor(cssWidth * dpr)
  canvas.height = Math.floor(cssHeight * dpr)

  ctx = canvas.getContext('2d', { alpha: true, desynchronized: true })
  if (ctx) ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
}

const drawCurve = ({ phase, a, b, wobble, stroke, width, blurPx, alpha }) => {
  if (!ctx) return
  const w = cssWidth
  const h = cssHeight
  const size = Math.min(w, h)
  const scale = size * 0.36
  const cx = w * 0.52
  const cy = h * 0.32

  ctx.save()
  ctx.globalCompositeOperation = 'lighter'
  ctx.globalAlpha = alpha
  ctx.lineWidth = width
  ctx.lineCap = 'round'
  ctx.lineJoin = 'round'
  ctx.strokeStyle = stroke
  ctx.filter = `blur(${blurPx}px)`

  const loops = 2.5
  const step = 0.018
  let first = true

  ctx.beginPath()
  for (let t = 0; t < Math.PI * 2 * loops; t += step) {
    const p = phase + t
    const x =
      Math.sin(a * t + p * 0.85) * (1 - wobble) +
      Math.sin((a + 2) * t + p * 0.55) * wobble
    const y =
      Math.sin(b * t + p * 0.75) * (1 - wobble) +
      Math.sin((b + 3) * t + p * 0.5) * wobble

    const px = cx + x * scale
    const py = cy + y * (scale * 0.85)

    if (first) {
      ctx.moveTo(px, py)
      first = false
    } else {
      ctx.lineTo(px, py)
    }
  }
  ctx.stroke()
  ctx.restore()
}

const drawFrame = (timeMs) => {
  if (!ctx) return

  if (prefersReducedMotion()) {
    if (lastFrameTime) return
  } else {
    // ~30fps throttle
    if (timeMs - lastFrameTime < 32) {
      animationFrameId = requestAnimationFrame(drawFrame)
      return
    }
  }

  lastFrameTime = timeMs

  ctx.clearRect(0, 0, cssWidth, cssHeight)

  const { glow1, glow2 } = getThemeColors()
  const t = timeMs / 1000
  const baseAlpha = 0.45

  drawCurve({
    phase: t * 0.45,
    a: 3,
    b: 2,
    wobble: 0.18,
    stroke: rgbaWithAlpha(glow1, 0.85),
    width: 2.2,
    blurPx: 18,
    alpha: baseAlpha,
  })

  drawCurve({
    phase: t * 0.35 + 1.2,
    a: 5,
    b: 4,
    wobble: 0.22,
    stroke: rgbaWithAlpha(glow2, 0.8),
    width: 2.1,
    blurPx: 20,
    alpha: baseAlpha * 0.9,
  })

  drawCurve({
    phase: t * 0.25 + 2.4,
    a: 7,
    b: 3,
    wobble: 0.14,
    stroke: rgbaWithAlpha(glow1, 0.65),
    width: 1.8,
    blurPx: 26,
    alpha: baseAlpha * 0.7,
  })

  animationFrameId = requestAnimationFrame(drawFrame)
}

const start = () => {
  cancelAnimationFrame(animationFrameId)
  animationFrameId = requestAnimationFrame(drawFrame)
}

const onResize = () => {
  clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    resizeCanvas()
    lastFrameTime = 0
    start()
  }, 120)
}

onMounted(() => {
  if (typeof window === 'undefined') return
  resizeCanvas()
  start()
  window.addEventListener('resize', onResize, { passive: true })
})

onBeforeUnmount(() => {
  if (typeof window !== 'undefined') window.removeEventListener('resize', onResize)
  cancelAnimationFrame(animationFrameId)
  clearTimeout(resizeTimer)
})
</script>

<style scoped lang="scss">
.formula-backdrop {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
  opacity: 0.9;
  mask-image: radial-gradient(circle at 50% 28%, #000 0%, rgba(0, 0, 0, 0.2) 52%, transparent 76%);
  -webkit-mask-image: radial-gradient(circle at 50% 28%, #000 0%, rgba(0, 0, 0, 0.2) 52%, transparent 76%);
}

.formula-canvas {
  width: 100%;
  height: 100%;
  display: block;
  opacity: 0.85;
  mix-blend-mode: screen;
}

.formula-grain {
  position: absolute;
  inset: 0;
  opacity: 0.16;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.65) 0 1px, transparent 2px),
    radial-gradient(circle at 70% 65%, rgba(255, 255, 255, 0.55) 0 1px, transparent 2px);
  background-size: 180px 180px, 220px 220px;
  background-position: 0 0, 90px 120px;
}

@media (prefers-reduced-motion: reduce) {
  .formula-grain {
    opacity: 0.12;
  }
}
</style>

