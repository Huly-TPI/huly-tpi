import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotChallengeCard from '../../components/Chatbot/ChatbotChallengeCard'
import { clickButton, verifyTextPresent } from '../testHelpers'

describe('ChatbotChallengeCard', () => {
  let onAcceptMock: any
  let onRejectMock: any

  beforeEach(() => {
    onAcceptMock = vi.fn()
    onRejectMock = vi.fn()
  })

  it('renderiza el reto y las acciones cuando no hay decisión', () => {
    renderCard()
    verifyChallengeAndActionsShown()
  })

  it('llama a los manejadores al hacer click en los botones', () => {
    renderCard()
    return clickAccept()
      .then(() => clickReject())
      .then(() => {
        verifyAcceptCalled()
        verifyRejectCalled()
      })
  })

  it('muestra el texto del estado resuelto como aceptado', () => {
    renderCard('accepted')
    verifyStatusText('Reto aceptado.')
  })

  it('muestra el texto del estado resuelto como rechazado', () => {
    renderCard('rejected')
    verifyStatusText('Reto rechazado.')
  })

  /* helpers */

  const renderCard = (decision?: 'accepted' | 'rejected') => {
    render(
      <ChatbotChallengeCard
        title="Reto"
        description="Descripcion"
        decision={decision}
        onAccept={onAcceptMock}
        onReject={onRejectMock}
      />
    )
  }

  const verifyChallengeAndActionsShown = () => {
    verifyTextPresent('Reto propuesto')
    expect(screen.getByRole('button', { name: 'Aceptar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Rechazar' })).toBeInTheDocument()
  }

  const clickAccept = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Aceptar')
  }

  const clickReject = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Rechazar')
  }

  const verifyAcceptCalled = () => {
    expect(onAcceptMock).toHaveBeenCalledTimes(1)
  }

  const verifyRejectCalled = () => {
    expect(onRejectMock).toHaveBeenCalledTimes(1)
  }

  const verifyStatusText = (text: string) => {
    verifyTextPresent(text)
  }
})
