import { ApiError } from './apiError'

const BASE_URL = `${import.meta.env.VITE_API_URL ?? ''}/api`
type RequestOptions = Omit<RequestInit, 'body'> & { body?: unknown }

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { body, headers, ...rest } = options

  const response = await fetch(`${BASE_URL}${path}`, {
    ...rest,
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  })

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
}