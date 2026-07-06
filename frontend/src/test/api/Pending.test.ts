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
    vi.clearAllMocks()
  })

  it('list llama a GET /pending sin filtro', async () => {
    mockedGet.mockResolvedValueOnce([] as never)
    await pendingApi.list()
    expect(mockedGet).toHaveBeenCalledWith('/pending')
  })

  it('list llama a GET /pending con filtro de status', async () => {
    mockedGet.mockResolvedValueOnce([] as never)
    await pendingApi.list('PENDING')
    expect(mockedGet).toHaveBeenCalledWith('/pending?status=PENDING')
  })

  it('create llama a POST /pending con los datos', async () => {
    mockedPost.mockResolvedValueOnce({ id: 1 } as never)
    await pendingApi.create({ title: 'Lavar platos' })
    expect(mockedPost).toHaveBeenCalledWith('/pending', { title: 'Lavar platos' })
  })

  it('update llama a PATCH /pending/{id}', async () => {
    mockedPatch.mockResolvedValueOnce(undefined as never)
    await pendingApi.update(5, { title: 'Actualizado' })
    expect(mockedPatch).toHaveBeenCalledWith('/pending/5', { title: 'Actualizado' })
  })

  it('delete llama a DELETE /pending/{id}', async () => {
    mockedDelete.mockResolvedValueOnce(undefined as never)
    await pendingApi.delete(3)
    expect(mockedDelete).toHaveBeenCalledWith('/pending/3')
  })

  it('complete llama a PATCH /pending/{id}/complete', async () => {
    mockedPatch.mockResolvedValueOnce(undefined as never)
    await pendingApi.complete(3)
    expect(mockedPatch).toHaveBeenCalledWith('/pending/3/complete')
  })

  it('addSubtask llama a POST /pending/{id}/subtasks', async () => {
    mockedPost.mockResolvedValueOnce({ id: 1 } as never)
    await pendingApi.addSubtask(3, 'Comprar detergente')
    expect(mockedPost).toHaveBeenCalledWith('/pending/3/subtasks', { text: 'Comprar detergente' })
  })

  it('toggleSubtask llama a PATCH /pending/{id}/subtasks/{subtaskId}/toggle', async () => {
    mockedPatch.mockResolvedValueOnce(undefined as never)
    await pendingApi.toggleSubtask(3, 9)
    expect(mockedPatch).toHaveBeenCalledWith('/pending/3/subtasks/9/toggle')
  })

  it('deleteSubtask llama a DELETE /pending/{id}/subtasks/{subtaskId}', async () => {
    mockedDelete.mockResolvedValueOnce(undefined as never)
    await pendingApi.deleteSubtask(3, 9)
    expect(mockedDelete).toHaveBeenCalledWith('/pending/3/subtasks/9')
  })

  it('updatePosition llama a PATCH /pending/{id}/position sin rotación', async () => {
    mockedPatch.mockResolvedValueOnce({ id: 3 } as never)
    await pendingApi.updatePosition(3, 50, 60)
    expect(mockedPatch).toHaveBeenCalledWith('/pending/3/position', { positionX: 50, positionY: 60 })
  })

  it('getTodayRecommendation llama a GET /pending/recommendation/today', async () => {
    mockedGet.mockResolvedValueOnce(null as never)
    await pendingApi.getTodayRecommendation()
    expect(mockedGet).toHaveBeenCalledWith('/pending/recommendation/today')
  })

  it('respondToRecommendation llama a POST /pending/recommendation/{id}/respond', async () => {
    mockedPost.mockResolvedValueOnce({ recommendationId: 1 } as never)
    await pendingApi.respondToRecommendation(1, 'ACCEPTED')
    expect(mockedPost).toHaveBeenCalledWith('/pending/recommendation/1/respond', { decision: 'ACCEPTED' })
  })
})
