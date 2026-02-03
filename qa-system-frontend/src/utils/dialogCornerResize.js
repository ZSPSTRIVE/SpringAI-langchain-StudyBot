export function attachDialogCornerResize(dialogEl, options = {}) {
  if (!dialogEl) return () => {}

  const minWidth = Number(options.minWidth ?? 520)
  const minHeight = Number(options.minHeight ?? 220)

  dialogEl.style.position = 'relative'

  const corners = ['nw', 'ne', 'sw', 'se']
  const edges = ['n', 'e', 's', 'w']

  const handles = new Map()

  const ensureHandle = (type, id) => {
    const attr = type === 'corner' ? 'data-corner' : 'data-edge'
    let handle = dialogEl.querySelector(`.dialog-resize-handle[${attr}="${id}"]`)
    if (!handle) {
      handle = document.createElement('div')
      handle.className = 'dialog-resize-handle'
      if (type === 'corner') handle.dataset.corner = id
      else handle.dataset.edge = id
      dialogEl.appendChild(handle)
    }
    handles.set(`${type}:${id}`, handle)
    return handle
  }

  corners.forEach((c) => ensureHandle('corner', c))
  edges.forEach((e) => ensureHandle('edge', e))

  const getTranslate = () => {
    const style = window.getComputedStyle(dialogEl)
    const transform = style.transform
    if (!transform || transform === 'none') return { x: 0, y: 0 }
    try {
      const matrix = new DOMMatrixReadOnly(transform)
      return { x: matrix.m41 || 0, y: matrix.m42 || 0 }
    } catch {
      return { x: 0, y: 0 }
    }
  }

  let active = null

  const onPointerMove = (e) => {
    if (!active) return

    const dx = e.clientX - active.startX
    const dy = e.clientY - active.startY

    let newW = active.startW
    let newH = active.startH

    const handle = active.handle
    const affectsWidth = handle.includes('e') || handle.includes('w')
    const affectsHeight = handle.includes('n') || handle.includes('s')

    if (affectsWidth) {
      newW = handle.includes('e') ? active.startW + dx : active.startW - dx
    }
    if (affectsHeight) {
      newH = handle.includes('s') ? active.startH + dy : active.startH - dy
    }

    newW = Math.max(minWidth, Math.round(newW))
    newH = Math.max(minHeight, Math.round(newH))

    let deltaX = 0
    let deltaY = 0

    if (handle.includes('w')) {
      deltaX = active.startW - newW
    }
    if (handle.includes('n')) {
      deltaY = active.startH - newH
    }

    dialogEl.style.width = `${newW}px`
    dialogEl.style.height = `${newH}px`
    dialogEl.style.transform = `translate(${active.startTx + deltaX}px, ${active.startTy + deltaY}px)`
  }

  const stop = () => {
    if (!active) return
    active = null
    window.removeEventListener('pointermove', onPointerMove)
    window.removeEventListener('pointerup', stop)
    window.removeEventListener('pointercancel', stop)
  }

  const onPointerDown = (handle, e) => {
    if (e.button && e.button !== 0) return
    e.preventDefault()
    e.stopPropagation()

    const rect = dialogEl.getBoundingClientRect()
    const { x: tx, y: ty } = getTranslate()

    active = {
      handle,
      startX: e.clientX,
      startY: e.clientY,
      startW: rect.width,
      startH: rect.height,
      startTx: tx,
      startTy: ty
    }

    window.addEventListener('pointermove', onPointerMove)
    window.addEventListener('pointerup', stop)
    window.addEventListener('pointercancel', stop)
  }

  const allHandles = [
    ...corners.map((c) => ({ type: 'corner', id: c })),
    ...edges.map((e) => ({ type: 'edge', id: e }))
  ]

  allHandles.forEach(({ type, id }) => {
    const el = handles.get(`${type}:${id}`)
    if (!el) return
    const listener = (e) => onPointerDown(id, e)
    el.addEventListener('pointerdown', listener)
    el.__cornerResizeListener = listener
  })

  return () => {
    stop()
    allHandles.forEach(({ type, id }) => {
      const el = handles.get(`${type}:${id}`)
      if (!el) return
      const listener = el.__cornerResizeListener
      if (listener) el.removeEventListener('pointerdown', listener)
      el.remove()
    })
  }
}
