import { clearAllMocks, setupMockedGetResponse, setupMockedPostResponse, setupMockedPutResponse } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { userGoalsApi } from '../../api/userGoals'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
}))

const mockedGet = vi.mocked(api.get)
const mockedPost = vi.mocked(api.post)
const mockedPut = vi.mocked(api.put)
const mockedPatch = vi.mocked(api.patch)
const mockedDelete = vi.mocked(api.delete)

describe('userGoalsApi', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  it('getForCurrentUser llama a GET /user-goals/me con paginación por defecto', () => {
    setupMockedGetResponse({ completados: { content: [] }, pendientes: { content: [] } })
    return callGetForCurrentUser().then(() => {
      verifyGetCalledWith('/user-goals/me?page=0&size=50')
    })
  })

  it('getForCurrentUser reenvía paginación personalizada', () => {
    setupMockedGetResponse({ completados: { content: [] }, pendientes: { content: [] } })
    return callGetForCurrentUser(2, 10).then(() => {
      verifyGetCalledWith('/user-goals/me?page=2&size=10')
    })
  })

  it('create llama a POST /user-goals con los datos', () => {
    setupMockedPostResponse({ id: 1 })
    return callCreate({ title: 'Reto 1' }).then(() => {
      verifyPostCalledWith('/user-goals', { title: 'Reto 1' })
    })
  })

  it('create incluye descripción opcional si se provee', () => {
    setupMockedPostResponse({ id: 2 })
    return callCreate({ title: 'Reto', description: 'Descripción', activityId: 5 }).then(() => {
      verifyPostCalledWith('/user-goals', {
        title: 'Reto',
        description: 'Descripción',
        activityId: 5,
      })
    })
  })

  it('update llama a PUT /user-goals/{id} con los datos', () => {
    setupMockedPutResponse({ id: 5 })
    return callUpdate(5, { title: 'Actualizado' }).then(() => {
      verifyPutCalledWith('/user-goals/5', { title: 'Actualizado' })
    })
  })

  it('delete llama a DELETE /user-goals/{id}', () => {
    setupMockedDeleteResponse(undefined)
    return callDelete(3).then(() => {
      verifyDeleteCalledWith('/user-goals/3')
    })
  })

  it('complete llama a PATCH /user-goals/{id}/complete con FormData', () => {
    setupMockedPatchResponse({ id: 7, status: 'COMPLETED' })
    return callComplete(7).then(() => {
      verifyPatchCalledWith('/user-goals/7/complete', expect.any(FormData))
    })
  })

  it('complete incluye la imagen en el FormData cuando se provee', () => {
    setupMockedPatchResponse({ id: 7, status: 'COMPLETED' })
    return callCompleteWithDummyFile(7).then(() => {
      verifyPatchCalledWithDummyFile()
    })
  })

  /* helpers */

  

  

  

  const setupMockedDeleteResponse = (response: any) => {
    mockedDelete.mockResolvedValueOnce(response as never)
  }

  const setupMockedPatchResponse = (response: any) => {
    mockedPatch.mockResolvedValueOnce(response as never)
  }

  const callGetForCurrentUser = (page?: number, size?: number) => {
    return userGoalsApi.getForCurrentUser(page, size)
  }

  const callCreate = (dto: any) => {
    return userGoalsApi.create(dto)
  }

  const callUpdate = (id: number, dto: any) => {
    return userGoalsApi.update(id, dto)
  }

  const callDelete = (id: number) => {
    return userGoalsApi.delete(id)
  }

  const callComplete = (id: number) => {
    return userGoalsApi.complete(id)
  }

  const callCompleteWithDummyFile = (id: number) => {
    const file = new File(['img'], 'foto.jpg', { type: 'image/jpeg' })
    return userGoalsApi.complete(id, file)
  }

  const verifyGetCalledWith = (url: string) => {
    expect(mockedGet).toHaveBeenCalledWith(url)
  }

  const verifyPostCalledWith = (url: string, body: any) => {
    expect(mockedPost).toHaveBeenCalledWith(url, body)
  }

  const verifyPutCalledWith = (url: string, body: any) => {
    expect(mockedPut).toHaveBeenCalledWith(url, body)
  }

  const verifyDeleteCalledWith = (url: string) => {
    expect(mockedDelete).toHaveBeenCalledWith(url)
  }

  const verifyPatchCalledWith = (url: string, body: any) => {
    expect(mockedPatch).toHaveBeenCalledWith(url, body)
  }

  const verifyPatchCalledWithDummyFile = () => {
    const [, formData] = mockedPatch.mock.calls[0] as [string, FormData]
    expect(formData.get('image')).toBeInstanceOf(File)
    expect((formData.get('image') as File).name).toBe('foto.jpg')
  }
})
