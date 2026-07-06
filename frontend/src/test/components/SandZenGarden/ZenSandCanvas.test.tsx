import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ZenSandCanvas from '../../../components/SandZenGarden/ZenSandCanvas'

vi.mock('../../../hooks/useSandAudio', () => ({
  useSandAudio: () => ({
    start: vi.fn(),
    stop: vi.fn(),
    update: vi.fn(),
  }),
}))

class MockResizeObserver {
  observe = vi.fn()
  disconnect = vi.fn()
}





describe('ZenSandCanvas', () => {
  beforeEach(() => {
    vi.stubGlobal('ResizeObserver', MockResizeObserver)
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(
      createContext() as unknown as CanvasRenderingContext2D,
    )
  })

  it('renderiza el marco del arenero y limita la superficie interactiva al canvas interno', () => {
    renderDefaultCanvas()
    verifyInteractiveSurfaceHasClass('zen-sand__canvas')
    verifyExportButtonHasClass('zen-sand__export-button')
    verifyClearButtonHasClass('zen-sand__clear-button')
    verifyFrameImagePresent()
  })
  let renderResult: any

  /* helpers */

  const renderDefaultCanvas = () => {
    renderResult = render(<ZenSandCanvas onDraw={vi.fn()} />)
  }

  const verifyInteractiveSurfaceHasClass = (className: string) => {
    expect(
      screen.getByLabelText('Superficie de arena interactiva para dibujar'),
    ).toHaveClass(className)
  }

  const verifyExportButtonHasClass = (className: string) => {
    expect(screen.getByRole('button', { name: 'Descargar dibujo de arena' })).toHaveClass(className)
  }

  const verifyClearButtonHasClass = (className: string) => {
    expect(screen.getByRole('button', { name: 'Limpiar arena y borrar todos los trazos' })).toHaveClass(className)
  }

  const verifyFrameImagePresent = () => {
    expect(renderResult.container.querySelector('.zen-sand__frame-image')).toBeInTheDocument()
  }
})

function createGradient() {
  return ({
  addColorStop: vi.fn(),
})
}

function createContext() {
  return ({
  setTransform: vi.fn(),
  clearRect: vi.fn(),
  createLinearGradient: vi.fn(createGradient),
  createRadialGradient: vi.fn(createGradient),
  fillRect: vi.fn(),
  beginPath: vi.fn(),
  arc: vi.fn(),
  fill: vi.fn(),
  moveTo: vi.fn(),
  lineTo: vi.fn(),
  stroke: vi.fn(),
  imageSmoothingEnabled: true,
  fillStyle: '',
  lineCap: '',
  lineJoin: '',
  lineWidth: 0,
  strokeStyle: '',
})
}
