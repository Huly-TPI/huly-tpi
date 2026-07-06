import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import ChatbotChallengeCard from '../../components/Chatbot/ChatbotChallengeCard'

const mockedNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockedNavigate,
  }
})

describe('ChatbotChallengeCard', () => {
  it('renders challenge and actions when no decision', () => {
    render(
      <MemoryRouter>
        <ChatbotChallengeCard
          title="Reto"
          description="Descripcion"
          onClose={vi.fn()}
          onAccept={vi.fn()}
          onReject={vi.fn()}
        />
      </MemoryRouter>,
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
      <MemoryRouter>
        <ChatbotChallengeCard
          title="Reto"
          description="Descripcion"
          onClose={vi.fn()}
          onAccept={onAccept}
          onReject={onReject}
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'Aceptar' }))
    await user.click(screen.getByRole('button', { name: 'Rechazar' }))

    expect(onAccept).toHaveBeenCalledTimes(1)
    expect(onReject).toHaveBeenCalledTimes(1)
  })

  it('shows accepted state with go-to-challenges button and navigates', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()

    render(
      <MemoryRouter>
        <ChatbotChallengeCard
          title="Reto"
          description="Descripcion"
          decision="accepted"
          onClose={onClose}
          onAccept={vi.fn()}
          onReject={vi.fn()}
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Reto aceptado.')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Ir a mis retos' }))

    expect(onClose).toHaveBeenCalledTimes(1)
    expect(mockedNavigate).toHaveBeenCalledWith('/challenges')
  })

  it('shows rejected state text', () => {
    render(
      <MemoryRouter>
        <ChatbotChallengeCard
          title="Reto"
          description="Descripcion"
          decision="rejected"
          onClose={vi.fn()}
          onAccept={vi.fn()}
          onReject={vi.fn()}
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Reto rechazado.')).toBeInTheDocument()
  })
})
