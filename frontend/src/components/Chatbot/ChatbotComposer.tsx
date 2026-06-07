import Button from '../Buttons/Button/Button'

interface ChatbotComposerProps {
  input: string
  isSending: boolean
  onInputChange: (value: string) => void
  onSend: () => void
}

export default function ChatbotComposer({ input, isSending, onInputChange, onSend }: ChatbotComposerProps) {
  return (
    <footer className="border-t border-[var(--border-soft)] px-5 py-5">
      <div className="flex items-end gap-2">
        <textarea
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
