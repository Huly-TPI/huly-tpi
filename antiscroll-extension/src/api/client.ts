import { ExtensionSettings, DomainMetric } from '../utils/types';
import { getSettings, getToken, setToken } from '../utils/storage';

let isRefreshing = false;
let refreshQueue: ((token: string | null) => void)[] = [];

const processQueue = (token: string | null) => {
  refreshQueue.forEach(cb => cb(token));
  refreshQueue = [];
};

export const tryToRefreshToken = async (): Promise<string | null> => {
  if (isRefreshing) {
    return new Promise((resolve) => {
      refreshQueue.push(resolve);
    });
  }

  isRefreshing = true;

  try {
    const { backendUrl } = await getSettings();
    const response = await fetch(`${backendUrl}/api/auth/refresh`, {
      method: 'POST',
      credentials: 'include',
    });

    if (response.ok) {
      const data = await response.json();
      const newToken = data.accessToken;
      if (newToken) {
        await setToken(newToken);
        processQueue(newToken);
        isRefreshing = false;
        return newToken;
      }
    }
  } catch (error) {
    console.error('Failed to refresh token in extension:', error);
  }

  await setToken(null);
  processQueue(null);
  isRefreshing = false;
  return null;
};

const getOrRefreshDecoderToken = async (): Promise<string | null> => {
  let token = await getToken();
  if (!token) {
    token = await tryToRefreshToken();
  }
  return token;
};

export const fetchRemoteSettings = async (): Promise<Partial<ExtensionSettings> | null> => {
  try {
    const { backendUrl } = await getSettings();
    let token = await getOrRefreshDecoderToken();
    
    const headers: HeadersInit = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    let response = await fetch(`${backendUrl}/api/extension/settings`, { headers });
    
    if (response.status === 401) {
      const newToken = await tryToRefreshToken();
      if (newToken) {
        const retryHeaders: HeadersInit = {
          'Authorization': `Bearer ${newToken}`
        };
        response = await fetch(`${backendUrl}/api/extension/settings`, { headers: retryHeaders });
      } else {
        return null;
      }
    }
    
    if (!response.ok) return null;
    
    const data = await response.json();
    return {
      enabled: data.enabled,
      pauseIntervalSeconds: data.pauseIntervalMinutes * 60,
      gardenUrl: data.gardenUrl,
      backendUrl: data.backendUrl,
      monitoredDomains: data.monitoredDomains,
    };
  } catch (error) {
    console.error('Failed to fetch settings:', error);
    return null;
  }
};

export const pushRemoteSettings = async (settings: Partial<ExtensionSettings>): Promise<boolean> => {
  try {
    const current = await getSettings();
    const merged = { ...current, ...settings };
    let token = await getOrRefreshDecoderToken();
    
    if (!token) return false;

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    };

    let response = await fetch(`${merged.backendUrl}/api/extension/settings`, {
      method: 'POST',
      headers,
      body: JSON.stringify({
        enabled: merged.enabled,
        pauseIntervalMinutes: Math.floor(merged.pauseIntervalSeconds / 60),
        monitoredDomains: merged.monitoredDomains,
      }),
    });

    if (response.status === 401) {
      const newToken = await tryToRefreshToken();
      if (newToken) {
        const retryHeaders: HeadersInit = {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${newToken}`,
        };
        response = await fetch(`${merged.backendUrl}/api/extension/settings`, {
          method: 'POST',
          headers: retryHeaders,
          body: JSON.stringify({
            enabled: merged.enabled,
            pauseIntervalMinutes: Math.floor(merged.pauseIntervalSeconds / 60),
            monitoredDomains: merged.monitoredDomains,
          }),
        });
      } else {
        return false;
      }
    }

    return response.ok;
  } catch (error) {
    console.error('Failed to push settings:', error);
    return false;
  }
};

export const pushMetrics = async (metrics: DomainMetric[]): Promise<boolean> => {
  try {
    const { backendUrl } = await getSettings();
    let token = await getOrRefreshDecoderToken();
    
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    let response = await fetch(`${backendUrl}/api/extension/metrics`, {
      method: 'POST',
      headers,
      body: JSON.stringify(metrics),
    });

    if (response.status === 401) {
      const newToken = await tryToRefreshToken();
      if (newToken) {
        const retryHeaders: HeadersInit = {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${newToken}`,
        };
        response = await fetch(`${backendUrl}/api/extension/metrics`, {
          method: 'POST',
          headers: retryHeaders,
          body: JSON.stringify(metrics),
        });
      } else {
        return false;
      }
    }

    return response.ok;
  } catch (error) {
    console.error('Failed to push metrics:', error);
    return false;
  }
};
