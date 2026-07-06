import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import BadgeModal from '../../components/Badges/BadgeModal'
import { BadgeWithStatus } from '../../hooks/useBadges'
import { verifyTextPresent, verifyTextNotPresent, clearAllMocks } from '../testHelpers'

const mockUseBadges = vi.fn()
vi.mock('../../hooks/useBadges', () => ({ useBadges: () => mockUseBadges() }))

vi.mock('../../components/Badges/BadgeCard', () => ({
  default: ({ badge, unlocked }: { badge: { name: string }, unlocked: boolean }) => (
    <div data-testid="badge-card">{badge.name} - {unlocked ? 'desbloqueada' : 'bloqueada'}</div>
  ),
}))



describe('BadgeModal', () => {
    let onCloseMock: any

    beforeEach(() => {
        clearAllMocks()
        onCloseMock = vi.fn()
    })

    it('no renderiza nada cuando isOpen es false', () => {
        setupUseBadges([], false)
        renderModal(false)
        verifyTitleNotPresent()
    })

    it('muestra el titulo cuando está abierto', () => {
        setupUseBadges([], false)
        renderModal(true)
        verifyTitlePresent()
    })

    it('renderiza una BadgeCard por cada insignia', () => {
        setupUseBadges([
            makeBadgeWithStatus('PRIMER_PASO', true),
            makeBadgeWithStatus('VALENTIA', false),
        ], false)
        renderModal(true)
        verifyBadgeCardsCount(2)
    })

    it('muestra loading mientras se cargan las insignias', () => {
        setupUseBadges([], true)
        renderModal(true)
        verifyLoadingStatePresent()
    })

    it('llama onClose al hacer click en cerrar', () => {
        setupUseBadges([], false)
        renderModal(true)
        return clickCloseButton().then(() => {
            verifyOnCloseCalled()
        })
    })
    const setupUseBadges = (badges: BadgeWithStatus[], loading: boolean, error: string | null = null) => {
        mockUseBadges.mockReturnValue({ badges, loading, error })
    }

    const renderModal = (isOpen: boolean) => {
        render(<BadgeModal isOpen={isOpen} onClose={onCloseMock} />)
    }

    const verifyTitlePresent = () => {
        verifyTextPresent('Mis estampitas')
    }

    const verifyTitleNotPresent = () => {
        verifyTextNotPresent('Mis estampitas')
    }

    const verifyBadgeCardsCount = (count: number) => {
        expect(screen.getAllByTestId('badge-card')).toHaveLength(count)
    }

    const verifyLoadingStatePresent = () => {
        verifyTextPresent('Cargando estampitas...')
    }

    const clickCloseButton = () => {
        const user = userEvent.setup()
        return user.click(screen.getByRole('button', { name: 'Cerrar' }))
    }

    const verifyOnCloseCalled = () => {
        expect(onCloseMock).toHaveBeenCalledOnce()
    }
})

function makeBadgeWithStatus(code: string, unlocked: boolean): BadgeWithStatus {
  return ({
    badge: { id:1, code, name: code, description: null, imageUrl: null, createdAt: '' }, 
    unlocked, 
    obtainedAt: unlocked ? '2026-01-01T00:00:00.000Z' : null
})
}
