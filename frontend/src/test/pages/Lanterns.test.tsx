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
import { clickButton, clearAllMocks } from '../testHelpers'

describe('LanternsActivity', () => {
  let user: ReturnType<typeof userEvent.setup>
  let activeUnmount: () => void

  beforeEach(() => {
    clearAllMocks()
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

  it('renderiza la vista de faroles y lista los pensamientos', () => {
    renderLanternsPage()
    return waitForPensamientoVisible('Pensamiento 1')
  })

  it('no registra sesion si entra y sale sin liberar, soltar o completar', () => {
    renderLanternsPage()
    return waitForPensamientoVisible('Pensamiento 1').then(() => {
      unmountLanterns()
      verifyRegisterActivitySessionNotCalled()
    })
  })

  it('registra sesion al salir si liberó un farol', () => {
    renderLanternsPage()
    return waitForPensamientoVisible('Pensamiento 1').then(() => {
      return clickLiberarButton().then(() => {
        unmountLanterns()
        verifyRegisterActivitySessionCalledWithKeepalive()
      })
    })
  })

  it('registra sesion al salir si soltó un farol nuevo', () => {
    setupCreateMockResponse('Pensamiento nuevo')
    renderLanternsPage()
    return waitForPensamientoVisible('Pensamiento 1').then(() => {
      return typeNewPensamiento('Pensamiento nuevo').then(() => {
        return clickSoltarButton().then(() => {
          return waitForCreateCall('Pensamiento nuevo').then(() => {
            unmountLanterns()
            verifyRegisterActivitySessionCalledWithKeepalive()
          })
        })
      })
    })
  })

  it('registra sesion inmediatamente al completar un farol', () => {
    setupListMockResponse([{ id: 1, text: 'Pensamiento 1', workedOn: true, createdAt: '2026-01-01T00:00:00Z' }])
    renderLanternsPage()
    return waitForPensamientoVisible('Pensamiento 1').then(() => {
      return clickCompletarButton().then(() => {
        return waitForCompletarVerification(1)
      })
    })
  })

  /* helpers */

  const renderLanternsPage = () => {
    user = userEvent.setup()
    const { unmount } = render(
      <ThemeProvider>
        <MemoryRouter>
          <LanternsActivity />
        </MemoryRouter>
      </ThemeProvider>
    )
    activeUnmount = unmount
  }

  const unmountLanterns = () => {
    activeUnmount()
  }

  const setupListMockResponse = (response: any) => {
    vi.mocked(lanternsApi.list).mockResolvedValue(response)
  }

  const setupCreateMockResponse = (text: string) => {
    vi.mocked(lanternsApi.create).mockResolvedValue({
      id: 2,
      text,
      workedOn: false,
      createdAt: '2026-01-01T00:00:00Z',
    })
  }

  const waitForPensamientoVisible = (text: string) => {
    return waitFor(() => {
      expect(screen.getAllByText(text).length).toBeGreaterThan(0)
    })
  }

  const clickLiberarButton = () => {
    const liberarButton = screen.getAllByRole('button', { name: 'Liberar' })[0]
    return user.click(liberarButton)
  }

  const typeNewPensamiento = (text: string) => {
    const input = screen.getByPlaceholderText('¿Qué te da vueltas?')
    return user.type(input, text)
  }

  const clickSoltarButton = () => {
    return clickButton(user, 'Soltar')
  }

  const waitForCreateCall = (text: string) => {
    return waitFor(() => {
      expect(lanternsApi.create).toHaveBeenCalledWith(text)
    })
  }

  const clickCompletarButton = () => {
    const completarButton = screen.getAllByRole('button', { name: 'Completar' })[0]
    return user.click(completarButton)
  }

  const waitForCompletarVerification = (id: number) => {
    return waitFor(() => {
      expect(lanternsApi.updateStatus).toHaveBeenCalledWith(id, 'COMPLETED')
      expect(registerActivitySession).toHaveBeenCalledTimes(1)
      expect(registerActivitySession).toHaveBeenCalledWith({
        activityType: 'LANTERN',
      }, undefined)
    })
  }

  const verifyRegisterActivitySessionNotCalled = () => {
    expect(registerActivitySession).not.toHaveBeenCalled()
  }

  const verifyRegisterActivitySessionCalledWithKeepalive = () => {
    expect(registerActivitySession).toHaveBeenCalledTimes(1)
    expect(registerActivitySession).toHaveBeenCalledWith({
      activityType: 'LANTERN',
    }, { keepalive: true })
  }
})
