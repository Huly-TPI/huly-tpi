import { clearAllMocks } from '../../testHelpers'
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
    clearAllMocks()
  })

  it('buy devuelve true y limpia busy al comprar un item y que no haya error', () => {
    setupBuyResolved()
    setupHook()
    return callBuy(10).then((ok) => {
      verifyActionResponse(ok, true)
      verifyBusyIdIsNull()
      verifyErrorIsNull()
      verifyBuyCalledWith(10)
    })
  })

  it('buy devuelve false y setea el mensaje del ApiError al fallar', () => {
    setupBuyRejected('Saldo insuficiente')
    setupHook()
    return callBuy(10).then((ok) => {
      verifyActionResponse(ok, false)
      verifyErrorMessage('Saldo insuficiente')
    })
  })

  it('equip devuelve true al equipar un item y que no haya error', () => {
    setupEquipResolved()
    setupHook()
    return callEquip(20).then((ok) => {
      verifyActionResponse(ok, true)
      verifyEquipCalledWith(20)
    })
  })

  it('unequip devuelve true al quitar ok', () => {
    setupUnequipResolved()
    setupHook()
    return callUnequip(30).then((ok) => {
      verifyActionResponse(ok, true)
      verifyUnequipCalledWith(30)
    })
  })
  let rendered: ReturnType<typeof renderHook<ReturnType<typeof useCosmeticActions>, undefined>>

  /* helpers */

  const setupHook = () => {
    rendered = renderHook(() => useCosmeticActions())
  }

  const setupBuyResolved = () => {
    mockedBuy.mockResolvedValueOnce(undefined as never)
  }

  const setupBuyRejected = (msg: string) => {
    mockedBuy.mockRejectedValueOnce(new ApiError(msg))
  }

  const setupEquipResolved = () => {
    mockedEquip.mockResolvedValueOnce(undefined as never)
  }

  const setupUnequipResolved = () => {
    mockedUnequip.mockResolvedValueOnce(undefined as never)
  }

  const callBuy = async (id: number) => {
    let ok: boolean | undefined
    await act(async () => {
      ok = await rendered.result.current.buy(id)
    })
    return ok!
  }

  const callEquip = async (id: number) => {
    let ok: boolean | undefined
    await act(async () => {
      ok = await rendered.result.current.equip(id)
    })
    return ok!
  }

  const callUnequip = async (id: number) => {
    let ok: boolean | undefined
    await act(async () => {
      ok = await rendered.result.current.unequip(id)
    })
    return ok!
  }

  const verifyActionResponse = (ok: boolean, expected: boolean) => {
    expect(ok).toBe(expected)
  }

  const verifyBusyIdIsNull = () => {
    expect(rendered.result.current.busyId).toBeNull()
  }

  const verifyErrorIsNull = () => {
    expect(rendered.result.current.error).toBeNull()
  }

  const verifyErrorMessage = (msg: string) => {
    expect(rendered.result.current.error).toBe(msg)
  }

  const verifyBuyCalledWith = (id: number) => {
    expect(mockedBuy).toHaveBeenCalledWith(id)
  }

  const verifyEquipCalledWith = (id: number) => {
    expect(mockedEquip).toHaveBeenCalledWith(id)
  }

  const verifyUnequipCalledWith = (id: number) => {
    expect(mockedUnequip).toHaveBeenCalledWith(id)
  }
})