import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotComposer from '../../components/Chatbot/ChatbotComposer'
import { clickButton, typePlaceholder, verifyPlaceholderPresent } from '../testHelpers'

describe('ChatbotComposer', () => {
  let onInputChangeMock: any
  let onSendMock: any
  let onSendAudioMock: any

  beforeEach(() => {
    onInputChangeMock = vi.fn()
    onSendMock = vi.fn()
    onSendAudioMock = vi.fn()
  })

  it('renderiza el input y el botón de enviar', () => {
    renderComposer()
    verifyInputAndSendButtonShown()
  })

  it('llama a onInputChange al escribir', () => {
    renderComposer()
    return typeIntoComposer('hola').then(() => {
      verifyOnInputChangeCalled()
    })
  })

  it('llama a onSend al hacer click en el botón y presionar enter', () => {
    renderComposer('hola')
    return clickSendButton()
      .then(() => {
        verifyOnSendCalledTimes(1)
        return typeIntoComposer('{enter}')
      })
      .then(() => {
        verifyOnSendCalledTimes(2)
      })
  })

  it('hace autofoco en el textarea al montar', () => {
    renderComposer()
    return verifyTextareaHasFocus()
  })

  it('no renderiza el botón de limpiar chat en el composer', () => {
    renderComposer()
    verifyResetButtonNotPresent()
  })

  /* helpers */

  const renderComposer = (input: string = '', isSending: boolean = false) => {
    render(
      <ChatbotComposer
        input={input}
        isSending={isSending}
        onInputChange={onInputChangeMock}
        onSend={onSendMock}
        onSendAudio={onSendAudioMock}
      />
    )
  }

  const verifyInputAndSendButtonShown = () => {
    verifyPlaceholderPresent('Escribí tu mensaje...')
    expect(screen.getByRole('button', { name: 'Enviar' })).toBeInTheDocument()
  }

  const typeIntoComposer = (text: string) => {
    const user = userEvent.setup()
    return typePlaceholder(user, 'Escribí tu mensaje...', text)
  }

  const verifyOnInputChangeCalled = () => {
    expect(onInputChangeMock).toHaveBeenCalled()
  }

  const clickSendButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Enviar')
  }

  const verifyOnSendCalledTimes = (times: number) => {
    expect(onSendMock).toHaveBeenCalledTimes(times)
  }

  const verifyTextareaHasFocus = () => {
    return waitFor(() => {
      expect(screen.getByPlaceholderText('Escribí tu mensaje...')).toHaveFocus()
    })
  }

  const verifyResetButtonNotPresent = () => {
    expect(screen.queryByRole('button', { name: 'Limpiar chat' })).not.toBeInTheDocument()
  }
})
