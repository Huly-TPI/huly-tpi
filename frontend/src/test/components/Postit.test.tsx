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
    const { container, rerender } = render(
      <Postit task={baseTask} isRecommended={false} onPickUp={vi.fn()} onOpen={vi.fn()} />,
    )
    const firstStyle = (container.querySelector('.postit') as HTMLElement).style.transform

    rerender(<Postit task={baseTask} isRecommended={false} onPickUp={vi.fn()} onOpen={vi.fn()} />)
    const secondStyle = (container.querySelector('.postit') as HTMLElement).style.transform

    expect(firstStyle).toContain('rotate(3.5deg)')
    expect(firstStyle).toBe(secondStyle)
  })

  it('muestra el pin cuando la tarea ya está fijada', () => {
    render(<Postit task={baseTask} isRecommended={false} onPickUp={vi.fn()} onOpen={vi.fn()} />)
    expect(screen.getByTestId('postit-pin')).toBeInTheDocument()
  })

  it('no muestra el pin cuando la tarea no está fijada', () => {
    render(<Postit task={{ ...baseTask, pinnedAt: null }} isRecommended={false} onPickUp={vi.fn()} onOpen={vi.fn()} />)
    expect(screen.queryByTestId('postit-pin')).not.toBeInTheDocument()
  })

  it('muestra el badge de recomendado solo cuando isRecommended es true', () => {
    const { rerender } = render(<Postit task={baseTask} isRecommended={false} onPickUp={vi.fn()} onOpen={vi.fn()} />)
    expect(screen.queryByLabelText(/recomendado hoy/)).not.toBeInTheDocument()

    rerender(<Postit task={baseTask} isRecommended onPickUp={vi.fn()} onOpen={vi.fn()} />)
    expect(screen.getByLabelText(/recomendado hoy/)).toBeInTheDocument()
  })
})
