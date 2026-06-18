import { MicrophoneIcon, PauseIcon, PlayIcon } from '@heroicons/react/24/solid'
import { useEffect, useRef, useState } from 'react'

interface AudioMessagePlayerProps {
  audioUrl?: string
  isLoading?: boolean
}

function formatTime(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

export default function AudioMessagePlayer({ audioUrl, isLoading = false }: AudioMessagePlayerProps) {
  const audioRef = useRef<HTMLAudioElement>(null)
  const [isPlaying, setIsPlaying] = useState(false)
  const [currentTime, setCurrentTime] = useState(0)
  const [duration, setDuration] = useState(0)

  useEffect(() => {
    setIsPlaying(false)
    setCurrentTime(0)
    setDuration(0)
  }, [audioUrl])

  const togglePlay = () => {
    const audio = audioRef.current
    if (!audio) return
    if (isPlaying) {
      audio.pause()
    } else {
      void audio.play()
    }
  }

  const handleTimeUpdate = () => {
    if (audioRef.current) setCurrentTime(audioRef.current.currentTime)
  }

  const handleLoadedMetadata = () => {
    if (audioRef.current) setDuration(audioRef.current.duration)
  }

  const handleEnded = () => {
    setIsPlaying(false)
    setCurrentTime(0)
    if (audioRef.current) audioRef.current.currentTime = 0
  }

  const handleSeek = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = Number(e.target.value)
    setCurrentTime(value)
    if (audioRef.current) audioRef.current.currentTime = value
  }

  const progress = duration > 0 ? currentTime / duration : 0

  if (isLoading) {
    return (
      <div className="flex w-full min-w-[200px] flex-col gap-1.5 py-1">
        <div className="h-2 animate-pulse rounded-full bg-white/30" />
        <div className="flex items-center gap-2">
          <div className="h-7 w-7 animate-pulse rounded-full bg-white/30" />
          <div className="h-3 w-10 animate-pulse rounded bg-white/30" />
        </div>
      </div>
    )
  }

  if (!audioUrl) {
    return (
      <div className="flex items-center gap-2 text-white/80">
        <MicrophoneIcon className="h-4 w-4 shrink-0" />
        <span className="text-sm">Mensaje de voz</span>
      </div>
    )
  }

  return (
    <div className="flex w-full min-w-[200px] flex-col gap-1">
      {/* eslint-disable-next-line jsx-a11y/media-has-caption */}
      <audio
        ref={audioRef}
        src={audioUrl}
        onTimeUpdate={handleTimeUpdate}
        onLoadedMetadata={handleLoadedMetadata}
        onEnded={handleEnded}
        onPlay={() => setIsPlaying(true)}
        onPause={() => setIsPlaying(false)}
      />

      {/* Progress bar */}
      <input
        type="range"
        min={0}
        max={duration || 1}
        step={0.01}
        value={currentTime}
        onChange={handleSeek}
        className="audio-seek-bar h-2 w-full cursor-pointer appearance-none rounded-full"
        style={{
          background: `linear-gradient(to right, white ${progress * 100}%, rgba(255,255,255,0.35) ${progress * 100}%)`,
        }}
        aria-label="Progreso del audio"
      />

      {/* Controls row */}
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={togglePlay}
          aria-label={isPlaying ? 'Pausar' : 'Reproducir'}
          className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-white/20 transition-colors hover:bg-white/30"
        >
          {isPlaying ? (
            <PauseIcon className="h-3.5 w-3.5 text-white" />
          ) : (
            <PlayIcon className="h-3.5 w-3.5 text-white" />
          )}
        </button>
        <span className="text-xs tabular-nums text-white/80">
          {formatTime(currentTime)}{duration > 0 ? `/${formatTime(duration)}` : ''}
        </span>
      </div>
    </div>
  )
}
