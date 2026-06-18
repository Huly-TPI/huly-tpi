import { ExtensionSettings } from './types';
import { buildAppOrigins, resolveExtensionEnvironment, trimTrailingSlash } from './runtimeConfig';

export const LEGACY_LOCAL_FRONTEND_URL = 'http://localhost:5173';
export const LEGACY_LOCAL_BACKEND_URL = 'http://localhost:8080';
export const PROD_FRONTEND_URL = 'https://huly-tpi-frontend.onrender.com';
export const PROD_BACKEND_URL = 'https://huly-tpi.onrender.com';

const extensionEnvironment = resolveExtensionEnvironment(
  typeof chrome !== 'undefined' ? chrome.runtime?.id : undefined,
  import.meta.env.PROD,
);

const defaultFrontendUrl = trimTrailingSlash(
  import.meta.env.VITE_HULY_FRONTEND_URL ||
    (extensionEnvironment === 'prod' ? PROD_FRONTEND_URL : LEGACY_LOCAL_FRONTEND_URL),
);

const defaultBackendUrl = trimTrailingSlash(
  import.meta.env.VITE_HULY_BACKEND_URL ||
    (extensionEnvironment === 'prod' ? PROD_BACKEND_URL : LEGACY_LOCAL_BACKEND_URL),
);

const configuredOrigins = (import.meta.env.VITE_HULY_APP_ORIGINS || '')
  .split(',')
  .map((origin: string) => trimTrailingSlash(origin.trim()))
  .filter(Boolean);

export const HULY_APP_ORIGINS = buildAppOrigins(
  defaultFrontendUrl,
  configuredOrigins,
);

export const DEFAULT_SETTINGS: ExtensionSettings = {
  enabled: true,
  pauseIntervalSeconds: 1200,
  gardenUrl: `${defaultFrontendUrl}/`,
  backendUrl: defaultBackendUrl,
  monitoredDomains: ['twitter.com', 'x.com', 'instagram.com', 'tiktok.com', 'youtube.com', 'facebook.com'],
  dataSharingConsent: false,
  userName: '',
};

export const STORAGE_KEYS = {
  SETTINGS: 'huly_settings',
  METRICS: 'huly_metrics',
  TOKEN: 'huly_token',
  SESSION_ACTIVE: 'huly_session_active',
  ACTIVE_USER_ID: 'huly_active_user_id',
  USER_SCROLL_TIMES: 'huly_user_scroll_times',
  ACCUMULATED_SCROLL_TIME: 'huly_accumulated_scroll_time',
} as const;

export const ALARM_KEYS = {
  SYNC_METRICS: 'huly_sync_metrics',
  FETCH_SETTINGS: 'huly_fetch_settings',
} as const;
