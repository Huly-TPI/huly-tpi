import { describe, it } from 'vitest'
import { render } from '@testing-library/react'
import ChatMessageBubble from '../../components/Chatbot/ChatMessageBubble'
import { verifyTextPresent } from '../testHelpers'

describe('ChatMessageBubble', () => {
  it('renderiza el contenido del usuario', () => {
    renderMessage('user', 'hola user')
    verifyMessageContent('hola user')
  })

  it('renderiza el contenido del asistente', () => {
    renderMessage('assistant', 'hola assistant')
    verifyMessageContent('hola assistant')
  })

  /* helpers */

  const renderMessage = (role: 'user' | 'assistant', content: string) => {
    render(<ChatMessageBubble role={role} content={content} />)
  }

  const verifyMessageContent = (content: string) => {
    verifyTextPresent(content)
  }
})
