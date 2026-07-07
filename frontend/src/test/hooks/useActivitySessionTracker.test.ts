import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'
import { ActivityType } from '../../api/activities'
import * as activitiesApi from '../../api/activities'

vi.mock('../../api/activities', () => ({
  ActivityType: {
    BREATHING: 'BREATHING',
    DIARY: 'DIARY',
    LANTERN: 'LANTERN',
    BUBBLE: 'BUBBLE',
    CHALLENGE: 'CHALLENGE',
    ZEN_GARDEN: 'ZEN_GARDEN',
    MANDALA: 'MANDALA',
  },
  registerActivitySession: vi.fn(),
}))

const mockedRegister = vi.mocked(activitiesApi.registerActivitySession)

describe('useActivitySessionTracker', () => {
  beforeEach(() => {
    clearAllMocks()
    mockedRegister.mockResolvedValue(null)
    vi.spyOn(Date, 'now').mockReturnValue(100_000)
  })

  it('startSession establece el timestamp de inicio', () => {
    setupHook(ActivityType.BUBBLE)
    callStartSession()
    callMarkConditionMet()
    return callSaveSession().then(() => {
      verifyRegisterCalledWith({ activityType: ActivityType.BUBBLE, contextId: undefined }, undefined)
    })
  })

  it('startSession es idempotente – solo establece el timestamp una vez', () => {
    setupDateNowSpy(100_000)
    setupHook(ActivityType.BUBBLE, { minDurationSeconds: 5 })
    callStartSession()
    setupDateNowSpy(200_000)
    callStartSession()
    callMarkConditionMet()
    return callSaveSession().then(() => {
      verifyRegisterCalled()
    })
  })

  it('markConditionMet establece la condición', () => {
    setupHook(ActivityType.BUBBLE)
    callStartSession()
    return callSaveSession().then(() => {
      verifyRegisterNotCalled()
      callMarkConditionMet()
      return callSaveSession().then(() => {
        verifyRegisterCalledTimes(1)
      })
    })
  })

  it('saveSession llama a registerActivitySession cuando las condiciones se cumplen', () => {
    setupHook(ActivityType.BREATHING)
    callStartSession()
    callMarkConditionMet()
    return callSaveSession().then(() => {
      verifyRegisterCalledOnce()
      verifyRegisterCalledWith({ activityType: ActivityType.BREATHING, contextId: undefined }, undefined)
    })
  })

  it('saveSession no llama si no se inició la sesión', () => {
    setupHook(ActivityType.BUBBLE)
    callMarkConditionMet()
    return callSaveSession().then(() => {
      verifyRegisterNotCalled()
    })
  })

  it('saveSession no llama si ya fue guardada', () => {
    setupHook(ActivityType.BUBBLE)
    callStartSession()
    callMarkConditionMet()
    return callSaveSession()
      .then(() => {
        verifyRegisterCalledOnce()
        return callSaveSession()
      })
      .then(() => {
        verifyRegisterCalledOnce()
      })
  })

  it('saveSession no llama si la condición no se cumplió', () => {
    setupHook(ActivityType.BUBBLE)
    callStartSession()
    return callSaveSession().then(() => {
      verifyRegisterNotCalled()
    })
  })

  it('saveSession no llama si minDurationSeconds no se alcanzó', () => {
    setupDateNowSpy(100_000)
    setupHook(ActivityType.BUBBLE, { minDurationSeconds: 60 })
    callStartSession()
    callMarkConditionMet()
    setupDateNowSpy(110_000)
    return callSaveSession().then(() => {
      verifyRegisterNotCalled()
    })
  })

  it('saveSession con force: true omite las verificaciones de condición y duración', () => {
    setupDateNowSpy(100_000)
    setupHook(ActivityType.BUBBLE, { minDurationSeconds: 60 })
    callStartSession()
    return callSaveSession({ force: true }).then(() => {
      verifyRegisterCalledOnce()
    })
  })

  it('autoStart activa startSession al montar', () => {
    setupHook(ActivityType.BUBBLE, { autoStart: true })
    callMarkConditionMet()
    return callSaveSession().then(() => {
      verifyRegisterCalledOnce()
    })
  })

  it('stopSession reinicia todos los estados', () => {
    setupHook(ActivityType.BUBBLE)
    callStartSession()
    callMarkConditionMet()
    callStopSession()
    return callSaveSession().then(() => {
      verifyRegisterNotCalled()
    })
  })
  let rendered: ReturnType<typeof renderHook<any, any>>

  /* helpers */

  const setupHook = (activityType: ActivityType, options?: Parameters<typeof useActivitySessionTracker>[1]) => {
    rendered = renderHook(() => useActivitySessionTracker(activityType, options))
  }

  const setupDateNowSpy = (time: number) => {
    vi.spyOn(Date, 'now').mockReturnValue(time)
  }

  const callStartSession = () => {
    act(() => {
      rendered.result.current.startSession()
    })
  }

  const callMarkConditionMet = () => {
    act(() => {
      rendered.result.current.markConditionMet()
    })
  }

  const callStopSession = () => {
    act(() => {
      rendered.result.current.stopSession()
    })
  }

  const callSaveSession = async (options?: { force?: boolean }) => {
    await act(async () => {
      await rendered.result.current.saveSession(options)
    })
  }

  const verifyRegisterCalled = () => {
    expect(mockedRegister).toHaveBeenCalled()
  }

  const verifyRegisterNotCalled = () => {
    expect(mockedRegister).not.toHaveBeenCalled()
  }

  const verifyRegisterCalledOnce = () => {
    expect(mockedRegister).toHaveBeenCalledOnce()
  }

  const verifyRegisterCalledTimes = (times: number) => {
    expect(mockedRegister).toHaveBeenCalledTimes(times)
  }

  const verifyRegisterCalledWith = (firstArg: any, secondArg: any) => {
    expect(mockedRegister).toHaveBeenCalledWith(firstArg, secondArg)
  }
})
