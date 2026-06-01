import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import ChatbotMessages from '../../components/Chatbot/ChatbotMessages'

describe('ChatbotMessages', () => {
  it('renders empty prompt when no messages', () => {
    render(
      <MemoryRouter>
        <ChatbotMessages
          messages={[]}
          isSending={false}
          isLoadingHistory={false}
          error=""
          onClose={vi.fn()}
          onChallengeDecision={vi.fn()}
          onSuggestedActionDecision={vi.fn()}
          bottomRef={{ current: null }}
        />
      </MemoryRouter>,
    )

    expect(screen.getByText('Contame cómo te sentís hoy')).toBeInTheDocument()
  })

  it('calls challenge handlers on buttons', async () => {
    const user = userEvent.setup()
    const onChallengeDecision = vi.fn()

    render(
      <MemoryRouter>
        <ChatbotMessages
          messages={[
            {
              role: 'assistant',
              content: 'te propongo',
              generated_challenge: {
                title: 'Reto',
                description: 'Descripcion',
              },
            },
          ]}
          isSending={false}
          isLoadingHistory={false}
          error=""
          onClose={vi.fn()}
          onChallengeDecision={onChallengeDecision}
          onSuggestedActionDecision={vi.fn()}
          bottomRef={{ current: null }}
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'Aceptar' }))
    await user.click(screen.getByRole('button', { name: 'Rechazar' }))

    expect(onChallengeDecision).toHaveBeenNthCalledWith(1, 0, 'accepted')
    expect(onChallengeDecision).toHaveBeenNthCalledWith(2, 0, 'rejected')
  })

  it('calls suggested action reject handler', async () => {
    const user = userEvent.setup()
    const onSuggestedActionDecision = vi.fn()

    render(
      <MemoryRouter>
        <ChatbotMessages
          messages={[
            {
              role: 'assistant',
              content: 'actividad sugerida',
              suggested_action: {
                type: 'INTERNAL',
                action_id: 'X',
                title: 'Respiracion',
                description: 'desc',
                action_url: '/activities',
              },
            },
          ]}
          isSending={false}
          isLoadingHistory={false}
          error=""
          onClose={vi.fn()}
          onChallengeDecision={vi.fn()}
          onSuggestedActionDecision={onSuggestedActionDecision}
          bottomRef={{ current: null }}
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'No ir por ahora' }))

    expect(onSuggestedActionDecision).toHaveBeenCalledWith(0, 'rejected')
  })
})

