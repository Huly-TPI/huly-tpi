import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import BubblesActivity from '../../pages/BubblesActivity/BubblesActivity.tsx'

vi.mock('../../components/Bubbles', () => ({
    BubblesActivity:({onBack}: {onBack?: () => void}) => (
        <div data-testid="bubbles-activity">
            <button onClick={onBack}>Volver</button>
        </div>
    )
}))

describe('BubblesActivity', () => {
    it('renderiza el componente de burbujas', () => {
        renderBubblesActivity()
        verifyBubblesComponentRendered()
    })
    const renderBubblesActivity = () => {
        render(<MemoryRouter><BubblesActivity /></MemoryRouter>)
    }

    const verifyBubblesComponentRendered = () => {
        expect(screen.getByTestId('bubbles-activity')).toBeInTheDocument()
    }
})