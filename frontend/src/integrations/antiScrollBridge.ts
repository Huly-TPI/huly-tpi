import { getBackendOrigin } from '../api/client'
import { getExtensionSettings } from '../api/extension'

export interface AntiScrollRuntimeConfig {
  backendUrl: string
  gardenUrl: string
}

interface ExtensionSettingsBridgePayload {
  enabled: boolean
  pauseIntervalSeconds: number
  gardenUrl: string
  backendUrl: string
  monitoredDomains: string[]
  dataSharingConsent: boolean
}

const BACKEND_URL_ATTR = 'data-huly-backend-url'
const GARDEN_URL_ATTR = 'data-huly-garden-url'

export const getAntiScrollRuntimeConfig = (): AntiScrollRuntimeConfig => ({
  backendUrl: getBackendOrigin(),
  gardenUrl: new URL('/garden', window.location.origin).toString(),
})

export const publishAntiScrollRuntimeConfig = (): void => {
  const config = getAntiScrollRuntimeConfig()

  document.documentElement.setAttribute(BACKEND_URL_ATTR, config.backendUrl)
  document.documentElement.setAttribute(GARDEN_URL_ATTR, config.gardenUrl)

  window.dispatchEvent(
    new CustomEvent<AntiScrollRuntimeConfig>('huly:extension-config', {
      detail: config,
    }),
  )
}

export const publishAntiScrollExtensionSettings = async (): Promise<void> => {
  const settings = await getExtensionSettings()
  const payload: ExtensionSettingsBridgePayload = {
    enabled: settings.enabled,
    pauseIntervalSeconds: settings.pauseIntervalMinutes * 60,
    gardenUrl: settings.gardenUrl,
    backendUrl: settings.backendUrl,
    monitoredDomains: settings.monitoredDomains,
    dataSharingConsent: settings.dataSharingConsent,
  }

  window.dispatchEvent(
    new CustomEvent<ExtensionSettingsBridgePayload>('huly:extension-settings', {
      detail: payload,
    }),
  )
}

export const setupAntiScrollBridge = (): (() => void) => {
  const handleUserLoaded = () => {
    void publishAntiScrollExtensionSettings()
  }

  publishAntiScrollRuntimeConfig()
  void publishAntiScrollExtensionSettings().catch(() => {})
  window.addEventListener('auth:user-loaded', handleUserLoaded)

  return () => {
    window.removeEventListener('auth:user-loaded', handleUserLoaded)
  }
}
