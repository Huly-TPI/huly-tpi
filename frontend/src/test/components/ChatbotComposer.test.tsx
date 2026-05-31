import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotComposer from '../../components/Chatbot/ChatbotComposer'

describe('ChatbotComposer', () => {
  it('renders input and send button', () => {
    render(
      <ChatbotComposer
        input=""
        isSending={false}
        onInputChange={vi.fn()}
        onSend={vi.fn()}
      />,
    )

    expect(screen.getByPlaceholderText('Escribí tu mensaje...')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Enviar' })).toBeInTheDocument()
  })

  it('calls onInputChange when typing', async () => {
    const user = userEvent.setup()
    const onInputChange = vi.fn()

    render(
      <ChatbotComposer
        input=""
        isSending={false}
        onInputChange={onInputChange}
        onSend={vi.fn()}
      />,
    )

    await user.type(screen.getByPlaceholderText('Escribí tu mensaje...'), 'hola')
    expect(onInputChange).toHaveBeenCalled()
  })

  it('calls onSend on button click and enter key', async () => {
    const user = userEvent.setup()
    const onSend = vi.fn()

    render(
      <ChatbotComposer
        input="hola"
        isSending={false}
        onInputChange={vi.fn()}
        onSend={onSend}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Enviar' }))
    expect(onSend).toHaveBeenCalledTimes(1)

    await user.type(screen.getByPlaceholderText('Escribí tu mensaje...'), '{enter}')
    expect(onSend).toHaveBeenCalledTimes(2)
  })
})

