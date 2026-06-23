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

const createGradient = () => ({
  addColorStop: vi.fn(),
})

const createContext = () => ({
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

describe('ZenSandCanvas', () => {
  beforeEach(() => {
    vi.stubGlobal('ResizeObserver', MockResizeObserver)
    vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(
      createContext() as unknown as CanvasRenderingContext2D,
    )
  })

  it('renderiza el marco del arenero y limita la superficie interactiva al canvas interno', () => {
    const { container } = render(<ZenSandCanvas onDraw={vi.fn()} />)

    expect(
      screen.getByLabelText('Superficie de arena interactiva para dibujar'),
    ).toHaveClass('zen-sand__canvas')
    expect(screen.getByRole('button', { name: /descargar dibujo de arena/i })).toHaveClass('zen-sand__export-button')
    expect(screen.getByRole('button', { name: /limpiar arena/i })).toHaveClass('zen-sand__clear-button')
    expect(container.querySelector('.zen-sand__frame-image')).toBeInTheDocument()
  })
})
