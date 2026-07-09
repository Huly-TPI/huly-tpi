import { clearAllMocks } from '../testHelpers'
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

const mockedCompleteTutorial = vi.mocked(completeTutorial)

describe('useHomeOnboarding - prompt de notificaciones', () => {
    const refreshUser = vi.fn()

    beforeEach(() => {
        clearAllMocks()
        vi.mocked(useAuth).mockReturnValue({
            user: { onBoardingCompleted: true, onboardingTutorialCompleted: false },
            refreshUser,
        } as unknown as ReturnType<typeof useAuth>) 
    })

    it('no muestra el prompt antes de completar el tutorial', () => {
        setupHook(3)
        verifyShowNotificationsPrompt(false)
    })

    it('al completar el tutorial muestra el prompt de notificaciones', () => {
        setupCompleteTutorialResolved()
        setupHook(1)
        callStartOnboarding()
        verifyOnboardingMode('steps')
        return callAdvanceOnboarding().then(() => {
            verifyCompleteTutorialCalledTimes(1)
            verifyShowNotificationsPrompt(true)
        })
    })

    it('closeNotificationPrompt oculta el prompt', () => {
        setupCompleteTutorialResolved()
        setupHook(1)
        callStartOnboarding()
        return callAdvanceOnboarding().then(() => {
            verifyShowNotificationsPrompt(true)
            callCloseNotificationsPrompt()
            verifyShowNotificationsPrompt(false)
        })
    })
    let rendered: ReturnType<typeof renderHook<ReturnType<typeof useHomeOnboarding>, undefined>>

    const setupHook = (steps: number) => {
        rendered = renderHook(() => useHomeOnboarding(steps))
    }

    const setupCompleteTutorialResolved = () => {
        mockedCompleteTutorial.mockResolvedValue(undefined)
    }

    const callStartOnboarding = () => {
        act(() => {
            rendered.result.current.startOnboarding()
        })
    }

    const callAdvanceOnboarding = async () => {
        await act(async () => {
            await rendered.result.current.advanceOnboarding()
        })
    }

    const callCloseNotificationsPrompt = () => {
        act(() => {
            rendered.result.current.closeNotificationsPrompt()
        })
    }

    const verifyShowNotificationsPrompt = (expected: boolean) => {
        expect(rendered.result.current.showNotificationsPrompt).toBe(expected)
    }

    const verifyOnboardingMode = (expected: string) => {
        expect(rendered.result.current.onboardingMode).toBe(expected)
    }

    const verifyCompleteTutorialCalledTimes = (times: number) => {
        expect(mockedCompleteTutorial).toHaveBeenCalledTimes(times)
    }
})
