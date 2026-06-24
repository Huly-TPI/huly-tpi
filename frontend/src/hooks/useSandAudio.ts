import { useCallback, useEffect, useRef } from 'react'
import { useAudioSettings } from '../context/audioSettings'

interface SandAudioNodes {
  filter: BiquadFilterNode
  gain: GainNode
  source: AudioBufferSourceNode
}

const createNoiseBuffer = (context: AudioContext) => {
  const frameCount = context.sampleRate * 2
  const buffer = context.createBuffer(1, frameCount, context.sampleRate)
  const data = buffer.getChannelData(0)
  let previousSample = 0

  for (let index = 0; index < frameCount; index += 1) {
    const whiteNoise = Math.random() * 2 - 1
    previousSample = previousSample * 0.72 + whiteNoise * 0.28
    data[index] = previousSample
  }

  return buffer
}

export const useSandAudio = () => {
  const { minigameVolume } = useAudioSettings()
  const contextRef = useRef<AudioContext | null>(null)
  const nodesRef = useRef<SandAudioNodes | null>(null)
  const effectiveVolume = minigameVolume

  const stop = useCallback(() => {
    const nodes = nodesRef.current
    const context = contextRef.current
    if (!nodes || !context) return

    const now = context.currentTime
    nodes.gain.gain.cancelScheduledValues(now)
    nodes.gain.gain.setValueAtTime(nodes.gain.gain.value, now)
    nodes.gain.gain.exponentialRampToValueAtTime(0.0001, now + 0.06)
    nodes.source.stop(now + 0.07)
    nodes.source.onended = () => {
      nodes.source.disconnect()
      nodes.filter.disconnect()
      nodes.gain.disconnect()
    }
    nodesRef.current = null
  }, [])

  const start = useCallback(() => {
    if (effectiveVolume <= 0) return
    if (nodesRef.current) return

    const context = contextRef.current ?? new AudioContext()
    contextRef.current = context

    if (context.state === 'suspended') {
      void context.resume()
    }

    const source = context.createBufferSource()
    const filter = context.createBiquadFilter()
    const gain = context.createGain()
    const now = context.currentTime

    source.buffer = createNoiseBuffer(context)
    source.loop = true
    filter.type = 'bandpass'
    filter.frequency.setValueAtTime(720, now)
    filter.Q.setValueAtTime(0.7, now)
    gain.gain.setValueAtTime(0.0001, now)
    gain.gain.exponentialRampToValueAtTime(0.018 * effectiveVolume, now + 0.05)

    source.connect(filter)
    filter.connect(gain)
    gain.connect(context.destination)
    source.start()

    nodesRef.current = { source, filter, gain }
  }, [effectiveVolume])

  const update = useCallback((speed: number) => {
    const nodes = nodesRef.current
    const context = contextRef.current
    if (!nodes || !context) return

    const normalizedSpeed = Math.min(Math.max(speed / 1.4, 0), 1)
    const now = context.currentTime
    const frequency = 620 + normalizedSpeed * 780
    const volume = (0.012 + normalizedSpeed * 0.024) * effectiveVolume

    nodes.filter.frequency.setTargetAtTime(frequency, now, 0.035)
    nodes.gain.gain.setTargetAtTime(Math.max(0.0001, volume), now, 0.025)
  }, [effectiveVolume])

  useEffect(() => {
    if (effectiveVolume <= 0) {
      stop()
      return
    }

    const nodes = nodesRef.current
    const context = contextRef.current
    if (!nodes || !context) return

    const now = context.currentTime
    nodes.gain.gain.setTargetAtTime(Math.max(0.0001, 0.018 * effectiveVolume), now, 0.04)
  }, [effectiveVolume, stop])

  useEffect(
    () => () => {
      stop()
      const context = contextRef.current
      if (context && context.state !== 'closed') {
        void context.close()
      }
    },
    [stop],
  )

  return { start, stop, update }
}
