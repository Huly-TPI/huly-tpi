import Button from '../Buttons/Button/Button'

interface ChatbotComposerProps {
  input: string
  isSending: boolean
  onInputChange: (value: string) => void
  onSend: () => void
}

export default function ChatbotComposer({ input, isSending, onInputChange, onSend }: ChatbotComposerProps) {
  return (
    <footer className="border-t border-gray-100 px-5 py-5">
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
          className="min-h-[44px] w-full flex-1 resize-none rounded-xl border border-gray-200 px-3 py-3 text-sm outline-none focus:border-violeta md:min-h-[52px]"
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
