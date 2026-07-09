import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import type { ReactNode } from 'react'
import { AuthProvider, useAuth } from '../../context/auth'

const mockGetMe = vi.fn()
const mockLogin = vi.fn()
const mockLogout = vi.fn()
vi.mock('../../api/auth', () => ({
  getMe: () => mockGetMe(),
  login: (creds: unknown) => mockLogin(creds),
  logout: () => mockLogout(),
}))

const mockGetToken = vi.fn()
const mockSetToken = vi.fn()
const mockClearToken = vi.fn()
const mockTryRehydrateSession = vi.fn()
vi.mock('../../api/client', () => ({
  getToken: () => mockGetToken(),
  setToken: (t: string) => mockSetToken(t),
  clearToken: () => mockClearToken(),
  tryRehydrateSession: () => mockTryRehydrateSession(),
}))

const SESSION_FLAG = 'huly:has-session'



const sampleUser = {
  id: 1,
  name: 'Mili',
  email: 'mili@huly.com',
  role: 'USER',
}

describe('AuthContext', () => {
  let hookResult: { current: ReturnType<typeof useAuth> }

  beforeEach(() => {
    clearAllMocks()
    localStorage.clear()
  })

  it('arranca sin usuario y termina loading si no hay cookie de refresh', () => {
    setupRehydrateSession(null)
    renderAuthHook()
    return waitForLoadingFinished().then(() => {
      verifyUserIsNull()
      verifyIsNotAuthenticated()
      verifyGetMeNotCalled()
    })
  })

  it('rehidrata el usuario si la cookie de refresh es válida', () => {
    setupSessionFlag()
    setupRehydrateSession('rehydrated-token')
    setupGetMeResponse(sampleUser)
    renderAuthHook()
    return waitForLoadingFinished().then(() => {
      verifyUserIs(sampleUser)
      verifyIsAuthenticated()
    })
  })

  it('mantiene el token si tryRehydrateSession funciona pero /me falla transitoriamente', () => {
    setupSessionFlag()
    setupRehydrateSession('rehydrated-token')
    setupGetMeError(new Error('503'))
    renderAuthHook()
    return waitForLoadingFinished().then(() => {
      verifyUserIsNull()
      verifyClearTokenNotCalled()
    })
  })

  it('limpia la sesión si tryRehydrateSession lanza una excepción', () => {
    setupSessionFlag()
    setupRehydrateSessionError(new Error('network down'))
    renderAuthHook()
    return waitForLoadingFinished().then(() => {
      verifyUserIsNull()
      verifyClearTokenCalled()
    })
  })

  it('login guarda el token y puebla el usuario', () => {
    setupRehydrateSession(null)
    setupLoginResponse({ accessToken: 'new-token', role: 'USER' })
    setupGetMeResponse(sampleUser)
    renderAuthHook()
    return waitForLoadingFinished()
      .then(() => callLogin({ email: 'mili@huly.com', password: '123456' }))
      .then(() => {
        verifySetTokenCalledWith('new-token')
        verifyUserIs(sampleUser)
        verifyIsAuthenticated()
      })
  })

  it('refreshUser vuelve a pedir /me y actualiza el usuario', () => {
    setupSessionFlag()
    setupRehydrateSession('rehydrated-token')
    setupGetMeMultipleResponses([sampleUser, { ...sampleUser, name: 'Milagros' }])
    renderAuthHook()
    return waitForLoadingFinished()
      .then(() => callRefreshUser())
      .then(() => {
        verifyUserIs({ ...sampleUser, name: 'Milagros' })
      })
  })

  it('login tira error si no llega accessToken', () => {
    setupRehydrateSession(null)
    setupLoginResponse({ role: 'USER' })
    renderAuthHook()
    return waitForLoadingFinished()
      .then(() => verifyLoginRejects({ email: 'a@b.com', password: '123456' }))
  })

  it('loginWithToken guarda el token y puebla el usuario', () => {
    setupRehydrateSession(null)
    setupGetMeResponse(sampleUser)
    renderAuthHook()
    return waitForLoadingFinished()
      .then(() => callLoginWithToken('reg-token'))
      .then(() => {
        verifySetTokenCalledWith('reg-token')
        verifyUserIs(sampleUser)
      })
  })

  it('logout limpia el token y el usuario', () => {
    setupSessionFlag()
    setupRehydrateSession('rehydrated-token')
    setupGetMeResponse(sampleUser)
    setupLogoutResponse(undefined)
    renderAuthHook()
    return waitForAuthenticated()
      .then(() => callLogout())
      .then(() => {
        verifyClearTokenCalled()
        verifyUserIsNull()
        verifyIsNotAuthenticated()
      })
  })

  it('el evento auth:expired limpia el usuario', () => {
    setupSessionFlag()
    setupRehydrateSession('rehydrated-token')
    setupGetMeResponse(sampleUser)
    renderAuthHook()
    return waitForAuthenticated()
      .then(() => dispatchAuthExpiredEvent())
      .then(() => waitForUserToBeNull())
  })

  /* helpers */

  const setupRehydrateSession = (value: string | null) => {
    mockTryRehydrateSession.mockResolvedValue(value)
  }

  const setupRehydrateSessionError = (error: Error) => {
    mockTryRehydrateSession.mockRejectedValue(error)
  }

  const setupGetMeResponse = (user: typeof sampleUser) => {
    mockGetMe.mockResolvedValue(user)
  }

  const setupGetMeMultipleResponses = (users: any[]) => {
    users.forEach((user) => {
      mockGetMe.mockResolvedValueOnce(user)
    })
  }

  const setupGetMeError = (error: Error) => {
    mockGetMe.mockRejectedValue(error)
  }

  const setupLoginResponse = (response: any) => {
    mockLogin.mockResolvedValue(response)
  }

  const setupLogoutResponse = (response: any) => {
    mockLogout.mockResolvedValue(response)
  }

  const setupSessionFlag = () => {
    localStorage.setItem(SESSION_FLAG, '1')
  }

  const renderAuthHook = () => {
    const { result } = renderHook(() => useAuth(), { wrapper })
    hookResult = result
  }

  const waitForLoadingFinished = () => {
    return waitFor(() => expect(hookResult.current.loading).toBe(false))
  }

  const waitForAuthenticated = () => {
    return waitFor(() => expect(hookResult.current.isAuthenticated).toBe(true))
  }

  const waitForUserToBeNull = () => {
    return waitFor(() => expect(hookResult.current.user).toBeNull())
  }

  const verifyUserIsNull = () => {
    expect(hookResult.current.user).toBeNull()
  }

  const verifyUserIs = (user: typeof sampleUser) => {
    expect(hookResult.current.user).toEqual(user)
  }

  const verifyIsAuthenticated = () => {
    expect(hookResult.current.isAuthenticated).toBe(true)
  }

  const verifyIsNotAuthenticated = () => {
    expect(hookResult.current.isAuthenticated).toBe(false)
  }

  const verifyGetMeNotCalled = () => {
    expect(mockGetMe).not.toHaveBeenCalled()
  }

  const verifyClearTokenNotCalled = () => {
    expect(mockClearToken).not.toHaveBeenCalled()
  }

  const verifyClearTokenCalled = () => {
    expect(mockClearToken).toHaveBeenCalled()
  }

  const verifySetTokenCalledWith = (token: string) => {
    expect(mockSetToken).toHaveBeenCalledWith(token)
  }

  const callLogin = (creds: any) => {
    return act(async () => {
      await hookResult.current.login(creds)
    })
  }

  const verifyLoginRejects = (creds: any) => {
    return expect(
      act(async () => {
        await hookResult.current.login(creds)
      }),
    ).rejects.toThrow()
  }

  const callRefreshUser = () => {
    return act(async () => {
      await hookResult.current.refreshUser()
    })
  }

  const callLoginWithToken = (token: string) => {
    return act(async () => {
      await hookResult.current.loginWithToken(token)
    })
  }

  const callLogout = () => {
    return act(async () => {
      await hookResult.current.logout()
    })
  }

  const dispatchAuthExpiredEvent = () => {
    act(() => {
      window.dispatchEvent(new CustomEvent('auth:expired'))
    })
  }
})

function wrapper({ children }: { children: ReactNode }) {
  return (
  <AuthProvider>{children}</AuthProvider>
)
}
