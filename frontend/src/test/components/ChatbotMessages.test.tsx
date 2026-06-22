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
          onDeleteAudioMessage={vi.fn()}
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
          onDeleteAudioMessage={vi.fn()}
          bottomRef={{ current: null }}
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'Aceptar' }))
    await user.click(screen.getByRole('button', { name: 'Rechazar' }))

    expect(onChallengeDecision).toHaveBeenNthCalledWith(1, 0, 'accepted')
    expect(onChallengeDecision).toHaveBeenNthCalledWith(2, 0, 'rejected')
  })

  it('renders multiple bubbles when assistant message has multiple long paragraphs', () => {
    const paragraph1 =
      'La ansiedad es una respuesta natural del cuerpo ante situaciones que percibe como amenazantes o desafiantes, generando tensión muscular y sensaciones de alerta que pueden resultar muy incómodas.'
    const paragraph2 =
      'Para manejarla de manera efectiva, lo más útil es reconocer qué situaciones la disparan y observar cómo se manifiesta en el cuerpo, los pensamientos y las emociones, sin juzgarse por ello.'

    render(
      <MemoryRouter>
        <ChatbotMessages
          messages={[{ role: 'assistant', content: `${paragraph1}\n\n${paragraph2}` }]}
          isSending={false}
          isLoadingHistory={false}
          error=""
          onClose={vi.fn()}
          onChallengeDecision={vi.fn()}
          onSuggestedActionDecision={vi.fn()}
          onDeleteAudioMessage={vi.fn()}
          bottomRef={{ current: null }}
        />
      </MemoryRouter>,
    )

    expect(screen.getByText(paragraph1)).toBeInTheDocument()
    expect(screen.getByText(paragraph2)).toBeInTheDocument()
  })

  it('merges short paragraph with the next one into a single bubble', () => {
    const shortParagraph = 'Lo importante es que no estás solo.'
    const nextParagraph =
      'Hay muchas formas de trabajar con la ansiedad y encontrar alivio en el día a día, incluso con pequeños cambios.'

    render(
      <MemoryRouter>
        <ChatbotMessages
          messages={[{ role: 'assistant', content: `${shortParagraph}\n\n${nextParagraph}` }]}
          isSending={false}
          isLoadingHistory={false}
          error=""
          onClose={vi.fn()}
          onChallengeDecision={vi.fn()}
          onSuggestedActionDecision={vi.fn()}
          onDeleteAudioMessage={vi.fn()}
          bottomRef={{ current: null }}
        />
      </MemoryRouter>,
    )

    expect(
      screen.getByText((content) => content.includes(shortParagraph) && content.includes(nextParagraph)),
    ).toBeInTheDocument()
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
          onDeleteAudioMessage={vi.fn()}
          bottomRef={{ current: null }}
        />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'Rechazar' }))

    expect(onSuggestedActionDecision).toHaveBeenCalledWith(0, 'rejected')
  })
})

