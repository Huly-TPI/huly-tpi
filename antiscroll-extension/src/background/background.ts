import browser from 'webextension-polyfill';
import {
  getAccumulatedScrollTime,
  getSettings,
  resetAccumulatedScrollTime,
  setAccumulatedScrollTime,
  setSettings,
  getMetrics,
  setMetrics,
  setSessionActive,
  setToken,
  switchActiveUser,
} from '../utils/storage';
import { fetchRemoteSettings, pushMetrics } from '../api/client';
import { ALARM_KEYS, STORAGE_KEYS } from '../utils/constants';
import { isInjectableHttpTab } from '../utils/runtimeConfig';
import { updateMetric } from '../utils/metrics';

browser.alarms.create(ALARM_KEYS.SYNC_METRICS, { periodInMinutes: 5 });
browser.alarms.create(ALARM_KEYS.FETCH_SETTINGS, { periodInMinutes: 15 });

const injectIntoOpenWebTabs = async () => {
  const tabs = await browser.tabs.query({});
  const manifest = browser.runtime.getManifest();
  const contentScript = manifest.content_scripts?.[0];

  if (!contentScript?.js?.length) {
    return;
  }

  await Promise.allSettled(
    tabs
      .filter(isInjectableHttpTab)
      .map(async (tab) => {
        const tabId = tab.id as number;

        if (contentScript.css?.length) {
          await browser.scripting.insertCSS({
            target: { tabId },
            files: contentScript.css,
          });
        }

        await browser.scripting.executeScript({
          target: { tabId },
          files: contentScript.js,
        });
      }),
  );
};

browser.runtime.onInstalled.addListener(async (details) => {
  console.log(`Extension Huly Pausa Digital ${details.reason}.`);
  await browser.storage.local.set({ [STORAGE_KEYS.ACCUMULATED_SCROLL_TIME]: 0 });
  await injectIntoOpenWebTabs();
});

async function syncMetricsNow() {
  try {
    const settings = await getSettings();
    if (!settings.dataSharingConsent) {
      console.log('Sync skipped: dataSharingConsent is false.');
      return;
    }

    const metricsMap = await getMetrics();
    const metricsList = Object.values(metricsMap);

    if (metricsList.length > 0) {
      console.log('Syncing metrics immediately...', metricsList);
      const success = await pushMetrics(metricsList);
      if (success) {
        console.log('Metrics synced successfully.');
        await setMetrics({});
      } else {
        console.warn('Metrics sync failed. Will retry later.');
      }
    }
  } catch (error) {
    console.error('Error during metrics sync:', error);
  }
}

syncMetricsNow();

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
    case 'REFRESH_REMOTE_SETTINGS': {
      const remoteSettings = await fetchRemoteSettings();
      if (remoteSettings) {
        await setSettings(remoteSettings);
      }
      break;
    }
    case 'SET_ACTIVE_USER':
      if (data?.userId) {
        await switchActiveUser(String(data.userId));
      }
      break;
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
      await resetAccumulatedScrollTime();
      await syncMetricsNow();
      break;
    case 'SET_TOKEN':
      console.log('[Huly Anti-Scroll BG] Received SET_TOKEN event. Token present:', !!data?.token);
      await setToken(data?.token || null);
      await setSessionActive(!!data?.token);
      if (data?.token) {
        console.log('[Huly Anti-Scroll BG] Fetching remote settings...');
        const remoteSettings = await fetchRemoteSettings();
        if (remoteSettings) {
          console.log('[Huly Anti-Scroll BG] Successfully fetched remote settings:', remoteSettings);
          await setSettings(remoteSettings);
        } else {
          console.warn('[Huly Anti-Scroll BG] Failed to fetch remote settings from backend.');
        }
      } else {
        console.log('[Huly Anti-Scroll BG] Token cleared.');
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

  if (type === 'SET_CONSENT') {
    const consent = message.consent;
    await setSettings({ dataSharingConsent: consent });
    return { success: true, message: `Consent updated to ${consent}` };
  }

  if (type === 'SET_INTERVAL') {
    await setSettings({ pauseIntervalSeconds });
    return { success: true, message: `Interval updated to ${pauseIntervalSeconds}s` };
  }

  if (type === 'PING') {
    return { success: true, version: '1.0.1' };
  }
});

async function checkPauseThreshold() {
  const settings = await getSettings();
  if (!settings.enabled) return;

  const currentScrollTime = (await getAccumulatedScrollTime()) + 1;

  await setAccumulatedScrollTime(currentScrollTime);

  if (currentScrollTime >= settings.pauseIntervalSeconds) {
    await setAccumulatedScrollTime(0);

    const tabs = await browser.tabs.query({ active: true, currentWindow: true });
    if (tabs[0]?.id) {
      browser.tabs.sendMessage(tabs[0].id, { type: 'SHOW_PAUSE_MODAL' });
    }
  }
}
