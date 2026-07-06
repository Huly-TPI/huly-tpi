import { beforeEach, describe, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BadgeToastProvider, useBadgeToast } from '../../context/BadgeToast'
import { BadgeResponse } from '../../api/badges'
import { verifyTextPresent, verifyTextNotPresent } from '../testHelpers'






describe('BadgeToast', () => {
  beforeEach(() => {
    document.body.removeAttribute('data-home-onboarding-active')
  })

  it('renderiza los hijos correctamente', () => {
    renderBadgeToastWithChildren()
    verifyHijoTextIsPresent()
  })

  it('showBadgeToast muestra el toast con el nombre del Badge', () => {
    renderBadgeToastWithTrigger()
    return clickMostrarButton().then(() => {
      verifyPrimerPasoTextIsPresent()
    })
  })

  it('el toast se oculta al hacer click en Cerrar', () => {
    renderBadgeToastWithTrigger()
    return clickMostrarButton()
      .then(() => clickCerrarButton())
      .then(() => {
        verifyPrimerPasoTextIsNotPresent()
      })
  })

  it('no muestra el toast mientras el tutorial principal esta activo', () => {
    setTutorialActive(true)
    renderBadgeToastWithTrigger()
    return clickMostrarButton().then(() => {
      verifyPrimerPasoTextIsNotPresent()
    })
  })

  /* helpers */

  const renderBadgeToastWithChildren = () => {
    render(
      <BadgeToastProvider>
        <p>Hijo</p>
      </BadgeToastProvider>
    )
  }

  const renderBadgeToastWithTrigger = () => {
    render(
      <BadgeToastProvider>
        <TriggerButton />
      </BadgeToastProvider>
    )
  }

  const verifyHijoTextIsPresent = () => {
    verifyTextPresent('Hijo')
  }

  const clickMostrarButton = () => {
    const button = screen.getByRole('button', { name: 'Mostrar' })
    return userEvent.click(button)
  }

  const clickCerrarButton = () => {
    const closeButton = screen.getByRole('button', { name: 'Cerrar' })
    return userEvent.click(closeButton)
  }

  const verifyPrimerPasoTextIsPresent = () => {
    verifyTextPresent('Primer paso')
  }

  const verifyPrimerPasoTextIsNotPresent = () => {
    verifyTextNotPresent('Primer paso')
  }

  const setTutorialActive = (active: boolean) => {
    if (active) {
      document.body.setAttribute('data-home-onboarding-active', 'true')
    } else {
      document.body.removeAttribute('data-home-onboarding-active')
    }
  }
})

function makeBadge(): BadgeResponse {
  return ({
  id: 1, code: 'PRIMER_PASO', name: 'Primer paso',
  description: null, imageUrl: null, createdAt: ''
})
}

function TriggerButton() {
  const { showBadgeToast } = useBadgeToast()
  return <button onClick={() => showBadgeToast(makeBadge())}>Mostrar</button>
}
