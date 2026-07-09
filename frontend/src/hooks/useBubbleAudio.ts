import { useCallback, useEffect, useRef } from 'react'
import { useAudioSettings } from '../context/audioSettings'
import bubblePopSound1 from '../assets/audio/minigame/bubble-pop.mp3'
import bubblePopSound2 from '../assets/audio/minigame/bubble-pop-2.mp3'

const isTestEnvironment = import.meta.env.MODE === 'test'

const bubblePopSounds = [bubblePopSound1, bubblePopSound2]

const getRandomPopSound = () =>
  bubblePopSounds[Math.floor(Math.random() * bubblePopSounds.length)]

export const useBubbleAudio = () => {
  const { minigameVolume } = useAudioSettings()
  const volumeRef = useRef(minigameVolume)

  useEffect(() => {
    volumeRef.current = minigameVolume
  }, [minigameVolume])

  const playPop = useCallback(() => {
    const volume = volumeRef.current
    if (volume <= 0 || isTestEnvironment) return

    const audio = new Audio(getRandomPopSound())
    audio.volume = volume

    try {
      const playResult = audio.play()
      if (playResult) {
        void playResult.catch(() => undefined)
      }
    } catch {
      // Ignored: Browser blocks autoplay or unsupported media playback.
    }
  }, [])

  return { playPop }
}
