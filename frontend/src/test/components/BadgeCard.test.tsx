import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import BadgeCard from '../../components/Badges/BadgeCard'
import { verifyTextPresent } from '../testHelpers'



describe('BadgeCard', () => {
    it('muestra el nombre de la insignia', () => {
        renderBadgeCard(true)
        verifyBadgeName('Primer paso')
    })

    it('muestra la imagen cuando está desbloqueada', () => {
        renderBadgeCard(true)
        verifyImageUnlocked('Primer paso')
    })

    it('aplica greyscale cuando está bloqueada', () => {
        renderBadgeCard(false)
        verifyImageLocked('Primer paso')
    })

    it('muestra el icono de candado cuando está bloqueada', () => {
        renderBadgeCard(false)
        verifyLockIconPresent()
    })

    it('no muestra el icono de candado cuando está desbloqueada', () => {
        renderBadgeCard(true)
        verifyLockIconNotPresent()
    })
    const renderBadgeCard = (unlocked: boolean) => {
        render(<BadgeCard badge={makeBadge()} unlocked={unlocked} obtainedAt={null} />)
    }

    const verifyBadgeName = (name: string) => {
        verifyTextPresent(name)
    }

    const verifyImageUnlocked = (name: string) => {
        const img = screen.getByRole('img', { name })
        expect(img).toBeInTheDocument()
        expect(img).not.toHaveClass('grayscale')
    }

    const verifyImageLocked = (name: string) => {
        expect(screen.getByRole('img', { name })).toHaveClass('grayscale')
    }

    const verifyLockIconPresent = () => {
        expect(screen.getByTestId('lock-icon')).toBeInTheDocument()
    }

    const verifyLockIconNotPresent = () => {
        expect(screen.queryByTestId('lock-icon')).not.toBeInTheDocument()
    }
})

function makeBadge(overrides = {}) {
  return ({
    id: 1,
    code: 'PRIMER_PASO',
    name: 'Primer paso',
    description: 'Empezaste tu camino.',
    imageUrl: 'badge_primer_paso.webp',
    createdAt: '2026-01-01T00:00:00Z',
    ...overrides,
})
}
