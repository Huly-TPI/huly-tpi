import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'

vi.mock('../../assets/garden/light-theme/background/day-background.webp', () => ({ default: 'day-bg.webp' }))
vi.mock('../../assets/challenges/stump.png', () => ({ default: 'stump.png' }))
vi.mock('../../assets/challenges/watering-can.png', () => ({ default: 'watering-can.png' }))
vi.mock('../../assets/challenges/challenge-detail-bg.png', () => ({ default: 'challenge-detail-bg.png' }))
vi.mock('../../assets/challenges/plant-stages/flowerpot-base.png', () => ({ default: 'flowerpot-base.png' }))
vi.mock('../../assets/challenges/plant-stages/flowerpot-top.png', () => ({ default: 'flowerpot-top.png' }))
vi.mock('../../assets/challenges/plant-stages/plant-0.png', () => ({ default: 'plant-0.png' }))
vi.mock('../../assets/challenges/plant-stages/plant-1.png', () => ({ default: 'plant-1.png' }))
vi.mock('../../assets/challenges/plant-stages/plant-2.png', () => ({ default: 'plant-2.png' }))
vi.mock('../../assets/challenges/plant-stages/plant-3.png', () => ({ default: 'plant-3.png' }))
vi.mock('../../assets/challenges/plant-stages/plant-4.png', () => ({ default: 'plant-4.png' }))
vi.mock('../../assets/challenges/plant-stages/plant-5.png', () => ({ default: 'plant-5.png' }))

vi.mock('../../hooks/useUserGoals', () => ({
  useUserGoals: vi.fn(),
}))

vi.mock('../../components/Buttons/BackButton/BackButton', () => ({
  default: () => <button>Volver</button>,
}))

import Challenges from '../../pages/Challenges/Challenges'
import { useUserGoals } from '../../hooks/useUserGoals'
import type { UserGoalResponse } from '../../api/userGoals'

const mockedUseUserGoals = vi.mocked(useUserGoals)

const makePageResponse = (goals: Partial<UserGoalResponse>[] = []) => ({
  content: goals as UserGoalResponse[],
  pageNumber: 0,
  pageSize: 50,
  totalElements: goals.length,
  totalPages: 1,
  first: true,
  last: true,
})

const makeGoal = (overrides: Partial<UserGoalResponse> = {}): UserGoalResponse => ({
  id: 1,
  userId: 10,
  title: 'Reto de prueba',
  description: null,
  status: 'PENDING',
  createdAt: '2026-01-01T00:00:00Z',
  activityId: null,
  ...overrides,
})

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
  beforeEach(() => {
    vi.clearAllMocks()
    mockedUseUserGoals.mockReturnValue(defaultHookReturn)
  })

  const renderChallenges = () => {
    const user = userEvent.setup()
    render(
      <MemoryRouter>
        <Challenges />
      </MemoryRouter>
    )
    return { user }
  }

  it('renderiza el título de la página', () => {
    renderChallenges()
    expect(screen.getByText('Mis Retos')).toBeInTheDocument()
  })

  it('muestra el estado de carga', () => {
    mockedUseUserGoals.mockReturnValue({ ...defaultHookReturn, loading: true })
    renderChallenges()
    expect(screen.getByText('Cargando retos…')).toBeInTheDocument()
  })

  it('muestra mensaje vacío cuando no hay retos pendientes', () => {
    renderChallenges()
    expect(screen.getByText('Sembrá tus metas. ¡Creá un nuevo reto!')).toBeInTheDocument()
  })

  it('muestra el botón para agregar un nuevo reto', () => {
    renderChallenges()
    expect(screen.getByText('+ Nuevo reto')).toBeInTheDocument()
  })

  it('muestra los retos pendientes', () => {
    mockedUseUserGoals.mockReturnValue({
      ...defaultHookReturn,
      pendientes: makePageResponse([makeGoal({ id: 1, title: 'Mi primer reto' })]),
    })
    renderChallenges()
    expect(screen.getByText('Mi primer reto')).toBeInTheDocument()
  })

  it('muestra los retos completados', () => {
    mockedUseUserGoals.mockReturnValue({
      ...defaultHookReturn,
      completados: makePageResponse([makeGoal({ id: 2, title: 'Reto completado', status: 'COMPLETED' })]),
    })
    renderChallenges()
    expect(screen.getByText('Reto completado')).toBeInTheDocument()
  })

  it('muestra error de la API', () => {
    mockedUseUserGoals.mockReturnValue({ ...defaultHookReturn, error: 'Error de red' })
    renderChallenges()
    expect(screen.getByText('Error de red')).toBeInTheDocument()
  })

  it('muestra el progreso del ciclo en 0 cuando no hay completados', () => {
    renderChallenges()
    expect(screen.getByRole('progressbar', { name: 'Progreso: 0%' })).toBeInTheDocument()
  })

  it('abre el modal de creación al hacer click en Nuevo reto', async () => {
    const { user } = renderChallenges()
    await user.click(screen.getByText('+ Nuevo reto'))
    expect(screen.getByPlaceholderText('¿Qué querés lograr?')).toBeInTheDocument()
  })

  it('cierra el modal de creación al hacer click en Cancelar', async () => {
    const { user } = renderChallenges()
    await user.click(screen.getByText('+ Nuevo reto'))
    await user.click(screen.getByText('Cancelar'))
    expect(screen.queryByPlaceholderText('¿Qué querés lograr?')).not.toBeInTheDocument()
  })

  it('llama a createGoal al guardar el formulario', async () => {
    const { user } = renderChallenges()
    await user.click(screen.getByText('+ Nuevo reto'))
    await user.type(screen.getByPlaceholderText('¿Qué querés lograr?'), 'Reto nuevo')
    await user.click(screen.getByText('Guardar'))

    await waitFor(() => {
      expect(defaultHookReturn.createGoal).toHaveBeenCalledWith({
        title: 'Reto nuevo',
        description: undefined,
      })
    })
  })

  it('abre el modal de detalle al hacer click en un reto', async () => {
    mockedUseUserGoals.mockReturnValue({
      ...defaultHookReturn,
      pendientes: makePageResponse([makeGoal({ title: 'Ver detalle' })]),
    })
    const { user } = renderChallenges()
    await user.click(screen.getByLabelText('Ver detalles: Ver detalle'))
    expect(screen.getByRole('heading', { name: 'Ver detalle' })).toBeInTheDocument()
  })
})
