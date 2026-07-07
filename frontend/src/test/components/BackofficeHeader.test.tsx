import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Header from '../../components/backoffice/Header'
import { ThemeProvider } from '../../context/theme'
import { verifyTextPresent, clearAllMocks } from '../testHelpers'

vi.mock('../../api/auth', () => ({
  updateThemePreference: vi.fn(() => Promise.resolve()),
}))

describe('BackofficeHeader', () => {
  let onOpenSidebarMock: any

  beforeEach(() => {
    onOpenSidebarMock = vi.fn()
    clearAllMocks()
    window.localStorage.clear()
    Object.defineProperty(window, 'matchMedia', {
      writable: true,
      value: (query: string) => ({
        matches: false,
        media: query,
        onchange: null,
        addListener: () => {},
        removeListener: () => {},
        addEventListener: () => {},
        removeEventListener: () => {},
        dispatchEvent: () => false,
      }),
    })
  })

  it('renderiza el título y la inicial del usuario si se proporciona', () => {
    renderHeaderComponent('J')
    verifyTitleAndInitialPresent('J')
  })

  it('renderiza vacío cuando no se proporciona userInitial', () => {
    renderHeaderComponent()
    verifyHeaderTitlePresent()
    verifyEmptyAvatarRendered()
  })

  it('dispara onOpenSidebar cuando se hace click en el botón del menú móvil', () => {
    renderHeaderComponent('A')
    return clickMobileMenuButton().then(() => {
      verifyOnOpenSidebarCalled()
    })
  })

  /* helpers */

  const renderHeaderComponent = (userInitial?: string) => {
    render(
      <ThemeProvider>
        <Header onOpenSidebar={onOpenSidebarMock} userInitial={userInitial} />
      </ThemeProvider>
    )
  }

  const verifyTitleAndInitialPresent = (initial: string) => {
    verifyTextPresent('Huly - Backoffice')
    verifyTextPresent(initial)
  }

  const verifyHeaderTitlePresent = () => {
    verifyTextPresent('Huly - Backoffice')
  }

  const verifyEmptyAvatarRendered = () => {
    const avatarDiv = document.querySelector('.rounded-full')
    expect(avatarDiv).toBeInTheDocument()
    expect(avatarDiv?.textContent).toBe('')
  }

  const clickMobileMenuButton = () => {
    const user = userEvent.setup()
    const button = screen.getByRole('button', { name: 'Abrir menú' })
    return user.click(button)
  }

  const verifyOnOpenSidebarCalled = () => {
    expect(onOpenSidebarMock).toHaveBeenCalledTimes(1)
  }
})
