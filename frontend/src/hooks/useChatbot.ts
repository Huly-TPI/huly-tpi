import { useEffect, useMemo, useRef, useState } from 'react'
import {
  chatApi,
  type ChatHistoryMessageDto,
  type SuggestedActionDto,
} from '../api/chat'
import { emotionalEventsApi } from '../api/emotionalEvents'
import { userGoalsApi } from '../api/userGoals'
import { type ChatbotMessage } from '../components/Chatbot/chatbotTypes'
import { useAuth } from '../context/auth'
import { deleteAudioBlob, getAudioBlob, saveAudioBlob } from './useAudioCache'

const CHAT_CONVERSATION_STORAGE_KEY = 'hulyChatConversationId'
const AUDIO_KEYS_PREFIX = 'hulyAudioKeys:'

function randomConversationId() {
  return `chat-${Math.random().toString(36).slice(2, 10)}`
}

function getAudioKeysStorageKey(conversationId: string) {
  return `${AUDIO_KEYS_PREFIX}${conversationId}`
}

function getStoredAudioKeys(conversationId: string): string[] {
  try {
    const raw = localStorage.getItem(getAudioKeysStorageKey(conversationId))
    return raw ? (JSON.parse(raw) as string[]) : []
  } catch {
    return []
  }
}

function pushAudioKey(conversationId: string, uuid: string) {
  const keys = getStoredAudioKeys(conversationId)
  keys.push(uuid)
  localStorage.setItem(getAudioKeysStorageKey(conversationId), JSON.stringify(keys))
}

function removeAudioKey(conversationId: string, uuid: string) {
  const keys = getStoredAudioKeys(conversationId).filter(k => k !== uuid)
  localStorage.setItem(getAudioKeysStorageKey(conversationId), JSON.stringify(keys))
}

async function mapHistoryMessage(
  message: ChatHistoryMessageDto,
  conversationId: string,
  audioKeyIterator: { index: number; keys: string[] },
): Promise<ChatbotMessage> {
  if (message.role === 'USER') {
    const isVoiceMessage = message.content.startsWith('[Mensaje de voz transcrito]')
    if (isVoiceMessage) {
      const uuid = audioKeyIterator.keys[audioKeyIterator.index] ?? null
      audioKeyIterator.index++

      if (uuid) {
        const cacheKey = `${conversationId}:${uuid}`
        const blob = await getAudioBlob(cacheKey).catch(() => null)
        if (blob) {
          return {
            role: 'user',
            content: message.content,
            audioBlob: blob,
            audioUrl: URL.createObjectURL(blob),
            audioKey: uuid,
          }
        }
      }
      // Blob not found — show placeholder
      return { role: 'user', content: message.content, audioKey: uuid ?? undefined }
    }
    return { role: 'user', content: message.content }
  }

  return { role: 'assistant', content: message.content }
}

function getSuggestedActionActivityId(action: SuggestedActionDto) {
  const activityId = Number(action.action_id)
  return Number.isInteger(activityId) && activityId > 0 ? activityId : null
}

function getConversationStorageKey(userId?: number) {
  return userId
    ? `${CHAT_CONVERSATION_STORAGE_KEY}:${userId}`
    : CHAT_CONVERSATION_STORAGE_KEY
}

function getOrCreateConversationId(storageKey: string) {
  const storedConversationId = localStorage.getItem(storageKey)
  if (storedConversationId) return storedConversationId

  const newConversationId = randomConversationId()
  localStorage.setItem(storageKey, newConversationId)
  return newConversationId
}

export function useChatbot() {
  const [messages, setMessages] = useState<ChatbotMessage[]>([])
  const [input, setInput] = useState('')
  const [isSending, setIsSending] = useState(false)
  const [isLoadingHistory, setIsLoadingHistory] = useState(false)
  const [error, setError] = useState('')
  const { user } = useAuth()
  const conversationStorageKey = useMemo(
    () => getConversationStorageKey(user?.id),
    [user?.id],
  )
  const [conversationId, setConversationId] = useState(() =>
    getOrCreateConversationId(conversationStorageKey),
  )
  const bottomRef = useRef<HTMLDivElement>(null)
  const sendingRef = useRef(false)
  const audioAbortRef = useRef<AbortController | null>(null)

  useEffect(() => {
    setConversationId(getOrCreateConversationId(conversationStorageKey))
    setMessages([])
    setError('')
  }, [conversationStorageKey])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, isSending])

  useEffect(() => {
    const loadHistory = async () => {
      setIsLoadingHistory(true)

      try {
        const history = await chatApi.getHistory(conversationId)
        const audioKeys = getStoredAudioKeys(conversationId)
        const iterator = { index: 0, keys: audioKeys }
        const mapped = await Promise.all(
          history.content.map(msg => mapHistoryMessage(msg, conversationId, iterator)),
        )
        setMessages(mapped)
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

  const sendAudioMessage = async (blob: Blob) => {
    if (sendingRef.current) return
    sendingRef.current = true

    const uuid = crypto.randomUUID()
    const cacheKey = `${conversationId}:${uuid}`

    await saveAudioBlob(cacheKey, blob).catch(() => null)
    pushAudioKey(conversationId, uuid)

    const audioUrl = URL.createObjectURL(blob)
    setMessages(prev => [
      ...prev,
      { role: 'user', content: '', audioBlob: blob, audioUrl, audioKey: uuid },
    ])
    setIsSending(true)
    setError('')

    const controller = new AbortController()
    audioAbortRef.current = controller

    try {
      const response = await chatApi.sendAudioMessage(blob, conversationId, controller.signal)
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
      if (requestError instanceof Error && requestError.name !== 'AbortError') {
        setError(requestError.message)
      }
    } finally {
      audioAbortRef.current = null
      sendingRef.current = false
      setIsSending(false)
    }
  }

  const deleteAudioMessage = async (index: number) => {
    const message = messages[index]
    if (!message || message.role !== 'user' || !('audioKey' in message)) return

    // Cancel in-flight request if we're still waiting for a response
    if (isSending && audioAbortRef.current) {
      audioAbortRef.current.abort()
      audioAbortRef.current = null
      sendingRef.current = false
      setIsSending(false)
    }

    // Revoke ObjectURL to free memory
    if ('audioUrl' in message && message.audioUrl) {
      URL.revokeObjectURL(message.audioUrl)
    }

    // Clean up IndexedDB and localStorage
    if (message.audioKey) {
      const cacheKey = `${conversationId}:${message.audioKey}`
      await deleteAudioBlob(cacheKey).catch(() => null)
      removeAudioKey(conversationId, message.audioKey)
    }

    setMessages(prev => prev.filter((_, i) => i !== index))
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

    const { title, description } = selectedMessage.generated_challenge

    if (decision === 'accepted') {
      try {
        await userGoalsApi.acceptChallenge({ title, description: description ?? undefined })
      } catch {
        // no bloquea el flujo si falla el guardado
      }
    }

    try {
      await chatApi.saveChallengeDecision({
        conversationId,
        title,
        description,
        decision: decision === 'accepted' ? 'ACCEPTED' : 'REJECTED',
      })
    } catch {
      // no bloquea el flujo si falla la memoria vectorial
    }

    const challengeResponseText =
      decision === 'accepted'
        ? 'Acepto este reto'
        : 'Rechazo este reto por ahora'

    await sendChatMessage(challengeResponseText)
  }

  const decideSuggestedAction = async (index: number, decision: 'accepted' | 'rejected') => {
    const selectedMessage = messages[index]
    if (!selectedMessage || selectedMessage.role !== 'assistant' || !selectedMessage.suggested_action) return
    if (selectedMessage.suggestedActionDecisionLoading) return

    const emotionalEventId = selectedMessage.suggested_action.emotional_event_id

    if (!emotionalEventId) {
      setMessages(prev =>
        prev.map((message, currentIndex) => {
          if (currentIndex !== index || message.role !== 'assistant') return message
          return {
            ...message,
            suggestedActionDecisionError: 'No se recibió emotional_event_id para guardar la decisión.',
          }
        }),
      )
      return
    }

    setMessages(prev =>
      prev.map((message, currentIndex) => {
        if (currentIndex !== index || message.role !== 'assistant') return message
        return {
          ...message,
          suggestedActionDecisionLoading: true,
          suggestedActionDecisionError: undefined,
        }
      }),
    )

    try {
      await emotionalEventsApi.updateDecision(emotionalEventId, {
        decision: decision === 'accepted' ? 'ACCEPTED' : 'IGNORED',
        chosenActivityId:
          decision === 'accepted'
            ? getSuggestedActionActivityId(selectedMessage.suggested_action)
            : null,
      })

      setMessages(prev =>
        prev.map((message, currentIndex) => {
          if (currentIndex !== index || message.role !== 'assistant') return message
          return {
            ...message,
            suggestedActionDecision: decision,
            suggestedActionDecisionLoading: false,
            suggestedActionDecisionError: undefined,
          }
        }),
      )
    } catch (requestError) {
      setMessages(prev =>
        prev.map((message, currentIndex) => {
          if (currentIndex !== index || message.role !== 'assistant') return message
          return {
            ...message,
            suggestedActionDecisionLoading: false,
            suggestedActionDecisionError:
              requestError instanceof Error
                ? requestError.message
                : 'No se pudo guardar la decisión.',
          }
        }),
      )
    }
  }

  const resetConversation = () => {
    const newConversationId = randomConversationId()
    localStorage.setItem(conversationStorageKey, newConversationId)
    setConversationId(newConversationId)
    setMessages([])
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
    sendAudioMessage,
    deleteAudioMessage,
    decideChallenge,
    decideSuggestedAction,
    resetConversation,
  }
}
