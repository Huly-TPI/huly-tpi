import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import ChatMessageBubble from '../../components/Chatbot/ChatMessageBubble'

describe('ChatMessageBubble', () => {
  it('renders user content', () => {
    render(<ChatMessageBubble role="user" content="hola user" />)
    expect(screen.getByText('hola user')).toBeInTheDocument()
  })

  it('renders assistant content', () => {
    render(<ChatMessageBubble role="assistant" content="hola assistant" />)
    expect(screen.getByText('hola assistant')).toBeInTheDocument()
  })
})

