import { TrashIcon } from '@heroicons/react/24/outline'
import AudioMessagePlayer from './AudioMessagePlayer'

interface ChatMessageBubbleProps {
  role: 'user' | 'assistant'
  content: string
  audioUrl?: string
  isAudioMessage?: boolean
  onDelete?: () => void
}

export default function ChatMessageBubble({
  role,
  content,
  audioUrl,
  isAudioMessage,
  onDelete,
}: ChatMessageBubbleProps) {
  if (role === 'user' && isAudioMessage) {
    return (
      <div className="group relative max-w-[85%]">
        <div className="rounded-2xl rounded-br-sm bg-violeta px-4 py-3">
          <AudioMessagePlayer audioUrl={audioUrl} />
        </div>
        {onDelete && (
          <button
            type="button"
            onClick={onDelete}
            aria-label="Eliminar mensaje de voz"
            className="absolute -top-2 -right-2 flex h-6 w-6 items-center justify-center rounded-full bg-white shadow-md opacity-0 transition-opacity group-hover:opacity-100 hover:bg-red-50"
          >
            <TrashIcon className="h-3.5 w-3.5 text-red-500" />
          </button>
        )}
      </div>
    )
  }

  return (
    <div
      className={`max-w-[85%] rounded-2xl px-4 py-2.5 text-[15px] leading-relaxed md:text-sm ${
        role === 'user'
          ? 'rounded-br-sm bg-violeta text-white'
          : 'rounded-bl-sm bg-[var(--surface-primary)] text-[var(--text-primary)] shadow-sm'
      }`}
    >
      {content}
    </div>
  )
}
