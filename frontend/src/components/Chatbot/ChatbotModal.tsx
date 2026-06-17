import hulySideImage from '../../assets/chatbot/huly-side.webp'
import { useChatbot } from '../../hooks/useChatbot'
import BaseModal from '../Modal/BaseModal'
import ChatbotComposer from './ChatbotComposer'
import ChatbotHeader from './ChatbotHeader'
import ChatbotMessages from './ChatbotMessages'

interface ChatbotModalProps {
  isOpen: boolean
  onClose: () => void
}

export default function ChatbotModal({ isOpen, onClose }: ChatbotModalProps) {
  const {
    messages,
    input,
    setInput,
    isSending,
    isLoadingHistory,
    error,
    bottomRef,
    sendMessage,
    decideChallenge,
    decideSuggestedAction,
    resetConversation,
  } = useChatbot()

  return (
    <BaseModal
      isOpen={isOpen}
      onClose={onClose}
      title="Chat de Huly"
      outsideContent={
        <img
          src={hulySideImage}
          alt=""
          aria-hidden
          className="pointer-events-none absolute bottom-[72px] right-full z-20 hidden w-[min(26vw,280px)] translate-x-[20px] select-none lg:block"
        />
      }
    >
      <ChatbotHeader onClose={onClose} />
      <ChatbotMessages
        messages={messages}
        isSending={isSending}
        isLoadingHistory={isLoadingHistory}
        error={error}
        onClose={onClose}
        onChallengeDecision={decideChallenge}
        onSuggestedActionDecision={decideSuggestedAction}
        bottomRef={bottomRef}
      />
      <ChatbotComposer
        input={input}
        isSending={isSending}
        onInputChange={setInput}
        onSend={() => void sendMessage()}
        onReset={resetConversation}
      />
    </BaseModal>
  )
}
