interface ChatMessageBubbleProps {
  role: 'user' | 'assistant'
  content: string
  audioUrl?: string
}

export default function ChatMessageBubble({ role, content, audioUrl }: ChatMessageBubbleProps) {
  if (role === 'user' && audioUrl) {
    return (
      <div className="max-w-[85%] rounded-2xl rounded-br-sm bg-violeta px-4 py-3">
        <audio src={audioUrl} controls className="h-8 w-full min-w-[200px]" />
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
