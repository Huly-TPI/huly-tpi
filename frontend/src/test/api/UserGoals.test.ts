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

  it('getForCurrentUser llama a GET /user-goals/me con paginación por defecto', async () => {
    mockedGet.mockResolvedValueOnce({ completados: { content: [] }, pendientes: { content: [] } } as never)
    await userGoalsApi.getForCurrentUser()
    expect(mockedGet).toHaveBeenCalledWith('/user-goals/me?page=0&size=50')
  })

  it('getForCurrentUser reenvía paginación personalizada', async () => {
    mockedGet.mockResolvedValueOnce({ completados: { content: [] }, pendientes: { content: [] } } as never)
    await userGoalsApi.getForCurrentUser(2, 10)
    expect(mockedGet).toHaveBeenCalledWith('/user-goals/me?page=2&size=10')
  })

  it('create llama a POST /user-goals con los datos', async () => {
    mockedPost.mockResolvedValueOnce({ id: 1 } as never)
    await userGoalsApi.create({ title: 'Reto 1' })
    expect(mockedPost).toHaveBeenCalledWith('/user-goals', { title: 'Reto 1' })
  })

  it('create incluye descripción opcional si se provee', async () => {
    mockedPost.mockResolvedValueOnce({ id: 2 } as never)
    await userGoalsApi.create({ title: 'Reto', description: 'Descripción', activityId: 5 })
    expect(mockedPost).toHaveBeenCalledWith('/user-goals', {
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

  it('complete llama a PATCH /user-goals/{id}/complete con FormData', async () => {
    mockedPatch.mockResolvedValueOnce({ id: 7, status: 'COMPLETED' } as never)
    await userGoalsApi.complete(7)
    expect(mockedPatch).toHaveBeenCalledWith('/user-goals/7/complete', expect.any(FormData))
  })

  it('complete incluye la imagen en el FormData cuando se provee', async () => {
    mockedPatch.mockResolvedValueOnce({ id: 7, status: 'COMPLETED' } as never)
    const file = new File(['img'], 'foto.jpg', { type: 'image/jpeg' })
    await userGoalsApi.complete(7, file)
    const [, formData] = mockedPatch.mock.calls[0] as [string, FormData]
    expect(formData.get('image')).toBe(file)
  })
})
