import browser from 'webextension-polyfill';
import { HULY_APP_ORIGINS } from '../utils/constants';
import { setSettings } from '../utils/storage';
import { buildRuntimeConfigPatch, RuntimeConfigDetail } from '../utils/runtimeConfig';
import { ExtensionSettings } from '../utils/types';

const ATTRS = {
  backendUrl: 'data-huly-backend-url',
  gardenUrl: 'data-huly-garden-url',
  installed: 'data-huly-antiscroll-installed',
  enabled: 'data-huly-antiscroll-enabled',
  consent: 'data-huly-antiscroll-consent',
  id: 'data-huly-antiscroll-id',
  ready: 'data-huly-antiscroll-script-ready',
} as const;

export const isHulyPage = (origin: string): boolean =>
  HULY_APP_ORIGINS.includes(origin);

export const readRuntimeConfigFromPage = (): RuntimeConfigDetail => ({
  backendUrl: document.documentElement.getAttribute(ATTRS.backendUrl) || undefined,
  gardenUrl: document.documentElement.getAttribute(ATTRS.gardenUrl) || undefined,
});

export const syncHulyAttributes = (settings: Pick<ExtensionSettings, 'enabled' | 'dataSharingConsent'>) => {
  document.documentElement.setAttribute(ATTRS.ready, 'true');
  document.documentElement.setAttribute(ATTRS.installed, 'true');
  document.documentElement.setAttribute(ATTRS.enabled, settings.enabled ? 'true' : 'false');
  document.documentElement.setAttribute(ATTRS.consent, settings.dataSharingConsent ? 'true' : 'false');

  if (browser.runtime.id) {
    document.documentElement.setAttribute(ATTRS.id, browser.runtime.id);
  }
};

export const clearHulyReadyAttribute = () => {
  document.documentElement.removeAttribute(ATTRS.ready);
};

export const applyRuntimeConfig = async (
  settings: ExtensionSettings,
  config: RuntimeConfigDetail,
): Promise<ExtensionSettings> => {
  const nextSettings = buildRuntimeConfigPatch(settings, config);

  if (Object.keys(nextSettings).length === 0) {
    return settings;
  }

  await setSettings(nextSettings);
  return { ...settings, ...nextSettings };
};

interface HulyBridgeOptions {
  onRuntimeConfig: (config: RuntimeConfigDetail) => Promise<void> | void;
  onSessionToken: (token: string | null) => Promise<void> | void;
  onUserLoaded?: (user: { id: number; name: string }) => Promise<void> | void;
  onExtensionSettingsLoaded?: (settings: Partial<ExtensionSettings>) => Promise<void> | void;
}

export const setupHulyPageBridge = ({
  onRuntimeConfig,
  onSessionToken,
  onUserLoaded,
  onExtensionSettingsLoaded,
}: HulyBridgeOptions) => {
  const initialToken = sessionStorage.getItem('huly:token');
  if (initialToken) {
    onSessionToken(initialToken);
  }

  const handleSessionEvent = (event: Event) => {
    const sessionEvent = event as CustomEvent<{ token?: string | null }>;
    onSessionToken(sessionEvent.detail?.token || null);
  };

  const handleExtensionConfigEvent = (event: Event) => {
    const runtimeEvent = event as CustomEvent<RuntimeConfigDetail>;
    void onRuntimeConfig(runtimeEvent.detail || {});
  };

  const handleUserLoadedEvent = (event: Event) => {
    if (!onUserLoaded) {
      return;
    }

    const userEvent = event as CustomEvent<{ id?: number; name?: string }>;
    const userName = userEvent.detail?.name?.trim();
    const userId = userEvent.detail?.id;
    if (userName && typeof userId === 'number') {
      void onUserLoaded({ id: userId, name: userName });
    }
  };

  const handleExtensionSettingsEvent = (event: Event) => {
    if (!onExtensionSettingsLoaded) {
      return;
    }

    const settingsEvent = event as CustomEvent<Partial<ExtensionSettings>>;
    void onExtensionSettingsLoaded(settingsEvent.detail || {});
  };

  const syncCurrentToken = () => {
    const currentToken = sessionStorage.getItem('huly:token');
    if (currentToken) {
      onSessionToken(currentToken);
    }
  };

  window.addEventListener('huly:session', handleSessionEvent);
  window.addEventListener('huly:extension-config', handleExtensionConfigEvent);
  window.addEventListener('auth:user-loaded', handleUserLoadedEvent);
  window.addEventListener('huly:extension-settings', handleExtensionSettingsEvent);
  document.addEventListener('visibilitychange', syncCurrentToken);

  return () => {
    window.removeEventListener('huly:session', handleSessionEvent);
    window.removeEventListener('huly:extension-config', handleExtensionConfigEvent);
    window.removeEventListener('auth:user-loaded', handleUserLoadedEvent);
    window.removeEventListener('huly:extension-settings', handleExtensionSettingsEvent);
    document.removeEventListener('visibilitychange', syncCurrentToken);
  };
};
