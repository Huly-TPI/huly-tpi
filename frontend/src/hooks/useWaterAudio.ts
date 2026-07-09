import { useCallback, useEffect, useRef } from 'react'
import { useAudioSettings } from '../context/audioSettings'
import waterSplashSound1 from '../assets/audio/minigame/water-splash.mp3'
import waterSplashSound2 from '../assets/audio/minigame/water-splash-2.mp3'

const isTestEnvironment = import.meta.env.MODE === 'test'

const waterSplashSounds = [waterSplashSound1, waterSplashSound2]

const getRandomSplashSound = () =>
  waterSplashSounds[Math.floor(Math.random() * waterSplashSounds.length)]

export const useWaterAudio = () => {
  const { minigameVolume } = useAudioSettings()
  const volumeRef = useRef(minigameVolume)

  useEffect(() => {
    volumeRef.current = minigameVolume
  }, [minigameVolume])

  const playDrop = useCallback(() => {
    const volume = volumeRef.current
    if (volume <= 0 || isTestEnvironment) return

    const audio = new Audio(getRandomSplashSound())
    audio.volume = volume

    try {
      const playResult = audio.play()
      if (playResult) {
        void playResult.catch(() => undefined)
      }
    } catch {
    }
  }, [])

  return { playDrop }
}
