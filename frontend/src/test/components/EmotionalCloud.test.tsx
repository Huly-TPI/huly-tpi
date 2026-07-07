import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, fireEvent, act } from '@testing-library/react'
import EmotionalCloud from '../../components/EmotionalClouds/EmotionalCloud'
import type { Cloud } from '../../components/EmotionalClouds'
import { verifyTextPresent } from '../testHelpers'

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
  let onRemoveSpy: any

  describe('renderizado', () => {
    it('muestra el texto del pensamiento', () => {
      renderWithMockCloud()
      verifyCloudText('Mis miedos')
    })

    it('renderiza con el data-testid correcto', () => {
      renderWithMockCloud()
      verifyCloudTestId('cloud-1')
    })

    it('aplica los CSS custom properties correctos', () => {
      renderWithMockCloud()
      verifyCloudStyles('cloud-1', {
        '--cloud-top': '20%',
        '--drift-duration': '20s',
        '--drift-delay': '0s',
      })
    })

    it('usa valores por defecto cuando top y duration no están definidos', () => {
      renderWithCloud({ id: 'cloud-2', text: 'Estrés' })
      verifyCloudStyles('cloud-2', {
        '--cloud-top': '20%',
        '--drift-duration': '20s',
      })
    })

    it('tiene role="status" y aria-live="polite"', () => {
      renderWithMockCloud()
      verifyCloudAccessibility()
    })

    it('no tiene la clase emotional-cloud--leaving al montar', () => {
      renderWithMockCloud()
      verifyCloudDoesNotHaveLeavingClass('cloud-1')
    })
  })

  describe('auto-remove por drift', () => {
    it('llama a onRemove después de la duración del drift', () => {
      renderWithSpy()
      advanceTime(20000)
      verifyOnRemoveCalledTimes(1)
    })

    it('respeta la duración personalizada', () => {
      renderWithCustomDuration('5s')
      advanceTime(4999)
      verifyOnRemoveCalledTimes(0)
      advanceTime(1)
      verifyOnRemoveCalledTimes(1)
    })

    it('cancela el timer al desmontar el componente', () => {
      renderAndUnmountWithSpy(20000)
      verifyOnRemoveCalledTimes(0)
    })
  })

  describe('click para hacer desaparecer', () => {
    it('agrega la clase emotional-cloud--leaving al hacer click', () => {
      renderWithMockCloud()
      clickCloud('cloud-1')
      verifyCloudHasLeavingClass('cloud-1')
    })

    it('llama a onRemove después de 3 segundos al hacer click', () => {
      renderWithSpy()
      clickCloud('cloud-1')
      advanceTime(2999)
      verifyOnRemoveCalledTimes(0)
      advanceTime(1)
      verifyOnRemoveCalledTimes(1)
    })

    it('ignora clicks adicionales si ya está en estado leaving', () => {
      renderWithSpy()
      clickCloudMultipleTimes('cloud-1', 3)
      advanceTime(3000)
      verifyOnRemoveCalledTimes(1)
    })
  })

  /* helpers */

  const renderWithMockCloud = () => {
    onRemoveSpy = vi.fn()
    render(<EmotionalCloud cloud={mockCloud} onRemove={onRemoveSpy} />)
  }

  const renderWithCloud = (cloud: Cloud) => {
    onRemoveSpy = vi.fn()
    render(<EmotionalCloud cloud={cloud} onRemove={onRemoveSpy} />)
  }

  const renderWithSpy = () => {
    onRemoveSpy = vi.fn()
    render(<EmotionalCloud cloud={mockCloud} onRemove={onRemoveSpy} />)
  }

  const renderWithCustomDuration = (duration: string) => {
    onRemoveSpy = vi.fn()
    const customCloud: Cloud = { ...mockCloud, duration }
    render(<EmotionalCloud cloud={customCloud} onRemove={onRemoveSpy} />)
  }

  const renderAndUnmountWithSpy = (timeToAdvance: number) => {
    onRemoveSpy = vi.fn()
    const { unmount } = render(<EmotionalCloud cloud={mockCloud} onRemove={onRemoveSpy} />)
    unmount()
    act(() => {
      vi.advanceTimersByTime(timeToAdvance)
    })
  }

  const verifyCloudText = (text: string) => {
    verifyTextPresent(text)
  }

  const verifyCloudTestId = (id: string) => {
    expect(screen.getByTestId(`cloud-${id}`)).toBeInTheDocument()
  }

  const verifyCloudStyles = (id: string, styles: Record<string, string>) => {
    const el = screen.getByTestId(`cloud-${id}`)
    expect(el).toHaveStyle(styles)
  }

  const verifyCloudAccessibility = () => {
    const el = screen.getByRole('status')
    expect(el).toHaveAttribute('aria-live', 'polite')
  }

  const verifyCloudDoesNotHaveLeavingClass = (id: string) => {
    const el = screen.getByTestId(`cloud-${id}`)
    expect(el).not.toHaveClass('emotional-cloud--leaving')
  }

  const verifyCloudHasLeavingClass = (id: string) => {
    const el = screen.getByTestId(`cloud-${id}`)
    expect(el).toHaveClass('emotional-cloud--leaving')
  }

  const advanceTime = (ms: number) => {
    act(() => {
      vi.advanceTimersByTime(ms)
    })
  }

  const verifyOnRemoveCalledTimes = (times: number) => {
    if (times === 0) {
      expect(onRemoveSpy).not.toHaveBeenCalled()
    } else {
      expect(onRemoveSpy).toHaveBeenCalledTimes(times)
    }
  }

  const clickCloud = (id: string) => {
    fireEvent.click(screen.getByTestId(`cloud-${id}`))
  }

  const clickCloudMultipleTimes = (id: string, times: number) => {
    const el = screen.getByTestId(`cloud-${id}`)
    for (let i = 0; i < times; i++) {
      fireEvent.click(el)
    }
  }
})
