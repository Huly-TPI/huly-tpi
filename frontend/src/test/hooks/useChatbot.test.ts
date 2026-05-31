import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useChatbot } from '../../hooks/useChatbot'
import { chatApi } from '../../api/chat'

vi.mock('../../api/chat', () => ({
  chatApi: {
    sendMessage: vi.fn(),
    getHistory: vi.fn(),
  },
}))

const mockedSendMessage = vi.mocked(chatApi.sendMessage)
const mockedGetHistory = vi.mocked(chatApi.getHistory)

describe('useChatbot', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('loads history on mount using persisted conversationId', async () => {
    localStorage.setItem('hulyChatConversationId', 'conv-history')
    mockedGetHistory.mockResolvedValueOnce({
      content: [
        {
          id: 1,
          role: 'USER',
          content: 'hola',
          created_at: '2026-01-01T00:00:00Z',
        },
        {
          id: 2,
          role: 'ASSISTANT',
          content: 'hola yo',
          created_at: '2026-01-01T00:01:00Z',
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

    expect(mockedGetHistory).toHaveBeenCalledWith('conv-history')
    expect(result.current.messages).toEqual([
      { role: 'user', content: 'hola' },
      { role: 'assistant', content: 'hola yo' },
    ])
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

  it('decideSuggestedAction rejected sends follow-up message', async () => {
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
        },
        generated_challenge: null,
      } as never)
      .mockResolvedValueOnce({
        huly_reply: 'sigamos charlando',
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
      await result.current.decideSuggestedAction(1, 'rejected')
    })

    expect(mockedSendMessage).toHaveBeenNthCalledWith(2, {
      message: 'No voy a ir por ahora, prefiero seguir conversando',
      conversationId: expect.any(String),
    })
    expect(result.current.messages[1]).toMatchObject({
      role: 'assistant',
      suggestedActionDecision: 'rejected',
    })
  })
})

