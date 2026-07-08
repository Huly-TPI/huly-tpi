import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import BadgeLauncher from '../../components/Badges/BadgeLauncher'
import { verifyTextPresent, clearAllMocks } from '../testHelpers'

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
       clearAllMocks()
       document.body.removeAttribute('data-home-onboarding-active')
   })

   it('no renderiza cuando no está autenticado', () => {
        setupAuth(false)
        renderLauncher()
        verifyLauncherButtonNotPresent()
   })

   it('renderiza cuando está autenticado', () => {
        setupAuth(true)
        renderLauncher()
        verifyLauncherButtonPresent()
   })

   it('abre el modal al hacer click', () => {
        setupAuth(true)
        renderLauncher()
        return clickLauncherButton().then(() => {
            verifyModalOpen()
        })
   })

   it('no renderiza en la ruta /onboarding', () => {
        setupAuth(true)
        renderLauncher('/onboarding')
        verifyLauncherButtonNotPresent()
   })

  /* helpers */

  const setupAuth = (isAuthenticated: boolean) => {
    mockUseAuth.mockReturnValue({ isAuthenticated })
  }

  const renderLauncher = (initialRoute: string = '/') => {
    render(
      <MemoryRouter initialEntries={[initialRoute]}>
        <BadgeLauncher />
      </MemoryRouter>
    )
  }

  const verifyLauncherButtonPresent = () => {
    expect(screen.getByRole('button', { name: 'Abrir insignias' })).toBeInTheDocument()
  }

  const verifyLauncherButtonNotPresent = () => {
    expect(screen.queryByRole('button', { name: 'Abrir insignias' })).not.toBeInTheDocument()
  }

  const clickLauncherButton = () => {
    const user = userEvent.setup()
    return user.click(screen.getByRole('button', { name: 'Abrir insignias' }))
  }

  const verifyModalOpen = () => {
    verifyTextPresent('modal-open')
  }
})