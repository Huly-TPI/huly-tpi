import { ExtensionSettings } from './types';

export interface RuntimeConfigDetail {
  backendUrl?: string;
  gardenUrl?: string;
}

export const PROD_EXTENSION_ID = 'opafcmdcpkhcfnbfmfipdninpkkpamgh';
export const DEV_EXTENSION_ID = 'kmkblhmalfkbnkipaohgfllbecajkpom';

export const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '');

export const resolveExtensionEnvironment = (
  runtimeId: string | undefined,
  isProdBuild: boolean,
): 'prod' | 'local' => {
  if (runtimeId === PROD_EXTENSION_ID) {
    return 'prod';
  }

  if (runtimeId === DEV_EXTENSION_ID) {
    return 'local';
  }

  return isProdBuild ? 'prod' : 'local';
};

export const buildAppOrigins = (
  defaultFrontendUrl: string,
  configuredOrigins: string[],
): string[] =>
  Array.from(
    new Set([
      defaultFrontendUrl,
      ...configuredOrigins,
    ]),
  );

export const normalizeLegacyUrls = (
  settings: ExtensionSettings,
  defaultSettings: Pick<ExtensionSettings, 'backendUrl' | 'gardenUrl'>,
  legacyLocalFrontendUrl: string,
  legacyLocalBackendUrl: string,
): ExtensionSettings => {
  const migrated = { ...settings };
  const legacyGardenUrl = `${legacyLocalFrontendUrl}/garden`;

  if (
    defaultSettings.backendUrl !== legacyLocalBackendUrl &&
    migrated.backendUrl === legacyLocalBackendUrl
  ) {
    migrated.backendUrl = defaultSettings.backendUrl;
  }

  if (
    defaultSettings.gardenUrl !== legacyGardenUrl &&
    migrated.gardenUrl === legacyGardenUrl
  ) {
    migrated.gardenUrl = defaultSettings.gardenUrl;
  }

  return migrated;
};

export const buildRuntimeConfigPatch = (
  settings: Pick<ExtensionSettings, 'backendUrl' | 'gardenUrl'>,
  config: RuntimeConfigDetail,
): Partial<ExtensionSettings> => {
  const nextSettings: Partial<ExtensionSettings> = {};

  if (config.backendUrl && config.backendUrl !== settings.backendUrl) {
    nextSettings.backendUrl = config.backendUrl;
  }

  if (config.gardenUrl && config.gardenUrl !== settings.gardenUrl) {
    nextSettings.gardenUrl = config.gardenUrl;
  }

  return nextSettings;
};

export const isInjectableHttpTab = (tab: { id?: number; url?: string }): boolean =>
  typeof tab.id === 'number' && /^https?:\/\//.test(tab.url || '');
