import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import BadgeUnlockToast from '../../components/Badges/BadgeUnlockToast'
import { verifyTextPresent } from '../testHelpers'

interface Badge {
  id: number
  code: string
  name: string
  description: string
  imageUrl: string
  createdAt: string
}

describe('BadgeUnlockToast', () => {
  let onDismissMock: ReturnType<typeof vi.fn>

  beforeEach(() => {
    onDismissMock = vi.fn()
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

  it('llama onDismiss al hacer click', async () => {
    renderToast(makeBadge())
    await clickCloseButton()
    verifyOnDismissCalled()
  })

  /* helpers */

  const makeBadge = (): Badge => ({
    id: 1,
    code: 'PRIMER_PASO',
    name: 'Primer paso',
    description: 'Empezaste tu camino.',
    imageUrl: 'badge_primer_paso.webp',
    createdAt: '',
  })

  const renderToast = (badge: Badge | null) => {
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
})