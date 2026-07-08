import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { pendingApi } from '../../api/pending'
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
const mockedPatch = vi.mocked(api.patch)
const mockedDelete = vi.mocked(api.delete)

describe('pendingApi', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  it('list llama a GET /pending sin filtro', () => {
    setupMockedGetResponse([])
    return callList().then(() => {
      verifyGetCalledWith('/pending')
    })
  })

  it('list llama a GET /pending con filtro de status', () => {
    setupMockedGetResponse([])
    return callList('PENDING').then(() => {
      verifyGetCalledWith('/pending?status=PENDING')
    })
  })

  it('create llama a POST /pending con los datos', () => {
    setupMockedPostResponse({ id: 1 })
    return callCreate({ title: 'Lavar platos' }).then(() => {
      verifyPostCalledWith('/pending', { title: 'Lavar platos' })
    })
  })

  it('update llama a PATCH /pending/{id}', () => {
    setupMockedPatchResponse(undefined)
    return callUpdate(5, { title: 'Actualizado' }).then(() => {
      verifyPatchCalledWith('/pending/5', { title: 'Actualizado' })
    })
  })

  it('delete llama a DELETE /pending/{id}', () => {
    setupMockedDeleteResponse(undefined)
    return callDelete(3).then(() => {
      verifyDeleteCalledWith('/pending/3')
    })
  })

  it('complete llama a PATCH /pending/{id}/complete', () => {
    setupMockedPatchResponse(undefined)
    return callComplete(3).then(() => {
      verifyPatchCalledWithNoBody('/pending/3/complete')
    })
  })

  it('addSubtask llama a POST /pending/{id}/subtasks', () => {
    setupMockedPostResponse({ id: 1 })
    return callAddSubtask(3, 'Comprar detergente').then(() => {
      verifyPostCalledWith('/pending/3/subtasks', { text: 'Comprar detergente' })
    })
  })

  it('toggleSubtask llama a PATCH /pending/{id}/subtasks/{subtaskId}/toggle', () => {
    setupMockedPatchResponse(undefined)
    return callToggleSubtask(3, 9).then(() => {
      verifyPatchCalledWithNoBody('/pending/3/subtasks/9/toggle')
    })
  })

  it('deleteSubtask llama a DELETE /pending/{id}/subtasks/{subtaskId}', () => {
    setupMockedDeleteResponse(undefined)
    return callDeleteSubtask(3, 9).then(() => {
      verifyDeleteCalledWith('/pending/3/subtasks/9')
    })
  })

  it('updatePosition llama a PATCH /pending/{id}/position', () => {
    setupMockedPatchResponse({ id: 3 })
    return callUpdatePosition(3, 50, 60).then(() => {
      verifyPatchCalledWith('/pending/3/position', { positionX: 50, positionY: 60 })
    })
  })

  it('getTodayRecommendation llama a GET /pending/recommendation/today', () => {
    setupMockedGetResponse(null)
    return callGetTodayRecommendation().then(() => {
      verifyGetCalledWith('/pending/recommendation/today')
    })
  })

  it('respondToRecommendation llama a POST /pending/recommendation/{id}/respond', () => {
    setupMockedPostResponse({ recommendationId: 1 })
    return callRespondToRecommendation(1, 'ACCEPTED').then(() => {
      verifyPostCalledWith('/pending/recommendation/1/respond', { decision: 'ACCEPTED' })
    })
  })

  /* helpers */

  const setupMockedGetResponse = (response: any) => {
    mockedGet.mockResolvedValueOnce(response as never)
  }

  const setupMockedPostResponse = (response: any) => {
    mockedPost.mockResolvedValueOnce(response as never)
  }

  const setupMockedPatchResponse = (response: any) => {
    mockedPatch.mockResolvedValueOnce(response as never)
  }

  const setupMockedDeleteResponse = (response: any) => {
    mockedDelete.mockResolvedValueOnce(response as never)
  }

  const callList = (status?: 'PENDING' | 'COMPLETED' | 'CANCELLED') => {
    return pendingApi.list(status)
  }

  const callCreate = (dto: any) => {
    return pendingApi.create(dto)
  }

  const callUpdate = (id: number, dto: any) => {
    return pendingApi.update(id, dto)
  }

  const callDelete = (id: number) => {
    return pendingApi.delete(id)
  }

  const callComplete = (id: number) => {
    return pendingApi.complete(id)
  }

  const callAddSubtask = (taskId: number, text: string) => {
    return pendingApi.addSubtask(taskId, text)
  }

  const callToggleSubtask = (taskId: number, subtaskId: number) => {
    return pendingApi.toggleSubtask(taskId, subtaskId)
  }

  const callDeleteSubtask = (taskId: number, subtaskId: number) => {
    return pendingApi.deleteSubtask(taskId, subtaskId)
  }

  const callUpdatePosition = (id: number, positionX: number, positionY: number) => {
    return pendingApi.updatePosition(id, positionX, positionY)
  }

  const callGetTodayRecommendation = () => {
    return pendingApi.getTodayRecommendation()
  }

  const callRespondToRecommendation = (recommendationId: number, decision: 'ACCEPTED' | 'REJECTED') => {
    return pendingApi.respondToRecommendation(recommendationId, decision)
  }

  const verifyGetCalledWith = (url: string) => {
    expect(mockedGet).toHaveBeenCalledWith(url)
  }

  const verifyPostCalledWith = (url: string, body: any) => {
    expect(mockedPost).toHaveBeenCalledWith(url, body)
  }

  const verifyPatchCalledWith = (url: string, body: any) => {
    expect(mockedPatch).toHaveBeenCalledWith(url, body)
  }

  const verifyPatchCalledWithNoBody = (url: string) => {
    expect(mockedPatch).toHaveBeenCalledWith(url)
  }

  const verifyDeleteCalledWith = (url: string) => {
    expect(mockedDelete).toHaveBeenCalledWith(url)
  }
})
