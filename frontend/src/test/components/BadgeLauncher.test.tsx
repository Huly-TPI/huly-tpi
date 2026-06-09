import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import BadgeLauncher from '../../components/Badges/BadgeLauncher'

const mockUseAuth = vi.fn()
vi.mock('../../context/auth', () => ({
    useAuth: () => mockUseAuth(),
}))

vi.mock('../../components/Badges/BadgeModal', () => ({
  default: ({ isOpen }: { isOpen: boolean }) => (
    <div>{isOpen ? 'modal-open' : 'modal-closed'}</div>
  ),
}))

describe('BadgeLauncher', () => {
   beforeEach(() => {
       vi.clearAllMocks()
       document.body.removeAttribute('data-home-onboarding-active')
   })

   it('no renderiza cuando no está autenticado', () => {
        mockUseAuth.mockReturnValue({ isAuthenticated: false })
        render(
            <MemoryRouter>
                <BadgeLauncher />
            </MemoryRouter>,
        )
        expect(screen.queryByRole('button', { name: 'Abrir insignias' })).not.toBeInTheDocument()
   })

   it('renderiza cuando está autenticado', () => {
        mockUseAuth.mockReturnValue({ isAuthenticated: true })
        render(
            <MemoryRouter>
                <BadgeLauncher />
            </MemoryRouter>,
        )
        expect(screen.getByRole('button', { name: 'Abrir insignias' })).toBeInTheDocument()
   })

   it('abre el modal al hacer click', async () => {
        mockUseAuth.mockReturnValue({ isAuthenticated: true })
        render(
            <MemoryRouter>
                <BadgeLauncher />
            </MemoryRouter>,
        )
        await userEvent.click(screen.getByRole('button', { name: 'Abrir insignias' }))
        expect(screen.getByText('modal-open')).toBeInTheDocument()
   })

   it('no renderiza en la ruta /onboarding', () => {
        mockUseAuth.mockReturnValue({ isAuthenticated: true })
        render(
            <MemoryRouter initialEntries={['/onboarding']}>
                <BadgeLauncher />
            </MemoryRouter>,
        )
        expect(screen.queryByRole('button', { name: 'Abrir insignias' })).not.toBeInTheDocument()
   })
})

    