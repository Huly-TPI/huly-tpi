import { clearAllMocks, setupMockedPostResponse, setupMockedPostError, setupMockedGetResponse, setupMockedGetError } from '../testHelpers'
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

describe('journalApi', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  describe('create', () => {
    it('llama a api.post con la ruta y los datos correctos', () => {
      setupMockedPostResponse(validEntry)
      return callCreate({ content: 'Hoy me sentí bien', mood: 'HAPPY' }).then(() => {
        verifyPostCalledWith('/journal', { content: 'Hoy me sentí bien', mood: 'HAPPY' })
      })
    })

    it('retorna la entrada creada por el backend', () => {
      setupMockedPostResponse(validEntry)
      return callCreateAndVerifyResult({ content: 'Algo', mood: null }, validEntry)
    })

    it('permite crear una entrada sin mood', () => {
      const entryWithoutMood = { ...validEntry, mood: null }
      setupMockedPostResponse(entryWithoutMood)
      return callCreateAndVerifyMoodIsNull({ content: 'Solo escribir', mood: null }).then(() => {
        verifyPostCalledWith('/journal', { content: 'Solo escribir', mood: null })
      })
    })

    it('propaga errores del backend', () => {
      setupMockedPostError(new Error('No autorizado'))
      return verifyCreateThrowsError({ content: 'Algo' }, 'No autorizado')
    })
  })

  describe('list', () => {
    it('llama a api.get con la ruta correcta', () => {
      setupMockedGetResponse([])
      return callList().then(() => {
        verifyGetCalledWith('/journal')
      })
    })

    it('retorna la lista de entradas del backend', () => {
      const entries = [validEntry, { ...validEntry, id: 2 }]
      setupMockedGetResponse(entries)
      return callListAndVerifyResultAndLength(entries, 2)
    })

    it('retorna lista vacía cuando no hay entradas', () => {
      setupMockedGetResponse([])
      return callListAndVerifyResult([])
    })

    it('propaga errores del backend', () => {
      setupMockedGetError(new Error('Error del servidor'))
      return verifyListThrowsError('Error del servidor')
    })
  })

  /* helpers */

  

  

  

  

  const callCreate = (data: any) => {
    return journalApi.create(data)
  }

  const callCreateAndVerifyResult = (data: any, expected: any) => {
    return journalApi.create(data).then((res) => {
      expect(res).toEqual(expected)
    })
  }

  const callCreateAndVerifyMoodIsNull = (data: any) => {
    return journalApi.create(data).then((res) => {
      expect(res.mood).toBeNull()
    })
  }

  const verifyCreateThrowsError = (data: any, expectedErrorMsg: string) => {
    return expect(journalApi.create(data)).rejects.toThrow(expectedErrorMsg)
  }

  const callList = () => {
    return journalApi.list()
  }

  const callListAndVerifyResult = (expected: any[]) => {
    return journalApi.list().then((res) => {
      expect(res).toEqual(expected)
    })
  }

  const callListAndVerifyResultAndLength = (expected: any[], expectedLength: number) => {
    return journalApi.list().then((res) => {
      expect(res).toEqual(expected)
      expect(res).toHaveLength(expectedLength)
    })
  }

  const verifyListThrowsError = (expectedErrorMsg: string) => {
    return expect(journalApi.list()).rejects.toThrow(expectedErrorMsg)
  }

  const verifyPostCalledWith = (url: string, body: any) => {
    expect(mockedPost).toHaveBeenCalledWith(url, body)
  }

  const verifyGetCalledWith = (url: string) => {
    expect(mockedGet).toHaveBeenCalledWith(url)
  }
})
