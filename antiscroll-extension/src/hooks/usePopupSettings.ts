import React, { useEffect, useState } from 'react';
import browser from 'webextension-polyfill';
import { getSettings, setSettings, getSessionActive, getToken } from '../utils/storage';
import { pushRemoteSettings } from '../api/client';
import { ExtensionSettings } from '../utils/types';
import { STORAGE_KEYS } from '../utils/constants';

export const PREDEFINED_SITES = [
  { name: 'Twitter / X', domains: ['twitter.com', 'x.com'] },
  { name: 'Instagram', domains: ['instagram.com'] },
  { name: 'TikTok', domains: ['tiktok.com'] },
  { name: 'YouTube', domains: ['youtube.com'] },
  { name: 'Facebook', domains: ['facebook.com'] },
  { name: 'Reddit', domains: ['reddit.com'] },
];

export const usePopupSettings = () => {
  const [settings, setSettingsState] = useState<ExtensionSettings | null>(null);
  const [tokenPresent, setTokenPresent] = useState<boolean>(false);
  const [sessionActive, setSessionActive] = useState<boolean>(false);
  const [mins, setMins] = useState<number>(0);
  const [secs, setSecs] = useState<number>(0);
  const [newDomain, setNewDomain] = useState<string>('');
  const [errorMsg, setErrorMsg] = useState<string>('');
  const [accumulated, setAccumulated] = useState<number>(0);

  useEffect(() => {
    getSettings().then((s) => {
      setSettingsState(s);
      setMins(Math.floor(s.pauseIntervalSeconds / 60));
      setSecs(s.pauseIntervalSeconds % 60);
    });

    getToken().then((token) => {
      setTokenPresent(!!token);
    });
    getSessionActive().then((sessionActive) => {
      setSessionActive(sessionActive);
    });

    browser.storage.local.get(STORAGE_KEYS.ACCUMULATED_SCROLL_TIME).then((data) => {
      setAccumulated(data[STORAGE_KEYS.ACCUMULATED_SCROLL_TIME] || 0);
    });

    const handleStorageChange = (changes: any) => {
      if (changes[STORAGE_KEYS.ACCUMULATED_SCROLL_TIME]) {
        setAccumulated(changes[STORAGE_KEYS.ACCUMULATED_SCROLL_TIME].newValue || 0);
      }
      if (changes[STORAGE_KEYS.SETTINGS]) {
        const newSettings = changes[STORAGE_KEYS.SETTINGS].newValue;
        if (newSettings) {
          setSettingsState(newSettings);
          setMins(Math.floor(newSettings.pauseIntervalSeconds / 60));
          setSecs(newSettings.pauseIntervalSeconds % 60);
        }
      }
      if (changes[STORAGE_KEYS.TOKEN]) {
        setTokenPresent(!!changes[STORAGE_KEYS.TOKEN].newValue);
      }
      if (changes[STORAGE_KEYS.SESSION_ACTIVE]) {
        setSessionActive(!!changes[STORAGE_KEYS.SESSION_ACTIVE].newValue);
      }
    };

    browser.storage.onChanged.addListener(handleStorageChange);
    return () => {
      browser.storage.onChanged.removeListener(handleStorageChange);
    };
  }, []);

  const saveAndSync = async (newS: ExtensionSettings) => {
    setSettingsState(newS);
    await setSettings(newS);
    await pushRemoteSettings(newS);
  };

  const handleToggle = async () => {
    if (!settings) return;
    const newEnabled = !settings.enabled;
    const updated = { ...settings, enabled: newEnabled };
    await saveAndSync(updated);
  };

  const updateInterval = async (m: number, s: number) => {
    if (!settings) return;
    const totalSeconds = (m * 60) + s;
    const updated = { ...settings, pauseIntervalSeconds: totalSeconds };
    await saveAndSync(updated);
  };

  const handleMinChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = parseInt(e.target.value, 10) || 0;
    setMins(val);
    updateInterval(val, secs);
  };

  const handleSecChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = Math.min(59, parseInt(e.target.value, 10) || 0);
    setSecs(val);
    updateInterval(mins, val);
  };

  const isPredefinedEnabled = (domains: string[]) => {
    if (!settings) return false;
    return domains.every(d => settings.monitoredDomains.includes(d));
  };

  const handleTogglePredefined = async (domains: string[]) => {
    if (!settings) return;
    const enabled = isPredefinedEnabled(domains);
    let updatedDomains: string[];
    if (enabled) {
      updatedDomains = settings.monitoredDomains.filter(d => !domains.includes(d));
    } else {
      updatedDomains = [...settings.monitoredDomains];
      domains.forEach(d => {
        if (!updatedDomains.includes(d)) {
          updatedDomains.push(d);
        }
      });
    }
    const updated = { ...settings, monitoredDomains: updatedDomains };
    await saveAndSync(updated);
  };

  const getCustomDomains = () => {
    if (!settings) return [];
    const predefinedFlat = PREDEFINED_SITES.flatMap(site => site.domains);
    return settings.monitoredDomains.filter(d => !predefinedFlat.includes(d));
  };

  const handleAddCustomDomain = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!settings || !newDomain.trim()) return;

    let domain = newDomain.trim().toLowerCase();
    domain = domain.replace(/^(https?:\/\/)?(www\.)?/, '');
    if (!domain.includes('.') || domain.length < 4) {
      setErrorMsg('Dominio inválido. Ej: reddit.com');
      return;
    }
    setErrorMsg('');

    if (settings.monitoredDomains.includes(domain)) {
      setNewDomain('');
      return;
    }

    const updatedDomains = [...settings.monitoredDomains, domain];
    const updated = { ...settings, monitoredDomains: updatedDomains };
    await saveAndSync(updated);
    setNewDomain('');
  };

  const handleRemoveCustomDomain = async (domain: string) => {
    if (!settings) return;
    const updatedDomains = settings.monitoredDomains.filter(d => d !== domain);
    const updated = { ...settings, monitoredDomains: updatedDomains };
    await saveAndSync(updated);
  };

  return {
    settings,
    isLoggedIn: tokenPresent || sessionActive,
    mins,
    secs,
    newDomain,
    setNewDomain,
    errorMsg,
    setErrorMsg,
    accumulated,
    handleToggle,
    handleMinChange,
    handleSecChange,
    isPredefinedEnabled,
    handleTogglePredefined,
    getCustomDomains,
    handleAddCustomDomain,
    handleRemoveCustomDomain,
  };
};
