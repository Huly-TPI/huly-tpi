import { getMetrics, setMetrics } from './storage';
import { MetricUpdate } from './types';
import { normalizeDomain } from './domain';

export const updateMetric = async (domain: string, update: MetricUpdate) => {
  const metrics = await getMetrics();
  const normalizedDomain = normalizeDomain(domain);
  
  if (!metrics[normalizedDomain]) {
    metrics[normalizedDomain] = {
      domain: normalizedDomain,
      activeSeconds: 0,
      scrollCount: 0,
      modalsShown: 0,
      redirects: 0,
    };
  }

  const m = metrics[normalizedDomain];
  if (update.activeSeconds) m.activeSeconds += update.activeSeconds;
  if (update.scrollCount) m.scrollCount += update.scrollCount;
  if (update.modalsShown) m.modalsShown += update.modalsShown;
  if (update.redirects) m.redirects += update.redirects;

  await setMetrics(metrics);
};
