import { forwardRef, useImperativeHandle } from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import Mandalas from '../../pages/Mandalas/Mandalas'

vi.mock('../../components/Mandalas/MandalaCanvas', () => ({
  default: forwardRef(function MockMandalaCanvas(_, ref) {
    useImperativeHandle(ref, () => ({ clear: vi.fn() }))
    return <div data-testid="mandala-canvas" />
  }),
}))

describe('Mandalas page', () => {
  it('muestra primero la galeria y abre la vista de pintura al elegir una mandala', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { name: /elegí un mandala/i })).toBeInTheDocument()

    await user.click(screen.getAllByRole('button', { name: 'Pintar' })[0])

    expect(await screen.findByTestId('mandala-canvas')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /elegir otro mandala/i })).toBeInTheDocument()
  })

  it('vuelve desde la vista de pintura a la galeria', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <Mandalas />
      </MemoryRouter>,
    )

    await user.click(screen.getAllByRole('button', { name: 'Pintar' })[0])
    await screen.findByTestId('mandala-canvas')
    await user.click(screen.getByRole('button', { name: /elegir otro mandala/i }))

    expect(screen.getByRole('heading', { name: /elegí un mandala/i })).toBeInTheDocument()
  })
})
