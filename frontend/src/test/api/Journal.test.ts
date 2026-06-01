import { describe, it, expect, vi, beforeEach } from 'vitest'
import { journalApi } from '../../api/journal'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
  api: {
    post: vi.fn(),
    get: vi.fn(),
  },
}))

const mockedPost = vi.mocked(api.post)
const mockedGet = vi.mocked(api.get)

const validEntry = {
  id: 1,
  content: JSON.stringify({ adentro: 'Hoy fue bien', pensamiento: '', bien: '', manana: '' }),
  mood: 'HAPPY' as const,
  createdAt: '2025-01-15T10:00:00Z',
}

describe('journalApi.create', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('llama a api.post con la ruta y los datos correctos', async () => {
    mockedPost.mockResolvedValueOnce(validEntry)
    const data = { content: 'Hoy me sentí bien', mood: 'HAPPY' as const }

    await journalApi.create(data)

    expect(mockedPost).toHaveBeenCalledWith('/journal', data)
  })

  it('retorna la entrada creada por el backend', async () => {
    mockedPost.mockResolvedValueOnce(validEntry)

    const result = await journalApi.create({ content: 'Algo', mood: null })

    expect(result).toEqual(validEntry)
  })

  it('permite crear una entrada sin mood', async () => {
    const entryWithoutMood = { ...validEntry, mood: null }
    mockedPost.mockResolvedValueOnce(entryWithoutMood)

    const result = await journalApi.create({ content: 'Solo escribir', mood: null })

    expect(mockedPost).toHaveBeenCalledWith('/journal', { content: 'Solo escribir', mood: null })
    expect(result.mood).toBeNull()
  })

  it('propaga errores del backend', async () => {
    mockedPost.mockRejectedValueOnce(new Error('No autorizado'))

    await expect(journalApi.create({ content: 'Algo' })).rejects.toThrow('No autorizado')
  })
})

describe('journalApi.list', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('llama a api.get con la ruta correcta', async () => {
    mockedGet.mockResolvedValueOnce([])

    await journalApi.list()

    expect(mockedGet).toHaveBeenCalledWith('/journal')
  })

  it('retorna la lista de entradas del backend', async () => {
    const entries = [validEntry, { ...validEntry, id: 2 }]
    mockedGet.mockResolvedValueOnce(entries)

    const result = await journalApi.list()

    expect(result).toEqual(entries)
    expect(result).toHaveLength(2)
  })

  it('retorna lista vacía cuando no hay entradas', async () => {
    mockedGet.mockResolvedValueOnce([])

    const result = await journalApi.list()

    expect(result).toEqual([])
  })

  it('propaga errores del backend', async () => {
    mockedGet.mockRejectedValueOnce(new Error('Error del servidor'))

    await expect(journalApi.list()).rejects.toThrow('Error del servidor')
  })
})
