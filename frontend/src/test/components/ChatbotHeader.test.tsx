import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotHeader from '../../components/Chatbot/ChatbotHeader'
import { clickButton, verifyTextPresent } from '../testHelpers'

describe('ChatbotHeader', () => {
  let onCloseMock: any
  let onResetMock: any

  beforeEach(() => {
    onCloseMock = vi.fn()
    onResetMock = vi.fn()
  })

  it('renderiza el título, el botón de opciones y el botón de cerrar', () => {
    renderHeader()
    verifyTitleAndButtonsPresent()
  })

  it('llama a onClose al hacer click en cerrar', () => {
    renderHeader()
    return clickCloseButton().then(() => {
      verifyOnCloseCalled()
    })
  })

  it('llama a onReset desde el menú de opciones', () => {
    renderHeader()
    return clickOptionsButton()
      .then(() => clickResetMenuItem())
      .then(() => {
        verifyOnResetCalled()
      })
  })

  /* helpers */

  const renderHeader = () => {
    render(<ChatbotHeader onClose={onCloseMock} onReset={onResetMock} />)
  }

  const verifyTitleAndButtonsPresent = () => {
    verifyTextPresent('Huly')
    expect(screen.getByRole('button', { name: 'Abrir opciones del chat' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cerrar' })).toBeInTheDocument()
  }

  const clickCloseButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Cerrar')
  }

  const verifyOnCloseCalled = () => {
    expect(onCloseMock).toHaveBeenCalledTimes(1)
  }

  const clickOptionsButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Abrir opciones del chat')
  }

  const clickResetMenuItem = () => {
    const user = userEvent.setup()
    return user.click(screen.getByRole('menuitem', { name: 'Limpiar chat' }))
  }

  const verifyOnResetCalled = () => {
    expect(onResetMock).toHaveBeenCalledTimes(1)
  }
})
