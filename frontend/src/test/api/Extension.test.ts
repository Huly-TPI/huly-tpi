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
  pauseIntervalMinutes: 20,
  gardenUrl: 'http://localhost:5173/garden',
  backendUrl: 'http://localhost:8080',
  monitoredDomains: ['twitter.com'],
  dataSharingConsent: true,
}

describe('extensionApi', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('getExtensionSettings', () => {
    it('llama a GET /extension/settings y retorna el resultado', async () => {
      mockedGet.mockResolvedValueOnce(sampleSettings)

      const res = await getExtensionSettings()

      expect(mockedGet).toHaveBeenCalledWith('/extension/settings')
      expect(res).toEqual(sampleSettings)
    })

    it('propaga errores del api client', async () => {
      mockedGet.mockRejectedValueOnce(new Error('Network Error'))

      await expect(getExtensionSettings()).rejects.toThrow('Network Error')
    })
  })

  describe('saveExtensionSettings', () => {
    it('llama a POST /extension/settings con los datos del body', async () => {
      mockedPost.mockResolvedValueOnce(undefined)

      await saveExtensionSettings(sampleSettings)

      expect(mockedPost).toHaveBeenCalledWith('/extension/settings', sampleSettings)
    })

    it('propaga errores del api client', async () => {
      mockedPost.mockRejectedValueOnce(new Error('Validation Failed'))

      await expect(saveExtensionSettings(sampleSettings)).rejects.toThrow('Validation Failed')
    })
  })
})
