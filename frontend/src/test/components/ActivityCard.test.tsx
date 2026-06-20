import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Sparkles, Wind } from 'lucide-react'
import { ActivityCard, type Activity } from '../../components/backoffice/ActivityCard'

const makeActivity = (overrides?: Partial<Activity>): Activity => ({
  Icon: Sparkles,
  name: 'Reventar burbujas',
  pct: 65,
  barColor: 'bg-violeta',
  iconBg: 'bg-violet-100',
  iconColor: 'text-violeta',
  ...overrides,
})

describe('ActivityCard', () => {
  it('renderiza el icono, nombre y porcentaje', () => {
    const { container } = render(<ActivityCard activity={makeActivity()} />)

    expect(container.querySelector('svg')).toBeInTheDocument()
    expect(screen.getByText('Reventar burbujas')).toBeInTheDocument()
    expect(screen.getByText('65%')).toBeInTheDocument()
  })

  it('el ancho de la barra de progreso coincide con el porcentaje', () => {
    const { container } = render(<ActivityCard activity={makeActivity({ pct: 42 })} />)

    const progressBar = container.querySelector('.bg-violeta')
    expect(progressBar).toBeInTheDocument()
    expect(progressBar).toHaveStyle({ width: '42%' })
  })

  it('maneja el caso de 0%', () => {
    const { container } = render(<ActivityCard activity={makeActivity({ pct: 0 })} />)

    expect(screen.getByText('0%')).toBeInTheDocument()
    const progressBar = container.querySelector('.bg-violeta')
    expect(progressBar).toHaveStyle({ width: '0%' })
  })

  it('maneja el caso de 100%', () => {
    const { container } = render(<ActivityCard activity={makeActivity({ pct: 100 })} />)

    expect(screen.getByText('100%')).toBeInTheDocument()
    const progressBar = container.querySelector('.bg-violeta')
    expect(progressBar).toHaveStyle({ width: '100%' })
  })

  it('muestra icono y fondo personalizados', () => {
    const { container } = render(
      <ActivityCard
        activity={makeActivity({ Icon: Wind, iconBg: 'bg-teal-100', iconColor: 'text-teal-500' })}
      />,
    )

    const iconContainer = container.querySelector('.bg-teal-100')
    expect(iconContainer).toBeInTheDocument()
    expect(iconContainer?.querySelector('svg')).toBeInTheDocument()
  })
})
