import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import ChatbotModal from '../../components/Chatbot/ChatbotModal'

vi.mock('../../hooks/useChatbot', () => ({
  useChatbot: () => ({
    messages: [],
    input: '',
    setInput: vi.fn(),
    isSending: false,
    isLoadingHistory: false,
    error: '',
    bottomRef: { current: null },
    sendMessage: vi.fn(),
    decideChallenge: vi.fn(),
    decideSuggestedAction: vi.fn(),
  }),
}))

describe('ChatbotModal', () => {
  it('renders modal content when open', () => {
    render(
      <MemoryRouter>
        <ChatbotModal isOpen onClose={vi.fn()} />
      </MemoryRouter>,
    )

    expect(screen.getByRole('dialog')).toBeInTheDocument()
    expect(screen.getByText('Huly')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Escribí tu mensaje...')).toBeInTheDocument()
  })

  it('does not render dialog when closed', () => {
    render(
      <MemoryRouter>
        <ChatbotModal isOpen={false} onClose={vi.fn()} />
      </MemoryRouter>,
    )

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})

