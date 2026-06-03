import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import ChatbotSuggestedActionCard from '../../components/Chatbot/ChatbotSuggestedActionCard'

const mockedNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockedNavigate,
  }
})

describe('ChatbotSuggestedActionCard', () => {
  it('renders suggested action content and both actions', () => {
    render(
      <MemoryRouter>
        <ChatbotSuggestedActionCard
          title="Respiración 4-7-8"
          description="Descripción"
          actionUrl="/activities"
          onClose={vi.fn()}
          onAccept={vi.fn()}
          onReject={vi.fn()}
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Actividad sugerida')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Aceptar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Rechazar' })).toBeInTheDocument()
  })

  it('navigates on go-to-activity', async () => {
    const user = userEvent.setup()
    const onAccept = vi.fn()
    const onClose = vi.fn()

    const { rerender } = render(
      <MemoryRouter>
        <ChatbotSuggestedActionCard
          title="Respiración 4-7-8"
          description="Descripción"
          actionUrl="/activities"
          onClose={onClose}
          onAccept={onAccept}
          onReject={vi.fn()}
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'Aceptar' }))
    expect(onAccept).toHaveBeenCalledTimes(1)

    rerender(
      <MemoryRouter>
        <ChatbotSuggestedActionCard
          title="Respiración 4-7-8"
          description="Descripción"
          actionUrl="/activities"
          decision="accepted"
          onClose={onClose}
          onAccept={onAccept}
          onReject={vi.fn()}
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'Ir a la actividad' }))

    expect(onClose).toHaveBeenCalledTimes(1)
    expect(mockedNavigate).toHaveBeenCalledWith('/activities')
  })

  it('calls reject and shows state message when rejected', async () => {
    const user = userEvent.setup()
    const onReject = vi.fn()

    const { rerender } = render(
      <MemoryRouter>
        <ChatbotSuggestedActionCard
          title="Respiración 4-7-8"
          description="Descripción"
          actionUrl="/activities"
          onClose={vi.fn()}
          onAccept={vi.fn()}
          onReject={onReject}
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'Rechazar' }))
    expect(onReject).toHaveBeenCalledTimes(1)

    rerender(
      <MemoryRouter>
        <ChatbotSuggestedActionCard
          title="Respiración 4-7-8"
          description="Descripción"
          actionUrl="/activities"
          decision="rejected"
          onClose={vi.fn()}
          onAccept={vi.fn()}
          onReject={onReject}
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Actividad rechazada. Seguimos por chat.')).toBeInTheDocument()
  })
})

