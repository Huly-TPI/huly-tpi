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
          onReject={vi.fn()}
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Actividad sugerida')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Ir a la actividad' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'No ir por ahora' })).toBeInTheDocument()
  })

  it('navigates on go-to-activity', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()

    render(
      <MemoryRouter>
        <ChatbotSuggestedActionCard
          title="Respiración 4-7-8"
          description="Descripción"
          actionUrl="/activities"
          onClose={onClose}
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
          onReject={onReject}
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'No ir por ahora' }))
    expect(onReject).toHaveBeenCalledTimes(1)

    rerender(
      <MemoryRouter>
        <ChatbotSuggestedActionCard
          title="Respiración 4-7-8"
          description="Descripción"
          actionUrl="/activities"
          onClose={vi.fn()}
          decision="rejected"
          onReject={onReject}
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Seguimos por chat.')).toBeInTheDocument()
  })
})

