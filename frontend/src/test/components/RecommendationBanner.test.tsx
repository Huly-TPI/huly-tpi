import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import RecommendationBanner from '../../components/Pending/RecommendationBanner'
import { clickButton, verifyTextPresent } from '../testHelpers'

describe('RecommendationBanner', () => {
  it('muestra las tareas recomendadas', () => {
    renderBanner(['Lavar los platos', 'Escribir el informe'])

    verifyTextPresent('Lavar los platos')
    verifyTextPresent('Escribir el informe')
  })

  it('el botón de tilde llama a onAccept', () => {
    const onAccept = vi.fn().mockResolvedValue(undefined)
    renderBanner(['Tarea'], onAccept)

    return clickAccept().then(() => {
      verifyAcceptCalled(onAccept)
    })
  })

  it('el botón de cruz llama a onReject', () => {
    const onReject = vi.fn().mockResolvedValue(undefined)
    renderBanner(['Tarea'], vi.fn(), onReject)

    return clickReject().then(() => {
      verifyRejectCalled(onReject)
    })
  })

  it('no tiene un botón de cierre neutral: solo aceptar o rechazar', () => {
    renderBanner(['Tarea'])

    verifyNoNeutralCloseButton()
    verifyOnlyTwoButtonsPresent()
  })

  it('se muestra como un modal por encima del contenido (con backdrop)', () => {
    renderBanner(['Tarea'])

    verifyDialogPresent()
  })

  /* helpers */

  const renderBanner = (taskTitles: string[], onAccept = vi.fn(), onReject = vi.fn()) => {
    render(<RecommendationBanner taskTitles={taskTitles} onAccept={onAccept} onReject={onReject} />)
  }

  const clickAccept = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Aceptar recomendación')
  }

  const clickReject = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Rechazar recomendación')
  }

  const verifyAcceptCalled = (onAccept: ReturnType<typeof vi.fn>) => {
    expect(onAccept).toHaveBeenCalled()
  }

  const verifyRejectCalled = (onReject: ReturnType<typeof vi.fn>) => {
    expect(onReject).toHaveBeenCalled()
  }

  const verifyNoNeutralCloseButton = () => {
    expect(screen.queryByLabelText(/^cerrar$/i)).not.toBeInTheDocument()
  }

  const verifyOnlyTwoButtonsPresent = () => {
    expect(screen.getAllByRole('button')).toHaveLength(2)
  }

  const verifyDialogPresent = () => {
    expect(screen.getByRole('dialog')).toBeInTheDocument()
  }
})
