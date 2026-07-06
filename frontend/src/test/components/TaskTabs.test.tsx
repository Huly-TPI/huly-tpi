import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import TaskTabs from '../../components/Pending/TaskTabs'
import { clickButton } from '../testHelpers'

const hasValue = { date: false, duration: false, category: false, subtasks: false }

describe('TaskTabs', () => {
  it('llama a onTabChange con la tab clickeada', () => {
    const onTabChange = vi.fn()
    renderTabs(null, onTabChange)

    return clickTab('Fecha límite').then(() => {
      verifyTabChangeCalledWith(onTabChange, 'date')
    })
  })

  it('colapsa la tab activa si se clickea de nuevo', () => {
    const onTabChange = vi.fn()
    renderTabs('date', onTabChange)

    return clickTab('Fecha límite').then(() => {
      verifyTabChangeCalledWith(onTabChange, null)
    })
  })

  it('marca aria-pressed solo en la tab activa', () => {
    renderTabs('duration')

    verifyTabPressed('Duración estimada', 'true')
    verifyTabPressed('Fecha límite', 'false')
  })

  /* helpers */

  const renderTabs = (activeTab: 'date' | 'duration' | 'category' | 'subtasks' | null, onTabChange = vi.fn()) => {
    render(<TaskTabs activeTab={activeTab} onTabChange={onTabChange} hasValue={hasValue} />)
  }

  const clickTab = (label: string) => {
    const user = userEvent.setup()
    return clickButton(user, label)
  }

  const verifyTabChangeCalledWith = (onTabChange: ReturnType<typeof vi.fn>, expected: string | null) => {
    expect(onTabChange).toHaveBeenCalledWith(expected)
  }

  const verifyTabPressed = (label: string, expected: string) => {
    expect(screen.getByLabelText(label)).toHaveAttribute('aria-pressed', expected)
  }
})
