import { useEffect, useRef, useState } from 'react'
import {
  chatApi,
  type ChatHistoryMessageDto,
} from '../api/chat'
import { userGoalsApi } from '../api/userGoals'
import { type ChatbotMessage } from '../components/Chatbot/chatbotTypes'

const CHAT_CONVERSATION_STORAGE_KEY = 'hulyChatConversationId'

function randomConversationId() {
  return `chat-${Math.random().toString(36).slice(2, 10)}`
}

function mapHistoryMessage(message: ChatHistoryMessageDto): ChatbotMessage {
  if (message.role === 'USER') {
    return { role: 'user', content: message.content }
  }

  return { role: 'assistant', content: message.content }
}

export function useChatbot() {
  const [messages, setMessages] = useState<ChatbotMessage[]>([])
  const [input, setInput] = useState('')
  const [isSending, setIsSending] = useState(false)
  const [isLoadingHistory, setIsLoadingHistory] = useState(false)
  const [error, setError] = useState('')
  const [conversationId] = useState(() => {
    const storedConversationId = localStorage.getItem(CHAT_CONVERSATION_STORAGE_KEY)
    if (storedConversationId) return storedConversationId

    const newConversationId = randomConversationId()
    localStorage.setItem(CHAT_CONVERSATION_STORAGE_KEY, newConversationId)
    return newConversationId
  })
  const bottomRef = useRef<HTMLDivElement>(null)
  const sendingRef = useRef(false)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, isSending])

  useEffect(() => {
    const loadHistory = async () => {
      setIsLoadingHistory(true)

      try {
        const history = await chatApi.getHistory(conversationId)
        setMessages(history.content.map(mapHistoryMessage))
      } catch {
        setMessages([])
      } finally {
        setIsLoadingHistory(false)
      }
    }

    void loadHistory()
  }, [conversationId])

  const sendChatMessage = async (text: string) => {
    if (sendingRef.current) return
    sendingRef.current = true

    setMessages(prev => [...prev, { role: 'user', content: text }])
    setIsSending(true)
    setError('')

    try {
      const response = await chatApi.sendMessage({
        message: text,
        conversationId,
      })

      setMessages(prev => [
        ...prev,
        {
          role: 'assistant',
          content: response.huly_reply,
          detected_emotion: response.detected_emotion,
          intensity: response.intensity,
          suggested_action: response.suggested_action,
          generated_challenge: response.generated_challenge,
        },
      ])
    } catch (requestError) {
      if (requestError instanceof Error) {
        setError(requestError.message)
      }
    } finally {
      sendingRef.current = false
      setIsSending(false)
    }
  }

  const sendMessage = async () => {
    if (sendingRef.current) return

    const text = input.trim()
    if (!text) return

    setInput('')
    await sendChatMessage(text)
  }

  const decideChallenge = async (index: number, decision: 'accepted' | 'rejected') => {
    if (sendingRef.current) return

    const selectedMessage = messages[index]
    if (!selectedMessage || selectedMessage.role !== 'assistant' || !selectedMessage.generated_challenge) return

    setMessages(prev =>
      prev.map((message, currentIndex) => {
        if (currentIndex !== index || message.role !== 'assistant') return message
        return { ...message, challengeDecision: decision }
      }),
    )

    if (decision === 'accepted') {
      const { title, description } = selectedMessage.generated_challenge
      try {
        await userGoalsApi.acceptChallenge({ title, description: description ?? undefined })
      } catch {
        // no bloquea el flujo si falla el guardado
      }
    }

    const challengeResponseText =
      decision === 'accepted'
        ? 'Acepto este reto'
        : 'Rechazo este reto por ahora'

    await sendChatMessage(challengeResponseText)
  }

  const decideSuggestedAction = async (index: number, decision: 'accepted' | 'rejected') => {
    if (sendingRef.current) return

    const selectedMessage = messages[index]
    if (!selectedMessage || selectedMessage.role !== 'assistant' || !selectedMessage.suggested_action) return

    setMessages(prev =>
      prev.map((message, currentIndex) => {
        if (currentIndex !== index || message.role !== 'assistant') return message
        return { ...message, suggestedActionDecision: decision }
      }),
    )

    if (decision === 'accepted') return

    await sendChatMessage('No voy a ir por ahora, prefiero seguir conversando')
  }

  return {
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
  }
}
