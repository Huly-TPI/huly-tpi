import { type RefObject, type UIEventHandler } from 'react'
import ChatbotChallengeCard from './ChatbotChallengeCard'

const MIN_BUBBLE_LENGTH = 150

function splitIntoBubbles(content: string): string[] {
  const parts = content.split('\n\n').filter((p) => p.trim() !== '')
  const bubbles: string[] = []
  let pending = ''
  for (const part of parts) {
    const trimmed = part.trim()
    if (pending) {
      pending = pending + '\n\n' + trimmed
      if (pending.length >= MIN_BUBBLE_LENGTH) {
        bubbles.push(pending)
        pending = ''
      }
    } else if (trimmed.length < MIN_BUBBLE_LENGTH) {
      pending = trimmed
    } else {
      bubbles.push(trimmed)
    }
  }
  if (pending) bubbles.push(pending)
  return bubbles
}
import ChatbotSuggestedActionCard from './ChatbotSuggestedActionCard'
import { type ChatbotMessage } from './chatbotTypes'
import ChatMessageBubble from './ChatMessageBubble'
import ChatbotQuotaLimitCard from './ChatbotQuotaLimitCard'

function getSuggestedActionRoute(type: string, actionUrl: string) {
  switch (type) {
    case 'BREATHING':
      return '/guided-breathing'
    case 'DIARY':
      return '/diary'
    case 'LANTERN':
      return '/lanterns'
    case 'BUBBLE':
      return '/bubbles'
    case 'CHALLENGE':
      return '/challenges'
    case 'ZEN_GARDEN':
      return '/zen-sand-garden'
    case 'MANDALA':
      return '/mandalas'
    case 'STONES':
      return '/stones'
    case 'PENDING':
      return '/pending'
    default:
      return actionUrl.startsWith('/api/') ? '/' : actionUrl
  }
}

interface ChatbotMessagesProps {
  messages: ChatbotMessage[]
  isSending: boolean
  isLoadingHistory: boolean
  isLoadingOlderHistory?: boolean
  error: string
  onClose: () => void
  onChallengeDecision: (index: number, decision: 'accepted' | 'rejected') => void | Promise<void>
  onSuggestedActionDecision: (index: number, decision: 'accepted' | 'rejected') => void | Promise<void>
  onDeleteAudioMessage: (index: number) => void | Promise<void>
  bottomRef: RefObject<HTMLDivElement>
  containerRef?: RefObject<HTMLElement>
  onScroll?: UIEventHandler<HTMLElement>
}

export default function ChatbotMessages({
  messages,
  isSending,
  isLoadingHistory,
  isLoadingOlderHistory = false,
  error,
  onClose,
  onChallengeDecision,
  onSuggestedActionDecision,
  onDeleteAudioMessage,
  bottomRef,
  containerRef,
  onScroll,
}: ChatbotMessagesProps) {
  return (
    <section
      ref={containerRef}
      onScroll={onScroll}
      className="relative flex min-h-0 flex-1 flex-col gap-4 overflow-y-auto bg-[var(--surface-secondary)] px-6 py-5"
    >
      {isLoadingOlderHistory && (
        <div className="flex justify-center">
          <div className="rounded-full bg-[var(--surface-primary)] px-3 py-1 text-xs text-[var(--text-muted)] shadow-sm">
            Cargando mensajes anteriores...
          </div>
        </div>
      )}

      {isLoadingHistory && (
        <div className="flex items-start">
          <div className="max-w-[85%] rounded-2xl rounded-bl-sm bg-[var(--surface-primary)] px-4 py-2.5 text-sm text-[var(--text-secondary)] shadow-sm">
            Cargando conversación...
          </div>
        </div>
      )}

      {messages.length === 0 && !isLoadingHistory && (
        <div className="flex items-start">
          <div className="max-w-[85%] rounded-2xl rounded-bl-sm bg-[var(--surface-primary)] px-4 py-2.5 text-sm text-[var(--text-secondary)] shadow-sm">
            Contame cómo te sentís hoy
          </div>
        </div>
      )}

      {messages.map((message, index) => (
        <div key={index} className={`flex flex-col gap-1.5 ${message.role === 'user' ? 'items-end' : 'items-start'}`}>
          {message.role === 'assistant'
            ? splitIntoBubbles(message.content).map((part, partIndex) => (
                  <ChatMessageBubble key={partIndex} role="assistant" content={part} />
                ))
            : (
              <ChatMessageBubble
                role={message.role}
                content={message.content}
                audioUrl={'audioUrl' in message ? message.audioUrl : undefined}
                isAudioMessage={'audioKey' in message}
                onDelete={'audioKey' in message ? () => void onDeleteAudioMessage(index) : undefined}
              />
            )}

          {message.role === 'assistant' && (
            <div className="ml-1 mt-2 flex w-full max-w-[85%] min-w-0 flex-col gap-2 sm:w-auto">
              {message.suggested_action && (
                <ChatbotSuggestedActionCard
                  title={message.suggested_action.title}
                  description={message.suggested_action.description}
                  actionUrl={getSuggestedActionRoute(
                    message.suggested_action.type,
                    message.suggested_action.action_url,
                  )}
                  onClose={onClose}
                  decision={message.suggestedActionDecision}
                  isLoading={message.suggestedActionDecisionLoading}
                  error={message.suggestedActionDecisionError}
                  onAccept={() => onSuggestedActionDecision(index, 'accepted')}
                  onReject={() => onSuggestedActionDecision(index, 'rejected')}
                />
              )}

              {message.generated_challenge && (
                <ChatbotChallengeCard
                  title={message.generated_challenge.title}
                  description={message.generated_challenge.description}
                  onClose={onClose}
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
          <div className="rounded-2xl rounded-bl-sm bg-[var(--surface-primary)] px-4 py-2.5 text-sm text-[var(--text-muted)] shadow-sm">
            Huly está escribiendo...
          </div>
        </div>
      )}

      {!!error && (
        error.includes('Alcanzaste el límite diario') || error.includes('plan gratuito') || error.includes('audios diarios') ? (
          <ChatbotQuotaLimitCard message={error} onClose={onClose} />
        ) : (
          <p className="text-center text-xs text-red-500">{error}</p>
        )
      )}
      <div ref={bottomRef} />
    </section>
  )
}
