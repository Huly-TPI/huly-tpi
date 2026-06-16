import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'

vi.mock('../../hooks/backoffice/useActivities', () => ({
  useActivities: vi.fn(),
}))

import { ActivitiesSection } from '../../components/backoffice/ActivitiesSection'
import { useActivities } from '../../hooks/backoffice/useActivities'

const mockedUseActivities = vi.mocked(useActivities)

describe('ActivitiesSection', () => {
  it('muestra skeletons de carga cuando loading es true', () => {
    mockedUseActivities.mockReturnValue({ activities: [], loading: true })

    render(<ActivitiesSection />)

    // Skeleton renders 4 placeholder items with animate-pulse divs
    const skeletons = document.querySelectorAll('.animate-pulse')
    expect(skeletons.length).toBeGreaterThanOrEqual(4)
  })

  it('renderiza las ActivityCards cuando los datos están cargados', () => {
    mockedUseActivities.mockReturnValue({
      activities: [
        { emoji: '🫧', name: 'Reventar burbujas', pct: 75, barColor: 'bg-violeta', iconBg: 'bg-violet-100' },
        { emoji: '🌬️', name: 'Respiraciones guiadas', pct: 50, barColor: 'bg-teal-400', iconBg: 'bg-teal-100' },
      ],
      loading: false,
    })

    render(<ActivitiesSection />)

    expect(screen.getByText('Reventar burbujas')).toBeInTheDocument()
    expect(screen.getByText('Respiraciones guiadas')).toBeInTheDocument()
    expect(screen.getByText('75%')).toBeInTheDocument()
    expect(screen.getByText('50%')).toBeInTheDocument()
  })

  it('muestra el título de la sección', () => {
    mockedUseActivities.mockReturnValue({ activities: [], loading: false })

    render(<ActivitiesSection />)

    expect(screen.getByText('Biblioteca de Actividades')).toBeInTheDocument()
  })

  it('muestra el botón "Editar todo"', () => {
    mockedUseActivities.mockReturnValue({ activities: [], loading: false })

    render(<ActivitiesSection />)

    expect(screen.getByRole('button', { name: /editar todo/i })).toBeInTheDocument()
  })
})
