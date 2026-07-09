import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import BadgeUnlockToast from '../../components/Badges/BadgeUnlockToast'
import { verifyTextPresent } from '../testHelpers'

describe('BadgeUnloackToast', () => {
  let onDismissMock: any

  beforeEach(() => {
    onDismissMock = vi.fn()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  // --- CASOS DE PRUEBA (TEST SUITE) ---

  it('no renderiza nada si badge es null', () => {
    renderToast(null)
    verifyStatusNotPresent()
  })

  it('muestra el nombre de la insignia desbloqueada', () => {
    renderToast(makeBadge())
    verifyBadgeNamePresent('Primer paso')
  })

  it('muestra el mensaje de desbloqueo', () => {
    renderToast(makeBadge())
    verifyUnlockMessagePresent()
  })

  it('llama onDismiss al hacer click', () => {
    renderToast(makeBadge())
    return clickCloseButton().then(() => {
      verifyOnDismissCalled()
    })
  })

  it('programa el auto-dismiss a los 10 segundos', () => {
    const setTimeoutSpy = vi.spyOn(window, 'setTimeout')
    renderToast(makeBadge())
    return waitFor(() => {
      verifyAutoDismissScheduled(setTimeoutSpy)
    })
  })

  /* helpers */

  const makeBadge = () => ({
    id: 1,
    code: 'PRIMER_PASO',
    name: 'Primer paso',
    description: 'Empezaste tu camino.',
    imageUrl: 'badge_primer_paso.webp',
    createdAt: '',
  })

  const renderToast = (badge: any) => {
    render(<BadgeUnlockToast badge={badge} onDismiss={onDismissMock} />)
  }

  const verifyStatusNotPresent = () => {
    expect(screen.queryByRole('status')).not.toBeInTheDocument()
  }

  const verifyBadgeNamePresent = (name: string) => {
    verifyTextPresent(name)
  }

  const verifyUnlockMessagePresent = () => {
    verifyTextPresent('✨ ¡Nueva estampita!')
  }

  const clickCloseButton = () => {
    const user = userEvent.setup()
    const button = screen.getByRole('button', { name: 'Cerrar' })
    return user.click(button)
  }

  const verifyOnDismissCalled = () => {
    expect(onDismissMock).toHaveBeenCalledOnce()
  }

  const verifyAutoDismissScheduled = (setTimeoutSpy: any) => {
    expect(setTimeoutSpy).toHaveBeenCalledWith(onDismissMock, 10000)
  }
})