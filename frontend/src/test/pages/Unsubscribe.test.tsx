import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import Unsubscribe from '../../pages/Unsubscribe/Unsubscribe'

function renderWithStatus(status: string) {
    render(
        <MemoryRouter initialEntries={[`/unsubscribe?status=${status}`]}>
            <Unsubscribe />
        </MemoryRouter>
    )
}

describe('Unsubscribe', () => {
    it('muestra la confirmación cuando status es ok', () => {
        renderWithStatus('ok')
        expect(screen.getByText(/te diste de baja/i)).toBeInTheDocument()
    })

    it('muestra el error cuando status es error', () => {
        renderWithStatus('error')
        expect(screen.getByText(/no pudimos/i)).toBeInTheDocument()
    })
})