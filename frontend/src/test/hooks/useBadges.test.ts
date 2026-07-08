import { clearAllMocks } from '../testHelpers'
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



describe('useBadges', () => {
    beforeEach(() => {
        clearAllMocks()
    })

    it('combina todas las insignas y marca las desbloqueadas', () => {
        setupGetBadgesResolved(
            [makeBadge('PRIMER_PASO'), makeBadge('VALENTÍA')],
            [{ id: 1, badge: makeBadge('PRIMER_PASO'), obtainedAt: '2026-01-02T00:00:00Z' }]
        )
        setupHook()
        return waitForLoadingFinished().then(() => {
            verifyBadgesLength(2)
            verifyUnlockedAtIndex(0, true)
            verifyUnlockedAtIndex(1, false)
        })
    })

    it('retorna lista vacia si no hay insignias', () => {
        setupGetBadgesResolved([], [])
        setupHook()
        return waitForLoadingFinished().then(() => {
            verifyBadgesLength(0)
        })
    })

    it('setea error si falla la carga', () => {
        setupGetBadgesRejected('Error de red')
        setupHook()
        return waitForError('Error al cargar los badges')
    })
    let rendered: ReturnType<typeof renderHook<ReturnType<typeof useBadges>, undefined>>

    const setupHook = () => {
        rendered = renderHook(() => useBadges())
    }

    const setupGetBadgesResolved = (allBadges: any[], myBadges: any[]) => {
        mockedGetAll.mockResolvedValueOnce(allBadges as never)
        mockedGetMyBadges.mockResolvedValueOnce(myBadges as never)
    }

    const setupGetBadgesRejected = (msg: string) => {
        mockedGetAll.mockRejectedValueOnce(new Error(msg) as never)
    }

    const waitForLoadingFinished = () => {
        return waitFor(() => {
            expect(rendered.result.current.loading).toBe(false)
        })
    }

    const waitForError = (expectedError: string) => {
        return waitFor(() => {
            expect(rendered.result.current.error).toBe(expectedError)
        })
    }

    const verifyBadgesLength = (length: number) => {
        expect(rendered.result.current.badges).toHaveLength(length)
    }

    const verifyUnlockedAtIndex = (index: number, unlocked: boolean) => {
        expect(rendered.result.current.badges[index].unlocked).toBe(unlocked)
    }
})

function makeBadge(code: string) {
  return ({
    id: 1,
    code,
    name: 'Insignia',
    description: null,
    imageUrl: null,
    createdAt: '2026-01-01T00:00:00Z'
})
}
