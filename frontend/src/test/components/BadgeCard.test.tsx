import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import BadgeCard from '../../components/Badges/BadgeCard'

const makeBadge = (overrides = {}) => ({
    id: 1,
    code: 'PRIMER_PASO',
    name: 'Primer paso',
    description: 'Empezaste tu camino.',
    imageUrl: 'badge_primer_paso.webp',
    createdAt: '2026-01-01T00:00:00Z',
    ...overrides,
})

describe('BadgeCard', () => {
    it('muestra el nombre de la insignia', () => {
        render(<BadgeCard badge={makeBadge()} unlocked={true} obtainedAt={null} />)
        expect(screen.getByText('Primer paso')).toBeInTheDocument()
    })


    it('muestra la imagen cuando está desbloqueada', () =>
    {
        render(<BadgeCard badge={makeBadge()} unlocked={true} obtainedAt={null} />)
        const img = screen.getByRole('img', { name: 'Primer paso'})
        expect(img).toBeInTheDocument()
        expect(img).not.toHaveClass('grayscale')
    })

    it('aplica greyscale cuando está bloqueada', () => {
        render(<BadgeCard badge={makeBadge()} unlocked={false} obtainedAt={null} />)
        expect(screen.getByRole('img', { name: 'Primer paso'})).toHaveClass('grayscale')
    })

    it('muestra el icono de candado cuando está bloqueada', () => {
        render(<BadgeCard badge={makeBadge()} unlocked={false} obtainedAt={null} />)
        expect(screen.getByTestId('lock-icon')).toBeInTheDocument()
    })

    it('no muestra el icono de candado cuando está desbloqueada', () => {
        render(<BadgeCard badge={makeBadge()} unlocked={true} obtainedAt={null} />)
        expect(screen.queryByTestId('lock-icon')).not.toBeInTheDocument()
    })
})