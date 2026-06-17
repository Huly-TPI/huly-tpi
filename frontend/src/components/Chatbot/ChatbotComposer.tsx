import { useEffect, useRef } from 'react'
import Button from '../Buttons/Button/Button'
import { FiTrash2 } from 'react-icons/fi'

interface ChatbotComposerProps {
  input: string
  isSending: boolean
  onInputChange: (value: string) => void
  onSend: () => void
  onReset: () => void
}

export default function ChatbotComposer({
  input,
  isSending,
  onInputChange,
  onSend,
  onReset,
}: ChatbotComposerProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  useEffect(() => {
    const timer = setTimeout(() => {
      textareaRef.current?.focus()
    }, 50)
    return () => clearTimeout(timer)
  }, [])

  return (
    <footer className="border-t border-[var(--border-soft)] px-5 py-5">
      <div className="flex items-end gap-2">
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
          className="min-h-[44px] w-full flex-1 resize-none rounded-xl border border-[var(--border-soft)] bg-[var(--surface-secondary)] px-3 py-3 text-sm text-[var(--text-primary)] outline-none placeholder:text-[var(--text-muted)] focus:border-violeta md:min-h-[52px]"
        />
        <button
          type="button"
          onClick={onReset}
          disabled={isSending}
          className="flex h-10 w-10 items-center justify-center rounded-full border border-violeta text-violeta hover:bg-violeta/10 focus-visible:outline focus-visible:outline-[3px] focus-visible:outline-offset-3 focus-visible:outline-[#8869ac59] transition-colors shrink-0 disabled:opacity-50 disabled:cursor-not-allowed mb-[2px]"
          title="Limpiar chat"
          aria-label="Limpiar chat"
        >
          <FiTrash2 className="h-5 w-5" />
        </button>
        <Button
          type="button"
          onClick={onSend}
          disabled={isSending || !input.trim()}
          variant="primary"
          size="sm"
          className="shrink-0 !w-auto !min-w-0"
        >
          Enviar
        </Button>
      </div>
    </footer>
  )
}
