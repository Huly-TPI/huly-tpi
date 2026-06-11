import { ExtensionSettings } from './types';

export const DEFAULT_SETTINGS: ExtensionSettings = {
  enabled: true,
  pauseIntervalSeconds: 1200, // 20 minutes default
  gardenUrl: 'http://localhost:5173/garden', // Default dev URL
  backendUrl: 'http://localhost:8080',
  monitoredDomains: ['twitter.com', 'x.com', 'instagram.com', 'tiktok.com', 'youtube.com', 'facebook.com'],
};

export const STORAGE_KEYS = {
  SETTINGS: 'huly_settings',
  METRICS: 'huly_metrics',
  TOKEN: 'huly_token',
  ACCUMULATED_SCROLL_TIME: 'huly_accumulated_scroll_time',
} as const;

export const ALARM_KEYS = {
  SYNC_METRICS: 'huly_sync_metrics',
  FETCH_SETTINGS: 'huly_fetch_settings',
} as const;
