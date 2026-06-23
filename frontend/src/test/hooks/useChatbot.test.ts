import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useChatbot } from '../../hooks/useChatbot'
import { chatApi } from '../../api/chat'
import { emotionalEventsApi } from '../../api/emotionalEvents'
import { saveAudioBlob } from '../../hooks/useAudioCache'

vi.mock('../../api/chat', () => ({
  chatApi: {
    sendMessage: vi.fn(),
    getHistory: vi.fn(),
    sendAudioMessage: vi.fn(),
    saveChallengeDecision: vi.fn(),
  },
}))

vi.mock('../../hooks/useAudioCache', () => ({
  saveAudioBlob: vi.fn().mockResolvedValue(undefined),
  getAudioBlob: vi.fn().mockResolvedValue(null),
  deleteAudioBlob: vi.fn().mockResolvedValue(undefined),
}))

vi.mock('../../context/auth', () => ({
  useAuth: () => ({
    user: { id: 1, name: 'Mili', email: 'mili@huly.com', role: 'USER' },
  }),
}))

vi.mock('../../api/emotionalEvents', () => ({
  emotionalEventsApi: {
    updateDecision: vi.fn(),
  },
}))

const mockedSendMessage = vi.mocked(chatApi.sendMessage)
const mockedGetHistory = vi.mocked(chatApi.getHistory)
const mockedSendAudioMessage = vi.mocked(chatApi.sendAudioMessage)
const mockedUpdateDecision = vi.mocked(emotionalEventsApi.updateDecision)
const mockedSaveAudioBlob = vi.mocked(saveAudioBlob)

vi.mock('../../api/auth', () => ({
  getMyMembership: vi.fn(),
}))

const mockedGetMyMembership = vi.mocked(await import('../../api/auth')).getMyMembership

describe('useChatbot', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    global.URL.createObjectURL = vi.fn().mockReturnValue('blob:fake-url')
    global.URL.revokeObjectURL = vi.fn()
  })


  it('loads history on mount using persisted conversationId', async () => {
    localStorage.setItem('hulyChatConversationId:1', 'conv-history')
    mockedGetHistory.mockResolvedValueOnce({
      content: [
        {
          id: 2,
          role: 'ASSISTANT',
          content: 'hola yo',
          suggested_action: {
            type: 'RESPIRACION',
            action_id: '7',
            title: 'Respiracion guiada',
            description: 'Respira con calma',
            action_url: '/guided-breathing',
            emotional_event_id: 22,
          },
          suggested_action_decision: 'accepted',
          created_at: '2026-01-01T00:01:00Z',
        },
        {
          id: 1,
          role: 'USER',
          content: 'hola',
          created_at: '2026-01-01T00:00:00Z',
        },
      ],
      page_number: 0,
      page_size: 20,
      total_elements: 2,
      total_pages: 1,
      first: true,
      last: true,
    } as never)

    const { result } = renderHook(() => useChatbot())

    await waitFor(() => {
      expect(result.current.isLoadingHistory).toBe(false)
    })

    expect(mockedGetHistory).toHaveBeenCalledWith('conv-history', 0, 20)
    expect(result.current.messages).toEqual([
      { role: 'user', content: 'hola' },
      {
        role: 'assistant',
        content: 'hola yo',
        detected_emotion: undefined,
        suggested_action: {
          type: 'RESPIRACION',
          action_id: '7',
          title: 'Respiracion guiada',
          description: 'Respira con calma',
          action_url: '/guided-breathing',
          emotional_event_id: 22,
        },
        generated_challenge: undefined,
        suggestedActionDecision: 'accepted',
        challengeDecision: undefined,
      },
    ])
  })

  it('restores quota limit error on mount if limit date matches today and user has no active membership', async () => {
    const today = new Date().toISOString().split('T')[0]
    localStorage.setItem('huly:chat-limit-date:1', today)
    localStorage.setItem('huly:chat-limit-message:1', 'Alcanzaste el límite diario de 10 mensajes del plan gratuito.')
    localStorage.setItem('hulyChatConversationId:1', 'some-conv-id')

    mockedGetMyMembership.mockResolvedValueOnce({
      active: false,
      planCode: null,
      productId: null,
      expiresAt: null,
    })

    mockedGetHistory.mockResolvedValueOnce({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    } as never)

    const { result } = renderHook(() => useChatbot())

    await waitFor(() => expect(result.current.isLoadingHistory).toBe(false))

    expect(result.current.error).toBe('Alcanzaste el límite diario de 10 mensajes del plan gratuito.')
  })


  it('loads older history page when scrolling to the top', async () => {
    localStorage.setItem('hulyChatConversationId:1', 'conv-paged')
    mockedGetHistory
      .mockResolvedValueOnce({
        content: [
          {
            id: 101,
            role: 'ASSISTANT',
            content: 'ultima respuesta',
            generated_challenge: { title: 'Reto final', description: 'Desc final' },
            created_at: '2026-01-01T01:40:00Z',
          },
        ],
        page_number: 0,
        page_size: 20,
        total_elements: 101,
        total_pages: 2,
        first: true,
        last: false,
      } as never)
      .mockResolvedValueOnce({
        content: [
          {
            id: 1,
            role: 'USER',
            content: 'primer mensaje',
            created_at: '2026-01-01T00:00:00Z',
          },
        ],
        page_number: 1,
        page_size: 20,
        total_elements: 101,
        total_pages: 2,
        first: false,
        last: true,
      } as never)

    const { result } = renderHook(() => useChatbot())

    await waitFor(() => expect(result.current.isLoadingHistory).toBe(false))

    const container = document.createElement('section')
    Object.defineProperty(container, 'scrollTop', { value: 0, writable: true })
    Object.defineProperty(container, 'scrollHeight', { value: 300, writable: true })
    ;(result.current.messagesContainerRef as { current: HTMLElement | null }).current = container

    await act(async () => {
      result.current.handleMessagesScroll()
    })

    await waitFor(() => expect(mockedGetHistory).toHaveBeenCalledTimes(2))

    expect(mockedGetHistory).toHaveBeenNthCalledWith(1, 'conv-paged', 0, 20)
    expect(mockedGetHistory).toHaveBeenNthCalledWith(2, 'conv-paged', 1, 20)
    expect(result.current.messages).toHaveLength(2)
    expect(result.current.messages[0]).toMatchObject({
      role: 'user',
      content: 'primer mensaje',
    })
    expect(result.current.messages[1]).toMatchObject({
      role: 'assistant',
      content: 'ultima respuesta',
      generated_challenge: { title: 'Reto final', description: 'Desc final' },
    })
  })

  it('sendMessage appends user and assistant messages', async () => {
    mockedGetHistory.mockResolvedValueOnce({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    } as never)
    mockedSendMessage.mockResolvedValueOnce({
      huly_reply: 'respuesta',
      detected_emotion: null,
      intensity: null,
      suggested_action: null,
      generated_challenge: null,
      metadata: null,
    } as never)

    const { result } = renderHook(() => useChatbot())

    await waitFor(() => expect(result.current.isLoadingHistory).toBe(false))

    act(() => {
      result.current.setInput('mensaje de prueba')
    })

    await act(async () => {
      await result.current.sendMessage()
    })

    expect(mockedSendMessage).toHaveBeenCalled()
    expect(result.current.messages).toEqual([
      { role: 'user', content: 'mensaje de prueba' },
      {
        role: 'assistant',
        content: 'respuesta',
        detected_emotion: null,
        intensity: null,
        suggested_action: null,
        generated_challenge: null,
      },
    ])
  })

  it('decideChallenge marks decision and sends follow-up message', async () => {
    mockedGetHistory.mockResolvedValueOnce({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    } as never)
    mockedSendMessage
      .mockResolvedValueOnce({
        huly_reply: 'te propongo un reto',
        suggested_action: null,
        generated_challenge: { title: 'Reto 1', description: 'Desc' },
      } as never)
      .mockResolvedValueOnce({
        huly_reply: 'gracias por responder',
        suggested_action: null,
        generated_challenge: null,
      } as never)

    const { result } = renderHook(() => useChatbot())
    await waitFor(() => expect(result.current.isLoadingHistory).toBe(false))

    act(() => {
      result.current.setInput('hola')
    })
    await act(async () => {
      await result.current.sendMessage()
    })

    await act(async () => {
      await result.current.decideChallenge(1, 'rejected')
    })

    expect(mockedSendMessage).toHaveBeenNthCalledWith(2, {
      message: 'Rechazo este reto por ahora',
      conversationId: expect.any(String),
    })
    expect(result.current.messages[1]).toMatchObject({
      role: 'assistant',
      challengeDecision: 'rejected',
    })
  })

  it('decideSuggestedAction rejected saves the decision', async () => {
    mockedGetHistory.mockResolvedValueOnce({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    } as never)
    mockedSendMessage
      .mockResolvedValueOnce({
        huly_reply: 'actividad',
        suggested_action: {
          type: 'INTERNAL',
          action_id: 'GUIDED_BREATHING',
          title: 'Resp',
          description: 'Desc',
          action_url: '/activities',
          emotional_event_id: 15,
        },
        generated_challenge: null,
      } as never)
    mockedUpdateDecision.mockResolvedValueOnce({} as never)

    const { result } = renderHook(() => useChatbot())
    await waitFor(() => expect(result.current.isLoadingHistory).toBe(false))

    act(() => {
      result.current.setInput('hola')
    })
    await act(async () => {
      await result.current.sendMessage()
    })

    await act(async () => {
      await result.current.decideSuggestedAction(1, 'rejected')
    })

    expect(mockedSendMessage).toHaveBeenCalledTimes(1)
    expect(mockedUpdateDecision).toHaveBeenCalledWith(15, {
      decision: 'IGNORED',
      chosenActivityId: null,
    })
    expect(result.current.messages[1]).toMatchObject({
      role: 'assistant',
      suggestedActionDecision: 'rejected',
    })
  })

  it('sendAudioMessage appends audio user message and assistant reply', async () => {
    mockedGetHistory.mockResolvedValueOnce({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    } as never)
    mockedSendAudioMessage.mockResolvedValueOnce({
      huly_reply: 'entendí tu mensaje de voz',
      detected_emotion: 'neutral',
      intensity: 3,
      suggested_action: null,
      generated_challenge: null,
      metadata: null,
    } as never)

    const { result } = renderHook(() => useChatbot())
    await waitFor(() => expect(result.current.isLoadingHistory).toBe(false))

    const blob = new Blob(['audio'], { type: 'audio/webm' })
    await act(async () => {
      await result.current.sendAudioMessage(blob)
    })

    expect(result.current.messages[0]).toMatchObject({
      role: 'user',
      audioBlob: blob,
      audioUrl: 'blob:fake-url',
    })
    expect(result.current.messages[1]).toMatchObject({
      role: 'assistant',
      content: 'entendí tu mensaje de voz',
    })
  })

  it('sendAudioMessage saves blob to cache and tracks audio key in localStorage', async () => {
    mockedGetHistory.mockResolvedValueOnce({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    } as never)
    mockedSendAudioMessage.mockResolvedValueOnce({
      huly_reply: 'ok',
      detected_emotion: null,
      intensity: null,
      suggested_action: null,
      generated_challenge: null,
      metadata: null,
    } as never)

    const { result } = renderHook(() => useChatbot())
    await waitFor(() => expect(result.current.isLoadingHistory).toBe(false))

    const blob = new Blob(['audio'], { type: 'audio/webm' })
    await act(async () => {
      await result.current.sendAudioMessage(blob)
    })

    expect(mockedSaveAudioBlob).toHaveBeenCalledWith(
      expect.stringContaining(':'),
      blob,
    )
    const storedKeys = Object.keys(localStorage).find(k => k.startsWith('hulyAudioKeys:'))
    expect(storedKeys).toBeDefined()
  })

  it('sendAudioMessage handles API error without crashing', async () => {
    mockedGetHistory.mockResolvedValueOnce({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    } as never)
    mockedSendAudioMessage.mockRejectedValueOnce(new Error('red caída'))

    const { result } = renderHook(() => useChatbot())
    await waitFor(() => expect(result.current.isLoadingHistory).toBe(false))

    const blob = new Blob(['audio'], { type: 'audio/webm' })
    await act(async () => {
      await result.current.sendAudioMessage(blob)
    })

    expect(result.current.isSending).toBe(false)
    expect(result.current.error).toBe('red caída')
  })

  it('resetConversation generates a new conversationId and clears messages', async () => {
    mockedGetHistory.mockResolvedValue({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    } as never)

    localStorage.setItem('hulyChatConversationId:1', 'old-conv-id')

    const { result } = renderHook(() => useChatbot())
    await waitFor(() => expect(result.current.isLoadingHistory).toBe(false))

    expect(mockedGetHistory).toHaveBeenLastCalledWith('old-conv-id', 0, 20)

    act(() => {
      result.current.resetConversation()
    })

    await waitFor(() => {
      expect(mockedGetHistory).toHaveBeenCalledTimes(2)
    })

    const newConvId = localStorage.getItem('hulyChatConversationId:1')
    expect(newConvId).not.toBe('old-conv-id')
    expect(newConvId).not.toBeNull()
    expect(result.current.messages).toEqual([])
  })
})

