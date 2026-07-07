import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Sparkles, Wind } from 'lucide-react'

vi.mock('../../hooks/backoffice/useActivities', () => ({
  useActivities: vi.fn(),
}))

import { ActivitiesSection } from '../../components/backoffice/ActivitiesSection'
import { useActivities } from '../../hooks/backoffice/useActivities'
import { verifyTextPresent } from '../testHelpers'

const mockedUseActivities = vi.mocked(useActivities)

describe('ActivitiesSection', () => {
  it('muestra skeletons de carga cuando loading es true', () => {
    setupUseActivities({ activities: [], loading: true })
    renderActivitiesSection()
    verifySkeletonsCountGreaterThanOrEqual(4)
  })

  it('renderiza las ActivityCards cuando los datos están cargados', () => {
    setupUseActivities({
      activities: [
        { Icon: Sparkles, name: 'Reventar burbujas', pct: 75, barColor: 'bg-violeta', iconBg: 'bg-violet-100', iconColor: 'text-violeta' },
        { Icon: Wind, name: 'Respiraciones guiadas', pct: 50, barColor: 'bg-teal-400', iconBg: 'bg-teal-100', iconColor: 'text-teal-500' },
      ],
      loading: false,
    })
    renderActivitiesSection()
    verifyActivityNamesAndPercentages(['Reventar burbujas', 'Respiraciones guiadas'], ['75%', '50%'])
  })

  it('muestra el título de la sección', () => {
    setupUseActivities({ activities: [], loading: false })
    renderActivitiesSection()
    verifySectionTitle()
  })

  it('muestra el botón "Editar todo"', () => {
    setupUseActivities({ activities: [], loading: false })
    renderActivitiesSection()
    verifyEditButtonIsPresent()
  })

  /* helpers */

  const setupUseActivities = (data: { activities: any[]; loading: boolean }) => {
    mockedUseActivities.mockReturnValue(data)
  }

  const renderActivitiesSection = () => {
    render(<ActivitiesSection />)
  }

  const verifySkeletonsCountGreaterThanOrEqual = (minCount: number) => {
    const skeletons = document.querySelectorAll('.animate-pulse')
    expect(skeletons.length).toBeGreaterThanOrEqual(minCount)
  }

  const verifyActivityNamesAndPercentages = (names: string[], percentages: string[]) => {
    names.forEach(name => verifyTextPresent(name))
    percentages.forEach(pct => verifyTextPresent(pct))
  }

  const verifySectionTitle = () => {
    verifyTextPresent('Biblioteca de Actividades')
  }

  const verifyEditButtonIsPresent = () => {
    expect(screen.getByRole('button', { name: 'Editar todo' })).toBeInTheDocument()
  }
})
