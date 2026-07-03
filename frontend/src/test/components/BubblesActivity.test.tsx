import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'

vi.mock('../../context/authGate', () => ({
  useAuthGate: () => ({
    requireAuth: (action: () => void) => action(),
  }),
}))

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

const mockedUseTracker = vi.mocked(useActivitySessionTracker)

const renderActivity = () =>
  render(
    <MemoryRouter>
      <BubblesActivity />
    </MemoryRouter>,
  )

describe('BubblesActivity component', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockSaveSession.mockResolvedValue(undefined)
  })

  it('renderiza el menú de inicio', () => {
    renderActivity()
    expect(screen.getByRole('button', { name: /comenzar/i })).toBeInTheDocument()
  })

  it('renderiza el componente de burbujas al hacer click en comenzar', async () => {
    const user = userEvent.setup()
    renderActivity()
    await user.click(screen.getByRole('button', { name: /comenzar/i }))
    expect(screen.getByTestId('bubbles-container')).toBeInTheDocument()
  })

  it('muestra el botón de volver una vez iniciado el juego', async () => {
    const user = userEvent.setup()
    renderActivity()

    expect(screen.queryByRole('button', { name: /volver/i })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /comenzar/i }))
    expect(screen.getByRole('button', { name: /volver/i })).toBeInTheDocument()
  })

  it('navega a minijuegos al hacer click en volver', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter initialEntries={['/bubbles']}>
        <Routes>
          <Route path="/minigames" element={<h1>Vista Minijuegos</h1>} />
          <Route path="/bubbles" element={<BubblesActivity />} />
        </Routes>
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: /comenzar/i }))
    await user.click(screen.getByRole('button', { name: /volver/i }))
    expect(await screen.findByRole('heading', { name: 'Vista Minijuegos' })).toBeInTheDocument()
  })

  it('llama a startSession al hacer click en "Comenzar"', async () => {
    const user = userEvent.setup()
    renderActivity()

    expect(mockStartSession).not.toHaveBeenCalled()

    await user.click(screen.getByRole('button', { name: /comenzar/i }))

    expect(mockStartSession).toHaveBeenCalledOnce()
  })

  it('llama a markConditionMet al interactuar con una burbuja', async () => {
    const user = userEvent.setup()
    renderActivity()

    await user.click(screen.getByRole('button', { name: /comenzar/i }))

    expect(mockMarkConditionMet).not.toHaveBeenCalled()

    // Click on the first bubble
    const bubble = screen.getByTestId('bubble-b-0')
    await user.click(bubble)

    expect(mockMarkConditionMet).toHaveBeenCalled()
  })

  it('usa los valores retornados por el hook mockeado', () => {
    renderActivity()

    // Verify the hook was called with the correct arguments
    expect(mockedUseTracker).toHaveBeenCalledWith(
      'BUBBLE',
      expect.objectContaining({ autoStart: false }),
    )
  })
})