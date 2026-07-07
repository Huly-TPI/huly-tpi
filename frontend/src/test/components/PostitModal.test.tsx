import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PostitModal from '../../components/Challenges/PostitModal'
import type { UserGoalResponse } from '../../api/userGoals'
import { verifyTextPresent } from '../testHelpers'


vi.mock('../../assets/challenges/challenge-detail-bg.png', () => ({ default: 'challenge-detail-bg.png' }))



describe('PostitModal — sección de imagen', () => {
  let onCompleteSpy: any
  let onCloseSpy: any

  it('muestra la comparación de monedas cuando el reto está pendiente y se puede completar', () => {
    renderDetailWithGoalAndComplete(makeGoal())
    expect(screen.getByText('Sin foto', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('Con foto', { exact: false })).toBeInTheDocument()
    expect(screen.getByText('+ Adjuntar imagen', { exact: false })).toBeInTheDocument()
  })

  it('muestra los valores de monedas del reto', () => {
    renderDetailWithGoalAndComplete(makeGoal({ coinsReward: 15, coinsRewardWithImage: 30 }))
    verifyTextPresent('15')
    verifyTextPresent('30')
  })

  it('no muestra la sección de imagen cuando el reto está completado', () => {
    renderDetailWithGoalAndComplete(makeGoal({ status: 'COMPLETED' }))
    expect(screen.queryByText('Sin foto', { exact: false })).not.toBeInTheDocument()
    expect(screen.queryByText('Adjuntar imagen', { exact: false })).not.toBeInTheDocument()
  })

  it('no muestra la sección de imagen cuando no se provee onComplete', () => {
    renderDetailWithGoal(makeGoal())
    expect(screen.queryByText('Sin foto', { exact: false })).not.toBeInTheDocument()
    expect(screen.queryByText('Adjuntar imagen', { exact: false })).not.toBeInTheDocument()
  })

  it('muestra el nombre del archivo cuando se selecciona una imagen', () => {
    renderDetailWithGoalAndCompleteWithUser(makeGoal())
    return uploadFile('foto.jpg', 'image/jpeg', 'contenido').then(() => {
      verifyTextPresent('📎 foto.jpg')
    })
  })

  it('llama a onComplete con la imagen seleccionada al completar', () => {
    setupOnCompleteSpy(vi.fn().mockResolvedValue(undefined))
    renderDetailWithGoalAndCustomCompleteWithUser(makeGoal())
    return uploadFile('foto.jpg', 'image/jpeg', 'contenido')
      .then(() => clickCompleteButton())
      .then(() => {
        verifyOnCompleteCalledWith(1, expect.any(File))
      })
  })

  it('llama a onComplete sin imagen cuando no se seleccionó archivo', () => {
    setupOnCompleteSpy(vi.fn().mockResolvedValue(undefined))
    renderDetailWithGoalAndCustomCompleteWithUser(makeGoal())
    return clickCompleteButton().then(() => {
      verifyOnCompleteCalledWith(1, undefined)
    })
  })

  it('muestra error y no selecciona el archivo cuando supera los 5 MB', () => {
    renderDetailWithGoalAndCompleteWithUser(makeGoal())
    return uploadFileWithSize('foto-grande.jpg', 'image/jpeg', 6 * 1024 * 1024)
      .then(() => waitForText('La imagen no puede superar los 5 MB.'))
      .then(() => {
        expect(screen.queryByText('📎', { exact: false })).not.toBeInTheDocument()
      })
  })

  it('muestra el mensaje de error cuando onComplete falla', () => {
    setupOnCompleteSpy(vi.fn().mockRejectedValue(new Error('La imagen no tiene relación con el reto')))
    setupOnCloseSpy()
    renderDetailWithGoalAndCustomCompleteAndCloseWithUser(makeGoal())
    return clickCompleteButton()
      .then(() => waitForTextInScreen('La imagen no tiene relación con el reto'))
      .then(() => {
        verifyOnCloseNotCalled()
      })
  })

  it('no cierra el modal cuando onComplete falla', () => {
    setupOnCompleteSpy(vi.fn().mockRejectedValue(new Error('Formato no soportado')))
    setupOnCloseSpy()
    renderDetailWithGoalAndCustomCompleteAndCloseWithUser(makeGoal())
    return clickCompleteButton()
      .then(() => waitForText('✓ Completar'))
      .then(() => {
        verifyOnCloseNotCalled()
      })
  })
  let user: any

  /* helpers */

  const renderDetailWithGoalAndComplete = (goal: UserGoalResponse) => {
    render(
      <PostitModal
        initialMode="detail"
        goal={goal}
        onClose={vi.fn()}
        onComplete={vi.fn()}
      />
    )
  }

  const renderDetailWithGoal = (goal: UserGoalResponse) => {
    render(
      <PostitModal
        initialMode="detail"
        goal={goal}
        onClose={vi.fn()}
      />
    )
  }

  const renderDetailWithGoalAndCompleteWithUser = (goal: UserGoalResponse) => {
    user = userEvent.setup()
    renderDetailWithGoalAndComplete(goal)
  }

  const setupOnCompleteSpy = (spy: any) => {
    onCompleteSpy = spy
  }

  const setupOnCloseSpy = () => {
    onCloseSpy = vi.fn()
  }

  const renderDetailWithGoalAndCustomCompleteWithUser = (goal: UserGoalResponse) => {
    user = userEvent.setup()
    render(
      <PostitModal
        initialMode="detail"
        goal={goal}
        onClose={vi.fn()}
        onComplete={onCompleteSpy}
      />
    )
  }

  const renderDetailWithGoalAndCustomCompleteAndCloseWithUser = (goal: UserGoalResponse) => {
    user = userEvent.setup()
    render(
      <PostitModal
        initialMode="detail"
        goal={goal}
        onClose={onCloseSpy}
        onComplete={onCompleteSpy}
      />
    )
  }

  const uploadFile = (name: string, type: string, content: string) => {
    const file = new File([content], name, { type })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    return user.upload(input, file)
  }

  const uploadFileWithSize = (name: string, type: string, size: number) => {
    const file = new File(['x'], name, { type })
    Object.defineProperty(file, 'size', { value: size })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    return user.upload(input, file)
  }

  const clickCompleteButton = () => {
    return user.click(screen.getByText('✓ Completar'))
  }

  const verifyOnCompleteCalledWith = (id: number, file: any) => {
    expect(onCompleteSpy).toHaveBeenCalledWith(id, file)
  }

  const waitForText = (text: string) => {
    return waitFor(() => {
      verifyTextPresent(text)
    })
  }

  const waitForTextInScreen = (text: string) => {
    return waitFor(() => {
      expect(screen.getByText(text)).toBeInTheDocument()
    })
  }

  const verifyOnCloseNotCalled = () => {
    expect(onCloseSpy).not.toHaveBeenCalled()
  }
})

function makeGoal(overrides: Partial<UserGoalResponse> = {}): UserGoalResponse {
  return ({
  id: 1,
  userId: 10,
  title: 'Meta de prueba',
  description: null,
  status: 'PENDING',
  createdAt: '2026-01-01T00:00:00Z',
  activityId: null,
  imageUrl: null,
  coinsReward: 10,
  coinsRewardWithImage: 25,
  ...overrides,
})
}
