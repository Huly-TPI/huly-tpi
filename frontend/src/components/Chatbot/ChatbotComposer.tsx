import { useState } from 'react'
import Button from '../Buttons/Button/Button'
import ChatbotAudioRecorder from './ChatbotAudioRecorder'
import { useEffect, useRef } from 'react'
import { SendHorizontal } from 'lucide-react'

interface ChatbotComposerProps {
  input: string
  isSending: boolean
  onInputChange: (value: string) => void
  onSend: () => void
  onSendAudio: (blob: Blob) => void
}

export default function ChatbotComposer({
  input,
  isSending,
  onInputChange,
  onSend,
  onSendAudio
}: ChatbotComposerProps) {
  const [isRecorderActive, setIsRecorderActive] = useState(false)
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  useEffect(() => {
    const timer = setTimeout(() => {
      textareaRef.current?.focus()
    }, 50)
    return () => clearTimeout(timer)
  }, [])

  return (
    <footer className="border-t border-[var(--border-soft)] px-5 py-5">
      <div className="flex h-14 items-center gap-2">
        {!isRecorderActive && (
          <>
            <textarea
              ref={textareaRef}
              rows={2}
              value={input}
              onChange={event => onInputChange(event.target.value)}
              placeholder="Escribí tu mensaje..."
              onKeyDown={event => {
                if (event.key === 'Enter' && !event.shiftKey) {
                  event.preventDefault()
                  onSend()
                }
              }}
              disabled={isSending}
              className="h-14 w-full flex-1 resize-none rounded-xl border border-[var(--border-soft)] bg-[var(--surface-secondary)] px-3 py-4 text-sm text-[var(--text-primary)] outline-none placeholder:text-[var(--text-muted)] focus:border-violeta"
            />
          </>
        )}
        <ChatbotAudioRecorder
          onSend={onSendAudio}
          disabled={isSending}
          onActiveChange={setIsRecorderActive}
        />
        {!isRecorderActive && (
          <Button
            type="button"
            onClick={onSend}
            disabled={isSending || !input.trim()}
            variant="primary"
            className="h-10 w-10 shrink-0 !min-w-0 !rounded-full !px-0 !py-0 !transition-[background-color,color,opacity,box-shadow] !duration-300 !ease-in-out max-md:!w-10"
            aria-label="Enviar"
            title="Enviar"
          >
            <SendHorizontal className="h-4 w-4" strokeWidth={2} />
          </Button>
        )}
      </div>
    </footer>
  )
}
