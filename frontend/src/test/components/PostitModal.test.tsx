import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PostitModal from '../../components/Challenges/PostitModal'
import type { UserGoalResponse } from '../../api/userGoals'

vi.mock('../../assets/challenges/challenge-detail-bg.png', () => ({ default: 'challenge-detail-bg.png' }))

const makeGoal = (overrides: Partial<UserGoalResponse> = {}): UserGoalResponse => ({
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

describe('PostitModal — sección de imagen', () => {
  it('muestra la comparación de monedas cuando el reto está pendiente y se puede completar', () => {
    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal()}
        onClose={vi.fn()}
        onComplete={vi.fn()}
      />
    )

    expect(screen.getByText(/Sin foto/)).toBeInTheDocument()
    expect(screen.getByText(/Con foto/)).toBeInTheDocument()
    expect(screen.getByText('+ Adjuntar imagen')).toBeInTheDocument()
  })

  it('muestra los valores de monedas del reto', () => {
    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal({ coinsReward: 15, coinsRewardWithImage: 30 })}
        onClose={vi.fn()}
        onComplete={vi.fn()}
      />
    )

    expect(screen.getByText('15')).toBeInTheDocument()
    expect(screen.getByText('30')).toBeInTheDocument()
  })

  it('no muestra la sección de imagen cuando el reto está completado', () => {
    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal({ status: 'COMPLETED' })}
        onClose={vi.fn()}
        onComplete={vi.fn()}
      />
    )

    expect(screen.queryByText(/Sin foto/)).not.toBeInTheDocument()
    expect(screen.queryByText(/Adjuntar imagen/)).not.toBeInTheDocument()
  })

  it('no muestra la sección de imagen cuando no se provee onComplete', () => {
    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal()}
        onClose={vi.fn()}
      />
    )

    expect(screen.queryByText(/Sin foto/)).not.toBeInTheDocument()
    expect(screen.queryByText(/Adjuntar imagen/)).not.toBeInTheDocument()
  })

  it('muestra el nombre del archivo cuando se selecciona una imagen', async () => {
    const user = userEvent.setup()
    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal()}
        onClose={vi.fn()}
        onComplete={vi.fn()}
      />
    )

    const file = new File(['contenido'], 'foto.jpg', { type: 'image/jpeg' })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    await user.upload(input, file)

    expect(screen.getByText('📎 foto.jpg')).toBeInTheDocument()
  })

  it('llama a onComplete con la imagen seleccionada al completar', async () => {
    const user = userEvent.setup()
    const onComplete = vi.fn().mockResolvedValue(undefined)

    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal()}
        onClose={vi.fn()}
        onComplete={onComplete}
      />
    )

    const file = new File(['contenido'], 'foto.jpg', { type: 'image/jpeg' })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    await user.upload(input, file)
    await user.click(screen.getByText('✓ Completar'))

    expect(onComplete).toHaveBeenCalledWith(1, file)
  })

  it('llama a onComplete sin imagen cuando no se seleccionó archivo', async () => {
    const user = userEvent.setup()
    const onComplete = vi.fn().mockResolvedValue(undefined)

    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal()}
        onClose={vi.fn()}
        onComplete={onComplete}
      />
    )

    await user.click(screen.getByText('✓ Completar'))

    expect(onComplete).toHaveBeenCalledWith(1, undefined)
  })

  it('muestra error y no selecciona el archivo cuando supera los 5 MB', async () => {
    const user = userEvent.setup()
    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal()}
        onClose={vi.fn()}
        onComplete={vi.fn()}
      />
    )

    const largeFile = new File(['x'], 'foto-grande.jpg', { type: 'image/jpeg' })
    Object.defineProperty(largeFile, 'size', { value: 6 * 1024 * 1024 })

    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    await user.upload(input, largeFile)

    await waitFor(() =>
      expect(screen.getByText('La imagen no puede superar los 5 MB.')).toBeInTheDocument()
    )
    expect(screen.queryByText(/📎/)).not.toBeInTheDocument()
  })

  it('muestra el mensaje de error cuando onComplete falla', async () => {
    const user = userEvent.setup()
    const onComplete = vi.fn().mockRejectedValue(new Error('La imagen no tiene relación con el reto'))
    const onClose = vi.fn()

    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal()}
        onClose={onClose}
        onComplete={onComplete}
      />
    )

    await user.click(screen.getByText('✓ Completar'))

    await waitFor(() =>
      expect(
        screen.getByText('La imagen no tiene relación con el reto')
      ).toBeInTheDocument()
    )
    expect(onClose).not.toHaveBeenCalled()
  })

  it('no cierra el modal cuando onComplete falla', async () => {
    const user = userEvent.setup()
    const onComplete = vi.fn().mockRejectedValue(new Error('Formato no soportado'))
    const onClose = vi.fn()

    render(
      <PostitModal
        initialMode="detail"
        goal={makeGoal()}
        onClose={onClose}
        onComplete={onComplete}
      />
    )

    await user.click(screen.getByText('✓ Completar'))

    await waitFor(() => expect(screen.getByText('✓ Completar')).toBeInTheDocument())
    expect(onClose).not.toHaveBeenCalled()
  })
})
