export interface ExtensionSettings {
  enabled: boolean;
  pauseIntervalSeconds: number;
  gardenUrl: string;
  backendUrl: string;
  monitoredDomains: string[];
  dataSharingConsent: boolean;
  userName?: string;
}

export interface DomainMetric {
  domain: string;
  activeSeconds: number;
  scrollCount: number;
  modalsShown: number;
  redirects: number;
}

export interface MetricUpdate {
  activeSeconds?: number;
  scrollCount?: number;
  modalsShown?: number;
  redirects?: number;
}
