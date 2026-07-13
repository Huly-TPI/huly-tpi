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
    setupListSuccess([product])
  })

  it('carga los productos al montar', async () => {
    const { result } = renderAdminProducts('COIN_PACK')
    await verifyLoadingFinished(result)
    verifyProducts(result, [product])
    verifyListCalledWith('COIN_PACK')
  })

  it('create llama a la api y devuelve true', async () => {
    setupCreateSuccess()
    const { result } = renderAdminProducts()
    await verifyLoadingFinished(result)
    const ok = await callCreate(result, {} as never)
    verifyCreateCalledOnce()
    expect(ok).toBe(true)
  })

  it('create devuelve false y setea error si falla', async () => {
    setupCreateFailure(new Error('boom'))
    const { result } = renderAdminProducts()
    await verifyLoadingFinished(result)
    const ok = await callCreate(result, {} as never)
    expect(ok).toBe(false)
    verifyError(result, 'boom')
  })

  it('setActive llama a la api con el id y estado', async () => {
    setupSetActiveSuccess()
    const { result } = renderAdminProducts()
    await verifyLoadingFinished(result)
    await callSetActive(result, 1, false)
    verifySetActiveCalledWith(1, false)
  })

  const renderAdminProducts = (type?: string) => {
    return renderHook(() => useAdminProducts(type))
  }

  const setupListSuccess = (val: any) => {
    list.mockResolvedValue(val)
  }

  const setupCreateSuccess = () => {
    create.mockResolvedValue(undefined)
  }

  const setupCreateFailure = (err: any) => {
    create.mockRejectedValue(err)
  }

  const setupSetActiveSuccess = () => {
    setActive.mockResolvedValue(undefined)
  }

  const verifyLoadingFinished = async (result: any) => {
    await waitFor(() => expect(result.current.loading).toBe(false))
  }

  const verifyProducts = (result: any, expected: any[]) => {
    expect(result.current.products).toEqual(expected)
  }

  const verifyListCalledWith = (type?: string) => {
    expect(list).toHaveBeenCalledWith(type)
  }

  const verifyCreateCalledOnce = () => {
    expect(create).toHaveBeenCalledOnce()
  }

  const verifySetActiveCalledWith = (id: number, active: boolean) => {
    expect(setActive).toHaveBeenCalledWith(id, active)
  }

  const verifyError = (result: any, expectedError: string | null) => {
    expect(result.current.error).toBe(expectedError)
  }

  const callCreate = async (result: any, d: any) => {
    let ok: boolean | undefined
    await act(async () => { ok = await result.current.create(d) })
    return ok
  }

  const callSetActive = async (result: any, id: number, active: boolean) => {
    let ok: boolean | undefined
    await act(async () => { ok = await result.current.setActive(id, active) })
    return ok
  }
})