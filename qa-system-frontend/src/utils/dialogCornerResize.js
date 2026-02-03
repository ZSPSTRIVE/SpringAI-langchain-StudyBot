export function attachDialogCornerResize(dialogEl, options = {}) {
  if (!dialogEl) return () => {}

  const minWidth = Number(options.minWidth ?? 520)
  const minHeight = Number(options.minHeight ?? 200)

  dialogEl.style.position = 'relative'

  const corners = ['nw', 'ne', 'sw', 'se']

  const handles = new Map()

  const ensureHandle = (corner) => {
    let handle = dialogEl.querySelector(`.dialog-resize-handle[data-corner="${corner}"]`)
    if (!handle) {
      handle = document.createElement('div')
      handle.className = 'dialog-resize-handle'
      handle.dataset.corner = corner
      dialogEl.appendChild(handle)
    }
    handles.set(corner, handle)
    return handle
  }

  corners.forEach(ensureHandle)

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

    if (active.corner === 'se') {
      newW = active.startW + dx
      newH = active.startH + dy
    } else if (active.corner === 'sw') {
      newW = active.startW - dx
      newH = active.startH + dy
    } else if (active.corner === 'ne') {
      newW = active.startW + dx
      newH = active.startH - dy
    } else if (active.corner === 'nw') {
      newW = active.startW - dx
      newH = active.startH - dy
    }

    newW = Math.max(minWidth, Math.round(newW))
    newH = Math.max(minHeight, Math.round(newH))

    let deltaX = 0
    let deltaY = 0

    if (active.corner === 'sw' || active.corner === 'nw') {
      deltaX = active.startW - newW
    }
    if (active.corner === 'ne' || active.corner === 'nw') {
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

  const onPointerDown = (corner, e) => {
    if (e.button && e.button !== 0) return
    e.preventDefault()
    e.stopPropagation()

    const rect = dialogEl.getBoundingClientRect()
    const { x: tx, y: ty } = getTranslate()

    active = {
      corner,
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

  corners.forEach((corner) => {
    const handle = handles.get(corner)
    if (!handle) return
    const listener = (e) => onPointerDown(corner, e)
    handle.addEventListener('pointerdown', listener)
    handle.__cornerResizeListener = listener
  })

  return () => {
    stop()
    corners.forEach((corner) => {
      const handle = handles.get(corner)
      if (!handle) return
      const listener = handle.__cornerResizeListener
      if (listener) handle.removeEventListener('pointerdown', listener)
      handle.remove()
    })
  }
}
