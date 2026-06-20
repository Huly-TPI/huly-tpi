import { ApiError } from './apiError'

const BASE_URL = `${import.meta.env.VITE_API_URL ?? ''}/api`

export const getBackendOrigin = (): string => {
  const rawApiUrl = import.meta.env.VITE_API_URL?.trim()

  if (!rawApiUrl) 
    return window.location.origin
  
  return new URL(rawApiUrl, window.location.origin).origin
}

export type RequestOptions = Omit<RequestInit, 'body'> & {
  body?: unknown
  skipAuthRedirect?: boolean
}

export class SessionExpiredError extends Error {
  constructor() {
    super('Session expired')
    this.name = 'SessionExpiredError'
  }
}

let accessToken: string | null = null
let inFlightRefresh: Promise<string | null> | null = null

export const getToken = (): string | null => accessToken

export const setToken = (token: string): void => {
  accessToken = token
  sessionStorage.setItem('huly:token', token)
  window.dispatchEvent(new CustomEvent('huly:session', { detail: { token } }))
}

export const clearToken = (): void => {
  accessToken = null
  sessionStorage.removeItem('huly:token')
  window.dispatchEvent(new CustomEvent('huly:session', { detail: { token: null } }))
}

interface RefreshResponse {
  accessToken?: string
}

async function performRefresh(): Promise<string | null> {
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

function refreshAccessToken(): Promise<string | null> {
  if (inFlightRefresh) {
    return inFlightRefresh
  }
  inFlightRefresh = performRefresh().finally(() => {
    inFlightRefresh = null
  })
  return inFlightRefresh
}

export const tryRehydrateSession = (): Promise<string | null> =>
  refreshAccessToken()

async function request<T>(
  path: string,
  options: RequestOptions = {},
  retry = true,
): Promise<T> {
  const { body, headers, skipAuthRedirect, ...rest } = options
  const token = getToken()
  const isFormData = body instanceof FormData

  const response = await fetch(`${BASE_URL}${path}`, {
    ...rest,
    cache: 'no-store',
    credentials: 'include',
    headers: {
      ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    body: body !== undefined ? (isFormData ? body : JSON.stringify(body)) : undefined,
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
    throw new SessionExpiredError()
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

async function requestMultipart<T>(path: string, formData: FormData, retry = true, signal?: AbortSignal): Promise<T> {
  const token = getToken()

  const response = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: formData,
    signal,
  })

  if (response.status === 401 && retry && path !== '/auth/refresh') {
    const newToken = await refreshAccessToken()
    if (newToken) {
      return requestMultipart<T>(path, formData, false, signal)
    }
    clearToken()
    window.dispatchEvent(new CustomEvent('auth:expired'))
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null)
    const message = errorBody?.message || errorBody?.error || `Error HTTP ${response.status}`
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
  postMultipart: <T>(path: string, formData: FormData, signal?: AbortSignal) =>
    requestMultipart<T>(path, formData, true, signal),
  put: <T>(path: string, body: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'PUT', body }),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'PATCH', body }),
  delete: <T>(path: string, options?: RequestOptions) =>
    request<T>(path, { ...options, method: 'DELETE' }),
}
