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
vi.mock('../../api/client', () => ({
  getToken: () => mockGetToken(),
  setToken: (t: string) => mockSetToken(t),
  clearToken: () => mockClearToken(),
}))

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
  })

  it('arranca sin usuario y termina loading si no hay token', async () => {
    mockGetToken.mockReturnValue(null)
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.user).toBeNull()
    expect(result.current.isAuthenticated).toBe(false)
    expect(mockGetMe).not.toHaveBeenCalled()
  })

  it('rehidrata el usuario si hay token al montar', async () => {
    mockGetToken.mockReturnValue('valid-token')
    mockGetMe.mockResolvedValue(sampleUser)
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.user).toEqual(sampleUser)
    expect(result.current.isAuthenticated).toBe(true)
  })

  it('limpia el token si el /me falla en la rehidratación', async () => {
    mockGetToken.mockReturnValue('expired-token')
    mockGetMe.mockRejectedValue(new Error('401'))
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.user).toBeNull()
    expect(mockClearToken).toHaveBeenCalled()
  })

  it('login guarda el token y puebla el usuario', async () => {
    mockGetToken.mockReturnValue(null)
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
    mockGetToken.mockReturnValue('valid-token')
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
    mockGetToken.mockReturnValue(null)
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
    mockGetToken.mockReturnValue(null)
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
    mockGetToken.mockReturnValue('valid-token')
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
    mockGetToken.mockReturnValue('valid-token')
    mockGetMe.mockResolvedValue(sampleUser)
    const { result } = renderHook(() => useAuth(), { wrapper })

    await waitFor(() => expect(result.current.isAuthenticated).toBe(true))

    act(() => {
      window.dispatchEvent(new CustomEvent('auth:expired'))
    })

    await waitFor(() => expect(result.current.user).toBeNull())
  })
})