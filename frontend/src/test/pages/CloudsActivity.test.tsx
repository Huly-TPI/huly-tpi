import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import CloudsActivity from '../../pages/CloudsActivity/CloudsActivity'

vi.mock('../../components/EmotionalClouds', () => ({
    EmotionalCloudsActivity: ({ onThoughtSubmit, maxClouds }: {
        onThoughtSubmit: (text: string) => void
        maxClouds: number
    }) => (
        <div data-testid="emotional-clouds-activity">
            <span data-testid="max-clouds">{maxClouds}</span>
            <button onClick={() => onThoughtSubmit('test')}>submit</button>
        </div>
    ),
}))

describe('CloudsActivity', () => {
    it('renderiza EmotionalCloudsActivity', () => {
        render(<CloudsActivity />)
        expect(screen.getByTestId('emotional-clouds-activity')).toBeInTheDocument()
    })

    it('pasa maxClouds={8} a EmotionalCloudsActivity', () => {
        render(<CloudsActivity />)
        expect(screen.getByTestId('max-clouds')).toHaveTextContent('8')
    })

    it('pasa un handler onThoughtSubmit que no lanza errores', () => {
        const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {})
        render(<CloudsActivity />)

        expect(() => screen.getByRole('button', { name: 'submit' }).click()).not.toThrow()

        consoleSpy.mockRestore()
    })
})
