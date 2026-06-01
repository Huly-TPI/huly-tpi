import { describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Button from '../../components/Buttons/Button/Button'

describe('Button', () => {
  it.each([
    { variant: 'primary' as const, expectedClass: 'bg-violeta' },
    { variant: 'secondary' as const, expectedClass: 'text-violeta' },
    { variant: 'tertiary' as const, expectedClass: 'text-bosque' },
    { variant: 'alert' as const, expectedClass: 'bg-anaranjado' },
  ])('aplica variante visual $variant', ({ variant, expectedClass }) => {
    render(<Button variant={variant}>Accion</Button>)

    const button = screen.getByRole('button', { name: 'Accion' })
    expect(button).toHaveClass(expectedClass)
  })

  it('aplica tamano configurable', () => {
    render(<Button size="lg">Accion</Button>)

    const button = screen.getByRole('button', { name: 'Accion' })
    expect(button).toHaveClass('min-w-[13.5rem]')
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
