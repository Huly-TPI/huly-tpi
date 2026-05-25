import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, act } from '@testing-library/react'
import EmotionalCloud from '../../components/EmotionalClouds/EmotionalCloud'
import type { Cloud } from '../../components/EmotionalClouds'

vi.mock('./EmotionalClouds.css', () => ({}))

const mockCloud: Cloud = {
  id: 'cloud-1',
  text: 'Mis miedos',
  top: '20%',
  duration: '20s',
}

beforeEach(() => {
  vi.useFakeTimers()
})

afterEach(() => {
  vi.restoreAllMocks()
})

describe('EmotionalCloud', () => {

  describe('renderizado', () => {
    it('muestra el texto del pensamiento', () => {
      render(<EmotionalCloud cloud={mockCloud} onRemove={vi.fn()} />)
      expect(screen.getByText('Mis miedos')).toBeInTheDocument()
    })

    it('renderiza con el data-testid correcto', () => {
      render(<EmotionalCloud cloud={mockCloud} onRemove={vi.fn()} />)
      expect(screen.getByTestId('cloud-cloud-1')).toBeInTheDocument()
    })

    it('aplica los CSS custom properties correctos', () => {
      render(<EmotionalCloud cloud={mockCloud} onRemove={vi.fn()} />)
      const el = screen.getByTestId('cloud-cloud-1')
      expect(el).toHaveStyle({ '--cloud-top': '20%' })
      expect(el).toHaveStyle({ '--drift-duration': '20s' })
      expect(el).toHaveStyle({ '--drift-delay': '0s' })
    })

    it('usa valores por defecto cuando top y duration no están definidos', () => {
      const cloudSinOpcionales: Cloud = { id: 'cloud-2', text: 'Estrés' }
      render(<EmotionalCloud cloud={cloudSinOpcionales} onRemove={vi.fn()} />)
      const el = screen.getByTestId('cloud-cloud-2')
      expect(el).toHaveStyle({ '--cloud-top': '20%' })
      expect(el).toHaveStyle({ '--drift-duration': '20s' })
    })

    it('tiene role="status" y aria-live="polite"', () => {
      render(<EmotionalCloud cloud={mockCloud} onRemove={vi.fn()} />)
      const el = screen.getByRole('status')
      expect(el).toHaveAttribute('aria-live', 'polite')
    })

    it('no tiene la clase emotional-cloud--leaving al montar', () => {
      render(<EmotionalCloud cloud={mockCloud} onRemove={vi.fn()} />)
      const el = screen.getByTestId('cloud-cloud-1')
      expect(el).not.toHaveClass('emotional-cloud--leaving')
    })
  })

  describe('auto-remove por drift', () => {
    it('llama a onRemove después de la duración del drift', () => {
      const onRemove = vi.fn()
      render(<EmotionalCloud cloud={mockCloud} onRemove={onRemove} />)

      expect(onRemove).not.toHaveBeenCalled()

      act(() => { vi.advanceTimersByTime(20000) })

      expect(onRemove).toHaveBeenCalledTimes(1)
    })

    it('respeta la duración personalizada', () => {
      const onRemove = vi.fn()
      const cloudCorto: Cloud = { ...mockCloud, duration: '5s' }
      render(<EmotionalCloud cloud={cloudCorto} onRemove={onRemove} />)

      act(() => { vi.advanceTimersByTime(4999) })
      expect(onRemove).not.toHaveBeenCalled()

      act(() => { vi.advanceTimersByTime(1) })
      expect(onRemove).toHaveBeenCalledTimes(1)
    })

    it('cancela el timer al desmontar el componente', () => {
      const onRemove = vi.fn()
      const { unmount } = render(<EmotionalCloud cloud={mockCloud} onRemove={onRemove} />)

      unmount()
      act(() => { vi.advanceTimersByTime(20000) })

      expect(onRemove).not.toHaveBeenCalled()
    })
  })

  describe('click para hacer desaparecer', () => {
    it('agrega la clase emotional-cloud--leaving al hacer click', () => {
      render(<EmotionalCloud cloud={mockCloud} onRemove={vi.fn()} />)
      const el = screen.getByTestId('cloud-cloud-1')

      fireEvent.click(el)

      expect(el).toHaveClass('emotional-cloud--leaving')
    })

    it('llama a onRemove después de 3 segundos al hacer click', () => {
      const onRemove = vi.fn()
      render(<EmotionalCloud cloud={mockCloud} onRemove={onRemove} />)

      fireEvent.click(screen.getByTestId('cloud-cloud-1'))

      act(() => { vi.advanceTimersByTime(2999) })
      expect(onRemove).not.toHaveBeenCalled()

      act(() => { vi.advanceTimersByTime(1) })
      expect(onRemove).toHaveBeenCalledTimes(1)
    })

    it('ignora clicks adicionales si ya está en estado leaving', () => {
      const onRemove = vi.fn()
      render(<EmotionalCloud cloud={mockCloud} onRemove={onRemove} />)
      const el = screen.getByTestId('cloud-cloud-1')

      fireEvent.click(el)
      fireEvent.click(el)
      fireEvent.click(el)

      act(() => { vi.advanceTimersByTime(3000) })

      expect(onRemove).toHaveBeenCalledTimes(1)
    })
  })
})

