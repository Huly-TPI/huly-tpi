import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import ChatbotSuggestedActionCard from '../../components/Chatbot/ChatbotSuggestedActionCard'
import { clickButton, verifyTextPresent, clearAllMocks } from '../testHelpers'

const mockedNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockedNavigate,
  }
})

describe('ChatbotSuggestedActionCard', () => {
  let onCloseMock: any
  let onAcceptMock: any
  let onRejectMock: any

  beforeEach(() => {
    clearAllMocks()
    onCloseMock = vi.fn()
    onAcceptMock = vi.fn()
    onRejectMock = vi.fn()
  })

  it('renderiza el contenido de la acción sugerida y ambas acciones', () => {
    renderCard()
    verifySuggestedActionContentAndButtonsShown()
  })

  it('llama a onAccept al hacer click en aceptar', () => {
    renderCard()
    return clickAccept().then(() => {
      verifyAcceptCalled()
    })
  })

  it('navega e invoca onClose al hacer click en Ir a la actividad', () => {
    renderCard('accepted')
    return clickGoToActivity().then(() => {
      verifyCloseAndNavigateCalled()
    })
  })

  it('llama a onReject al hacer click en rechazar', () => {
    renderCard()
    return clickReject().then(() => {
      verifyRejectCalled()
    })
  })

  it('muestra el mensaje de estado rechazado', () => {
    renderCard('rejected')
    verifyRejectedStateMessageShown()
  })

  /* helpers */

  const renderCard = (decision?: 'accepted' | 'rejected') => {
    render(
      <MemoryRouter>
        <ChatbotSuggestedActionCard
          title="Respiración 4-7-8"
          description="Descripción"
          actionUrl="/activities"
          decision={decision}
          onClose={onCloseMock}
          onAccept={onAcceptMock}
          onReject={onRejectMock}
        />
      </MemoryRouter>
    )
  }

  const verifySuggestedActionContentAndButtonsShown = () => {
    verifyTextPresent('Actividad sugerida')
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

  const clickGoToActivity = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Ir a la actividad')
  }

  const verifyAcceptCalled = () => {
    expect(onAcceptMock).toHaveBeenCalledTimes(1)
  }

  const verifyRejectCalled = () => {
    expect(onRejectMock).toHaveBeenCalledTimes(1)
  }

  const verifyCloseAndNavigateCalled = () => {
    expect(onCloseMock).toHaveBeenCalledTimes(1)
    expect(mockedNavigate).toHaveBeenCalledWith('/activities')
  }

  const verifyRejectedStateMessageShown = () => {
    verifyTextPresent('Actividad rechazada. Seguimos por chat.')
  }
})
