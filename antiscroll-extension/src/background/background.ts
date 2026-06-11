import browser from 'webextension-polyfill';
import { getSettings, setSettings, getMetrics, setMetrics, setToken } from '../utils/storage';
import { fetchRemoteSettings, pushMetrics } from '../api/client';
import { ALARM_KEYS, STORAGE_KEYS } from '../utils/constants';
import { updateMetric } from '../utils/metrics';

// Register alarms globally (runs when the service worker starts)
browser.alarms.create(ALARM_KEYS.SYNC_METRICS, { periodInMinutes: 5 });
browser.alarms.create(ALARM_KEYS.FETCH_SETTINGS, { periodInMinutes: 15 });

browser.runtime.onInstalled.addListener(async () => {
  console.log('Extensión Huly Pausa Digital instalada.');
  await browser.storage.local.set({ [STORAGE_KEYS.ACCUMULATED_SCROLL_TIME]: 0 });
});

// Implement immediate sync helper
async function syncMetricsNow() {
  try {
    const metricsMap = await getMetrics();
    const metricsList = Object.values(metricsMap);
    
    if (metricsList.length > 0) {
      console.log('Sincronizando métricas inmediatamente...', metricsList);
      const success = await pushMetrics(metricsList);
      if (success) {
        console.log('Métricas sincronizadas exitosamente.');
        await setMetrics({});
      } else {
        console.warn('Fallo al sincronizar métricas. Se reintentará después.');
      }
    }
  } catch (error) {
    console.error('Error durante la sincronización de métricas:', error);
  }
}

// Sync on startup
syncMetricsNow();

// Handle Alarms
browser.alarms.onAlarm.addListener(async (alarm) => {
  if (alarm.name === ALARM_KEYS.SYNC_METRICS) {
    await syncMetricsNow();
  } else if (alarm.name === ALARM_KEYS.FETCH_SETTINGS) {
    const remoteSettings = await fetchRemoteSettings();
    if (remoteSettings) {
      await setSettings(remoteSettings);
    }
  }
});

browser.runtime.onMessage.addListener(async (message) => {
  const { type, domain, data } = message;

  switch (type) {
    case 'TICK_ACTIVE':
      await updateMetric(domain, { activeSeconds: 1 });
      await checkPauseThreshold();
      break;
    case 'SCROLL':
      await updateMetric(domain, { scrollCount: 1 });
      break;
    case 'MODAL_SHOWN':
      await updateMetric(domain, { modalsShown: 1 });
      await syncMetricsNow();
      break;
    case 'REDIRECT_CLICKED':
      await updateMetric(domain, { redirects: 1 });
      await syncMetricsNow();
      break;
    case 'RESET_TIMER':
      await browser.storage.local.set({ [STORAGE_KEYS.ACCUMULATED_SCROLL_TIME]: 0 });
      await syncMetricsNow();
      break;
    case 'SET_TOKEN':
      await setToken(data?.token || null);
      if (data?.token) {
        const remoteSettings = await fetchRemoteSettings();
        if (remoteSettings) {
          await setSettings(remoteSettings);
        }
      }
      await syncMetricsNow();
      break;
  }
});

browser.runtime.onMessageExternal.addListener(async (message, sender) => {
  console.log('Received external message:', message, 'from', sender.url);
  
  const { type, enabled, pauseIntervalSeconds } = message;

  if (type === 'SET_ENABLED') {
    await setSettings({ enabled });
    return { success: true, message: `Extension ${enabled ? 'enabled' : 'disabled'}` };
  }

  if (type === 'SET_INTERVAL') {
    await setSettings({ pauseIntervalSeconds });
    return { success: true, message: `Interval updated to ${pauseIntervalSeconds}s` };
  }

  if (type === 'PING') {
    return { success: true, version: '1.0.0' };
  }
});

async function checkPauseThreshold() {
  const settings = await getSettings();
  if (!settings.enabled) return;

  const data = await browser.storage.local.get(STORAGE_KEYS.ACCUMULATED_SCROLL_TIME);
  const currentScrollTime = (data[STORAGE_KEYS.ACCUMULATED_SCROLL_TIME] || 0) + 1;
  
  await browser.storage.local.set({ [STORAGE_KEYS.ACCUMULATED_SCROLL_TIME]: currentScrollTime });
  
  if (currentScrollTime >= settings.pauseIntervalSeconds) {
    await browser.storage.local.set({ [STORAGE_KEYS.ACCUMULATED_SCROLL_TIME]: 0 });
    
    const tabs = await browser.tabs.query({ active: true, currentWindow: true });
    if (tabs[0]?.id) {
      browser.tabs.sendMessage(tabs[0].id, { type: 'SHOW_PAUSE_MODAL' });
    }
  }
}
