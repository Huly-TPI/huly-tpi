import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

const mockStartSession = vi.fn()
const mockMarkConditionMet = vi.fn()
const mockStopSession = vi.fn()
const mockSaveSession = vi.fn().mockResolvedValue(undefined)

vi.mock('../../hooks/useActivitySessionTracker', () => ({
  useActivitySessionTracker: vi.fn(() => ({
    startSession: mockStartSession,
    markConditionMet: mockMarkConditionMet,
    stopSession: mockStopSession,
    saveSession: mockSaveSession,
  })),
}))

import BubblesActivity from '../../components/Bubbles/BubblesActivity.tsx'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'
import { clickButton, clearAllMocks } from '../testHelpers'

const mockedUseTracker = vi.mocked(useActivitySessionTracker)

describe('BubblesActivity component', () => {
  beforeEach(() => {
    clearAllMocks()
    mockSaveSession.mockResolvedValue(undefined)
  })

  it('renderiza el menú de inicio', () => {
    renderActivityComponent()
    verifyComenzarButtonPresent()
  })

  it('renderiza el componente de burbujas al hacer click en comenzar', () => {
    renderActivityComponent()
    return clickComenzar().then(() => {
      verifyBubblesContainerPresent()
    })
  })

  it('muestra el botón de volver una vez iniciado el juego', () => {
    renderActivityComponent()
    verifyVolverButtonNotPresent()
    return clickComenzar().then(() => {
      verifyVolverButtonPresent()
    })
  })

  it('navega a minijuegos al hacer click en volver', () => {
    renderActivityWithRoutes()
    return clickComenzar()
      .then(() => clickVolver())
      .then(() => verifyMinigamesViewPresent())
  })

  it('llama a startSession al hacer click en "Comenzar"', () => {
    renderActivityComponent()
    verifyStartSessionNotCalled()
    return clickComenzar().then(() => {
      verifyStartSessionCalledOnce()
    })
  })

  it('llama a markConditionMet al interactuar con una burbuja', () => {
    renderActivityComponent()
    return clickComenzar()
      .then(() => verifyMarkConditionMetNotCalled())
      .then(() => clickFirstBubble())
      .then(() => verifyMarkConditionMetCalled())
  })

  it('usa los valores retornados por el hook mockeado', () => {
    renderActivityComponent()
    verifyTrackerHookCalled()
  })

  /* helpers */

  const renderActivityComponent = () => {
    render(
      <MemoryRouter>
        <BubblesActivity />
      </MemoryRouter>,
    )
  }

  const renderActivityWithRoutes = () => {
    render(
      <MemoryRouter initialEntries={['/bubbles']}>
        <Routes>
          <Route path="/minigames" element={<h1>Vista Minijuegos</h1>} />
          <Route path="/bubbles" element={<BubblesActivity />} />
        </Routes>
      </MemoryRouter>,
    )
  }

  const clickComenzar = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Comenzar')
  }

  const clickVolver = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Volver')
  }

  const verifyComenzarButtonPresent = () => {
    expect(screen.getByRole('button', { name: 'Comenzar' })).toBeInTheDocument()
  }

  const verifyBubblesContainerPresent = () => {
    expect(screen.getByTestId('bubbles-container')).toBeInTheDocument()
  }

  const verifyVolverButtonNotPresent = () => {
    expect(screen.queryByRole('button', { name: 'Volver' })).not.toBeInTheDocument()
  }

  const verifyVolverButtonPresent = () => {
    expect(screen.getByRole('button', { name: 'Volver' })).toBeInTheDocument()
  }

  const verifyMinigamesViewPresent = () => {
    return screen.findByRole('heading', { name: 'Vista Minijuegos' }).then((el) => {
      expect(el).toBeInTheDocument()
    })
  }

  const verifyStartSessionNotCalled = () => {
    expect(mockStartSession).not.toHaveBeenCalled()
  }

  const verifyStartSessionCalledOnce = () => {
    expect(mockStartSession).toHaveBeenCalledOnce()
  }

  const clickFirstBubble = () => {
    const user = userEvent.setup()
    const bubble = screen.getByTestId('bubble-b-0')
    return user.click(bubble)
  }

  const verifyMarkConditionMetNotCalled = () => {
    expect(mockMarkConditionMet).not.toHaveBeenCalled()
  }

  const verifyMarkConditionMetCalled = () => {
    expect(mockMarkConditionMet).toHaveBeenCalled()
  }

  const verifyTrackerHookCalled = () => {
    expect(mockedUseTracker).toHaveBeenCalledWith(
      'BUBBLE',
      expect.objectContaining({ autoStart: false }),
    )
  }
})