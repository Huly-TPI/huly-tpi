import { clearAllMocks, setupMockedGetResponse, setupMockedGetError, setupMockedPostResponse, setupMockedPostError } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { getExtensionSettings, saveExtensionSettings } from '../../api/extension'
import type { ExtensionSettings } from '../../api/extension'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
  api: {
    get: vi.fn(),
    post: vi.fn(),
  },
}))

const mockedGet = vi.mocked(api.get)
const mockedPost = vi.mocked(api.post)

const sampleSettings: ExtensionSettings = {
  enabled: true,
  pauseIntervalSeconds: 20,
  gardenUrl: 'http://localhost:5173/',
  backendUrl: 'http://localhost:8080',
  monitoredDomains: ['twitter.com'],
  dataSharingConsent: true,
}

describe('extensionApi', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  describe('getExtensionSettings', () => {
    it('llama a GET /extension/settings y retorna el resultado', () => {
      setupMockedGetResponse(sampleSettings)
      return callGetExtensionSettingsAndVerify(sampleSettings).then(() => {
        verifyGetCalledWith('/extension/settings')
      })
    })

    it('propaga errores del api client', () => {
      setupMockedGetError(new Error('Network Error'))
      return verifyGetExtensionSettingsThrows('Network Error')
    })
  })

  describe('saveExtensionSettings', () => {
    it('llama a POST /extension/settings con los datos del body', () => {
      setupMockedPostResponse(undefined)
      return callSaveExtensionSettings(sampleSettings).then(() => {
        verifyPostCalledWith('/extension/settings', sampleSettings)
      })
    })

    it('propaga errores del api client', () => {
      setupMockedPostError(new Error('Validation Failed'))
      return verifySaveExtensionSettingsThrows(sampleSettings, 'Validation Failed')
    })
  })

  /* helpers */

  

  

  

  

  const callGetExtensionSettingsAndVerify = (expected: ExtensionSettings) => {
    return getExtensionSettings().then((res) => {
      expect(res).toEqual(expected)
    })
  }

  const verifyGetExtensionSettingsThrows = (expectedErrorMsg: string) => {
    return expect(getExtensionSettings()).rejects.toThrow(expectedErrorMsg)
  }

  const callSaveExtensionSettings = (settings: ExtensionSettings) => {
    return saveExtensionSettings(settings)
  }

  const verifySaveExtensionSettingsThrows = (settings: ExtensionSettings, expectedErrorMsg: string) => {
    return expect(saveExtensionSettings(settings)).rejects.toThrow(expectedErrorMsg)
  }

  const verifyGetCalledWith = (url: string) => {
    expect(mockedGet).toHaveBeenCalledWith(url)
  }

  const verifyPostCalledWith = (url: string, body: any) => {
    expect(mockedPost).toHaveBeenCalledWith(url, body)
  }
})
