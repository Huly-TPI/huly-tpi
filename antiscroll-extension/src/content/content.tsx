import browser from 'webextension-polyfill';
import { createModalController } from './modalController';
import {
  applyRuntimeConfig,
  clearHulyReadyAttribute,
  isHulyPage,
  readRuntimeConfigFromPage,
  setupHulyPageBridge,
  syncHulyAttributes,
} from './runtimeBridge';
import { setupActivityTracker } from './activityTracker';
import { getSettings, setSettings } from '../utils/storage';
import { STORAGE_KEYS } from '../utils/constants';
import { ExtensionSettings } from '../utils/types';
import { normalizeDomain, stripCountrySuffix } from '../utils/domain';

declare global {
  interface Window {
    __hulyAntiScrollCleanup?: () => void;
  }
}

const DOMAIN = normalizeDomain(window.location.hostname);

let settings: ExtensionSettings | null = null;

const sendTokenToBackground = async (token: string | null) => {
  try {
    await browser.runtime.sendMessage({ type: 'SET_TOKEN', data: { token } });
  } catch (error) {
    console.error('[Huly Anti-Scroll] Send session token error:', error);
  }
};

const syncUserContext = async ({ id, name }: { id: number; name: string }) => {
  const currentToken = sessionStorage.getItem('huly:token');
  if (currentToken) {
    await sendTokenToBackground(currentToken);
  }

  if (settings && settings.userName !== name) {
    settings = { ...settings, userName: name };
    await setSettings({ userName: name });
  }

  try {
    await browser.runtime.sendMessage({
      type: 'SET_ACTIVE_USER',
      data: { userId: String(id) },
    });
    await browser.runtime.sendMessage({ type: 'REFRESH_REMOTE_SETTINGS' });
  } catch (error) {
    console.error('[Huly Anti-Scroll] Refresh settings error:', error);
  }
};

const syncExtensionSettings = async (partialSettings: Partial<ExtensionSettings>) => {
  if (!settings) {
    settings = await getSettings();
  }

  settings = { ...settings, ...partialSettings };
  await setSettings(partialSettings);
};

const createSettingsHelpers = (modalController: ReturnType<typeof createModalController>) => ({
  getGardenUrl: async () => {
    settings = await getSettings();
    return settings.gardenUrl;
  },
  isMonitored: () => {
    if (!settings || !settings.enabled) return false;
    const cleanDomain = stripCountrySuffix(DOMAIN);
    return settings.monitoredDomains.some((domain) => {
      const normalized = normalizeDomain(domain);
      return cleanDomain === stripCountrySuffix(normalized);
    });
  },
  isPaused: () => modalController.isOpen(),
});

async function init() {
  window.__hulyAntiScrollCleanup?.();
  settings = await getSettings();

  const cleanupFns: Array<() => void> = [];
  const hulyPage = isHulyPage(window.location.origin);
  const modalController = createModalController({
    domain: DOMAIN,
    getGardenUrl: async () => {
      settings = await getSettings();
      return settings.gardenUrl;
    },
  });

  if (hulyPage) {
    settings = await applyRuntimeConfig(settings, readRuntimeConfigFromPage());
    syncHulyAttributes(settings);
  }

  const handleStorageChanged = (changes: Record<string, browser.Storage.StorageChange>) => {
    if (!changes[STORAGE_KEYS.SETTINGS]) {
      return;
    }

    settings = { ...settings, ...changes[STORAGE_KEYS.SETTINGS].newValue } as ExtensionSettings;

    if (hulyPage && settings) {
      syncHulyAttributes(settings);
    }
  };

  browser.storage.onChanged.addListener(handleStorageChanged);
  cleanupFns.push(() => browser.storage.onChanged.removeListener(handleStorageChanged));

  if (hulyPage) {
    cleanupFns.push(
      setupHulyPageBridge({
        onRuntimeConfig: async (config) => {
          if (!settings) {
            settings = await getSettings();
          }
          settings = await applyRuntimeConfig(settings, config);
        },
        onSessionToken: sendTokenToBackground,
        onUserLoaded: syncUserContext,
        onExtensionSettingsLoaded: syncExtensionSettings,
      }),
    );
  }

  const settingsHelpers = createSettingsHelpers(modalController);
  cleanupFns.push(
    setupActivityTracker({
      domain: DOMAIN,
      isMonitored: settingsHelpers.isMonitored,
      isPaused: settingsHelpers.isPaused,
    }),
  );

  const handleRuntimeMessage = (message: { type?: string }) => {
    if (message.type === 'SHOW_PAUSE_MODAL') {
      void modalController.showModal();
    }
  };

  browser.runtime.onMessage.addListener(handleRuntimeMessage);
  cleanupFns.push(() => browser.runtime.onMessage.removeListener(handleRuntimeMessage));

  window.__hulyAntiScrollCleanup = () => {
    cleanupFns.forEach((cleanup) => cleanup());
    modalController.destroyModal();
    if (hulyPage) {
      clearHulyReadyAttribute();
    }
  };
}

init();
