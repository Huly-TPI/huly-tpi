import { describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Button from '../../components/buttons/Button/Button'

describe('Button', () => {
  it('aplica variante y tamano configurables', () => {
    render(
      <Button variant="neutral" size="lg">
        Accion
      </Button>,
    )

    const button = screen.getByRole('button', { name: 'Accion' })
    expect(button).toHaveClass('button--neutral')
    expect(button).toHaveClass('button--lg')
  })

  it('muestra loadingLabel y queda deshabilitado cuando isLoading es true', () => {
    render(
      <Button isLoading loadingLabel="Guardando...">
        Guardar
      </Button>,
    )

    const button = screen.getByRole('button', { name: 'Guardando...' })
    expect(button).toBeDisabled()
    expect(button).toHaveAttribute('aria-busy', 'true')
  })

  it('bloquea doble click mientras se resuelve un onClick asincrono', async () => {
    const user = userEvent.setup()
    let resolveClick: (() => void) | undefined
    const onClick = vi.fn(
      () =>
        new Promise<void>(resolve => {
          resolveClick = resolve
        }),
    )

    render(<Button onClick={onClick}>Enviar</Button>)

    const button = screen.getByRole('button', { name: 'Enviar' })
    await user.click(button)
    await user.click(button)

    expect(onClick).toHaveBeenCalledTimes(1)
    expect(button).toBeDisabled()

    resolveClick?.()
    await waitFor(() => expect(button).not.toBeDisabled())
  })

  it('delegates async errors through onAsyncError when provided', async () => {
    const user = userEvent.setup()
    const asyncError = new Error('fallo')
    const onAsyncError = vi.fn()
    const onClick = vi.fn(() => Promise.reject(asyncError))

    render(
      <Button onClick={onClick} onAsyncError={onAsyncError}>
        Enviar
      </Button>,
    )

    await user.click(screen.getByRole('button', { name: 'Enviar' }))

    expect(onAsyncError).toHaveBeenCalledWith(asyncError)
  })
})
