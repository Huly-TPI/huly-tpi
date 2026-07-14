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

  it('list llama GET', () => {
    setupMockedGetResponse([] as never)
    return callList().then(() => {
      verifyGetCalledWith('/admin/breathing-techniques')
    })
  })

  it('create llama POST', () => {
    setupMockedPostResponse({} as never)
    return callCreate(data).then(() => {
      verifyPostCalledWith('/admin/breathing-techniques', data)
    })
  })

  it('update llama PUT con el id', () => {
    setupMockedPutResponse({} as never)
    return callUpdate(5, data).then(() => {
      verifyPutCalledWith('/admin/breathing-techniques/5', data)
    })
  })

  it('setActive llama PATCH con el active', () => {
    setupMockedPatchResponse({} as never)
    return callSetActive(9, false).then(() => {
      verifyPatchCalledWith('/admin/breathing-techniques/9/active', { active: false })
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

  const callList = () => {
    return Promise.resolve(adminBreathingApi.list())
  }

  const callCreate = (d: typeof data) => {
    return Promise.resolve(adminBreathingApi.create(d))
  }

  const callUpdate = (id: number, d: typeof data) => {
    return Promise.resolve(adminBreathingApi.update(id, d))
  }

  const callSetActive = (id: number, active: boolean) => {
    return Promise.resolve(adminBreathingApi.setActive(id, active))
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