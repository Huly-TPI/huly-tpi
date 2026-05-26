import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import CloudsActivity from '../../pages/CloudsActivity/CloudsActivity'

vi.mock('../../api/client', () => ({
    api: {
        post: vi.fn().mockResolvedValue({
            activity_type: 'diary',
            action_id: 'diary',
            title: 'Escribí en tu diario',
            description: 'Plasmar lo que sentiste puede ayudarte a procesarlo.',
            redirect_url: '/diary',
        }),
    },
}))

vi.mock('../../components/EmotionalClouds', () => ({
    EmotionalCloudsActivity: ({ onFinish, maxClouds }: {
        onFinish?: (thoughts: string[]) => void
        maxClouds: number
    }) => (
        <div data-testid="emotional-clouds-activity">
            <span data-testid="max-clouds">{maxClouds}</span>
            <button onClick={() => onFinish?.(['test'])}>finish</button>
        </div>
    ),
}))

describe('CloudsActivity', () => {
    it('renderiza EmotionalCloudsActivity', () => {
        render(<MemoryRouter><CloudsActivity /></MemoryRouter>)
        expect(screen.getByTestId('emotional-clouds-activity')).toBeInTheDocument()
    })

    it('pasa maxClouds={8} a EmotionalCloudsActivity', () => {
        render(<MemoryRouter><CloudsActivity /></MemoryRouter>)
        expect(screen.getByTestId('max-clouds')).toHaveTextContent('8')
    })

    it('pasa un handler onFinish que no lanza errores', () => {
        render(<MemoryRouter><CloudsActivity /></MemoryRouter>)
        expect(() => screen.getByRole('button', { name: 'finish' }).click()).not.toThrow()
    })
})
