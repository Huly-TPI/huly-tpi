import { describe, it, expect, vi, beforeEach } from 'vitest'
import { adminBreathingApi } from '../../api/adminBreathing'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
  api: { get: vi.fn(), post: vi.fn(), put: vi.fn(), patch: vi.fn() },
}))

const mockedGet = vi.mocked(api.get)
const mockedPost = vi.mocked(api.post)
const mockedPut = vi.mocked(api.put)
const mockedPatch = vi.mocked(api.patch)

const data = { name: 'Diafragmática', description: 'd', inhaleSeconds: 4, holdSeconds: 0, exhaleSeconds: 4, roundsInterval: 1, rounds: 4 }

describe('adminBreathingApi', () => {
  beforeEach(() => { vi.clearAllMocks() })

  it('list llama GET', async () => {
    mockedGet.mockResolvedValueOnce([] as never)
    await adminBreathingApi.list()
    expect(mockedGet).toHaveBeenCalledWith('/admin/breathing-techniques')
  })

  it('create llama POST', async () => {
    mockedPost.mockResolvedValueOnce({} as never)
    await adminBreathingApi.create(data)
    expect(mockedPost).toHaveBeenCalledWith('/admin/breathing-techniques', data)
  })

  it('update llama PUT con el id', async () => {
    mockedPut.mockResolvedValueOnce({} as never)
    await adminBreathingApi.update(5, data)
    expect(mockedPut).toHaveBeenCalledWith('/admin/breathing-techniques/5', data)
  })

  it('setActive llama PATCH con el active', async () => {
    mockedPatch.mockResolvedValueOnce({} as never)
    await adminBreathingApi.setActive(9, false)
    expect(mockedPatch).toHaveBeenCalledWith('/admin/breathing-techniques/9/active', { active: false })
  })
})