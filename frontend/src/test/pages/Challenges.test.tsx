import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { ThemeProvider } from '../../context/theme'

vi.mock('../../assets/shared/day-background.webp', () => ({ default: 'day-bg.webp' }))
vi.mock('../../assets/shared/dark-background.webp', () => ({ default: 'dark-bg.webp' }))
vi.mock('../../assets/challenges/stump.png', () => ({ default: 'stump.png' }))
vi.mock('../../assets/challenges/watering-can.png', () => ({ default: 'watering-can.png' }))
vi.mock('../../assets/challenges/challenge-detail-bg.png', () => ({ default: 'challenge-detail-bg.png' }))
vi.mock('../../config/plantImages', () => ({
  getPlantAssets: vi.fn().mockReturnValue({
    stages: ['p0.png', 'p1.png', 'p2.png', 'p3.png', 'p4.png', 'p5.png'],
    flowerpotBase: 'flowerpot-base.png',
    flowerpotTop: 'flowerpot-top.png',
  }),
  getPlantImages: vi.fn().mockReturnValue(['p0.png', 'p1.png', 'p2.png', 'p3.png', 'p4.png', 'p5.png']),
}))

const mockRequireAuth = vi.hoisted(() => vi.fn((fn: () => void) => fn()))

vi.mock('../../context/authGate', () => ({
  useAuthGate: () => ({ requireAuth: mockRequireAuth }),
}))

const mockShowToast = vi.hoisted(() => vi.fn())

vi.mock('../../context/toast', () => ({
  useToast: () => ({ showToast: mockShowToast }),
}))

vi.mock('../../hooks/useUserGoals', () => ({
  useUserGoals: vi.fn(),
}))

vi.mock('../../api/activities', () => ({
  registerActivitySession: vi.fn().mockResolvedValue({}),
  ActivityType: {
    CHALLENGE: 'CHALLENGE',
  },
}))

vi.mock('../../components/Buttons/BackButton/BackButton', () => ({
  default: () => <button>Volver</button>,
}))

import Challenges from '../../pages/Challenges/Challenges'
import { useUserGoals } from '../../hooks/useUserGoals'
import type { UserGoalResponse } from '../../api/userGoals'
import { registerActivitySession } from '../../api/activities'
import { typePlaceholder, verifyTextPresent, verifyPlaceholderPresent, clearAllMocks } from '../testHelpers'

const mockedUseUserGoals = vi.mocked(useUserGoals)





const defaultHookReturn = {
  pendientes: makePageResponse([]),
  completados: makePageResponse([]),
  loading: false,
  error: null,
  createGoal: vi.fn().mockResolvedValue(undefined),
  updateGoal: vi.fn().mockResolvedValue(undefined),
  deleteGoal: vi.fn().mockResolvedValue(undefined),
  completeGoal: vi.fn().mockResolvedValue(undefined),
  reload: vi.fn(),
}

describe('Challenges', () => {
  let user: ReturnType<typeof userEvent.setup>
  let activeUnmount: () => void
  let dateNowSpy: any

  beforeEach(() => {
    clearAllMocks()
    mockedUseUserGoals.mockReturnValue(defaultHookReturn)
    mockRequireAuth.mockImplementation((fn: () => void) => fn())
  })

  it('renderiza el título de la página', () => {
    renderChallengesPage()
    verifyPageTitleVisible()
  })

  it('muestra el estado de carga', () => {
    setupLoadingState(true)
    renderChallengesPage()
    verifyLoadingMessageVisible()
  })

  it('muestra mensaje vacío cuando no hay retos pendientes', () => {
    renderChallengesPage()
    verifyEmptyStateMessageVisible()
  })

  it('muestra el botón para agregar un nuevo reto', () => {
    renderChallengesPage()
    verifyNewChallengeButtonVisible()
  })

  it('muestra los retos pendientes', () => {
    setupPendientesGoals([makeGoal({ id: 1, title: 'Mi primer reto' })])
    renderChallengesPage()
    verifyChallengeTitleVisible('Mi primer reto')
  })

  it('muestra los retos completados', () => {
    setupCompletadosGoals([makeGoal({ id: 2, title: 'Reto completado', status: 'COMPLETED' })])
    renderChallengesPage()
    verifyChallengeTitleVisible('Reto completado')
  })

  it('muestra error de la API', () => {
    setupErrorState('Error de red')
    renderChallengesPage()
    verifyErrorMessageVisible('Error de red')
  })

  it('muestra el progreso del ciclo en 0 cuando no hay completados', () => {
    renderChallengesPage()
    verifyProgressBarProgress('0')
  })

  it('abre el modal de creación al hacer click en Nuevo reto', () => {
    renderChallengesPage()
    return clickNewChallengeButton().then(() => {
      verifyCreateModalPlaceholderVisible()
    })
  })

  it('cierra el modal de creación al hacer click en Cancelar', () => {
    renderChallengesPage()
    return clickNewChallengeButton().then(() => {
      return clickCancelButton().then(() => {
        verifyCreateModalPlaceholderNotVisible()
      })
    })
  })

  it('llama a createGoal al guardar el formulario', () => {
    renderChallengesPage()
    return clickNewChallengeButton().then(() => {
      return fillChallengeTitle('Reto nuevo').then(() => {
        return clickSaveButton().then(() => {
          return waitForCreateGoalCall('Reto nuevo')
        })
      })
    })
  })

  it('no registra sesión al salir si solo creó retos sin completarlos', () => {
    setupDateNowMock('2025-01-01T10:00:00Z')
    renderChallengesPage()
    return createTwoChallengesAndUnmount('Reto uno', 'Reto dos', '2025-01-01T10:00:04Z').then(() => {
      return waitForActivitySessionNotCalled().then(() => {
        restoreDateMock()
      })
    })
  })

  it('registra la sesión al completar un reto', () => {
    setupDateNowMock('2025-01-01T10:00:00Z')
    setupPendientesGoals([makeGoal({ id: 7, title: 'Completar hoy' })])
    renderChallengesPage()
    setupDateNowMockTime('2025-01-01T10:00:06Z')
    return clickCompleteChallengeButton().then(() => {
      return waitForCompleteAndSessionRegistration(7, 'CHALLENGE').then(() => {
        restoreDateMock()
      })
    })
  })

  it('no registra sesión si entra y se va sin crear ni completar retos', () => {
    renderChallengesPage()
    unmountChallenges()
    verifyRegisterActivitySessionNotCalled()
  })

  it('abre el modal de detalle al hacer click en un reto', () => {
    setupPendientesGoals([makeGoal({ title: 'Ver detalle' })])
    renderChallengesPage()
    return clickChallengeDetailButton('Ver detalle').then(() => {
      verifyChallengeDetailHeadingVisible('Ver detalle')
    })
  })

  describe('acceso sin login', () => {
    beforeEach(() => {
      mockRequireAuth.mockImplementation(() => {})
    })

    it('llama a requireAuth al hacer click en Nuevo reto', () => {
      setupRequireAuthNoSession()
      renderChallengesPage()
      return clickNewChallengeButton().then(() => {
        verifyRequireAuthCalled()
      })
    })

    it('no abre el modal de creación cuando no hay sesión', () => {
      setupRequireAuthNoSession()
      renderChallengesPage()
      return clickNewChallengeButton().then(() => {
        verifyCreateModalPlaceholderNotVisible()
      })
    })
  })

  /* helpers */

  const renderChallengesPage = () => {
    user = userEvent.setup()
    const { unmount } = render(
      <ThemeProvider>
        <MemoryRouter>
          <Challenges />
        </MemoryRouter>
      </ThemeProvider>
    )
    activeUnmount = unmount
  }

  const unmountChallenges = () => {
    activeUnmount()
  }

  const setupLoadingState = (loading: boolean) => {
    mockedUseUserGoals.mockReturnValue({ ...defaultHookReturn, loading })
  }

  const setupErrorState = (error: string) => {
    mockedUseUserGoals.mockReturnValue({ ...defaultHookReturn, error })
  }

  const setupPendientesGoals = (goals: any[]) => {
    mockedUseUserGoals.mockReturnValue({
      ...defaultHookReturn,
      pendientes: makePageResponse(goals),
    })
  }

  const setupCompletadosGoals = (goals: any[]) => {
    mockedUseUserGoals.mockReturnValue({
      ...defaultHookReturn,
      completados: makePageResponse(goals),
    })
  }

  const setupRequireAuthNoSession = () => {
    mockRequireAuth.mockImplementation(() => {})
  }

  const verifyPageTitleVisible = () => {
    verifyTextPresent('Mis Retos')
  }

  const verifyLoadingMessageVisible = () => {
    verifyTextPresent('Cargando retos…')
  }

  const verifyEmptyStateMessageVisible = () => {
    verifyTextPresent('Sembrá tus metas. ¡Creá un nuevo reto!')
  }

  const verifyNewChallengeButtonVisible = () => {
    verifyTextPresent('+ Nuevo reto')
  }

  const verifyChallengeTitleVisible = (title: string) => {
    verifyTextPresent(title)
  }

  const verifyErrorMessageVisible = (error: string) => {
    verifyTextPresent(error)
  }

  const verifyProgressBarProgress = (progress: string) => {
    expect(screen.getByRole('progressbar', { name: `Progreso: ${progress}%` })).toBeInTheDocument()
  }

  const clickNewChallengeButton = () => {
    return user.click(screen.getByText('+ Nuevo reto'))
  }

  const verifyCreateModalPlaceholderVisible = () => {
    verifyPlaceholderPresent('¿Qué querés lograr?')
  }

  const verifyCreateModalPlaceholderNotVisible = () => {
    expect(screen.queryByPlaceholderText('¿Qué querés lograr?')).not.toBeInTheDocument()
  }

  const clickCancelButton = () => {
    return user.click(screen.getByText('Cancelar'))
  }

  const fillChallengeTitle = (title: string) => {
    return typePlaceholder(user, '¿Qué querés lograr?', title)
  }

  const clickSaveButton = () => {
    return user.click(screen.getByText('Guardar'))
  }

  const waitForCreateGoalCall = (title: string) => {
    return waitFor(() => {
      expect(defaultHookReturn.createGoal).toHaveBeenCalledWith({
        title,
      })
    })
  }

  const setupDateNowMock = (isoString: string) => {
    dateNowSpy = vi.spyOn(Date, 'now')
    dateNowSpy.mockReturnValue(new Date(isoString).getTime())
  }

  const setupDateNowMockTime = (isoString: string) => {
    dateNowSpy.mockReturnValue(new Date(isoString).getTime())
  }

  const restoreDateMock = () => {
    if (dateNowSpy) {
      dateNowSpy.mockRestore()
    }
  }

  const createChallengeSequence = (title: string) => {
    return clickNewChallengeButton().then(() => {
      return fillChallengeTitle(title).then(() => {
        return clickSaveButton()
      })
    })
  }

  const createTwoChallengesAndUnmount = (title1: string, title2: string, finalIsoString: string) => {
    return createChallengeSequence(title1).then(() => {
      return createChallengeSequence(title2).then(() => {
        setupDateNowMockTime(finalIsoString)
        unmountChallenges()
      })
    })
  }

  const waitForActivitySessionNotCalled = () => {
    return waitFor(() => {
      expect(registerActivitySession).not.toHaveBeenCalled()
    })
  }

  const verifyRegisterActivitySessionNotCalled = () => {
    expect(registerActivitySession).not.toHaveBeenCalled()
  }

  const clickCompleteChallengeButton = () => {
    return user.click(screen.getByLabelText('Completar reto'))
  }

  const waitForCompleteAndSessionRegistration = (goalId: number, activityType: string) => {
    return waitFor(() => {
      expect(defaultHookReturn.completeGoal).toHaveBeenCalledWith(goalId, undefined)
      expect(registerActivitySession).toHaveBeenCalledWith({
        activityType,
      }, undefined)
    })
  }

  const clickChallengeDetailButton = (title: string) => {
    return user.click(screen.getByLabelText(`Ver detalles: ${title}`))
  }

  const verifyChallengeDetailHeadingVisible = (title: string) => {
    expect(screen.getByRole('heading', { name: title })).toBeInTheDocument()
  }

  const verifyRequireAuthCalled = () => {
    expect(mockRequireAuth).toHaveBeenCalled()
  }
})

function makePageResponse(goals: Partial<UserGoalResponse>[] = []) {
  return ({
  content: goals as UserGoalResponse[],
  pageNumber: 0,
  pageSize: 50,
  totalElements: goals.length,
  totalPages: 1,
  first: true,
  last: true,
})
}

function makeGoal(overrides: Partial<UserGoalResponse> = {}): UserGoalResponse {
  return ({
  id: 1,
  userId: 10,
  title: 'Reto de prueba',
  description: null,
  status: 'PENDING',
  createdAt: '2026-01-01T00:00:00Z',
  activityId: null,
  imageUrl: null,
  coinsReward: 10,
  coinsRewardWithImage: 25,
  ...overrides,
})
}
