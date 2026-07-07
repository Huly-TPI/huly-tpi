import { createEvent, fireEvent, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import StonesLake from '../../../components/StonesLake/StonesLake'
import { ThemeProvider } from '../../../context/theme'

const playDrop = vi.fn()
const onStoneThrown = vi.fn()

vi.mock('../../../hooks/useWaterAudio', () => ({
  useWaterAudio: () => ({ playDrop }),
}))

class MockResizeObserver {
  observe = vi.fn()
  disconnect = vi.fn()
}

describe('StonesLake', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setupAnimationFrameStub()
    vi.stubGlobal('ResizeObserver', MockResizeObserver)
    vi.stubGlobal('cancelAnimationFrame', vi.fn())
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(
      createContext() as unknown as CanvasRenderingContext2D,
    )
  })

  afterEach(() => {
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it('renderiza la superficie del lago con su etiqueta accesible', () => {
    renderLake()
    verifyCanvasHasClass('stones-lake__canvas')
  })

  it('lanza una piedra que suena y ondula recién al impactar en el agua', () => {
    renderLake()
    throwStoneAt(40, 40)
    verifyStoneThrownCalledTimes(1)
    verifyPlayDropCalledTimes(0)
    advanceFrame(600)
    verifyPlayDropCalledTimes(1)
  })

  it('ignora los clicks del botón derecho', () => {
    renderLake()
    rightClickAt(40, 40)
    advanceFrame(600)
    verifyPlayDropCalledTimes(0)
  })

  let frameCallback: FrameRequestCallback | null
  let nowValue: number

  const setupAnimationFrameStub = () => {
    frameCallback = null
    nowValue = 0
    vi.stubGlobal('requestAnimationFrame', (cb: FrameRequestCallback) => {
      frameCallback = cb
      return 1
    })
    vi.spyOn(performance, 'now').mockImplementation(() => nowValue)
  }

  const renderLake = () =>
    render(
      <ThemeProvider>
        <StonesLake onStoneThrown={onStoneThrown} />
      </ThemeProvider>,
    )

  const getLakeCanvas = () =>
    screen.getByLabelText(
      'Lago de agua. Hacé clic para lanzar piedras y ver las ondas expandirse',
    )

  const verifyCanvasHasClass = (className: string) => {
    expect(getLakeCanvas()).toHaveClass(className)
  }

  const throwStoneAt = (clientX: number, clientY: number) =>
    fireEvent.pointerDown(getLakeCanvas(), { isPrimary: true, button: 0, clientX, clientY })

  const rightClickAt = (clientX: number, clientY: number) => {
    const canvas = getLakeCanvas()
    const event = createEvent.pointerDown(canvas, { clientX, clientY })
    Object.defineProperty(event, 'button', { value: 2 })
    fireEvent(canvas, event)
  }

  const advanceFrame = (time: number) => {
    nowValue = time
    frameCallback?.(time)
  }

  const verifyStoneThrownCalledTimes = (times: number) => {
    expect(onStoneThrown).toHaveBeenCalledTimes(times)
  }

  const verifyPlayDropCalledTimes = (times: number) => {
    expect(playDrop).toHaveBeenCalledTimes(times)
  }
})

function createContext() {
  return {
    setTransform: vi.fn(),
    clearRect: vi.fn(),
    drawImage: vi.fn(),
    save: vi.fn(),
    restore: vi.fn(),
    translate: vi.fn(),
    rotate: vi.fn(),
    beginPath: vi.fn(),
    arc: vi.fn(),
    ellipse: vi.fn(),
    moveTo: vi.fn(),
    lineTo: vi.fn(),
    stroke: vi.fn(),
    fill: vi.fn(),
    createRadialGradient: vi.fn(() => ({ addColorStop: vi.fn() })),
    lineWidth: 0,
    strokeStyle: '',
    fillStyle: '',
  }
}
