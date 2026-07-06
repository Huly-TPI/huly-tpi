import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import TaskTabs from '../../components/Pending/TaskTabs'

describe('TaskTabs', () => {
  const hasValue = { date: false, duration: false, category: false, subtasks: false }

  it('llama a onTabChange con la tab clickeada', () => {
    const onTabChange = vi.fn()
    render(<TaskTabs activeTab={null} onTabChange={onTabChange} hasValue={hasValue} />)

    fireEvent.click(screen.getByLabelText('Fecha límite'))

    expect(onTabChange).toHaveBeenCalledWith('date')
  })

  it('colapsa la tab activa si se clickea de nuevo', () => {
    const onTabChange = vi.fn()
    render(<TaskTabs activeTab="date" onTabChange={onTabChange} hasValue={hasValue} />)

    fireEvent.click(screen.getByLabelText('Fecha límite'))

    expect(onTabChange).toHaveBeenCalledWith(null)
  })

  it('marca aria-pressed solo en la tab activa', () => {
    render(<TaskTabs activeTab="duration" onTabChange={vi.fn()} hasValue={hasValue} />)

    expect(screen.getByLabelText('Duración estimada')).toHaveAttribute('aria-pressed', 'true')
    expect(screen.getByLabelText('Fecha límite')).toHaveAttribute('aria-pressed', 'false')
  })
})
