import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotHeader from '../../components/Chatbot/ChatbotHeader'

describe('ChatbotHeader', () => {
  it('renders title and close button', () => {
    render(<ChatbotHeader onClose={vi.fn()} />)
    expect(screen.getByText('Huly')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cerrar' })).toBeInTheDocument()
  })

  it('calls onClose when close is clicked', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()
    render(<ChatbotHeader onClose={onClose} />)

    await user.click(screen.getByRole('button', { name: 'Cerrar' }))
    expect(onClose).toHaveBeenCalledTimes(1)
  })
})

