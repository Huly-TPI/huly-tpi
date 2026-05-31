import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotChallengeCard from '../../components/Chatbot/ChatbotChallengeCard'

describe('ChatbotChallengeCard', () => {
  it('renders challenge and actions when no decision', () => {
    render(
      <ChatbotChallengeCard
        title="Reto"
        description="Descripcion"
        onAccept={vi.fn()}
        onReject={vi.fn()}
      />,
    )

    expect(screen.getByText('Reto propuesto')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Aceptar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Rechazar' })).toBeInTheDocument()
  })

  it('calls handlers on button click', async () => {
    const user = userEvent.setup()
    const onAccept = vi.fn()
    const onReject = vi.fn()

    render(
      <ChatbotChallengeCard
        title="Reto"
        description="Descripcion"
        onAccept={onAccept}
        onReject={onReject}
      />,
    )

    await user.click(screen.getByRole('button', { name: 'Aceptar' }))
    await user.click(screen.getByRole('button', { name: 'Rechazar' }))

    expect(onAccept).toHaveBeenCalledTimes(1)
    expect(onReject).toHaveBeenCalledTimes(1)
  })

  it('shows resolved state text', () => {
    const { rerender } = render(
      <ChatbotChallengeCard
        title="Reto"
        description="Descripcion"
        decision="accepted"
        onAccept={vi.fn()}
        onReject={vi.fn()}
      />,
    )

    expect(screen.getByText('Reto aceptado.')).toBeInTheDocument()

    rerender(
      <ChatbotChallengeCard
        title="Reto"
        description="Descripcion"
        decision="rejected"
        onAccept={vi.fn()}
        onReject={vi.fn()}
      />,
    )

    expect(screen.getByText('Reto rechazado.')).toBeInTheDocument()
  })
})

