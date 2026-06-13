import React from 'react';
import ReactDOM from 'react-dom/client';
import browser from 'webextension-polyfill';
import PauseModal from '../components/PauseModal/PauseModal';
import { getSettings } from '../utils/storage';
import { STORAGE_KEYS } from '../utils/constants';
import { ExtensionSettings } from '../utils/types';

const DOMAIN = window.location.hostname;
let activeInterval: any = null;
let modalRoot: ReactDOM.Root | null = null;
let shadowContainer: HTMLDivElement | null = null;

let settings: ExtensionSettings | null = null;
let lastScrollTime = 0;
let originalHtmlOverflow = '';
let originalBodyOverflow = '';

async function init() {
  settings = await getSettings();

  const isHulyPage = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' || window.location.origin.includes('huly-tpi-frontend');

  if (isHulyPage) {
    console.log('[Huly Anti-Scroll] Content script initialized on Huly page.');
    document.documentElement.setAttribute('data-huly-antiscroll-installed', 'true');
    document.documentElement.setAttribute('data-huly-antiscroll-enabled', settings?.enabled ? 'true' : 'false');
    document.documentElement.setAttribute('data-huly-antiscroll-consent', settings?.dataSharingConsent ? 'true' : 'false');
    if (browser.runtime.id) {
      document.documentElement.setAttribute('data-huly-antiscroll-id', browser.runtime.id);
    }


  }

  browser.storage.onChanged.addListener((changes) => {
    if (changes[STORAGE_KEYS.SETTINGS]) {
      settings = { ...settings, ...changes[STORAGE_KEYS.SETTINGS].newValue } as ExtensionSettings;
      if (isHulyPage && settings) {
        document.documentElement.setAttribute('data-huly-antiscroll-enabled', settings.enabled ? 'true' : 'false');
        document.documentElement.setAttribute('data-huly-antiscroll-consent', settings.dataSharingConsent ? 'true' : 'false');
      }
    }
  });

  if (isHulyPage) {
    const token = sessionStorage.getItem('huly:token');
    if (token) {
      console.log('[Huly Anti-Scroll] Found token on page load, sending to background.');
      browser.runtime.sendMessage({ type: 'SET_TOKEN', data: { token } }).catch(e => console.error('[Huly Anti-Scroll] Send token error:', e));
    }

    window.addEventListener('huly:session', (event: any) => {
      const t = event.detail?.token || null;
      console.log('[Huly Anti-Scroll] Received huly:session event, token present:', !!t);
      browser.runtime.sendMessage({ type: 'SET_TOKEN', data: { token: t } }).catch(e => console.error('[Huly Anti-Scroll] Send session token error:', e));
    });
  }

  setupTracking();
  listenForMessages();
}

const isMonitored = () => {
  if (!settings || !settings.enabled) return false;
  return settings.monitoredDomains.some(d => DOMAIN === d || DOMAIN.endsWith('.' + d));
};

function setupTracking() {
  let scrolled = false;

  window.addEventListener('scroll', () => {
    lastScrollTime = Date.now();
    if (isMonitored() && !scrolled && !shadowContainer) {
      browser.runtime.sendMessage({ type: 'SCROLL', domain: DOMAIN });
      scrolled = true;
      setTimeout(() => { scrolled = false; }, 2000);
    }
  }, { passive: true });

  const startTracking = () => {
    if (activeInterval) return;
    activeInterval = setInterval(() => {
      if (document.visibilityState === 'visible' && Date.now() - lastScrollTime < 2000 && isMonitored() && !shadowContainer) {
        browser.runtime.sendMessage({ type: 'TICK_ACTIVE', domain: DOMAIN });
      }
    }, 1000);
  };

  const stopTracking = () => {
    if (activeInterval) {
      clearInterval(activeInterval);
      activeInterval = null;
    }
  };

  if (document.visibilityState === 'visible') {
    startTracking();
  }

  document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'visible') {
      startTracking();
    } else {
      stopTracking();
    }
  });
}

function listenForMessages() {
  browser.runtime.onMessage.addListener((message) => {
    if (message.type === 'SHOW_PAUSE_MODAL') {
      showModal();
    }
  });
}

async function showModal() {
  if (shadowContainer) return;

  const settings = await getSettings();
  

  if (!document.getElementById('huly-font-link')) {
    const fontLink = document.createElement('link');
    fontLink.id = 'huly-font-link';
    fontLink.rel = 'stylesheet';
    fontLink.href = 'https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800&display=swap';
    document.head.appendChild(fontLink);
  }

  originalHtmlOverflow = document.documentElement.style.overflow;
  originalBodyOverflow = document.body.style.overflow;
  document.documentElement.style.overflow = 'hidden';
  document.body.style.overflow = 'hidden';

  shadowContainer = document.createElement('div');
  shadowContainer.id = 'huly-pause-root';
  shadowContainer.style.position = 'fixed';
  shadowContainer.style.zIndex = '2147483647';
  shadowContainer.style.top = '0';
  shadowContainer.style.left = '0';
  shadowContainer.style.width = '100vw';
  shadowContainer.style.height = '100vh';
  shadowContainer.style.pointerEvents = 'none';
  document.body.appendChild(shadowContainer);

  const shadow = shadowContainer.attachShadow({ mode: 'open' });
  
  const regularFontUrl = browser.runtime.getURL('fonts/Nunito-Regular.ttf');
  const boldFontUrl = browser.runtime.getURL('fonts/Nunito-Bold.ttf');

  const style = document.createElement('style');
  style.textContent = `
    @font-face {
      font-family: 'Nunito';
      src: url('${regularFontUrl}') format('truetype');
      font-weight: 400;
      font-style: normal;
    }
    @font-face {
      font-family: 'Nunito';
      src: url('${boldFontUrl}') format('truetype');
      font-weight: 700;
      font-style: normal;
    }
    
    .huly-modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      width: 100vw;
      height: 100vh;
      background: rgba(26, 16, 64, 0.4);
      backdrop-filter: blur(8px);
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: 'Nunito', sans-serif;
      padding: 1rem;
      box-sizing: border-box;
    }
    .huly-modal-content {
      background: #ffffff;
      padding: 2.5rem 2rem;
      border-radius: 2rem;
      box-shadow: 0 20px 50px rgba(26, 16, 64, 0.15);
      max-width: 380px;
      width: 100%;
      text-align: center;
      box-sizing: border-box;
      border: 1px solid rgba(136, 105, 172, 0.1);
    }
    .huly-modal-logo {
      height: 3.2rem;
      margin-bottom: 1.5rem;
      display: block;
      margin-left: auto;
      margin-right: auto;
      object-fit: contain;
    }
    .huly-modal-title {
      font-size: 1.5rem;
      font-weight: 800;
      margin-bottom: 1rem;
      color: #8869AC;
      margin-top: 0;
      letter-spacing: -0.02em;
    }
    .huly-modal-text {
      font-size: 1rem;
      color: #5c5c8a;
      margin-bottom: 2rem;
      line-height: 1.6;
      font-weight: 600;
    }
    .huly-modal-actions {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .huly-btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 9999px;
      font-weight: 500;
      line-height: 1.2;
      cursor: pointer;
      border: 1px solid transparent;
      transition: background-color 0.3s ease-in-out, border-color 0.3s ease-in-out, color 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
      font-size: 0.95rem;
      width: 100%;
      padding: 0.85rem 1.35rem;
      font-family: 'Nunito', sans-serif;
      box-sizing: border-box;
    }
    .huly-btn-primary {
      background: #8869AC;
      color: white;
      border-color: transparent;
    }
    .huly-btn-primary:hover {
      box-shadow: inset 0 0 0 9999px rgba(0, 0, 0, 0.12);
    }
    .huly-btn-secondary {
      background: transparent;
      color: #8869AC;
      border-color: #8869AC;
    }
    .huly-btn-secondary:hover {
      box-shadow: inset 0 0 0 9999px rgba(0, 0, 0, 0.08);
    }
  `;
  shadow.appendChild(style);

  const rootElement = document.createElement('div');
  rootElement.style.pointerEvents = 'auto';
  rootElement.style.width = '100%';
  rootElement.style.height = '100%';
  shadow.appendChild(rootElement);

  modalRoot = ReactDOM.createRoot(rootElement);
  
  browser.runtime.sendMessage({ type: 'MODAL_SHOWN', domain: DOMAIN });

  modalRoot.render(
    <PauseModal 
      onClose={() => {
        destroyModal();
        browser.runtime.sendMessage({ type: 'RESET_TIMER' });
      }}
      onRedirect={() => {
        browser.runtime.sendMessage({ type: 'REDIRECT_CLICKED', domain: DOMAIN });
        window.location.href = settings.gardenUrl;
      }}
    />
  );
}

function destroyModal() {
  if (modalRoot) {
    modalRoot.unmount();
    modalRoot = null;
  }
  if (shadowContainer) {
    shadowContainer.remove();
    shadowContainer = null;
  }
  document.documentElement.style.overflow = originalHtmlOverflow || '';
  document.body.style.overflow = originalBodyOverflow || '';
}

init();
