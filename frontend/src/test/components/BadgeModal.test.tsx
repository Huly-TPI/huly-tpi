import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import BadgeModal from '../../components/Badges/BadgeModal'
import { BadgeWithStatus } from '../../hooks/useBadges'

const mockUseBadges = vi.fn()
vi.mock('../../hooks/useBadges', () => ({ useBadges: () => mockUseBadges() }))

vi.mock('../../components/Badges/BadgeCard', () => ({
  default: ({ badge, unlocked }: { badge: { name: string }, unlocked: boolean }) => (
    <div data-testid="badge-card">{badge.name} - {unlocked ? 'desbloqueada' : 'bloqueada'}</div>
  ),
}))


const makeBadgeWithStatus = (code: string, unlocked: boolean): BadgeWithStatus => ({
    badge: { id:1, code, name: code, description: null, imageUrl: null, createdAt: '' }, 
    unlocked, 
    obtainedAt: unlocked ? '2026-01-01T00:00:00.000Z' : null
})

describe('BadgeModal', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('no renderiza nada cuando isOpen es false', () => {
        mockUseBadges.mockReturnValue({ badges: [], loading: false, error: null })
        render(<BadgeModal isOpen={false} onClose={() => {}} />)
        expect(screen.queryByText('Mis estampitas')).not.toBeInTheDocument()
    })

    it('muestra el titulo cuando está abierto', () => {
        mockUseBadges.mockReturnValue({ badges: [], loading: false, error: null })
        render(<BadgeModal isOpen onClose={() => {}} />)
        expect(screen.getByText('Mis estampitas')).toBeInTheDocument()
    })

    it('renderiza una BadgeCard por cada insignia', () => {
        mockUseBadges.mockReturnValue({ badges: [
            makeBadgeWithStatus('PRIMER_PASO', true),
            makeBadgeWithStatus('VALENTIA', false),
        ], loading: false, error: null })
        render(<BadgeModal isOpen={true} onClose={() => {}} />)
        expect(screen.getAllByTestId('badge-card')).toHaveLength(2)
    })

    it('muestra loading mientras se cargan las insignias', () => {
        mockUseBadges.mockReturnValue({ badges: [], loading: true, error: null })
        render(<BadgeModal isOpen={true} onClose={() => {}} />)
        expect(screen.getByText('Cargando insignias')).toBeInTheDocument()
    })

    it('llama onClose al hacer click en cerrar', async () => {
        mockUseBadges.mockReturnValue({ badges: [], loading: false, error: null })
        const onClose = vi.fn()
        render(<BadgeModal isOpen={true} onClose={onClose} />)
        await userEvent.click(screen.getByRole('button', { name: 'Cerrar' }))
        expect(onClose).toHaveBeenCalledOnce()
    })

})