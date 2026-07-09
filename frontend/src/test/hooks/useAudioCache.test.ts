import { describe, it, expect } from 'vitest'
import 'fake-indexeddb/auto'
import { saveAudioBlob, getAudioBlob, deleteAudioBlob } from '../../hooks/useAudioCache'

describe('audioCache', () => {
  it('saveAudioBlob almacena un blob recuperable por su clave', () => {
    return callSaveAudioBlob('test-key-1', getMockBlob('hello audio')).then(() => {
      return callGetAudioBlob('test-key-1').then((retrieved) => {
        verifyBlobExists(retrieved)
      })
    })
  })

  it('getAudioBlob devuelve null para una clave inexistente', () => {
    return callGetAudioBlob('nonexistent-key').then((result) => {
      verifyBlobIsNull(result)
    })
  })

  it('deleteAudioBlob elimina la entrada de modo que las lecturas posteriores devuelven null', () => {
    return callSaveAudioBlob('test-key-delete', getMockBlob('audio-data'))
      .then(() => callDeleteAudioBlob('test-key-delete'))
      .then(() => callGetAudioBlob('test-key-delete'))
      .then((result) => {
        verifyBlobIsNull(result)
      })
  })

  it('saveAudioBlob sobrescribe una entrada existente con la misma clave', () => {
    return callSaveAudioBlob('test-key-overwrite', getMockBlob('first'))
      .then(() => callSaveAudioBlob('test-key-overwrite', getMockBlob('second-longer')))
      .then(() => callGetAudioBlob('test-key-overwrite'))
      .then((retrieved) => {
        verifyBlobExists(retrieved)
      })
  })

  /* helpers */

  const getMockBlob = (content: string) => {
    return new Blob([content], { type: 'audio/webm' })
  }

  const callSaveAudioBlob = (key: string, blob: Blob) => {
    return saveAudioBlob(key, blob)
  }

  const callGetAudioBlob = (key: string) => {
    return getAudioBlob(key)
  }

  const callDeleteAudioBlob = (key: string) => {
    return deleteAudioBlob(key)
  }

  const verifyBlobExists = (retrieved: any) => {
    expect(retrieved).not.toBeNull()
  }

  const verifyBlobIsNull = (result: any) => {
    expect(result).toBeNull()
  }
})
