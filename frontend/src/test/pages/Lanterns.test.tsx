import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { ThemeProvider } from '../../context/theme'

vi.mock('../../assets/lanterns/ligth-theme/background-lanterns.webp', () => ({ default: 'bg.webp' }))
vi.mock('../../assets/lanterns/dark-theme/background/night-background.webp', () => ({ default: 'dark-bg.webp' }))
vi.mock('../../assets/lanterns/ligth-theme/Lantern-Ligth.webp', () => ({ default: 'lantern.webp' }))
vi.mock('../../assets/lanterns/dark-theme/lantern-dark.webp', () => ({ default: 'lantern-dark.webp' }))
vi.mock('../../assets/lanterns/paper.webp', () => ({ default: 'paper.webp' }))

const mockRequireAuth = vi.hoisted(() => vi.fn((fn: () => void) => fn()))

vi.mock('../../context/authGate', () => ({
  useAuthGate: () => ({ requireAuth: mockRequireAuth }),
}))

vi.mock('../../api/lanterns', () => ({
  lanternsApi: {
    list: vi.fn(),
    create: vi.fn(),
    updateStatus: vi.fn(),
    markWorkedOn: vi.fn(),
  },
}))

vi.mock('../../api/activities', () => ({
  registerActivitySession: vi.fn().mockResolvedValue({}),
  ActivityType: {
    LANTERN: 'LANTERN',
  },
}))

import LanternsActivity from '../../pages/Lanterns/Lanterns'
import { lanternsApi } from '../../api/lanterns'
import { registerActivitySession } from '../../api/activities'

describe('LanternsActivity', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockRequireAuth.mockImplementation((fn: () => void) => fn())
    vi.mocked(lanternsApi.list).mockResolvedValue([
      { id: 1, text: 'Pensamiento 1', workedOn: false, createdAt: '2026-01-01T00:00:00Z' },
    ])
    vi.mocked(lanternsApi.create).mockResolvedValue({
      id: 2,
      text: 'Pensamiento 2',
      workedOn: false,
      createdAt: '2026-01-01T00:00:00Z',
    })
    vi.mocked(lanternsApi.updateStatus).mockResolvedValue(undefined)
    vi.mocked(lanternsApi.markWorkedOn).mockResolvedValue(undefined)
  })

  const renderLanterns = () => {
    const user = userEvent.setup()
    const view = render(
      <ThemeProvider>
        <MemoryRouter>
          <LanternsActivity />
        </MemoryRouter>
      </ThemeProvider>
    )
    return { user, ...view }
  }

  it('renderiza la vista de faroles y lista los pensamientos', async () => {
    renderLanterns()
    await waitFor(() => {
      expect(screen.getAllByText('Pensamiento 1').length).toBeGreaterThan(0)
    })
  })

  it('no registra sesion si entra y sale sin liberar, soltar o completar', async () => {
    const { unmount } = renderLanterns()
    await waitFor(() => {
      expect(screen.getAllByText('Pensamiento 1').length).toBeGreaterThan(0)
    })
    unmount()
    expect(registerActivitySession).not.toHaveBeenCalled()
  })

  it('registra sesion al salir si liberó un farol', async () => {
    const { user, unmount } = renderLanterns()
    await waitFor(() => {
      expect(screen.getAllByText('Pensamiento 1').length).toBeGreaterThan(0)
    })

    const liberarButton = screen.getAllByRole('button', { name: 'Liberar' })[0]
    await user.click(liberarButton)

    unmount()

    expect(registerActivitySession).toHaveBeenCalledTimes(1)
    expect(registerActivitySession).toHaveBeenCalledWith({
      activityType: 'LANTERN',
    }, { keepalive: true })
  })

  it('registra sesion al salir si soltó un farol nuevo', async () => {
    vi.mocked(lanternsApi.create).mockResolvedValue({
      id: 2,
      text: 'Pensamiento nuevo',
      workedOn: false,
      createdAt: '2026-01-01T00:00:00Z',
    })

    const { user, unmount } = renderLanterns()
    await waitFor(() => {
      expect(screen.getAllByText('Pensamiento 1').length).toBeGreaterThan(0)
    })

    const input = screen.getByPlaceholderText('¿Qué te da vueltas?')
    await user.type(input, 'Pensamiento nuevo')
    await user.click(screen.getByRole('button', { name: 'Soltar' }))

    await waitFor(() => {
      expect(lanternsApi.create).toHaveBeenCalledWith('Pensamiento nuevo')
    })

    unmount()

    expect(registerActivitySession).toHaveBeenCalledTimes(1)
    expect(registerActivitySession).toHaveBeenCalledWith({
      activityType: 'LANTERN',
    }, { keepalive: true })
  })

  it('registra sesion inmediatamente al completar un farol', async () => {
    vi.mocked(lanternsApi.list).mockResolvedValue([
      { id: 1, text: 'Pensamiento 1', workedOn: true, createdAt: '2026-01-01T00:00:00Z' },
    ])

    const { user } = renderLanterns()
    await waitFor(() => {
      expect(screen.getAllByText('Pensamiento 1').length).toBeGreaterThan(0)
    })

    const completarButton = screen.getAllByRole('button', { name: 'Completar' })[0]
    await user.click(completarButton)

    await waitFor(() => {
      expect(lanternsApi.updateStatus).toHaveBeenCalledWith(1, 'COMPLETED')
      expect(registerActivitySession).toHaveBeenCalledTimes(1)
      expect(registerActivitySession).toHaveBeenCalledWith({
        activityType: 'LANTERN',
      }, undefined)
    })
  })
})
