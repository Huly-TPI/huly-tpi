import { type RefObject } from 'react'
import { type ChatbotMessage } from './chatbotTypes'
import ChatbotSuggestedActionCard from './ChatbotSuggestedActionCard'
import ChatbotChallengeCard from './ChatbotChallengeCard'
import ChatMessageBubble from './ChatMessageBubble'

interface ChatbotMessagesProps {
  messages: ChatbotMessage[]
  isSending: boolean
  isLoadingHistory: boolean
  error: string
  onClose: () => void
  onChallengeDecision: (index: number, decision: 'accepted' | 'rejected') => void | Promise<void>
  onSuggestedActionDecision: (index: number, decision: 'accepted' | 'rejected') => void | Promise<void>
  bottomRef: RefObject<HTMLDivElement>
}

export default function ChatbotMessages({
  messages,
  isSending,
  isLoadingHistory,
  error,
  onClose,
  onChallengeDecision,
  onSuggestedActionDecision,
  bottomRef,
}: ChatbotMessagesProps) {
  return (
    <section className="relative flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto bg-gray-50 px-6 py-5">
      {isLoadingHistory && (
        <div className="flex items-start">
          <div className="max-w-[85%] rounded-2xl rounded-bl-sm bg-white px-4 py-2.5 text-sm text-gray-500 shadow-sm">
            Cargando conversación...
          </div>
        </div>
      )}

      {messages.length === 0 && !isLoadingHistory && (
        <div className="flex items-start">
          <div className="max-w-[85%] rounded-2xl rounded-bl-sm bg-white px-4 py-2.5 text-sm text-gray-500 shadow-sm">
            Contame cómo te sentís hoy
          </div>
        </div>
      )}

      {messages.map((message, index) => (
        <div key={index} className={`flex flex-col ${message.role === 'user' ? 'items-end' : 'items-start'}`}>
          <ChatMessageBubble role={message.role} content={message.content} />

          {message.role === 'assistant' && (
            <div className="ml-1 mt-2 flex max-w-[85%] flex-col gap-2">
              {message.suggested_action && (
                <ChatbotSuggestedActionCard
                  title={message.suggested_action.title}
                  description={message.suggested_action.description}
                  actionUrl={message.suggested_action.action_url}
                  onClose={onClose}
                  decision={message.suggestedActionDecision}
                  onReject={() => onSuggestedActionDecision(index, 'rejected')}
                />
              )}

              {message.generated_challenge && (
                <ChatbotChallengeCard
                  title={message.generated_challenge.title}
                  description={message.generated_challenge.description}
                  decision={message.challengeDecision}
                  onAccept={() => onChallengeDecision(index, 'accepted')}
                  onReject={() => onChallengeDecision(index, 'rejected')}
                />
              )}
            </div>
          )}
        </div>
      ))}

      {isSending && (
        <div className="flex items-start">
          <div className="rounded-2xl rounded-bl-sm bg-white px-4 py-2.5 text-sm text-gray-400 shadow-sm">
            Huly está escribiendo...
          </div>
        </div>
      )}

      {!!error && <p className="text-center text-xs text-red-500">{error}</p>}
      <div ref={bottomRef} />
    </section>
  )
}
