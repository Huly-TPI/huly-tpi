import { describe, it, expect, vi } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import BadgeUnlockToast from '../../components/Badges/BadgeUnlockToast'

const makeBadge = () => ({
  id: 1,
  code: 'PRIMER_PASO',
  name: 'Primer paso',
  description: 'Empezaste tu camino.',
  imageUrl: 'badge_primer_paso.webp',
  createdAt: '',
})

describe('BadgeUnloackToast', () => {
  it('no renderiza nada si badge es null', () => {
    render(<BadgeUnlockToast badge={null} onDismiss={() => {}} />)
    expect(screen.queryByRole('status')).not.toBeInTheDocument()
  })

  it('muestra el nombre de la insignia cuando no hay badge', () => {
    render(<BadgeUnlockToast badge={makeBadge()} onDismiss={() => {}} />)
    expect(screen.getByText('Primer paso')).toBeInTheDocument()
  })

  it('muestra el mensaje de desbloqueo', () => {
    render(<BadgeUnlockToast badge={makeBadge()} onDismiss={() => {}} />)
    expect(screen.getByText('¡Nueva insignia desbloqueada!')).toBeInTheDocument()
  })

  it('llama onDismiss al hacer click', async () => {
    const onDismiss = vi.fn()
    render(<BadgeUnlockToast badge={makeBadge()} onDismiss={onDismiss} />)
    const button = screen.getByRole('button', { name: 'Cerrar' })
    await act(async () => {
      await userEvent.click(button)
    })
    expect(onDismiss).toHaveBeenCalledOnce()
  })

  it('llama onDismiss automáticamente después de 3 segundos', () => {
      vi.useFakeTimers()
      const onDismiss = vi.fn()
      render(<BadgeUnlockToast badge={makeBadge()} onDismiss={onDismiss} />)
      act(() => {vi.advanceTimersByTime(10000) })
      expect(onDismiss).toHaveBeenCalledOnce()
      vi.useRealTimers()
  })
})