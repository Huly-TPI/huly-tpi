import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import { useLocation } from 'react-router-dom'
import type { AudioSettings as ApiAudioSettings, UserProfile } from '../api/auth'
import { updateAudioSettings } from '../api/auth'
import interfaceClickSound1 from '../assets/audio/ui/interface-click1.mp3'
import interfaceClickSound2 from '../assets/audio/ui/interface-click2.mp3'
import gardenLoopSound1 from '../assets/audio/ambient/garden-loop1.mp3'
import gardenLoopSound2 from '../assets/audio/ambient/garden-loop2.mp3'
import gardenLoopSound3 from '../assets/audio/ambient/garden-loop3.mp3'

export interface AudioSettings {
  interfaceVolume: number
  ambientVolume: number
  minigameVolume: number
}

interface AudioSettingsContextValue extends AudioSettings {
  setInterfaceVolume: (volume: number) => void
  setAmbientVolume: (volume: number) => void
  setMinigameVolume: (volume: number) => void
}

const DEFAULT_AUDIO_SETTINGS: AudioSettings = {
  interfaceVolume: 0.7,
  ambientVolume: 0.1,
  minigameVolume: 1,
}

const interfaceClickSounds = [interfaceClickSound1, interfaceClickSound2]
const gardenLoopSounds = [gardenLoopSound1, gardenLoopSound2, gardenLoopSound3]

const clampVolume = (volume: number) => Math.min(1, Math.max(0, volume))
const isTestEnvironment = import.meta.env.MODE === 'test'

const audioChannel = !isTestEnvironment && typeof BroadcastChannel !== 'undefined'
  ? new BroadcastChannel('huly-audio-channel'): null

const playAudio = (audio: HTMLAudioElement) => {
  if (isTestEnvironment) return

  try {
    const playResult = audio.play()
    if (playResult) {
      void playResult.catch(() => undefined)
    }
  } catch {
    // Browsers can block playback until user interaction; tests may not implement media playback.
  }
}

const pauseAudio = (audio: HTMLAudioElement) => {
  if (isTestEnvironment) return

  try {
    audio.pause()
  } catch {
    // jsdom does not fully implement HTML media playback.
  }
}

const isInteractiveClickTarget = (target: EventTarget | null) => {
  if (!(target instanceof Element)) return false
  const interactiveElement = target.closest('button, a, [role="button"], [data-audio-click]')
  if (!(interactiveElement instanceof HTMLElement)) return false
  if (interactiveElement.getAttribute('aria-disabled') === 'true') return false
  if ('disabled' in interactiveElement && interactiveElement.disabled) return false
  return true
}

const getRandomItem = <T,>(items: T[]) => items[Math.floor(Math.random() * items.length)]

const normalizeAudioSettings = (settings?: Partial<ApiAudioSettings> | null): AudioSettings => ({
  interfaceVolume:
    typeof settings?.interfaceVolume === 'number'
      ? clampVolume(settings.interfaceVolume)
      : DEFAULT_AUDIO_SETTINGS.interfaceVolume,
  ambientVolume:
    typeof settings?.ambientVolume === 'number'
      ? clampVolume(settings.ambientVolume)
      : DEFAULT_AUDIO_SETTINGS.ambientVolume,
  minigameVolume:
    typeof settings?.minigameVolume === 'number'
      ? clampVolume(settings.minigameVolume)
      : DEFAULT_AUDIO_SETTINGS.minigameVolume,
})

const defaultContextValue: AudioSettingsContextValue = {
  ...DEFAULT_AUDIO_SETTINGS,
  setInterfaceVolume: () => undefined,
  setAmbientVolume: () => undefined,
  setMinigameVolume: () => undefined,
}

const AudioSettingsContext = createContext<AudioSettingsContextValue>(defaultContextValue)

interface AudioSettingsProviderProps {
  children: ReactNode
}

export function AudioSettingsProvider({ children }: AudioSettingsProviderProps) {
  const [settings, setSettings] = useState<AudioSettings>(DEFAULT_AUDIO_SETTINGS)
  const gardenAudioRef = useRef<HTMLAudioElement | null>(null)
  const hasUserInteractedRef = useRef(false)

  const location = useLocation()
  const isBackoffice = location.pathname.startsWith('/backoffice')

  const tabIdRef = useRef(Math.random().toString(36).substring(2, 11))
  const isMutedByOtherTabRef = useRef(false)

  const playClickFeedback = useCallback(() => {
    if (settings.interfaceVolume <= 0 || isBackoffice) return

    const audio = new Audio(getRandomItem(interfaceClickSounds))
    audio.volume = settings.interfaceVolume
    audio.currentTime = 0

    playAudio(audio)
  }, [settings.interfaceVolume, isBackoffice])

  const syncGardenAmbient = useCallback(() => {
    const audio = gardenAudioRef.current
    if (!audio) return

    audio.volume = settings.ambientVolume

    if (
      settings.ambientVolume <= 0 ||
      !hasUserInteractedRef.current ||
      isBackoffice ||
      document.hidden ||
      isMutedByOtherTabRef.current
    ) {
      pauseAudio(audio)
      return
    }

    playAudio(audio)
    audioChannel?.postMessage({ type: 'PLAY_AMBIENT', senderTabId: tabIdRef.current })
  }, [settings.ambientVolume, isBackoffice])

  useEffect(() => {
    const audio = new Audio(getRandomItem(gardenLoopSounds))
    audio.preload = 'auto'
    const rotateTrack = () => {
      audio.src = getRandomItem(gardenLoopSounds)
      audio.currentTime = 0
      playAudio(audio)
    }
    audio.addEventListener('ended', rotateTrack)
    gardenAudioRef.current = audio

    return () => {
      audio.removeEventListener('ended', rotateTrack)
      pauseAudio(audio)
      gardenAudioRef.current = null
    }
  }, [])

  useEffect(() => {
    const handleUserLoaded = (event: Event) => {
      const profile = (event as CustomEvent<UserProfile>).detail
      setSettings(normalizeAudioSettings(profile.audioSettings))
    }
    const handleUserCleared = () => {
      setSettings(DEFAULT_AUDIO_SETTINGS)
      hasUserInteractedRef.current = false
    }

    window.addEventListener('auth:user-loaded', handleUserLoaded)
    window.addEventListener('auth:user-cleared', handleUserCleared)
    return () => {
      window.removeEventListener('auth:user-loaded', handleUserLoaded)
      window.removeEventListener('auth:user-cleared', handleUserCleared)
    }
  }, [])

  useEffect(() => {
    if (!audioChannel) return

    const handleMessage = (event: MessageEvent) => {
      if (
        event.data &&
        event.data.type === 'PLAY_AMBIENT' &&
        event.data.senderTabId !== tabIdRef.current
      ) {
        isMutedByOtherTabRef.current = true
        syncGardenAmbient()
      }
    }

    audioChannel.addEventListener('message', handleMessage)
    return () => {
      audioChannel.removeEventListener('message', handleMessage)
    }
  }, [syncGardenAmbient])

  useEffect(() => {
    const handleVisibilityChange = () => {
      if (!document.hidden) {
        isMutedByOtherTabRef.current = false
      }
      syncGardenAmbient()
    }

    document.addEventListener('visibilitychange', handleVisibilityChange)
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }, [syncGardenAmbient])

  useEffect(() => {
    syncGardenAmbient()
  }, [syncGardenAmbient])

  useEffect(() => {
    const handleClick = (event: MouseEvent) => {
      if (isBackoffice) return
      isMutedByOtherTabRef.current = false
      hasUserInteractedRef.current = true
      syncGardenAmbient()
      if (!isInteractiveClickTarget(event.target)) return
      playClickFeedback()
    }

    document.addEventListener('click', handleClick)
    return () => document.removeEventListener('click', handleClick)
  }, [playClickFeedback, syncGardenAmbient, isBackoffice])

  const persistSettings = useCallback((nextSettings: AudioSettings) => {
    void updateAudioSettings(nextSettings).catch(() => undefined)
  }, [])

  const updateVolume = useCallback((key: keyof AudioSettings, volume: number) => {
    setSettings(currentSettings => {
      const nextSettings = {
        ...currentSettings,
        [key]: clampVolume(volume),
      }
      persistSettings(nextSettings)
      return nextSettings
    })
  }, [persistSettings])

  const setInterfaceVolume = useCallback((volume: number) => {
    updateVolume('interfaceVolume', volume)
  }, [updateVolume])

  const setAmbientVolume = useCallback((volume: number) => {
    updateVolume('ambientVolume', volume)
  }, [updateVolume])

  const setMinigameVolume = useCallback((volume: number) => {
    updateVolume('minigameVolume', volume)
  }, [updateVolume])

  const value = useMemo<AudioSettingsContextValue>(
    () => ({
      ...settings,
      setInterfaceVolume,
      setAmbientVolume,
      setMinigameVolume,
    }),
    [settings, setInterfaceVolume, setAmbientVolume, setMinigameVolume],
  )

  return <AudioSettingsContext.Provider value={value}>{children}</AudioSettingsContext.Provider>
}

export function useAudioSettings() {
  return useContext(AudioSettingsContext)
}
