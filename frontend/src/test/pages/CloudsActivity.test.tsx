import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import CloudsActivity from '../../pages/CloudsActivity/CloudsActivity'
import { api } from '../../api/client'
import { registerActivitySession } from '../../api/activities'

vi.mock('../../api/activities', () => ({
  ActivityType: {
    NUBE: 'NUBE',
  },
  registerActivitySession: vi.fn().mockResolvedValue(undefined),
}))

vi.mock('../../api/client', () => ({
  api: {
    post: vi.fn(),
  },
}))

vi.mock('../../components/EmotionalClouds', () => ({
  EmotionalCloudsActivity: ({ onThoughtAdded, onFinish, maxClouds }: {
    onThoughtAdded?: (thought: string) => void
    onFinish?: (thoughts: string[]) => void
    maxClouds: number
  }) => (
    <div data-testid="emotional-clouds-activity">
      <span data-testid="max-clouds">{maxClouds}</span>
      <button onClick={() => onThoughtAdded?.('nuevo pensamiento')}>add-thought</button>
      <button onClick={() => onFinish?.(['test'])}>finish</button>
    </div>
  ),
}))

const mockedPost = vi.mocked(api.post)
const mockedRegisterActivitySession = vi.mocked(registerActivitySession)

function makeRecommendation(activityType: string, redirectUrl: string) {
  return {
    activity_type: activityType,
    action_id: activityType,
    title: 'Título de prueba',
    description: 'Descripción de prueba.',
    redirect_url: redirectUrl,
  }
}

describe('CloudsActivity', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('renderiza EmotionalCloudsActivity', () => {
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)
    expect(screen.getByTestId('emotional-clouds-activity')).toBeInTheDocument()
  })

  it('pasa maxClouds={8} a EmotionalCloudsActivity', () => {
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)
    expect(screen.getByTestId('max-clouds')).toHaveTextContent('8')
  })

  it('llama a POST /clouds/thought cuando se agrega un pensamiento', async () => {
    mockedPost.mockResolvedValue(null)
    const user = userEvent.setup()
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'add-thought' }))

    expect(mockedPost).toHaveBeenCalledWith('/clouds/thought', { thought: 'nuevo pensamiento' })
  })

  it('no registra la sesión al cargar un pensamiento mientras sigue en la actividad', async () => {
    mockedPost.mockResolvedValue(null)
    const user = userEvent.setup()
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'add-thought' }))

    expect(mockedRegisterActivitySession).not.toHaveBeenCalled()
  })

  it('no registra sesión solo por finalizar si no se cargó un pensamiento nuevo', async () => {
    mockedPost.mockResolvedValueOnce(makeRecommendation('diary', '/diary'))
    const user = userEvent.setup()
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'finish' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Ir al diario' })).toBeInTheDocument()
    })

    expect(mockedRegisterActivitySession).not.toHaveBeenCalled()
  })

  it('registra la sesión al salir si se envió al menos un pensamiento', async () => {
    mockedPost.mockResolvedValue(null)
    const user = userEvent.setup()
    const { unmount } = render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'add-thought' }))

    unmount()

    await waitFor(() => {
      expect(mockedRegisterActivitySession).toHaveBeenCalledWith(
        { activityType: 'NUBE' },
        { keepalive: true },
      )
    })
  })

  it('no registra la sesión al salir si no se envió ningún pensamiento', () => {
    const { unmount } = render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    unmount()

    expect(mockedRegisterActivitySession).not.toHaveBeenCalled()
  })

  it('muestra botón "Ir al diario" cuando la recomendación es diary', async () => {
    mockedPost.mockResolvedValueOnce(makeRecommendation('diary', '/diary'))
    const user = userEvent.setup()
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'finish' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Ir al diario' })).toBeInTheDocument()
    })
  })

  it('muestra botón "Ir a las nubes" cuando la recomendación es clouds', async () => {
    mockedPost.mockResolvedValueOnce(makeRecommendation('clouds', '/clouds'))
    const user = userEvent.setup()
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'finish' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Ir a las nubes' })).toBeInTheDocument()
    })
  })

  it('muestra botón "Ir a respiración guiada" cuando la recomendación es breathing', async () => {
    mockedPost.mockResolvedValueOnce(makeRecommendation('breathing', '/guided-breathing'))
    const user = userEvent.setup()
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'finish' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: /respiración guiada/i })).toBeInTheDocument()
    })
  })

  it('muestra botón "Ir a las burbujas" cuando la recomendación es bubbles', async () => {
    mockedPost.mockResolvedValueOnce(makeRecommendation('bubbles', '/bubbles'))
    const user = userEvent.setup()
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'finish' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Ir a las burbujas' })).toBeInTheDocument()
    })
  })

  it('muestra botón "Ir a la actividad" para un tipo de actividad desconocido', async () => {
    mockedPost.mockResolvedValueOnce(makeRecommendation('unknown', '/unknown'))
    const user = userEvent.setup()
    render(<MemoryRouter><CloudsActivity /></MemoryRouter>)

    await user.click(screen.getByRole('button', { name: 'finish' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Ir a la actividad' })).toBeInTheDocument()
    })
  })
})
