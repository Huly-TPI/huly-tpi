import { ApiError } from './apiError'

const BASE_URL = `${import.meta.env.VITE_API_URL ?? ''}/api`
const TOKEN_KEY = 'huly:access-token'

type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown
  skipAuthRedirect?: boolean
}

export const getToken = (): string | null =>
  window.localStorage.getItem(TOKEN_KEY)

export const setToken = (token: string): void =>
  window.localStorage.setItem(TOKEN_KEY, token)

export const clearToken = (): void =>
  window.localStorage.removeItem(TOKEN_KEY)

interface RefreshResponse {
  accessToken?: string
}

async function refreshAccessToken(): Promise<string | null> {
  try {
    const response = await fetch(`${BASE_URL}/auth/refresh`, {
      method: 'POST',
      credentials: 'include',
    })

    if (!response.ok) return null

    const data = (await response.json()) as RefreshResponse
    if (!data?.accessToken) return null

    setToken(data.accessToken)
    return data.accessToken
  } catch {
    return null
  }
}

async function request<T>(
  path: string,
  options: RequestOptions = {},
  retry = true,
): Promise<T> {
  const { body, headers, skipAuthRedirect, ...rest } = options
  const token = getToken()

  const response = await fetch(`${BASE_URL}${path}`, {
    ...rest,
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

  if (
    response.status === 401 &&
    retry &&
    !skipAuthRedirect &&
    path !== '/auth/refresh'
  ) {
    const newToken = await refreshAccessToken()
    if (newToken) {
      return request<T>(path, options, false)
    }
    clearToken()
    window.dispatchEvent(new CustomEvent('auth:expired'))
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null)
    const message =
      errorBody?.message ||
      errorBody?.error ||
      `Error HTTP ${response.status}`
    throw new ApiError(message, errorBody?.errors ?? {})
  }

  const text = await response.text()
  return (text ? JSON.parse(text) : null) as T
}

export const api = {
  get: <T>(path: string, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'GET' }),
  post: <T>(path: string, body: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'POST', body }),
  put: <T>(path: string, body: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'PUT', body }),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'PATCH', body }),
  delete: <T>(path: string, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'DELETE' }),
}