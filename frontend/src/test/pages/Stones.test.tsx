import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Stones from '../../pages/Stones/Stones'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'

vi.mock('../../components/StonesLake', () => ({
    StonesLake: ({ onStoneThrown }: { onStoneThrown?: () => void }) => (
        <div data-testid="stones-lake-mock">
            <button onClick={onStoneThrown} data-testid="throw-stone-btn">Throw Stone</button>
        </div>
    )
}))

vi.mock('../../hooks/useActivitySessionTracker', () => ({
    useActivitySessionTracker: vi.fn(() => ({
        markConditionMet: vi.fn(),
        saveSession: vi.fn(),
        startSession: vi.fn(),
        stopSession: vi.fn(),
    }))
}))

describe('Stones Page', () => {
    it('renderiza el componente StonesPage con sus indicaciones', () => {
        renderStones()
        expect(screen.getByText('Hacé clic en el agua para lanzar piedras')).toBeInTheDocument()
        expect(screen.getByTestId('stones-lake-mock')).toBeInTheDocument()
    })

    it('llama a markConditionMet cuando se lanza una piedra', () => {
        const markConditionMetMock = vi.fn()
        vi.mocked(useActivitySessionTracker).mockReturnValue({
            markConditionMet: markConditionMetMock,
            saveSession: vi.fn(),
            startSession: vi.fn(),
            stopSession: vi.fn(),
        })

        renderStones()
        fireEvent.click(screen.getByTestId('throw-stone-btn'))
        expect(markConditionMetMock).toHaveBeenCalled()
    })

    const renderStones = () => {
        render(
            <MemoryRouter>
                <Stones />
            </MemoryRouter>
        )
    }
})
