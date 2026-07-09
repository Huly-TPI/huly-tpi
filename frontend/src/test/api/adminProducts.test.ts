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

  it('list llama GET con el type', async () => {
    mockedGet.mockResolvedValueOnce([] as never)
    await adminProductsApi.list('COIN_PACK')
    expect(mockedGet).toHaveBeenCalledWith('/admin/products?type=COIN_PACK')
  })

  it('create llama POST', async () => {
    mockedPost.mockResolvedValueOnce({} as never)
    await adminProductsApi.create(data)
    expect(mockedPost).toHaveBeenCalledWith('/admin/products', data)
  })

  it('update llama PUT con el id', async () => {
    mockedPut.mockResolvedValueOnce({} as never)
    await adminProductsApi.update(5, data)
    expect(mockedPut).toHaveBeenCalledWith('/admin/products/5', data)
  })

  it('setActive llama PATCH con el active', async () => {
    mockedPatch.mockResolvedValueOnce({} as never)
    await adminProductsApi.setActive(9, false)
    expect(mockedPatch).toHaveBeenCalledWith('/admin/products/9/active', { active: false })
  })
})