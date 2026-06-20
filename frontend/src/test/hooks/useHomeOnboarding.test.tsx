import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useHomeOnboarding } from '../../hooks/useHomeOnboarding'
import { useAuth } from '../../context/auth'
import { completeTutorial } from '../../api/onboarding'

vi.mock('../../context/auth', () => ({
    useAuth: vi.fn(),
}))


vi.mock('../../api/onboarding', () => ({
    completeTutorial: vi.fn(),
}))

describe('useHomeOnboarding - prompt de notificaciones', () => {
    const refreshUser = vi.fn()

    beforeEach(() => {
        vi.clearAllMocks()
        vi.mocked(useAuth).mockReturnValue({
            user: { onBoardingCompleted: true, onboardingTutorialCompleted: false },
            refreshUser,
        } as unknown as ReturnType<typeof useAuth>) 
    })

    it('no muestra el prompt antes de completar el tutorial', () => {
        const { result } = renderHook(() => useHomeOnboarding(3))

        expect(result.current.showNotificationsPrompt).toBe(false)    
    })

    it('al completar el tutorial muestra el prompt de notificaciones', async () => {
        vi.mocked(completeTutorial).mockResolvedValue(undefined) 
        const { result } = renderHook(() => useHomeOnboarding(1))
        act(() => { result.current.startOnboarding() })
        expect(result.current.onboardingMode).toBe('steps')
        await act(async () => { await result.current.advanceOnboarding() })

        expect(completeTutorial).toHaveBeenCalledTimes(1)
        expect(result.current.showNotificationsPrompt).toBe(true)
    })

    it('closeNotificationPrompt oculta el prompt' , async () => {
        vi.mocked(completeTutorial).mockResolvedValue(undefined)
        const { result } = renderHook(() => useHomeOnboarding(1))
        act(() => { result.current.startOnboarding() })
        await act(async () => { await result.current.advanceOnboarding() })
        expect(result.current.showNotificationsPrompt).toBe(true)
        act(() => { result.current.closeNotificationsPrompt() })
        expect(result.current.showNotificationsPrompt).toBe(false)
    })
})
