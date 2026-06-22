import { useEffect, useLayoutEffect, useRef } from 'react'
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
    messagesContainerRef,
    isLoadingOlderHistory,
    handleMessagesScroll,
    sendMessage,
    sendAudioMessage,
    deleteAudioMessage,
    decideChallenge,
    decideSuggestedAction,
    resetConversation,
  } = useChatbot()
  useLayoutEffect(() => {
    if (isOpen && !isLoadingHistory) {
      if (messagesContainerRef?.current) {
        messagesContainerRef.current.scrollTop = messagesContainerRef.current.scrollHeight
      } else if (typeof bottomRef?.current?.scrollIntoView === 'function') {
        bottomRef.current.scrollIntoView({ behavior: 'auto' })
      }
    }
  }, [isOpen, isLoadingHistory, bottomRef, messagesContainerRef])


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
      <ChatbotHeader onClose={onClose} onReset={resetConversation} />
      <ChatbotMessages
        messages={messages}
        isSending={isSending}
        isLoadingHistory={isLoadingHistory}
        error={error}
        onClose={onClose}
        onChallengeDecision={decideChallenge}
        onSuggestedActionDecision={decideSuggestedAction}
        onDeleteAudioMessage={deleteAudioMessage}
        bottomRef={bottomRef}
        containerRef={messagesContainerRef}
        isLoadingOlderHistory={isLoadingOlderHistory}
        onScroll={handleMessagesScroll}
      />
      <ChatbotComposer
        input={input}
        isSending={isSending}
        disabled={!!error && error.includes('Alcanzaste el límite diario')}
        onInputChange={setInput}
        onSend={() => void sendMessage()}
        onSendAudio={(blob) => void sendAudioMessage(blob)}
      />
    </BaseModal>
  )
}
