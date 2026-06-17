import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'
import { ActivityType } from '../../api/activities'
import * as activitiesApi from '../../api/activities'

vi.mock('../../api/activities', () => ({
  ActivityType: {
    RESPIRACION: 'RESPIRACION',
    DIARIO: 'DIARIO',
    NUBE: 'NUBE',
    BURBUJA: 'BURBUJA',
    RETO: 'RETO',
  },
  registerActivitySession: vi.fn(),
}))

const mockedRegister = vi.mocked(activitiesApi.registerActivitySession)

describe('useActivitySessionTracker', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedRegister.mockResolvedValue(null)
    vi.spyOn(Date, 'now').mockReturnValue(100_000)
  })

  it('startSession establece el timestamp de inicio', () => {
    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA),
    )

    act(() => {
      result.current.startSession()
    })

    // After starting, saveSession should not skip due to "not started"
    // We verify indirectly by calling markConditionMet + saveSession
    act(() => {
      result.current.markConditionMet()
    })

    act(() => {
      void result.current.saveSession()
    })

    expect(mockedRegister).toHaveBeenCalledWith(
      { activityType: ActivityType.BURBUJA },
      undefined,
    )
  })

  it('startSession es idempotente – solo establece el timestamp una vez', () => {
    const dateNowSpy = vi.spyOn(Date, 'now')
    dateNowSpy.mockReturnValue(100_000)

    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA, { minDurationSeconds: 5 }),
    )

    act(() => {
      result.current.startSession()
    })

    // Advance Date.now and call start again — should NOT overwrite timestamp
    dateNowSpy.mockReturnValue(200_000)

    act(() => {
      result.current.startSession()
    })

    act(() => {
      result.current.markConditionMet()
    })

    // With minDurationSeconds: 5, if startedAt were 200_000 the elapsed
    // time would be 0 and save would be skipped. Since it's idempotent
    // the original 100_000 is used, so elapsed = 100s > 5s => passes.
    act(() => {
      void result.current.saveSession()
    })

    expect(mockedRegister).toHaveBeenCalled()
  })

  it('markConditionMet establece la condición', () => {
    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA),
    )

    act(() => {
      result.current.startSession()
    })

    // Without markConditionMet, saveSession should skip
    act(() => {
      void result.current.saveSession()
    })

    expect(mockedRegister).not.toHaveBeenCalled()

    // Now mark it and try again
    act(() => {
      result.current.markConditionMet()
    })

    act(() => {
      void result.current.saveSession()
    })

    expect(mockedRegister).toHaveBeenCalledTimes(1)
  })

  it('saveSession llama a registerActivitySession cuando las condiciones se cumplen', async () => {
    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.RESPIRACION),
    )

    act(() => {
      result.current.startSession()
      result.current.markConditionMet()
    })

    await act(async () => {
      await result.current.saveSession()
    })

    expect(mockedRegister).toHaveBeenCalledOnce()
    expect(mockedRegister).toHaveBeenCalledWith(
      { activityType: ActivityType.RESPIRACION },
      undefined,
    )
  })

  it('saveSession no llama si no se inició la sesión', async () => {
    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA),
    )

    act(() => {
      result.current.markConditionMet()
    })

    await act(async () => {
      await result.current.saveSession()
    })

    expect(mockedRegister).not.toHaveBeenCalled()
  })

  it('saveSession no llama si ya fue guardada', async () => {
    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA),
    )

    act(() => {
      result.current.startSession()
      result.current.markConditionMet()
    })

    await act(async () => {
      await result.current.saveSession()
    })

    expect(mockedRegister).toHaveBeenCalledOnce()

    await act(async () => {
      await result.current.saveSession()
    })

    // Should still be 1 — second call skipped
    expect(mockedRegister).toHaveBeenCalledOnce()
  })

  it('saveSession no llama si la condición no se cumplió', async () => {
    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA),
    )

    act(() => {
      result.current.startSession()
    })

    await act(async () => {
      await result.current.saveSession()
    })

    expect(mockedRegister).not.toHaveBeenCalled()
  })

  it('saveSession no llama si minDurationSeconds no se alcanzó', async () => {
    vi.spyOn(Date, 'now').mockReturnValue(100_000)

    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA, {
        minDurationSeconds: 60,
      }),
    )

    act(() => {
      result.current.startSession()
      result.current.markConditionMet()
    })

    // Only 10 seconds have passed (110_000 - 100_000 = 10_000ms = 10s < 60s)
    vi.spyOn(Date, 'now').mockReturnValue(110_000)

    await act(async () => {
      await result.current.saveSession()
    })

    expect(mockedRegister).not.toHaveBeenCalled()
  })

  it('saveSession con force: true omite las verificaciones de condición y duración', async () => {
    vi.spyOn(Date, 'now').mockReturnValue(100_000)

    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA, {
        minDurationSeconds: 60,
      }),
    )

    act(() => {
      result.current.startSession()
    })

    // Condition not met, duration not reached, but force: true
    await act(async () => {
      await result.current.saveSession({ force: true })
    })

    expect(mockedRegister).toHaveBeenCalledOnce()
  })

  it('autoStart activa startSession al montar', async () => {
    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA, { autoStart: true }),
    )

    // Since autoStart triggers startSession on mount, we should be
    // able to save after marking condition met
    act(() => {
      result.current.markConditionMet()
    })

    await act(async () => {
      await result.current.saveSession()
    })

    expect(mockedRegister).toHaveBeenCalledOnce()
  })

  it('stopSession reinicia todos los estados', async () => {
    const { result } = renderHook(() =>
      useActivitySessionTracker(ActivityType.BURBUJA),
    )

    act(() => {
      result.current.startSession()
      result.current.markConditionMet()
    })

    act(() => {
      result.current.stopSession()
    })

    // After stop, saveSession should skip (not started)
    await act(async () => {
      await result.current.saveSession()
    })

    expect(mockedRegister).not.toHaveBeenCalled()
  })
})
