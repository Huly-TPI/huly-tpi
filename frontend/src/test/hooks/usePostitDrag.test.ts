import { describe, it, expect, vi } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { createRef } from 'react'
import { pixelToPercent, usePostitDrag } from '../../hooks/usePostitDrag'

function makeRect(overrides: Partial<DOMRect> = {}): DOMRect {
  return {
    x: 0, y: 0, width: 200, height: 100, top: 0, left: 0, right: 200, bottom: 100,
    toJSON: () => ({}),
    ...overrides,
  } as DOMRect
}

describe('pixelToPercent', () => {
  it('convierte coordenadas de pixeles a porcentaje relativo al tablero', () => {
    const result = callPixelToPercent(100, 50, makeRect())

    verifyX(result, 50)
    verifyY(result, 50)
  })

  it('clampea a un mínimo de 4%', () => {
    const result = callPixelToPercent(-1000, -1000, makeRect())

    expect(result.x).toBe(4)
    expect(result.y).toBe(4)
  })

  it('clampea a un máximo de 96%', () => {
    const result = callPixelToPercent(10000, 10000, makeRect())

    expect(result.x).toBe(96)
    expect(result.y).toBe(96)
  })

  it('tiene en cuenta el offset del tablero', () => {
    const result = callPixelToPercent(150, 70, makeRect({ left: 50, top: 20, right: 250, bottom: 120 }))

    verifyX(result, 50)
    verifyY(result, 50)
  })

  /* helpers */

  const callPixelToPercent = (clientX: number, clientY: number, rect: DOMRect) => {
    return pixelToPercent(clientX, clientY, rect)
  }

  const verifyX = (result: { x: number; y: number }, expected: number) => {
    expect(result.x).toBeCloseTo(expected)
  }

  const verifyY = (result: { x: number; y: number }, expected: number) => {
    expect(result.y).toBeCloseTo(expected)
  }
})

describe('usePostitDrag', () => {
  let rendered: ReturnType<typeof renderHook<ReturnType<typeof usePostitDrag>, undefined>>
  let onPlace: ReturnType<typeof vi.fn>

  it('pickUp entra en modo following', () => {
    setupHook(createRef<HTMLDivElement>())

    callPickUp(1)

    verifyDragMode('following')
  })

  it('cancel vuelve a modo idle sin llamar onPlace', () => {
    setupHook(createRef<HTMLDivElement>())

    callPickUp(1)
    callCancel()

    verifyDragMode('idle')
    verifyOnPlaceNotCalled()
  })

  it('un pointerup dentro del tablero mientras sigue al puntero llama a onPlace y vuelve a idle', () => {
    const boardEl = attachBoardElement()
    const boardRef = createRef<HTMLDivElement>()
    // @ts-expect-error asignación manual para test
    boardRef.current = boardEl
    setupHook(boardRef)

    callPickUp(7)
    dispatchWindowPointerUp(100, 50)

    verifyOnPlaceCalledWith(7, 50, 50)
    verifyDragMode('idle')

    document.body.removeChild(boardEl)
  })

  it('un pointerup fuera del tablero cancela sin llamar a onPlace', () => {
    const boardEl = attachBoardElement()
    const boardRef = createRef<HTMLDivElement>()
    // @ts-expect-error asignación manual para test
    boardRef.current = boardEl
    setupHook(boardRef)

    callPickUp(7)
    dispatchWindowPointerUp(-50, -50)

    verifyOnPlaceNotCalled()
    verifyDragMode('idle')

    document.body.removeChild(boardEl)
  })

  it('pickUp con origin inicializa dragState.x/y en ese origin en vez del centro de pantalla', () => {
    setupHook(createRef<HTMLDivElement>())

    callPickUp(1, { x: 30, y: 40 })

    verifyDragPosition(30, 40)
  })

  it('Escape cancela el modo following', () => {
    setupHook(createRef<HTMLDivElement>())

    callPickUp(1)
    dispatchEscapeKey()

    verifyDragMode('idle')
    verifyOnPlaceNotCalled()
  })

  /* helpers */

  const setupHook = (boardRef: React.RefObject<HTMLDivElement>) => {
    onPlace = vi.fn()
    rendered = renderHook(() => usePostitDrag(boardRef, onPlace))
  }

  const attachBoardElement = () => {
    const boardEl = document.createElement('div')
    document.body.appendChild(boardEl)
    vi.spyOn(boardEl, 'getBoundingClientRect').mockReturnValue(makeRect())
    return boardEl
  }

  const callPickUp = (taskId: number, origin?: { x: number; y: number }) => {
    act(() => rendered.result.current.pickUp(taskId, origin))
  }

  const callCancel = () => {
    act(() => rendered.result.current.cancel())
  }

  const dispatchWindowPointerUp = (clientX: number, clientY: number) => {
    act(() => {
      window.dispatchEvent(new MouseEvent('pointerup', { clientX, clientY, bubbles: true }))
    })
  }

  const verifyDragPosition = (x: number, y: number) => {
    const state = rendered.result.current.dragState
    expect(state.mode === 'following' && state.x).toBe(x)
    expect(state.mode === 'following' && state.y).toBe(y)
  }

  const dispatchEscapeKey = () => {
    act(() => {
      window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    })
  }

  const verifyDragMode = (expected: string) => {
    expect(rendered.result.current.dragState.mode).toBe(expected)
  }

  const verifyOnPlaceNotCalled = () => {
    expect(onPlace).not.toHaveBeenCalled()
  }

  const verifyOnPlaceCalledWith = (taskId: number, x: number, y: number) => {
    expect(onPlace).toHaveBeenCalledWith(taskId, x, y)
  }
})
