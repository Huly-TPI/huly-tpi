import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotLauncher from '../../components/Chatbot/ChatbotLauncher'

vi.mock('../../components/Chatbot/ChatbotModal', () => ({
  default: ({ isOpen }: { isOpen: boolean }) => <div>{isOpen ? 'modal-open' : 'modal-closed'}</div>,
}))

describe('ChatbotLauncher', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('does not render launcher without token', () => {
    render(<ChatbotLauncher />)
    expect(screen.queryByRole('button', { name: 'Abrir chat de Huly' })).not.toBeInTheDocument()
  })

  it('renders launcher with token and opens modal', async () => {
    const user = userEvent.setup()
    localStorage.setItem('token', 'abc')

    render(<ChatbotLauncher />)

    const openButton = await screen.findByRole('button', { name: 'Abrir chat de Huly' })
    expect(openButton).toBeInTheDocument()

    await user.click(openButton)
    expect(screen.getByText('modal-open')).toBeInTheDocument()
  })
})

