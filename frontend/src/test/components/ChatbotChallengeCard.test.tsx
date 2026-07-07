import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import ChatbotChallengeCard from '../../components/Chatbot/ChatbotChallengeCard'
import { clickButton, verifyTextPresent, clearAllMocks } from '../testHelpers'

const mockedNavigate = vi.fn()

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockedNavigate,
  }
})

describe('ChatbotChallengeCard', () => {
  let onAcceptMock: any
  let onRejectMock: any
  let onCloseMock: any

  beforeEach(() => {
    clearAllMocks()
    onAcceptMock = vi.fn()
    onRejectMock = vi.fn()
    onCloseMock = vi.fn()
  })

  it('renderiza el reto y las acciones cuando no hay decisión', () => {
    renderCard()
    verifyChallengeAndActionsShown()
  })

  it('llama a los manejadores al hacer click en los botones', async () => {
    renderCard()
    await clickAccept()
    await clickReject()

    verifyAcceptCalled()
    verifyRejectCalled()
  })

  it('muestra el estado aceptado y navega a mis retos', async () => {
    renderCard('accepted')
    verifyStatusText('Reto aceptado.')

    await clickButton(userEvent.setup(), 'Ir a mis retos')

    verifyCloseCalled()
    verifyNavigatedToChallenges()
  })

  it('muestra el texto del estado resuelto como rechazado', () => {
    renderCard('rejected')
    verifyStatusText('Reto rechazado.')
  })

  /* helpers */

  const renderCard = (decision?: 'accepted' | 'rejected') => {
    render(
      <MemoryRouter>
        <ChatbotChallengeCard
          title="Reto"
          description="Descripcion"
          decision={decision}
          onClose={onCloseMock}
          onAccept={onAcceptMock}
          onReject={onRejectMock}
        />
      </MemoryRouter>,
    )
  }

  const verifyChallengeAndActionsShown = () => {
    verifyTextPresent('Reto propuesto')
    expect(screen.getByRole('button', { name: 'Aceptar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Rechazar' })).toBeInTheDocument()
  }

  const clickAccept = () => clickButton(userEvent.setup(), 'Aceptar')

  const clickReject = () => clickButton(userEvent.setup(), 'Rechazar')

  const verifyAcceptCalled = () => {
    expect(onAcceptMock).toHaveBeenCalledTimes(1)
  }

  const verifyRejectCalled = () => {
    expect(onRejectMock).toHaveBeenCalledTimes(1)
  }

  const verifyStatusText = (text: string) => {
    verifyTextPresent(text)
  }

  const verifyCloseCalled = () => {
    expect(onCloseMock).toHaveBeenCalledTimes(1)
  }

  const verifyNavigatedToChallenges = () => {
    expect(mockedNavigate).toHaveBeenCalledWith('/challenges')
  }
})
