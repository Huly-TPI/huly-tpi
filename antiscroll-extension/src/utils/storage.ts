import browser from 'webextension-polyfill';
import { ExtensionSettings, DomainMetric } from './types';
import {
  STORAGE_KEYS,
  DEFAULT_SETTINGS,
  LEGACY_LOCAL_BACKEND_URL,
  LEGACY_LOCAL_FRONTEND_URL,
} from './constants';
import { normalizeLegacyUrls } from './runtimeConfig';

const storage = typeof chrome !== 'undefined' && chrome.storage ? chrome.storage.local : browser.storage.local;

export const getSettings = async (): Promise<ExtensionSettings> => {
  try {
    const data = await new Promise<any>((resolve) => {
      storage.get(STORAGE_KEYS.SETTINGS, resolve);
    });
    return normalizeLegacyUrls(
      {
        ...DEFAULT_SETTINGS,
        ...data[STORAGE_KEYS.SETTINGS],
      },
      DEFAULT_SETTINGS,
      LEGACY_LOCAL_FRONTEND_URL,
      LEGACY_LOCAL_BACKEND_URL,
    );
  } catch (error) {
    console.error('Error reading settings:', error);
    return DEFAULT_SETTINGS;
  }
};

export const setSettings = async (settings: Partial<ExtensionSettings>) => {
  const current = await getSettings();
  const newData = { [STORAGE_KEYS.SETTINGS]: { ...current, ...settings } };
  return new Promise<void>((resolve) => {
    storage.set(newData, () => resolve());
  });
};

export const getMetrics = async (): Promise<Record<string, DomainMetric>> => {
  const data = await browser.storage.local.get(STORAGE_KEYS.METRICS);
  return data[STORAGE_KEYS.METRICS] || {};
};

export const setMetrics = async (metrics: Record<string, DomainMetric>) => {
  await browser.storage.local.set({ [STORAGE_KEYS.METRICS]: metrics });
};

export const getToken = async (): Promise<string | null> => {
  const data = await browser.storage.local.get(STORAGE_KEYS.TOKEN);
  return data[STORAGE_KEYS.TOKEN] || null;
};

export const setToken = async (token: string | null) => {
  await browser.storage.local.set({ [STORAGE_KEYS.TOKEN]: token });
};

export const getSessionActive = async (): Promise<boolean> => {
  const data = await browser.storage.local.get(STORAGE_KEYS.SESSION_ACTIVE);
  return data[STORAGE_KEYS.SESSION_ACTIVE] ?? false;
};

export const setSessionActive = async (active: boolean) => {
  await browser.storage.local.set({ [STORAGE_KEYS.SESSION_ACTIVE]: active });
};

type UserScrollTimes = Record<string, number>;

const getUserScrollTimes = async (): Promise<UserScrollTimes> => {
  const data = await browser.storage.local.get(STORAGE_KEYS.USER_SCROLL_TIMES);
  return data[STORAGE_KEYS.USER_SCROLL_TIMES] || {};
};

export const getActiveUserId = async (): Promise<string | null> => {
  const data = await browser.storage.local.get(STORAGE_KEYS.ACTIVE_USER_ID);
  return data[STORAGE_KEYS.ACTIVE_USER_ID] || null;
};

export const setActiveUserId = async (userId: string | null) => {
  await browser.storage.local.set({ [STORAGE_KEYS.ACTIVE_USER_ID]: userId });
};

export const getAccumulatedScrollTime = async (): Promise<number> => {
  const data = await browser.storage.local.get(STORAGE_KEYS.ACCUMULATED_SCROLL_TIME);
  return data[STORAGE_KEYS.ACCUMULATED_SCROLL_TIME] || 0;
};

export const setAccumulatedScrollTime = async (value: number) => {
  const activeUserId = await getActiveUserId();
  const newData: Record<string, unknown> = {
    [STORAGE_KEYS.ACCUMULATED_SCROLL_TIME]: value,
  };

  if (activeUserId) {
    const userScrollTimes = await getUserScrollTimes();
    newData[STORAGE_KEYS.USER_SCROLL_TIMES] = {
      ...userScrollTimes,
      [activeUserId]: value,
    };
  }

  await browser.storage.local.set(newData);
};

export const switchActiveUser = async (userId: string) => {
  const userScrollTimes = await getUserScrollTimes();
  const currentAccumulated = await getAccumulatedScrollTime();
  const hasStoredTime = Object.prototype.hasOwnProperty.call(userScrollTimes, userId);
  const nextAccumulated = hasStoredTime ? userScrollTimes[userId] : currentAccumulated;

  await browser.storage.local.set({
    [STORAGE_KEYS.ACTIVE_USER_ID]: userId,
    [STORAGE_KEYS.ACCUMULATED_SCROLL_TIME]: nextAccumulated,
    [STORAGE_KEYS.USER_SCROLL_TIMES]: {
      ...userScrollTimes,
      [userId]: nextAccumulated,
    },
  });
};

export const resetAccumulatedScrollTime = async () => {
  await browser.storage.local.set({
    [STORAGE_KEYS.ACCUMULATED_SCROLL_TIME]: 0,
  });
};
