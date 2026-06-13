import { getMetrics, setMetrics } from './storage';
import { MetricUpdate } from './types';

export const updateMetric = async (domain: string, update: MetricUpdate) => {
  const metrics = await getMetrics();
  
  if (!metrics[domain]) {
    metrics[domain] = {
      domain,
      activeSeconds: 0,
      scrollCount: 0,
      modalsShown: 0,
      redirects: 0,
    };
  }

  const m = metrics[domain];
  if (update.activeSeconds) m.activeSeconds += update.activeSeconds;
  if (update.scrollCount) m.scrollCount += update.scrollCount;
  if (update.modalsShown) m.modalsShown += update.modalsShown;
  if (update.redirects) m.redirects += update.redirects;

  await setMetrics(metrics);
};
