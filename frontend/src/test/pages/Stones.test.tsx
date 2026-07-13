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
        renderPage()
        verifyIndicationsAndLakeShown()
    })

    it('llama a markConditionMet cuando se lanza una piedra', () => {
        const markConditionMetMock = vi.fn()
        setupSessionTrackerMock(markConditionMetMock)

        renderPage()
        clickThrowStone()
        verifyMarkConditionMetCalled(markConditionMetMock)
    })

    const renderPage = () => {
        render(
            <MemoryRouter>
                <Stones />
            </MemoryRouter>
        )
    }

    const setupSessionTrackerMock = (markConditionMetMock: any) => {
        vi.mocked(useActivitySessionTracker).mockReturnValue({
            markConditionMet: markConditionMetMock,
            saveSession: vi.fn(),
            startSession: vi.fn(),
            stopSession: vi.fn(),
        })
    }

    const clickThrowStone = () => {
        fireEvent.click(screen.getByTestId('throw-stone-btn'))
    }

    const verifyIndicationsAndLakeShown = () => {
        expect(screen.getByText('Hacé clic en el agua para lanzar piedras')).toBeInTheDocument()
        expect(screen.getByTestId('stones-lake-mock')).toBeInTheDocument()
    }

    const verifyMarkConditionMetCalled = (mock: any) => {
        expect(mock).toHaveBeenCalled()
    }
})
