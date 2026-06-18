import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { ActivityCard, type Activity } from '../../components/backoffice/ActivityCard'

const makeActivity = (overrides?: Partial<Activity>): Activity => ({
  emoji: '🫧',
  name: 'Reventar burbujas',
  pct: 65,
  barColor: 'bg-violeta',
  iconBg: 'bg-violet-100',
  ...overrides,
})

describe('ActivityCard', () => {
  it('renderiza el emoji, nombre y porcentaje', () => {
    render(<ActivityCard activity={makeActivity()} />)

    expect(screen.getByText('🫧')).toBeInTheDocument()
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

  it('muestra emoji y fondo de icono personalizados', () => {
    render(
      <ActivityCard
        activity={makeActivity({ emoji: '🌬️', iconBg: 'bg-teal-100' })}
      />,
    )

    expect(screen.getByText('🌬️')).toBeInTheDocument()
  })
})
