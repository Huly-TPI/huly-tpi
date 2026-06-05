import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import CloudsActivity from '../../pages/CloudsActivity/CloudsActivity'
import { api } from '../../api/client'

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
            expect(screen.getByRole('button', { name: 'Ir a respiración guiada' })).toBeInTheDocument()
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
