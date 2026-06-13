import browser from 'webextension-polyfill';
import { ExtensionSettings, DomainMetric } from './types';
import { STORAGE_KEYS, DEFAULT_SETTINGS } from './constants';

const storage = typeof chrome !== 'undefined' && chrome.storage ? chrome.storage.local : browser.storage.local;

export const getSettings = async (): Promise<ExtensionSettings> => {
  try {
    const data = await new Promise<any>((resolve) => {
      storage.get(STORAGE_KEYS.SETTINGS, resolve);
    });
    return { ...DEFAULT_SETTINGS, ...data[STORAGE_KEYS.SETTINGS] };
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
