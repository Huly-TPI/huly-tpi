import { describe, it, expect } from 'vitest'
import { render } from '@testing-library/react'
import { Sparkles, Wind } from 'lucide-react'
import { ActivityCard, type Activity } from '../../components/backoffice/ActivityCard'
import { verifyTextPresent } from '../testHelpers'



describe('ActivityCard', () => {
  it('renderiza el icono, nombre y porcentaje', () => {
    renderActivity()
    verifyIconIsPresent()
    verifyNameAndPercentage('Reventar burbujas', '65%')
  })

  it('el ancho de la barra de progreso coincide con el porcentaje', () => {
    renderActivity({ pct: 42 })
    verifyProgressBarWidth('bg-violeta', '42%')
  })

  it('maneja el caso de 0%', () => {
    renderActivity({ pct: 0 })
    verifyNameAndPercentage('Reventar burbujas', '0%')
    verifyProgressBarWidth('bg-violeta', '0%')
  })

  it('maneja el caso de 100%', () => {
    renderActivity({ pct: 100 })
    verifyNameAndPercentage('Reventar burbujas', '100%')
    verifyProgressBarWidth('bg-violeta', '100%')
  })

  it('muestra icono y fondo personalizados', () => {
    renderActivity({ Icon: Wind, iconBg: 'bg-teal-100', iconColor: 'text-teal-500' })
    verifyIconContainerClass('bg-teal-100')
  })

  /* helpers */

  const renderActivity = (overrides?: Partial<Activity>) => {
    render(<ActivityCard activity={makeActivity(overrides)} />)
  }

  const verifyIconIsPresent = () => {
    expect(document.querySelector('svg')).toBeInTheDocument()
  }

  const verifyNameAndPercentage = (name: string, pct: string) => {
    verifyTextPresent(name)
    verifyTextPresent(pct)
  }

  const verifyProgressBarWidth = (colorClass: string, width: string) => {
    const progressBar = document.querySelector(`.${colorClass}`)
    expect(progressBar).toBeInTheDocument()
    expect(progressBar).toHaveStyle({ width })
  }

  const verifyIconContainerClass = (bgClass: string) => {
    const iconContainer = document.querySelector(`.${bgClass}`)
    expect(iconContainer).toBeInTheDocument()
    expect(iconContainer?.querySelector('svg')).toBeInTheDocument()
  }
})

function makeActivity(overrides?: Partial<Activity>): Activity {
  return ({
  Icon: Sparkles,
  name: 'Reventar burbujas',
  pct: 65,
  barColor: 'bg-violeta',
  iconBg: 'bg-violet-100',
  iconColor: 'text-violeta',
  ...overrides,
})
}
