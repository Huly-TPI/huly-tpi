import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import ChatbotModal from '../../components/Chatbot/ChatbotModal'
import { verifyTextPresent, verifyPlaceholderPresent } from '../testHelpers'

vi.mock('../../hooks/shop/useMembership', () => ({
  useMembership: () => ({ membership: null, error: null, refresh: vi.fn() }),
}))

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
    resetConversation: vi.fn(),
  }),
}))

describe('ChatbotModal', () => {
  let onCloseMock: any

  beforeEach(() => {
    onCloseMock = vi.fn()
  })

  it('renderiza el contenido del modal cuando está abierto', () => {
    renderModal(true)
    verifyModalOpened()
  })

  it('no renderiza el diálogo cuando está cerrado', () => {
    renderModal(false)
    verifyModalClosed()
  })

  /* helpers */

  const renderModal = (isOpen: boolean) => {
    render(
      <MemoryRouter>
        <ChatbotModal isOpen={isOpen} onClose={onCloseMock} />
      </MemoryRouter>
    )
  }

  const verifyModalOpened = () => {
    expect(screen.getByRole('dialog')).toBeInTheDocument()
    verifyTextPresent('Huly')
    verifyPlaceholderPresent('Escribí tu mensaje...')
  }

  const verifyModalClosed = () => {
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  }
})
