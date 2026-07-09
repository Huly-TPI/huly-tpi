import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import ChatbotMessages from '../../components/Chatbot/ChatbotMessages'
import { clickButton, verifyTextPresent } from '../testHelpers'

const PARAGRAPH_1 =
  'La ansiedad es una respuesta natural del cuerpo ante situaciones que percibe como amenazantes o desafiantes, generando tensión muscular y sensaciones de alerta que pueden resultar muy incómodas.'
const PARAGRAPH_2 =
  'Para manejarla de manera efectiva, lo más útil es reconocer qué situaciones la disparan y observar cómo se manifiesta en el cuerpo, los pensamientos y las emociones, sin juzgarse por ello.'

const SHORT_PARAGRAPH = 'Lo importante es que no estás solo.'
const NEXT_PARAGRAPH =
  'Hay muchas formas de trabajar con la ansiedad y encontrar alivio en el día a día, incluso con pequeños cambios.'

describe('ChatbotMessages', () => {
  let onChallengeDecisionMock: any
  let onSuggestedActionDecisionMock: any
  let onCloseMock: any
  let onDeleteAudioMessageMock: any

  beforeEach(() => {
    onChallengeDecisionMock = vi.fn()
    onSuggestedActionDecisionMock = vi.fn()
    onCloseMock = vi.fn()
    onDeleteAudioMessageMock = vi.fn()
  })

  it('renderiza la indicación vacía cuando no hay mensajes', () => {
    renderMessages([])
    verifyEmptyPromptPresent()
  })

  it('llama a los manejadores de reto al hacer click en los botones', () => {
    renderMessages([
      {
        role: 'assistant',
        content: 'te propongo',
        generated_challenge: {
          title: 'Reto',
          description: 'Descripcion',
        },
      },
    ])
    return clickAccept()
      .then(() => clickReject())
      .then(() => {
        verifyChallengeDecisionCalledWith(0, 1, 'accepted')
        verifyChallengeDecisionCalledWith(0, 2, 'rejected')
      })
  })

  it('renderiza múltiples burbujas cuando el mensaje del asistente tiene varios párrafos largos', () => {
    renderMessages([{ role: 'assistant', content: `${PARAGRAPH_1}\n\n${PARAGRAPH_2}` }])
    verifyTextIsPresent(PARAGRAPH_1)
    verifyTextIsPresent(PARAGRAPH_2)
  })

  it('fusiona un párrafo corto con el siguiente en una sola burbuja', () => {
    renderMessages([{ role: 'assistant', content: `${SHORT_PARAGRAPH}\n\n${NEXT_PARAGRAPH}` }])
    verifyMergedParagraphPresent(SHORT_PARAGRAPH, NEXT_PARAGRAPH)
  })

  it('llama al manejador de rechazo de la acción sugerida', () => {
    renderMessages([
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
    ])
    return clickReject().then(() => {
      verifySuggestedActionDecisionCalledWith(0, 'rejected')
    })
  })

  /* helpers */

  const renderMessages = (messages: any[]) => {
    render(
      <MemoryRouter>
        <ChatbotMessages
          messages={messages}
          isSending={false}
          isLoadingHistory={false}
          error=""
          onClose={onCloseMock}
          onChallengeDecision={onChallengeDecisionMock}
          onSuggestedActionDecision={onSuggestedActionDecisionMock}
          onDeleteAudioMessage={onDeleteAudioMessageMock}
          bottomRef={{ current: null }}
        />
      </MemoryRouter>
    )
  }

  const verifyEmptyPromptPresent = () => {
    verifyTextPresent('Contame cómo te sentís hoy')
  }

  const clickAccept = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Aceptar')
  }

  const clickReject = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Rechazar')
  }

  const verifyChallengeDecisionCalledWith = (index: number, callIndex: number, decision: 'accepted' | 'rejected') => {
    expect(onChallengeDecisionMock).toHaveBeenNthCalledWith(callIndex, index, decision)
  }

  const verifyTextIsPresent = (text: string) => {
    verifyTextPresent(text)
  }

  const verifyMergedParagraphPresent = (shortPar: string, nextPar: string) => {
    expect(
      screen.getByText((content) => content.includes(shortPar) && content.includes(nextPar))
    ).toBeInTheDocument()
  }

  const verifySuggestedActionDecisionCalledWith = (index: number, decision: 'accepted' | 'rejected') => {
    expect(onSuggestedActionDecisionMock).toHaveBeenCalledWith(index, decision)
  }
})
