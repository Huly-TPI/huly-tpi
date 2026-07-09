import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { useAdminProducts } from '../../hooks/backoffice/useAdminProducts'

const list = vi.fn()
const create = vi.fn()
const update = vi.fn()
const setActive = vi.fn()

vi.mock('../../api/adminProducts', () => ({
  adminProductsApi: {
    list: (...a: unknown[]) => list(...a),
    create: (...a: unknown[]) => create(...a),
    update: (...a: unknown[]) => update(...a),
    setActive: (...a: unknown[]) => setActive(...a),
  },
}))

const product = { id: 1, name: 'Pack', description: 'd', price: 499, coinsAmount: 100, type: 'COIN_PACK', planCode: null, chatDailyLimit: null, audioDailyLimit: null, active: true }

describe('useAdminProducts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    list.mockResolvedValue([product])
  })

  it('carga los productos al montar', async () => {
    const { result } = renderHook(() => useAdminProducts('COIN_PACK'))
    await waitFor(() => expect(result.current.loading).toBe(false))
    expect(result.current.products).toHaveLength(1)
    expect(list).toHaveBeenCalledWith('COIN_PACK')
  })

  it('create llama a la api y devuelve true', async () => {
    create.mockResolvedValue(undefined)
    const { result } = renderHook(() => useAdminProducts())
    await waitFor(() => expect(result.current.loading).toBe(false))
    let ok: boolean | undefined
    await act(async () => { ok = await result.current.create({} as never) })
    expect(create).toHaveBeenCalledOnce()
    expect(ok).toBe(true)
  })

  it('create devuelve false y setea error si falla', async () => {
    create.mockRejectedValue(new Error('boom'))
    const { result } = renderHook(() => useAdminProducts())
    await waitFor(() => expect(result.current.loading).toBe(false))
    let ok: boolean | undefined
    await act(async () => { ok = await result.current.create({} as never) })
    expect(ok).toBe(false)
    expect(result.current.error).toBe('boom')
  })

  it('setActive llama a la api con el id y estado', async () => {
    setActive.mockResolvedValue(undefined)
    const { result } = renderHook(() => useAdminProducts())
    await waitFor(() => expect(result.current.loading).toBe(false))
    await act(async () => { await result.current.setActive(1, false) })
    expect(setActive).toHaveBeenCalledWith(1, false)
  })
})