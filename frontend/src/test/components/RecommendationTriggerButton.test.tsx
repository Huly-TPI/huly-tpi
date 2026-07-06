import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import RecommendationTriggerButton from '../../components/Pending/RecommendationTriggerButton'
import { clickButton, verifyTextPresent } from '../testHelpers'

describe('RecommendationTriggerButton', () => {
  it('llama a onClick al presionarlo', () => {
    const onClick = vi.fn().mockResolvedValue(undefined)
    renderTriggerButton(onClick)

    return clickTriggerButton().then(() => {
      verifyOnClickCalled(onClick)
    })
  })

  it('muestra un tooltip explicando la acción al pasar el mouse', () => {
    renderTriggerButton()

    return hoverTriggerButton().then(() => {
      verifyTextPresent('Generar combinación recomendada')
    })
  })

  it('se deshabilita mientras espera la respuesta', () => {
    let resolvePromise: () => void = () => {}
    const onClick = vi.fn(() => new Promise<void>(resolve => { resolvePromise = resolve }))
    renderTriggerButton(onClick)

    return clickTriggerButton()
      .then(() => waitFor(() => verifyTriggerButtonDisabled()))
      .then(() => {
        resolvePromise()
        return waitFor(() => verifyTriggerButtonEnabled())
      })
  })

  /* helpers */

  const renderTriggerButton = (onClick = vi.fn()) => {
    render(<RecommendationTriggerButton onClick={onClick} />)
  }

  const clickTriggerButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Generar combinación recomendada')
  }

  const hoverTriggerButton = () => {
    const user = userEvent.setup()
    return user.hover(screen.getByLabelText('Generar combinación recomendada'))
  }

  const verifyOnClickCalled = (onClick: ReturnType<typeof vi.fn>) => {
    expect(onClick).toHaveBeenCalled()
  }

  const verifyTriggerButtonDisabled = () => {
    expect(screen.getByLabelText('Generar combinación recomendada')).toBeDisabled()
  }

  const verifyTriggerButtonEnabled = () => {
    expect(screen.getByLabelText('Generar combinación recomendada')).not.toBeDisabled()
  }
})
