import { describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import Button from '../../components/Buttons/Button/Button'
import { clickButton, verifyButtonDisabled } from '../testHelpers'

describe('Button', () => {
  let user: ReturnType<typeof userEvent.setup>

  it.each([
    { variant: 'primary' as const, expectedClass: 'bg-violeta' },
    { variant: 'secondary' as const, expectedClass: 'text-violeta' },
    { variant: 'success' as const, expectedClass: 'bg-bosque' },
    { variant: 'successSecondary' as const, expectedClass: 'text-bosque' },
    { variant: 'tertiary' as const, expectedClass: 'text-bosque' },
    { variant: 'alert' as const, expectedClass: 'bg-anaranjado' },
  ])('aplica variante visual $variant', ({ variant, expectedClass }) => {
    renderButton({ variant })
    verifyButtonHasClass('Accion', expectedClass)
  })

  it('aplica tamaño configurable', () => {
    renderButton({ size: 'lg' })
    verifyButtonHasClass('Accion', 'min-w-[13.5rem]')
  })

  it('muestra loadingLabel y queda deshabilitado cuando isLoading es true', () => {
    renderButton({ isLoading: true, loadingLabel: 'Guardando...' }, 'Guardar')
    verifyButtonIsDisabled('Guardando...')
    verifyButtonHasAttribute('Guardando...', 'aria-busy', 'true')
  })

  it('bloquea doble click mientras se resuelve un onClick asíncrono', () => {
    return testDoubleSubmitPrevention()
  })

  it('delega errores asíncronos a través de onAsyncError cuando se proporciona', () => {
    return testAsyncErrorDelegation()
  })

  /* helpers */

  const renderButton = (props: React.ComponentProps<typeof Button>, children: React.ReactNode = 'Accion') => {
    user = userEvent.setup()
    render(<Button {...props}>{children}</Button>)
  }

  const verifyButtonHasClass = (name: string, expectedClass: string) => {
    const button = screen.getByRole('button', { name })
    expect(button).toHaveClass(expectedClass)
  }

  const verifyButtonIsDisabled = (name: string) => {
    verifyButtonDisabled(name)
  }

  const verifyButtonHasAttribute = (name: string, attribute: string, expectedValue: string) => {
    const button = screen.getByRole('button', { name })
    expect(button).toHaveAttribute(attribute, expectedValue)
  }

  const testDoubleSubmitPrevention = () => {
    let resolveClick: (() => void) | undefined
    const onClick = vi.fn(
      () =>
        new Promise<void>(resolve => {
          resolveClick = resolve
        }),
    )

    renderButton({ onClick }, 'Enviar')

    const button = screen.getByRole('button', { name: 'Enviar' })
    return user.click(button)
      .then(() => user.click(button))
      .then(() => {
        expect(onClick).toHaveBeenCalledTimes(1)
        expect(button).toBeDisabled()
        resolveClick?.()
        return waitFor(() => expect(button).not.toBeDisabled())
      })
  }

  const testAsyncErrorDelegation = () => {
    const asyncError = new Error('fallo')
    const onAsyncError = vi.fn()
    const onClick = vi.fn(() => Promise.reject(asyncError))

    renderButton({ onClick, onAsyncError }, 'Enviar')

    return clickButton(user, 'Enviar').then(() => {
      expect(onAsyncError).toHaveBeenCalledWith(asyncError)
    })
  }
})
