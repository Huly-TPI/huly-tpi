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
    const rect = makeRect()
    const result = pixelToPercent(100, 50, rect)
    expect(result.x).toBeCloseTo(50)
    expect(result.y).toBeCloseTo(50)
  })

  it('clampea a un mínimo de 4%', () => {
    const rect = makeRect()
    const result = pixelToPercent(-1000, -1000, rect)
    expect(result.x).toBe(4)
    expect(result.y).toBe(4)
  })

  it('clampea a un máximo de 96%', () => {
    const rect = makeRect()
    const result = pixelToPercent(10000, 10000, rect)
    expect(result.x).toBe(96)
    expect(result.y).toBe(96)
  })

  it('tiene en cuenta el offset del tablero', () => {
    const rect = makeRect({ left: 50, top: 20, right: 250, bottom: 120 })
    const result = pixelToPercent(150, 70, rect)
    expect(result.x).toBeCloseTo(50)
    expect(result.y).toBeCloseTo(50)
  })
})

describe('usePostitDrag', () => {
  it('pickUp entra en modo following', () => {
    const boardRef = createRef<HTMLDivElement>()
    const onPlace = vi.fn()
    const { result } = renderHook(() => usePostitDrag(boardRef, onPlace))

    act(() => result.current.pickUp(1))

    expect(result.current.dragState.mode).toBe('following')
  })

  it('cancel vuelve a modo idle sin llamar onPlace', () => {
    const boardRef = createRef<HTMLDivElement>()
    const onPlace = vi.fn()
    const { result } = renderHook(() => usePostitDrag(boardRef, onPlace))

    act(() => result.current.pickUp(1))
    act(() => result.current.cancel())

    expect(result.current.dragState.mode).toBe('idle')
    expect(onPlace).not.toHaveBeenCalled()
  })

  it('un pointerdown en el tablero mientras sigue al puntero llama a onPlace y vuelve a idle', () => {
    const boardEl = document.createElement('div')
    document.body.appendChild(boardEl)
    vi.spyOn(boardEl, 'getBoundingClientRect').mockReturnValue(makeRect())
    const boardRef = createRef<HTMLDivElement>()
    // @ts-expect-error asignación manual para test
    boardRef.current = boardEl

    const onPlace = vi.fn()
    const { result } = renderHook(() => usePostitDrag(boardRef, onPlace))

    act(() => result.current.pickUp(7))

    act(() => {
      boardEl.dispatchEvent(new MouseEvent('pointerdown', { clientX: 100, clientY: 50, bubbles: true }))
    })

    expect(onPlace).toHaveBeenCalledWith(7, 50, 50)
    expect(result.current.dragState.mode).toBe('idle')

    document.body.removeChild(boardEl)
  })

  it('Escape cancela el modo following', () => {
    const boardRef = createRef<HTMLDivElement>()
    const onPlace = vi.fn()
    const { result } = renderHook(() => usePostitDrag(boardRef, onPlace))

    act(() => result.current.pickUp(1))
    act(() => {
      window.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))
    })

    expect(result.current.dragState.mode).toBe('idle')
    expect(onPlace).not.toHaveBeenCalled()
  })
})
