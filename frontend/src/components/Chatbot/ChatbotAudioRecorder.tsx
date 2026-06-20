import { useRef, useState } from 'react'
import { MicrophoneIcon, StopIcon, TrashIcon } from '@heroicons/react/24/outline'
import { PaperAirplaneIcon } from '@heroicons/react/24/solid'
import AudioMessagePlayer from './AudioMessagePlayer'

type RecorderState = 'idle' | 'recording' | 'recorded'

interface ChatbotAudioRecorderProps {
  onSend: (blob: Blob) => void
  disabled: boolean
  onActiveChange: (active: boolean) => void
}

export default function ChatbotAudioRecorder({ onSend, disabled, onActiveChange }: ChatbotAudioRecorderProps) {
  const MAX_DURATION_SEC = 60

  const [state, setState] = useState<RecorderState>('idle')
  const [audioBlob, setAudioBlob] = useState<Blob | null>(null)
  const [audioUrl, setAudioUrl] = useState<string | null>(null)
  const [secondsLeft, setSecondsLeft] = useState(MAX_DURATION_SEC)
  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const chunksRef = useRef<BlobPart[]>([])
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
      const mediaRecorder = new MediaRecorder(stream)
      mediaRecorderRef.current = mediaRecorder
      chunksRef.current = []

      mediaRecorder.ondataavailable = (e) => {
        if (e.data.size > 0) chunksRef.current.push(e.data)
      }

      mediaRecorder.onstop = () => {
        const blob = new Blob(chunksRef.current, { type: mediaRecorder.mimeType || 'audio/webm' })
        const url = URL.createObjectURL(blob)
        setAudioBlob(blob)
        setAudioUrl(url)
        setState('recorded')
        stream.getTracks().forEach(track => track.stop())
      }

      mediaRecorder.start()
      setState('recording')
      onActiveChange(true)

      setSecondsLeft(MAX_DURATION_SEC)
      timerRef.current = setInterval(() => {
        setSecondsLeft(prev => {
          if (prev <= 1) {
            clearInterval(timerRef.current!)
            timerRef.current = null
            mediaRecorderRef.current?.stop()
            return 0
          }
          return prev - 1
        })
      }, 1000)
    } catch {
      // permiso denegado o API no disponible
    }
  }

  const stopRecording = () => {
    if (timerRef.current) { clearInterval(timerRef.current); timerRef.current = null }
    mediaRecorderRef.current?.stop()
  }

  const discardRecording = () => {
    if (timerRef.current) { clearInterval(timerRef.current); timerRef.current = null }
    if (audioUrl) URL.revokeObjectURL(audioUrl)
    setAudioBlob(null)
    setAudioUrl(null)
    setState('idle')
    onActiveChange(false)
  }

  const handleSend = () => {
    if (!audioBlob) return
    onSend(audioBlob)
    if (audioUrl) URL.revokeObjectURL(audioUrl)
    setAudioBlob(null)
    setAudioUrl(null)
    setState('idle')
    onActiveChange(false)
  }

  if (state === 'idle') {
    return (
      <button
        type="button"
        onClick={() => void startRecording()}
        disabled={disabled}
        aria-label="Grabar audio"
        className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-[var(--text-muted)] transition-[background-color,color,opacity,box-shadow] duration-300 ease-in-out hover:bg-violeta/10 hover:text-violeta focus-visible:outline focus-visible:outline-[3px] focus-visible:outline-offset-3 focus-visible:outline-[#8869ac59] disabled:opacity-50"
      >
        <MicrophoneIcon className="h-5 w-5" />
      </button>
    )
  }

  if (state === 'recording') {
    return (
      <div className="flex h-14 flex-1 items-center gap-3">
        <span className="h-3 w-3 animate-pulse rounded-full bg-red-500" />
        <span className={`flex-1 text-sm tabular-nums ${secondsLeft <= 10 ? 'font-semibold text-red-500' : 'text-[var(--text-muted)]'}`}>
          {`Grabando... ${secondsLeft}s`}
        </span>
        <button
          type="button"
          onClick={stopRecording}
          aria-label="Detener grabación"
          className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-red-500 transition-[background-color,color,opacity,box-shadow] duration-300 ease-in-out hover:bg-red-50 focus-visible:outline focus-visible:outline-[3px] focus-visible:outline-offset-3 focus-visible:outline-[rgba(239,68,68,0.2)]"
        >
          <StopIcon className="h-5 w-5" />
        </button>
      </div>
    )
  }

  return (
    <div className="flex h-14 flex-1 items-center gap-2">
      <div className="min-w-0 flex-1 rounded-xl bg-violeta px-3 py-2">
        <AudioMessagePlayer audioUrl={audioUrl ?? undefined} />
      </div>
      <button
        type="button"
        onClick={discardRecording}
        aria-label="Eliminar grabación"
        className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-[var(--text-muted)] transition-[background-color,color,opacity,box-shadow] duration-300 ease-in-out hover:bg-red-50 hover:text-red-500 focus-visible:outline focus-visible:outline-[3px] focus-visible:outline-offset-3 focus-visible:outline-[rgba(239,68,68,0.2)]"
      >
        <TrashIcon className="h-5 w-5" />
      </button>
      <button
        type="button"
        onClick={handleSend}
        disabled={disabled}
        aria-label="Enviar audio"
        className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-violeta text-white transition-[background-color,color,opacity,box-shadow] duration-300 ease-in-out hover:shadow-[inset_0_0_0_9999px_rgba(0,0,0,0.12)] focus-visible:outline focus-visible:outline-[3px] focus-visible:outline-offset-3 focus-visible:outline-[#8869ac59] disabled:opacity-50"
      >
        <PaperAirplaneIcon className="h-4 w-4 -rotate-45" />
      </button>
    </div>
  )
}
