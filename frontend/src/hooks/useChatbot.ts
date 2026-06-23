import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react'
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
import { getMyMembership } from '../api/auth'

function getTodayDateString() {
  return new Date().toISOString().split('T')[0]
}

const CHAT_CONVERSATION_STORAGE_KEY = 'hulyChatConversationId'
const AUDIO_KEYS_PREFIX = 'hulyAudioKeys:'
const HISTORY_PAGE_SIZE = 20

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

  return {
    role: 'assistant',
    content: message.content,
    detected_emotion: message.detected_emotion,
    suggested_action: message.suggested_action,
    generated_challenge: message.generated_challenge,
    suggestedActionDecision: message.suggested_action_decision ?? undefined,
    challengeDecision: message.challenge_decision ?? undefined,
  }
}

async function mapHistoryPageDescending(
  historyPage: ChatHistoryMessageDto[],
  conversationId: string,
  audioKeyIterator: { index: number; keys: string[] },
) {
  const mappedDescending = await Promise.all(
    historyPage.map(msg => mapHistoryMessage(msg, conversationId, audioKeyIterator)),
  )
  return mappedDescending.reverse()
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
  const [isLoadingOlderHistory, setIsLoadingOlderHistory] = useState(false)
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
  const messagesContainerRef = useRef<HTMLElement>(null)
  const sendingRef = useRef(false)
  const audioAbortRef = useRef<AbortController | null>(null)
  const nextHistoryPageRef = useRef(0)
  const hasMoreHistoryRef = useRef(false)
  const audioHistoryCursorRef = useRef<{ index: number; keys: string[] }>({ index: -1, keys: [] })
  const restoreScrollHeightRef = useRef<number | null>(null)
  const shouldAutoScrollRef = useRef(false)

  useEffect(() => {
    setConversationId(getOrCreateConversationId(conversationStorageKey))
    setMessages([])
    setError('')
    nextHistoryPageRef.current = 0
    hasMoreHistoryRef.current = false
    audioHistoryCursorRef.current = { index: -1, keys: [] }
    restoreScrollHeightRef.current = null
  }, [conversationStorageKey])

  useLayoutEffect(() => {
    const previousScrollHeight = restoreScrollHeightRef.current
    const container = messagesContainerRef.current

    if (previousScrollHeight !== null && container) {
      container.scrollTop += container.scrollHeight - previousScrollHeight
      restoreScrollHeightRef.current = null
      return
    }

    if (shouldAutoScrollRef.current && container) {
      container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' })
      shouldAutoScrollRef.current = false
    }
  }, [messages])

  useEffect(() => {
    const loadHistory = async () => {
      setIsLoadingHistory(true)

      const limitDate = localStorage.getItem(`huly:chat-limit-date:${user?.id ?? 'guest'}`)
      const today = getTodayDateString()
      if (limitDate === today) {
        try {
          const membership = await getMyMembership()
          if (membership?.active) {
            localStorage.removeItem(`huly:chat-limit-date:${user?.id ?? 'guest'}`)
            localStorage.removeItem(`huly:chat-limit-message:${user?.id ?? 'guest'}`)
          } else {
            const limitMsg = localStorage.getItem(`huly:chat-limit-message:${user?.id ?? 'guest'}`)
            if (limitMsg) {
              setError(limitMsg)
            }
          }
        } catch {
          const limitMsg = localStorage.getItem(`huly:chat-limit-message:${user?.id ?? 'guest'}`)
          if (limitMsg) {
            setError(limitMsg)
          }
        }
      }

      try {
        const historyPage = await chatApi.getHistory(conversationId, 0, HISTORY_PAGE_SIZE)
        const audioKeys = getStoredAudioKeys(conversationId)
        const iterator = { index: audioKeys.length - 1, keys: audioKeys }
        audioHistoryCursorRef.current = iterator
        const mapped = await mapHistoryPageDescending(
          historyPage.content,
          conversationId,
          iterator,
        )
        setMessages(mapped)
        nextHistoryPageRef.current = historyPage.page_number + 1
        hasMoreHistoryRef.current = !historyPage.last
      } catch {
        setMessages([])
        nextHistoryPageRef.current = 0
        hasMoreHistoryRef.current = false
      } finally {
        setIsLoadingHistory(false)
      }
    }

    void loadHistory()
  }, [conversationId])

  const loadOlderHistory = async () => {
    if (isLoadingHistory || isLoadingOlderHistory || !hasMoreHistoryRef.current) return

    const container = messagesContainerRef.current
    restoreScrollHeightRef.current = container?.scrollHeight ?? null
    setIsLoadingOlderHistory(true)

    try {
      const historyPage = await chatApi.getHistory(
        conversationId,
        nextHistoryPageRef.current,
        HISTORY_PAGE_SIZE,
      )
      const mapped = await mapHistoryPageDescending(
        historyPage.content,
        conversationId,
        audioHistoryCursorRef.current,
      )
      setMessages(prev => [...mapped, ...prev])
      nextHistoryPageRef.current = historyPage.page_number + 1
      hasMoreHistoryRef.current = !historyPage.last
    } catch {
      restoreScrollHeightRef.current = null
    } finally {
      setIsLoadingOlderHistory(false)
    }
  }

  const handleMessagesScroll = () => {
    const container = messagesContainerRef.current
    if (!container) return
    if (container.scrollTop > 48) return
    void loadOlderHistory()
  }

  const sendChatMessage = async (text: string) => {
    if (sendingRef.current) return
    sendingRef.current = true

    shouldAutoScrollRef.current = true
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
      shouldAutoScrollRef.current = true
    } catch (requestError) {
      if (requestError instanceof Error) {
        const errorMsg = requestError.message
        if (errorMsg.includes('Alcanzaste el límite diario')) {
          localStorage.setItem(`huly:chat-limit-date:${user?.id ?? 'guest'}`, getTodayDateString())
          localStorage.setItem(`huly:chat-limit-message:${user?.id ?? 'guest'}`, errorMsg)
        }
        setError(errorMsg)
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
    shouldAutoScrollRef.current = true
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
      shouldAutoScrollRef.current = true
    } catch (requestError) {
      if (requestError instanceof Error && requestError.name !== 'AbortError') {
        const errorMsg = requestError.message
        if (errorMsg.includes('Alcanzaste el límite diario')) {
          localStorage.setItem(`huly:chat-limit-date:${user?.id ?? 'guest'}`, getTodayDateString())
          localStorage.setItem(`huly:chat-limit-message:${user?.id ?? 'guest'}`, errorMsg)
        }
        setError(errorMsg)
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
    messagesContainerRef,
    isLoadingOlderHistory,
    handleMessagesScroll,
    sendMessage,
    sendAudioMessage,
    deleteAudioMessage,
    decideChallenge,
    decideSuggestedAction,
    resetConversation,
  }
}
