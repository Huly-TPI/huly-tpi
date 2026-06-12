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

const wrapper = ({ children }: { children: ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
)

const sampleUser = {
  id: 1,
  name: 'Mili',
  email: 'mili@huly.com',
  role: 'USER',
}

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('arranca sin usuario y termina loading si no hay cookie de refresh', async () => {
    mockTryRehydrateSession.mockResolvedValue(null)
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.user).toBeNull()
    expect(result.current.isAuthenticated).toBe(false)
    expect(mockGetMe).not.toHaveBeenCalled()
  })

  it('rehidrata el usuario si la cookie de refresh es válida', async () => {
    localStorage.setItem(SESSION_FLAG, '1')
    mockTryRehydrateSession.mockResolvedValue('rehydrated-token')
    mockGetMe.mockResolvedValue(sampleUser)
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.user).toEqual(sampleUser)
    expect(result.current.isAuthenticated).toBe(true)
  })

  it('mantiene el token si tryRehydrateSession funciona pero /me falla transitoriamente', async () => {
    localStorage.setItem(SESSION_FLAG, '1')
    mockTryRehydrateSession.mockResolvedValue('rehydrated-token')
    mockGetMe.mockRejectedValue(new Error('503'))
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.user).toBeNull()
    expect(mockClearToken).not.toHaveBeenCalled()
  })

  it('limpia la sesión si tryRehydrateSession lanza una excepción', async () => {
    localStorage.setItem(SESSION_FLAG, '1')
    mockTryRehydrateSession.mockRejectedValue(new Error('network down'))
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.user).toBeNull()
    expect(mockClearToken).toHaveBeenCalled()
  })

  it('login guarda el token y puebla el usuario', async () => {
    mockTryRehydrateSession.mockResolvedValue(null)
    mockLogin.mockResolvedValue({ accessToken: 'new-token', role: 'USER' })
    mockGetMe.mockResolvedValue(sampleUser)
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.login({ email: 'mili@huly.com', password: '123456' })
    })

    expect(mockSetToken).toHaveBeenCalledWith('new-token')
    expect(result.current.user).toEqual(sampleUser)
    expect(result.current.isAuthenticated).toBe(true)
  })

  it('refreshUser vuelve a pedir /me y actualiza el usuario', async () => {
    localStorage.setItem(SESSION_FLAG, '1')
    mockTryRehydrateSession.mockResolvedValue('rehydrated-token')
    mockGetMe
      .mockResolvedValueOnce(sampleUser)
      .mockResolvedValueOnce({ ...sampleUser, name: 'Milagros' })
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.refreshUser()
    })

    expect(result.current.user).toEqual({ ...sampleUser, name: 'Milagros' })
  })

  it('login tira error si no llega accessToken', async () => {
    mockTryRehydrateSession.mockResolvedValue(null)
    mockLogin.mockResolvedValue({ role: 'USER' })
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))

    await expect(
      act(async () => {
        await result.current.login({ email: 'a@b.com', password: '123456' })
      }),
    ).rejects.toThrow()
  })

  it('loginWithToken guarda el token y puebla el usuario', async () => {
    mockTryRehydrateSession.mockResolvedValue(null)
    mockGetMe.mockResolvedValue(sampleUser)
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))

    await act(async () => {
      await result.current.loginWithToken('reg-token')
    })

    expect(mockSetToken).toHaveBeenCalledWith('reg-token')
    expect(result.current.user).toEqual(sampleUser)
  })

  it('logout limpia el token y el usuario', async () => {
    localStorage.setItem(SESSION_FLAG, '1')
    mockTryRehydrateSession.mockResolvedValue('rehydrated-token')
    mockGetMe.mockResolvedValue(sampleUser)
    mockLogout.mockResolvedValue(undefined)
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.isAuthenticated).toBe(true))

    await act(async () => {
      await result.current.logout()
    })

    expect(mockClearToken).toHaveBeenCalled()
    expect(result.current.user).toBeNull()
    expect(result.current.isAuthenticated).toBe(false)
  })

  it('el evento auth:expired limpia el usuario', async () => {
    localStorage.setItem(SESSION_FLAG, '1')
    mockTryRehydrateSession.mockResolvedValue('rehydrated-token')
    mockGetMe.mockResolvedValue(sampleUser)
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.isAuthenticated).toBe(true))

    act(() => {
      window.dispatchEvent(new CustomEvent('auth:expired'))
    })

    await waitFor(() => expect(result.current.user).toBeNull())
  })
})
