import { describe, it, expect, vi, beforeEach } from 'vitest'
import { adminProductsApi } from '../../api/adminProducts'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
  api: { get: vi.fn(), post: vi.fn(), put: vi.fn(), patch: vi.fn() },
}))

const mockedGet = vi.mocked(api.get)
const mockedPost = vi.mocked(api.post)
const mockedPut = vi.mocked(api.put)
const mockedPatch = vi.mocked(api.patch)

const data = { name: 'Pack', description: 'd', price: 499, coinsAmount: 100, type: 'COIN_PACK' }

describe('adminProductsApi', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('list llama GET con el type', () => {
    setupMockedGetResponse([] as never)
    return callList('COIN_PACK').then(() => {
      verifyGetCalledWith('/admin/products?type=COIN_PACK')
    })
  })

  it('create llama POST', () => {
    setupMockedPostResponse({} as never)
    return callCreate(data).then(() => {
      verifyPostCalledWith('/admin/products', data)
    })
  })

  it('update llama PUT con el id', () => {
    setupMockedPutResponse({} as never)
    return callUpdate(5, data).then(() => {
      verifyPutCalledWith('/admin/products/5', data)
    })
  })

  it('setActive llama PATCH con el active', () => {
    setupMockedPatchResponse({} as never)
    return callSetActive(9, false).then(() => {
      verifyPatchCalledWith('/admin/products/9/active', { active: false })
    })
  })

  const setupMockedGetResponse = (res: any) => {
    mockedGet.mockResolvedValueOnce(res)
  }

  const setupMockedPostResponse = (res: any) => {
    mockedPost.mockResolvedValueOnce(res)
  }

  const setupMockedPutResponse = (res: any) => {
    mockedPut.mockResolvedValueOnce(res)
  }

  const setupMockedPatchResponse = (res: any) => {
    mockedPatch.mockResolvedValueOnce(res)
  }

  const callList = (type?: string) => {
    return Promise.resolve(adminProductsApi.list(type))
  }

  const callCreate = (d: typeof data) => {
    return Promise.resolve(adminProductsApi.create(d))
  }

  const callUpdate = (id: number, d: typeof data) => {
    return Promise.resolve(adminProductsApi.update(id, d))
  }

  const callSetActive = (id: number, active: boolean) => {
    return Promise.resolve(adminProductsApi.setActive(id, active))
  }

  const verifyGetCalledWith = (path: string) => {
    expect(mockedGet).toHaveBeenCalledWith(path)
  }

  const verifyPostCalledWith = (path: string, d: any) => {
    expect(mockedPost).toHaveBeenCalledWith(path, d)
  }

  const verifyPutCalledWith = (path: string, d: any) => {
    expect(mockedPut).toHaveBeenCalledWith(path, d)
  }

  const verifyPatchCalledWith = (path: string, d: any) => {
    expect(mockedPatch).toHaveBeenCalledWith(path, d)
  }
})