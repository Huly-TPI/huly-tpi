import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Header from '../../components/backoffice/Header'
import { ThemeProvider } from '../../context/theme'

vi.mock('../../api/auth', () => ({
  updateThemePreference: vi.fn(() => Promise.resolve()),
}))

describe('BackofficeHeader', () => {
  beforeEach(() => {
    vi.clearAllMocks()
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

  const renderHeader = (props: { onOpenSidebar: () => void; userInitial?: string }) => {
    return render(
      <ThemeProvider>
        <Header onOpenSidebar={props.onOpenSidebar} userInitial={props.userInitial} />
      </ThemeProvider>
    )
  }

  it('renders title and user initial if provided', () => {
    renderHeader({ onOpenSidebar: vi.fn(), userInitial: 'J' })
    expect(screen.getByText('Huly - Backoffice')).toBeInTheDocument()
    expect(screen.getByText('J')).toBeInTheDocument()
  })

  it('renders empty when userInitial is not provided', () => {
    const { container } = renderHeader({ onOpenSidebar: vi.fn() })
    expect(screen.getByText('Huly - Backoffice')).toBeInTheDocument()
    const avatarDiv = container.querySelector('.rounded-full')
    expect(avatarDiv).toBeInTheDocument()
    expect(avatarDiv?.textContent).toBe('')
  })

  it('triggers onOpenSidebar when mobile menu button is clicked', async () => {
    const onOpenSidebar = vi.fn()
    const user = userEvent.setup()
    renderHeader({ onOpenSidebar, userInitial: 'A' })
    const button = screen.getByRole('button', { name: 'Abrir menú' })
    await user.click(button)
    expect(onOpenSidebar).toHaveBeenCalledTimes(1)
  })
})
