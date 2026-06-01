interface ChatMessageBubbleProps {
  role: 'user' | 'assistant'
  content: string
}

export default function ChatMessageBubble({ role, content }: ChatMessageBubbleProps) {
  return (
    <div
      className={`max-w-[85%] rounded-2xl px-4 py-2.5 text-[15px] leading-relaxed md:text-sm ${
        role === 'user' ? 'rounded-br-sm bg-violeta text-white' : 'rounded-bl-sm bg-white text-gray-800 shadow-sm'
      }`}
    >
      {content}
    </div>
  )
}
