import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor } from '@testing-library/react'
import { useBadges } from '../../hooks/useBadges'
import { badgesApi } from '../../api/badges'

vi.mock('../../api/badges', () => ({
    badgesApi: {
        getAll: vi.fn(),
        getMyBadges: vi.fn(),
    },
}))

const mockedGetAll = vi.mocked(badgesApi.getAll)
const mockedGetMyBadges = vi.mocked(badgesApi.getMyBadges)

const makeBadge = (code: string) => ({
    id: 1,
    code,
    name: 'Insignia',
    description: null,
    imageUrl: null,
    createdAt: '2026-01-01T00:00:00Z'
})

describe('useBadges', () => {
    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('combina todas las insignas y marca las desbloqueadas', async () => {
        mockedGetAll.mockResolvedValueOnce([makeBadge('PRIMER_PASO'), makeBadge('VALENTÍA')] as never)
        mockedGetMyBadges.mockResolvedValueOnce([
            {
                id: 1,
                badge: makeBadge('PRIMER_PASO'),
                obtainedAt: '2026-01-02T00:00:00Z'
            }
        ] as never)
        const { result } = renderHook(() => useBadges())

        await waitFor(() => {
            expect(result.current.loading).toBe(false)
        })
        expect(result.current.badges).toHaveLength(2)
        expect(result.current.badges[0].unlocked).toBe(true)
        expect(result.current.badges[1].unlocked).toBe(false)
    })

    it('retorna lista vacia si no hay insignias', async () => {
        mockedGetAll.mockResolvedValueOnce([] as never)
        mockedGetMyBadges.mockResolvedValueOnce([] as never)
        const { result } = renderHook(() => useBadges())

        await waitFor(() => {
            expect(result.current.loading).toBe(false)
        })
        expect(result.current.badges).toHaveLength(0)
    })

    it('setea error si falla la carga', async () => {
        mockedGetAll.mockRejectedValueOnce(new Error('Error de red') as never)
        const { result } = renderHook(() => useBadges())

        await waitFor(() => {
            expect(result.current.error).toBe("Error al cargar los badges")
        })
    })
})
