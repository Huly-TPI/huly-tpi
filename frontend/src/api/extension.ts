import { api } from './client'

export interface ExtensionSettings {
  enabled: boolean
  pauseIntervalMinutes: number
  gardenUrl: string
  backendUrl: string
  monitoredDomains: string[]
  dataSharingConsent: boolean
}

export const getExtensionSettings = async (): Promise<ExtensionSettings> => {
  return api.get<ExtensionSettings>('/extension/settings')
}

export const saveExtensionSettings = async (settings: ExtensionSettings): Promise<void> => {
  return api.post<void>('/extension/settings', settings)
}
