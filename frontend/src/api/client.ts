import { ApiError } from './apiError'

const BASE_URL = `${import.meta.env.VITE_API_URL ?? ''}/api`
type RequestOptions = Omit<RequestInit, 'body'> & { body?: unknown; skipAuthRedirect?: boolean }

async function refreshAccessToken(): Promise<string | null> {
  const response = await fetch(`${BASE_URL}/auth/refresh`, {
    method: 'POST',
    credentials: 'include',
  })

  if (!response.ok) return null

  const data = await response.json()
  localStorage.setItem('token', data.accessToken)
  return data.accessToken
}

async function request<T>(path: string, options: RequestOptions = {}, retry = true): Promise<T> {
  const { body, headers, skipAuthRedirect, ...rest } = options
  const token = localStorage.getItem('token')

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

  if (response.status === 401 && retry && !skipAuthRedirect) {
    const newToken = await refreshAccessToken()

    if (newToken) {
      return request<T>(path, options, false)
    }

    localStorage.removeItem('token')
    localStorage.removeItem('role')
    window.location.href = '/login'
    throw new ApiError('Sesión expirada', {})
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