import browser from 'webextension-polyfill';

interface ActivityTrackerOptions {
  domain: string;
  isMonitored: () => boolean;
  isPaused: () => boolean;
}

export const setupActivityTracker = ({
  domain,
  isMonitored,
  isPaused,
}: ActivityTrackerOptions) => {
  let activeInterval: ReturnType<typeof setInterval> | null = null;
  let lastScrollTime = 0;
  let scrolled = false;

  const handleScroll = () => {
    lastScrollTime = Date.now();
    if (isMonitored() && !scrolled && !isPaused()) {
      browser.runtime.sendMessage({ type: 'SCROLL', domain });
      scrolled = true;
      setTimeout(() => {
        scrolled = false;
      }, 2000);
    }
  };

  const startTracking = () => {
    if (activeInterval) return;
    activeInterval = setInterval(() => {
      if (
        document.visibilityState === 'visible' &&
        Date.now() - lastScrollTime < 2000 &&
        isMonitored() &&
        !isPaused()
      ) {
        browser.runtime.sendMessage({ type: 'TICK_ACTIVE', domain });
      }
    }, 1000);
  };

  const stopTracking = () => {
    if (!activeInterval) return;
    clearInterval(activeInterval);
    activeInterval = null;
  };

  const handleVisibilityChange = () => {
    if (document.visibilityState === 'visible') {
      startTracking();
    } else {
      stopTracking();
    }
  };

  window.addEventListener('scroll', handleScroll, { passive: true });
  document.addEventListener('visibilitychange', handleVisibilityChange);

  if (document.visibilityState === 'visible') {
    startTracking();
  }

  return () => {
    stopTracking();
    window.removeEventListener('scroll', handleScroll);
    document.removeEventListener('visibilitychange', handleVisibilityChange);
  };
};
