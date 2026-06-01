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
    vi.clearAllMocks()
  })

  it('getByUser llama a GET con el userId y paginación por defecto', async () => {
    mockedGet.mockResolvedValueOnce({ completados: { content: [] }, pendientes: { content: [] } } as never)
    await userGoalsApi.getByUser(42)
    expect(mockedGet).toHaveBeenCalledWith('/user-goals/user/42?page=0&size=50')
  })

  it('getByUser reenvía paginación personalizada', async () => {
    mockedGet.mockResolvedValueOnce({ completados: { content: [] }, pendientes: { content: [] } } as never)
    await userGoalsApi.getByUser(1, 2, 10)
    expect(mockedGet).toHaveBeenCalledWith('/user-goals/user/1?page=2&size=10')
  })

  it('create llama a POST /user-goals con los datos', async () => {
    mockedPost.mockResolvedValueOnce({ id: 1 } as never)
    await userGoalsApi.create({ userId: 1, title: 'Reto 1' })
    expect(mockedPost).toHaveBeenCalledWith('/user-goals', { userId: 1, title: 'Reto 1' })
  })

  it('create incluye descripción opcional si se provee', async () => {
    mockedPost.mockResolvedValueOnce({ id: 2 } as never)
    await userGoalsApi.create({ userId: 1, title: 'Reto', description: 'Descripción', activityId: 5 })
    expect(mockedPost).toHaveBeenCalledWith('/user-goals', {
      userId: 1,
      title: 'Reto',
      description: 'Descripción',
      activityId: 5,
    })
  })

  it('update llama a PUT /user-goals/{id} con los datos', async () => {
    mockedPut.mockResolvedValueOnce({ id: 5 } as never)
    await userGoalsApi.update(5, { title: 'Actualizado' })
    expect(mockedPut).toHaveBeenCalledWith('/user-goals/5', { title: 'Actualizado' })
  })

  it('delete llama a DELETE /user-goals/{id}', async () => {
    mockedDelete.mockResolvedValueOnce(undefined as never)
    await userGoalsApi.delete(3)
    expect(mockedDelete).toHaveBeenCalledWith('/user-goals/3')
  })

  it('complete llama a PATCH /user-goals/{id}/complete', async () => {
    mockedPatch.mockResolvedValueOnce({ id: 7, status: 'COMPLETED' } as never)
    await userGoalsApi.complete(7)
    expect(mockedPatch).toHaveBeenCalledWith('/user-goals/7/complete')
  })
})
