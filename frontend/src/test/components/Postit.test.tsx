import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import Postit from '../../components/Pending/Postit'
import type { PendingTaskResponse } from '../../api/pending'

const baseTask: PendingTaskResponse = {
  id: 1,
  title: 'Lavar platos',
  description: null,
  dueDate: null,
  estimatedDuration: null,
  category: null,
  status: 'PENDING',
  subtasks: [],
  positionX: 10,
  positionY: 10,
  rotationDeg: 3.5,
  pinnedAt: '2026-01-01T00:00:00Z',
  recommended: false,
  createdAt: '2026-01-01T00:00:00Z',
  completedAt: null,
}

describe('Postit', () => {
  it('aplica la rotación indicada por el prop de forma estable entre renders', () => {
    const { container, rerender } = renderPostit(baseTask)
    const firstStyle = getPostitTransform(container)

    rerenderPostit(rerender, baseTask)
    const secondStyle = getPostitTransform(container)

    expect(firstStyle).toContain('rotate(3.5deg)')
    expect(firstStyle).toBe(secondStyle)
  })

  it('muestra el pin cuando la tarea ya está fijada', () => {
    renderPostit(baseTask)
    verifyPinPresent()
  })

  it('no muestra el pin cuando la tarea no está fijada', () => {
    renderPostit({ ...baseTask, pinnedAt: null })
    verifyPinNotPresent()
  })

  it('muestra el badge de recomendado solo cuando isRecommended es true', () => {
    const { rerender } = renderPostit(baseTask, false)
    verifyRecommendedBadgeNotPresent()

    rerenderPostit(rerender, baseTask, true)
    verifyRecommendedBadgePresent()
  })

  /* helpers */

  const renderPostit = (task: PendingTaskResponse, isRecommended = false) => {
    return render(<Postit task={task} isRecommended={isRecommended} onPickUp={vi.fn()} onOpen={vi.fn()} />)
  }

  const rerenderPostit = (
    rerender: (ui: React.ReactElement) => void,
    task: PendingTaskResponse,
    isRecommended = false,
  ) => {
    rerender(<Postit task={task} isRecommended={isRecommended} onPickUp={vi.fn()} onOpen={vi.fn()} />)
  }

  const getPostitTransform = (container: HTMLElement) => {
    return (container.querySelector('.postit') as HTMLElement).style.transform
  }

  const verifyPinPresent = () => {
    expect(screen.getByTestId('postit-pin')).toBeInTheDocument()
  }

  const verifyPinNotPresent = () => {
    expect(screen.queryByTestId('postit-pin')).not.toBeInTheDocument()
  }

  const verifyRecommendedBadgePresent = () => {
    expect(screen.getByLabelText(/recomendado hoy/)).toBeInTheDocument()
  }

  const verifyRecommendedBadgeNotPresent = () => {
    expect(screen.queryByLabelText(/recomendado hoy/)).not.toBeInTheDocument()
  }
})
