import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
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
  })

  it('does not render launcher when not authenticated', () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: false })

    render(<ChatbotLauncher />)

    expect(
      screen.queryByRole('button', { name: 'Abrir chat de Huly' }),
    ).not.toBeInTheDocument()
  })

  it('renders launcher when authenticated and opens modal', async () => {
    mockUseAuth.mockReturnValue({ isAuthenticated: true })
    const user = userEvent.setup()

    render(<ChatbotLauncher />)

    const openButton = await screen.findByRole('button', {
      name: 'Abrir chat de Huly',
    })
    expect(openButton).toBeInTheDocument()

    await user.click(openButton)
    expect(screen.getByText('modal-open')).toBeInTheDocument()
  })
})