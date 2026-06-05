import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { act } from 'react'
import { MemoryRouter } from 'react-router-dom'
import ChatbotLauncher from '../../components/Chatbot/ChatbotLauncher'

const mockUseAuth = vi.fn()
vi.mock('../../context/auth', () => ({
  useAuth: () => mockUseAuth(),
}))

vi.mock('../../components/Chatbot/ChatbotModal', () => ({
  default: ({ isOpen }: { isOpen: boolean }) => (
    <div>{isOpen ? 'modal-open' : 'modal-closed'}</div>
  ),
}))

describe('ChatbotLauncher', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    document.body.removeAttribute('data-home-onboarding-active')
  })

  it('does not render launcher when not authenticated', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false })

    render(
      <MemoryRouter>
        <ChatbotLauncher />
      </MemoryRouter>,
    )

    expect(
      screen.queryByRole('button', { name: 'Abrir chat de Huly' }),
    ).not.toBeInTheDocument()
  })

  it('renders launcher when authenticated and opens modal', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true })
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <ChatbotLauncher />
      </MemoryRouter>,
    )

    const openButton = await screen.findByRole('button', {
      name: 'Abrir chat de Huly',
    })
    expect(openButton).toBeInTheDocument()

    await user.click(openButton)
    expect(screen.getByText('modal-open')).toBeInTheDocument()
  })

  it('does not render launcher on emotional onboarding route', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true })

    render(
      <MemoryRouter initialEntries={['/onboarding']}>
        <ChatbotLauncher />
      </MemoryRouter>,
    )

    expect(
      screen.queryByRole('button', { name: 'Abrir chat de Huly' }),
    ).not.toBeInTheDocument()
  })

  it('does not render launcher while home onboarding is active', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true })
    document.body.setAttribute('data-home-onboarding-active', 'true')
    window.dispatchEvent(new CustomEvent('home-onboarding-visibility-change'))

    render(
      <MemoryRouter>
        <ChatbotLauncher />
      </MemoryRouter>,
    )

    expect(
      screen.queryByRole('button', { name: 'Abrir chat de Huly' }),
    ).not.toBeInTheDocument()
  })

  it('hides launcher when home onboarding becomes active after render', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true })

    render(
      <MemoryRouter>
        <ChatbotLauncher />
      </MemoryRouter>,
    )

    expect(screen.getByRole('button', { name: 'Abrir chat de Huly' })).toBeInTheDocument()

    act(() => {
      document.body.setAttribute('data-home-onboarding-active', 'true')
      window.dispatchEvent(new CustomEvent('home-onboarding-visibility-change'))
    })

    expect(
      screen.queryByRole('button', { name: 'Abrir chat de Huly' }),
    ).not.toBeInTheDocument()
  })
})
