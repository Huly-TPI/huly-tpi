import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useCosmeticActions } from '../../../hooks/store/useCosmeticActions'
import { storeApi } from '../../../api/store'
import { ApiError } from '../../../api/apiError'

vi.mock('../../../api/store', () => ({
  storeApi: {
    getItems: vi.fn(),
    getInventory: vi.fn(),
    buy: vi.fn(),
    equip: vi.fn(),
    unequip: vi.fn(),
  },
}))

const mockedBuy = vi.mocked(storeApi.buy)
const mockedEquip = vi.mocked(storeApi.equip)
const mockedUnequip = vi.mocked(storeApi.unequip)

describe('useCosmeticActions', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('buy devuelve true y limpia busy al comprar un item y que no haya error', async () => {
    mockedBuy.mockResolvedValueOnce(undefined as never)
    const { result } = renderHook(() => useCosmeticActions())

    let ok: boolean | undefined
    await act(async () => { ok = await result.current.buy(10) })
    expect(ok).toBe(true)
    expect(result.current.busyId).toBeNull()
    expect(result.current.error).toBeNull()
    expect(mockedBuy).toHaveBeenCalledWith(10)
  })

  it('buy devuelve false y setea el mensaje del ApiError al fallar', async () => {
    mockedBuy.mockRejectedValueOnce(new ApiError('Saldo insuficiente'))
    const { result } = renderHook(() => useCosmeticActions())

    let ok: boolean | undefined
    await act(async () => { ok = await result.current.buy(10) })
    expect(ok).toBe(false)
    expect(result.current.error).toBe('Saldo insuficiente')
  })

  it('equip devuelve true al equipar un item y que no haya error', async () => {
    mockedEquip.mockResolvedValueOnce(undefined as never)
    const { result } = renderHook(() => useCosmeticActions())

    let ok: boolean | undefined
    await act(async () => { ok = await result.current.equip(20) })
    expect(ok).toBe(true)
    expect(mockedEquip).toHaveBeenCalledWith(20)
  })

  it('unequip devuelve true al quitar ok', async () => {
    mockedUnequip.mockResolvedValueOnce(undefined as never)
    const { result } = renderHook(() => useCosmeticActions())

    let ok: boolean | undefined
    await act(async () => {
      ok = await result.current.unequip(30)
    })
    expect(ok).toBe(true)
    expect(mockedUnequip).toHaveBeenCalledWith(30)
  })
})