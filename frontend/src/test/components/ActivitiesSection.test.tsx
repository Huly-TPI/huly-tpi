import { describe, it, expect, vi } from 'vitest'
import { render } from '@testing-library/react'

vi.mock('../../hooks/backoffice/useActivities', () => ({
  useActivities: vi.fn(),
}))

import { ActivitiesSection } from '../../components/backoffice/ActivitiesSection'
import { useActivities } from '../../hooks/backoffice/useActivities'
import { verifyTextPresent } from '../testHelpers'

const mockedUseActivities = vi.mocked(useActivities)

describe('ActivitiesSection', () => {
  it('muestra skeletons de carga cuando loading es true', () => {
    setupUseActivities({ popularity: [], loading: true })
    renderActivitiesSection()
    verifySkeletonsCountGreaterThanOrEqual(4)
  })

  it('renderiza la popularidad cuando los datos están cargados', () => {
    setupUseActivities({
      popularity: [
        { activityType: 'BUBBLE', activityName: 'Reventar burbujas', totalSessions: 75 },
        { activityType: 'BREATHING', activityName: 'Respiraciones guiadas', totalSessions: 25 },
      ],
      loading: false,
    })
    renderActivitiesSection()
    verifyActivityNamesAndSessions(['Reventar burbujas', 'Respiraciones guiadas'], ['75', '25'])
  })

  it('muestra el título de la sección', () => {
    setupUseActivities({ popularity: [], loading: false })
    renderActivitiesSection()
    verifySectionTitle()
  })

  /* helpers */

  const setupUseActivities = (data: { popularity: any[]; loading: boolean }) => {
    mockedUseActivities.mockReturnValue(data)
  }

  const renderActivitiesSection = () => {
    render(<ActivitiesSection />)
  }

  const verifySkeletonsCountGreaterThanOrEqual = (minCount: number) => {
    const skeletons = document.querySelectorAll('.animate-pulse')
    expect(skeletons.length).toBeGreaterThanOrEqual(minCount)
  }

  const verifyActivityNamesAndSessions = (names: string[], sessions: string[]) => {
    names.forEach(name => verifyTextPresent(name))
    sessions.forEach(sess => verifyTextPresent(sess))
  }

  const verifySectionTitle = () => {
    verifyTextPresent('Popularidad por Sesiones')
  }
})
