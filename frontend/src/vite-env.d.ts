/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_HOME_ONBOARDING_MODE?: 'auto' | 'always' | 'never' | 'true' | 'false'
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
