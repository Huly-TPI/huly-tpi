import { beforeEach, describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BadgeToastProvider, useBadgeToast } from '../../context/BadgeToast'
import { BadgeResponse } from '../../api/badges'

const makeBadge = (): BadgeResponse => ({
  id: 1, code: 'PRIMER_PASO', name: 'Primer paso',
  description: null, imageUrl: null, createdAt: ''
})

function TriggerButton() {
  const { showBadgeToast } = useBadgeToast()
  return <button onClick={() => showBadgeToast(makeBadge())}>Mostrar</button>
}

describe('BadgeToast', () => {
  beforeEach(() => {
    document.body.removeAttribute('data-home-onboarding-active')
  })

  it('renderiza los hijos correctamente', () => {
    render(<BadgeToastProvider><p>Hijo</p></BadgeToastProvider>)
    expect(screen.getByText('Hijo')).toBeInTheDocument()
  })

  it('showBadgeToast muestra el toast con el nombre del Badge', async () => {
    render(<BadgeToastProvider><TriggerButton /></BadgeToastProvider>)
    const button = screen.getByRole('button', { name: 'Mostrar' })
    await userEvent.click(button)
    expect(screen.getByText('Primer paso')).toBeInTheDocument()
  })

  it('el toast se oculta al hacer click en Cerrar', async () => {
    render(<BadgeToastProvider><TriggerButton /></BadgeToastProvider>)
    const button = screen.getByRole('button', { name: 'Mostrar' })
    await userEvent.click(button)
    const closeButton = screen.getByRole('button', { name: 'Cerrar' })
    await userEvent.click(closeButton)
    expect(screen.queryByText('Primer paso')).not.toBeInTheDocument()
  })

  it('no muestra el toast mientras el tutorial principal esta activo', async () => {
    document.body.setAttribute('data-home-onboarding-active', 'true')

    render(<BadgeToastProvider><TriggerButton /></BadgeToastProvider>)

    await userEvent.click(screen.getByRole('button', { name: 'Mostrar' }))

    expect(screen.queryByText('Primer paso')).not.toBeInTheDocument()
  })
  })
