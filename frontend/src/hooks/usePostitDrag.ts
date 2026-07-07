import { useCallback, useEffect, useRef, useState, type RefObject } from 'react'

export type DragState =
  | { mode: 'idle' }
  | { mode: 'following'; taskId: number; x: number; y: number }

const MIN_PERCENT = 4
const EDGE_SAFETY_BUFFER_PERCENT = 2

export function pixelToPercent(
  clientX: number,
  clientY: number,
  boardRect: DOMRect,
  marginXPercent: number = MIN_PERCENT,
  marginYPercent: number = MIN_PERCENT,
): { x: number; y: number } {
  const rawX = boardRect.width === 0 ? 0 : ((clientX - boardRect.left) / boardRect.width) * 100
  const rawY = boardRect.height === 0 ? 0 : ((clientY - boardRect.top) / boardRect.height) * 100
  const clampedMarginX = Math.min(marginXPercent, 45)
  const clampedMarginY = Math.min(marginYPercent, 45)
  return {
    x: Math.min(100 - clampedMarginX, Math.max(clampedMarginX, rawX)),
    y: Math.min(100 - clampedMarginY, Math.max(clampedMarginY, rawY)),
  }
}

export function usePostitDrag(
  boardRef: RefObject<HTMLDivElement>,
  onPlace: (taskId: number, positionX: number, positionY: number) => void,
  followRef?: RefObject<HTMLDivElement>,
) {
  const [dragState, setDragState] = useState<DragState>({ mode: 'idle' })
  const dragStateRef = useRef(dragState)
  dragStateRef.current = dragState

  const stopFollowing = useCallback(() => {
    document.body.style.touchAction = ''
    setDragState({ mode: 'idle' })
  }, [])

  const pickUp = useCallback((taskId: number) => {
    document.body.style.touchAction = 'none'
    setDragState({ mode: 'following', taskId, x: window.innerWidth / 2, y: window.innerHeight / 2 })
  }, [])

  const cancel = useCallback(() => {
    stopFollowing()
  }, [stopFollowing])

  useEffect(() => {
    if (dragState.mode !== 'following') return

    const handlePointerMove = (event: PointerEvent) => {
      setDragState(prev => (prev.mode === 'following' ? { ...prev, x: event.clientX, y: event.clientY } : prev))
    }

    const handleBoardPointerDown = (event: PointerEvent) => {
      const current = dragStateRef.current
      if (current.mode !== 'following') return
      const board = boardRef.current
      if (!board) return
      const rect = board.getBoundingClientRect()
      const postitRect = followRef?.current?.getBoundingClientRect()
      const marginXPercent =
        postitRect && rect.width > 0 ? (postitRect.width / 2 / rect.width) * 100 + EDGE_SAFETY_BUFFER_PERCENT : MIN_PERCENT
      const marginYPercent =
        postitRect && rect.height > 0 ? (postitRect.height / 2 / rect.height) * 100 + EDGE_SAFETY_BUFFER_PERCENT : MIN_PERCENT
      const { x, y } = pixelToPercent(event.clientX, event.clientY, rect, marginXPercent, marginYPercent)
      onPlace(current.taskId, x, y)
      stopFollowing()
    }

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        cancel()
      }
    }

    window.addEventListener('pointermove', handlePointerMove)
    window.addEventListener('keydown', handleKeyDown)
    const board = boardRef.current
    board?.addEventListener('pointerdown', handleBoardPointerDown)

    return () => {
      window.removeEventListener('pointermove', handlePointerMove)
      window.removeEventListener('keydown', handleKeyDown)
      board?.removeEventListener('pointerdown', handleBoardPointerDown)
    }
  }, [dragState.mode, boardRef, followRef, onPlace, stopFollowing, cancel])

  return { dragState, pickUp, cancel }
}
