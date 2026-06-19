import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotComposer from '../../components/Chatbot/ChatbotComposer'

describe('ChatbotComposer', () => {
  it('renders input, send button, and reset button', () => {
    render(
      <ChatbotComposer
        input=""
        isSending={false}
        onInputChange={vi.fn()}
        onSend={vi.fn()}
        onReset={vi.fn()}
        onSendAudio={vi.fn()}
      />,
    )

    expect(screen.getByPlaceholderText('Escribí tu mensaje...')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Enviar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Limpiar chat' })).toBeInTheDocument()
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
        onReset={vi.fn()}
        onSendAudio={vi.fn()}
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
        onReset={vi.fn()}
        onSendAudio={vi.fn()}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Enviar' }))
    expect(onSend).toHaveBeenCalledTimes(1)

    await user.type(screen.getByPlaceholderText('Escribí tu mensaje...'), '{enter}')
    expect(onSend).toHaveBeenCalledTimes(2)
  })

  it('autofocuses the textarea on mount', async () => {
    render(
      <ChatbotComposer
        input=""
        isSending={false}
        onInputChange={vi.fn()}
        onSend={vi.fn()}
        onReset={vi.fn()}
        onSendAudio={vi.fn()}
      />,
    )

    await waitFor(() => {
      expect(screen.getByPlaceholderText('Escribí tu mensaje...')).toHaveFocus()
    })
  })

  it('calls onReset when trash button is clicked', async () => {
    const user = userEvent.setup()
    const onReset = vi.fn()

    render(
      <ChatbotComposer
        input=""
        isSending={false}
        onInputChange={vi.fn()}
        onSend={vi.fn()}
        onReset={onReset}
        onSendAudio={vi.fn()}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Limpiar chat' }))
    expect(onReset).toHaveBeenCalledTimes(1)
  })
})

