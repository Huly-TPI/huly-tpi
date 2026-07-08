import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useChatbot } from '../../hooks/useChatbot'
import { chatApi } from '../../api/chat'
import { emotionalEventsApi } from '../../api/emotionalEvents'
import { saveAudioBlob } from '../../hooks/useAudioCache'

// --- SIMULACIONES GLOBALES (MOCKS) ---
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
  let rendered: ReturnType<typeof renderHook<ReturnType<typeof useChatbot>, undefined>>

  beforeEach(() => {
    clearAllMocks()
    clearLocalStorage()
    mockUrlObjectMethods()
  })

  // --- CASOS DE PRUEBA (TEST SUITE) ---

  it('carga el historial al montar usando el conversationId persistido', () => {
    setupPersistedConversationId('conv-history')
    setupHistoryResponse(getMockHistoryResponse())
    setupHook()
    return waitForHistoryLoadingFinished().then(() => {
      verifyGetHistoryCalledWith('conv-history', 0, 20)
      verifyMessages(getExpectedMessagesForHistory())
    })
  })

  it('restaurará el error de límite de cuota al montar si la fecha límite coincide con hoy y el usuario no tiene membresía activa', () => {
    setupQuotaLimitInLocalStorage()
    setupPersistedConversationId('some-conv-id')
    setupMembershipResolved({ active: false, planCode: null, productId: null, expiresAt: null })
    setupHistoryResponse(getMockEmptyHistoryResponse())
    setupHook()
    return waitForHistoryLoadingFinished().then(() => {
      verifyError('Alcanzaste el límite diario de 10 mensajes del plan gratuito.')
    })
  })

  it('carga una página de historial más antigua al hacer scroll hacia arriba', () => {
    setupPersistedConversationId('conv-paged')
    setupHistoryMultipleResponses(getMockHistoryPage0(), getMockHistoryPage1())
    setupHook()
    return waitForHistoryLoadingFinished()
      .then(() => {
        setupContainerScrollTopZero()
        return callMessagesScroll()
      })
      .then(() => waitForHistoryCalledTimes(2))
      .then(() => {
        verifyGetHistoryNthCall(1, 'conv-paged', 0, 20)
        verifyGetHistoryNthCall(2, 'conv-paged', 1, 20)
        verifyMessagesLength(2)
        verifyMessageAtIndex(0, { role: 'user', content: 'primer mensaje' })
        verifyMessageAtIndex(1, { role: 'assistant', content: 'ultima respuesta', generated_challenge: { title: 'Reto final', description: 'Desc final' } })
      })
  })

  it('sendMessage agrega los mensajes del usuario y del asistente', () => {
    setupHistoryResponse(getMockEmptyHistoryResponse())
    setupSendMessageResolved({
      huly_reply: 'respuesta',
      detected_emotion: null,
      intensity: null,
      suggested_action: null,
      generated_challenge: null,
      metadata: null,
    })
    setupHook()
    return waitForHistoryLoadingFinished()
      .then(() => {
        updateInput('mensaje de prueba')
        return callSendMessage()
      })
      .then(() => {
        verifySendMessageCalled()
        verifyMessages([
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
  })

  it('decideChallenge marca la decisión y envía un mensaje de seguimiento', () => {
    setupHistoryResponse(getMockEmptyHistoryResponse())
    setupSendMessageMultipleResponses(
      {
        huly_reply: 'te propongo un reto',
        suggested_action: null,
        generated_challenge: { title: 'Reto 1', description: 'Desc' },
      },
      {
        huly_reply: 'gracias por responder',
        suggested_action: null,
        generated_challenge: null,
      }
    )
    setupHook()
    return waitForHistoryLoadingFinished()
      .then(() => {
        updateInput('hola')
        return callSendMessage()
      })
      .then(() => callDecideChallenge(1, 'rejected'))
      .then(() => {
        verifySendMessageNthCallWith(2, {
          message: 'Rechazo este reto por ahora',
          conversationId: expect.any(String),
        })
        verifyMessageAtIndexMatches(1, {
          role: 'assistant',
          challengeDecision: 'rejected',
        })
      })
  })

  it('decideSuggestedAction rechazado guarda la decisión', () => {
    setupHistoryResponse(getMockEmptyHistoryResponse())
    setupSendMessageResolved({
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
    })
    setupUpdateDecisionResolved()
    setupHook()
    return waitForHistoryLoadingFinished()
      .then(() => {
        updateInput('hola')
        return callSendMessage()
      })
      .then(() => callDecideSuggestedAction(1, 'rejected'))
      .then(() => {
        verifySendMessageCalledTimes(1)
        verifyUpdateDecisionCalledWith(15, {
          decision: 'IGNORED',
          chosenActivityId: null,
        })
        verifyMessageAtIndexMatches(1, {
          role: 'assistant',
          suggestedActionDecision: 'rejected',
        })
      })
  })

  it('sendAudioMessage agrega el mensaje de audio del usuario y la respuesta del asistente', () => {
    setupHistoryResponse(getMockEmptyHistoryResponse())
    setupSendAudioMessageResolved({
      huly_reply: 'entendí tu mensaje de voz',
      detected_emotion: 'neutral',
      intensity: 3,
      suggested_action: null,
      generated_challenge: null,
      metadata: null,
    })
    setupHook()
    return waitForHistoryLoadingFinished().then(() => {
      return callSendAudioMessage(getMockAudioBlob()).then(() => {
        verifyMessageAtIndexMatches(0, {
          role: 'user',
          audioBlob: getMockAudioBlob(),
          audioUrl: 'blob:fake-url',
        })
        verifyMessageAtIndexMatches(1, {
          role: 'assistant',
          content: 'entendí tu mensaje de voz',
        })
      })
    })
  })

  it('sendAudioMessage guarda el blob en cache y rastrea la clave de audio en localStorage', () => {
    setupHistoryResponse(getMockEmptyHistoryResponse())
    setupSendAudioMessageResolved({
      huly_reply: 'ok',
      detected_emotion: null,
      intensity: null,
      suggested_action: null,
      generated_challenge: null,
      metadata: null,
    })
    setupHook()
    return waitForHistoryLoadingFinished().then(() => {
      return callSendAudioMessage(getMockAudioBlob()).then(() => {
        verifySaveAudioBlobCalledWithBlob(getMockAudioBlob())
        verifyAudioKeysTrackedInLocalStorage()
      })
    })
  })

  it('sendAudioMessage maneja el error de API sin romperse', () => {
    setupHistoryResponse(getMockEmptyHistoryResponse())
    setupSendAudioMessageRejected('red caída')
    setupHook()
    return waitForHistoryLoadingFinished().then(() => {
      return callSendAudioMessage(getMockAudioBlob()).then(() => {
        verifyIsSending(false)
        verifyError('red caída')
      })
    })
  })

  it('resetConversation genera un nuevo conversationId y limpia los mensajes', () => {
    setupHistoryResponse(getMockEmptyHistoryResponse())
    setupPersistedConversationId('old-conv-id')
    setupHook()
    return waitForHistoryLoadingFinished()
      .then(() => {
        verifyGetHistoryLastCalledWith('old-conv-id', 0, 20)
        return callResetConversation()
      })
      .then(() => {
        verifyNewConversationIdInLocalStorageNotEqual('old-conv-id')
        verifyMessages([])
      })
  })
  const setupHook = () => {
    rendered = renderHook(() => useChatbot())
  }

  const setupPersistedConversationId = (id: string) => {
    localStorage.setItem('hulyChatConversationId:1', id)
  }

  const setupQuotaLimitInLocalStorage = () => {
    const today = new Date().toISOString().split('T')[0]
    localStorage.setItem('huly:chat-limit-date:1', today)
    localStorage.setItem('huly:chat-limit-message:1', 'Alcanzaste el límite diario de 10 mensajes del plan gratuito.')
  }

  /* helpers */

  const setupMembershipResolved = (val: any) => {
    mockedGetMyMembership.mockResolvedValueOnce(val)
  }

  const setupHistoryResponse = (val: any) => {
    mockedGetHistory.mockResolvedValueOnce(val as never)
  }

  const setupHistoryMultipleResponses = (val0: any, val1: any) => {
    mockedGetHistory.mockResolvedValueOnce(val0 as never).mockResolvedValueOnce(val1 as never)
  }

  const setupSendMessageResolved = (val: any) => {
    mockedSendMessage.mockResolvedValueOnce(val as never)
  }

  const setupSendMessageMultipleResponses = (val0: any, val1: any) => {
    mockedSendMessage.mockResolvedValueOnce(val0 as never).mockResolvedValueOnce(val1 as never)
  }

  const setupSendAudioMessageResolved = (val: any) => {
    mockedSendAudioMessage.mockResolvedValueOnce(val as never)
  }

  const setupSendAudioMessageRejected = (msg: string) => {
    mockedSendAudioMessage.mockRejectedValueOnce(new Error(msg))
  }

  const setupUpdateDecisionResolved = () => {
    mockedUpdateDecision.mockResolvedValueOnce({} as never)
  }

  const getMockAudioBlob = () => {
    return new Blob(['audio'], { type: 'audio/webm' })
  }

  const getMockEmptyHistoryResponse = () => ({
    content: [],
    page_number: 0,
    page_size: 20,
    total_elements: 0,
    total_pages: 0,
    first: true,
    last: true,
  })

  const getMockHistoryResponse = () => ({
    content: [
      {
        id: 2,
        role: 'ASSISTANT',
        content: 'hola yo',
        suggested_action: {
          type: 'BREATHING',
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
  })

  const getMockHistoryPage0 = () => ({
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
  })

  const getMockHistoryPage1 = () => ({
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
  })

  const getExpectedMessagesForHistory = () => [
    { role: 'user', content: 'hola' },
    {
      role: 'assistant',
      content: 'hola yo',
      detected_emotion: undefined,
      suggested_action: {
        type: 'BREATHING',
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
  ]

  const updateInput = (val: string) => {
    act(() => {
      rendered.result.current.setInput(val)
    })
  }

  const callSendMessage = async () => {
    await act(async () => {
      await rendered.result.current.sendMessage()
    })
  }

  const callDecideChallenge = async (index: number, decision: 'accepted' | 'rejected') => {
    await act(async () => {
      await rendered.result.current.decideChallenge(index, decision)
    })
  }

  const callDecideSuggestedAction = async (index: number, decision: 'accepted' | 'rejected') => {
    await act(async () => {
      await rendered.result.current.decideSuggestedAction(index, decision)
    })
  }

  const callSendAudioMessage = async (blob: Blob) => {
    await act(async () => {
      await rendered.result.current.sendAudioMessage(blob)
    })
  }

  const callResetConversation = async () => {
    await act(async () => {
      await rendered.result.current.resetConversation()
    })
    await waitFor(() => {
      expect(mockedGetHistory).toHaveBeenCalledTimes(2)
    })
  }

  const setupContainerScrollTopZero = () => {
    const container = document.createElement('section')
    Object.defineProperty(container, 'scrollTop', { value: 0, writable: true })
    Object.defineProperty(container, 'scrollHeight', { value: 300, writable: true })
    ;(rendered.result.current.messagesContainerRef as { current: HTMLElement | null }).current = container
  }

  const callMessagesScroll = async () => {
    await act(async () => {
      rendered.result.current.handleMessagesScroll()
    })
  }

  const waitForHistoryLoadingFinished = () => {
    return waitFor(() => {
      expect(rendered.result.current.isLoadingHistory).toBe(false)
    })
  }

  const waitForHistoryCalledTimes = (times: number) => {
    return waitFor(() => {
      expect(mockedGetHistory).toHaveBeenCalledTimes(times)
    })
  }

  const verifyGetHistoryCalledWith = (id: string, page: number, size: number) => {
    expect(mockedGetHistory).toHaveBeenCalledWith(id, page, size)
  }

  const verifyGetHistoryLastCalledWith = (id: string, page: number, size: number) => {
    expect(mockedGetHistory).toHaveBeenLastCalledWith(id, page, size)
  }

  const verifyGetHistoryNthCall = (n: number, id: string, page: number, size: number) => {
    expect(mockedGetHistory).toHaveBeenNthCalledWith(n, id, page, size)
  }

  const verifyMessages = (expected: any[]) => {
    expect(rendered.result.current.messages).toEqual(expected)
  }

  const verifyMessagesLength = (len: number) => {
    expect(rendered.result.current.messages).toHaveLength(len)
  }

  const verifyMessageAtIndex = (idx: number, expected: any) => {
    expect(rendered.result.current.messages[idx]).toMatchObject(expected)
  }

  const verifyMessageAtIndexMatches = (idx: number, expected: any) => {
    expect(rendered.result.current.messages[idx]).toMatchObject(expected)
  }

  const verifyError = (expected: string) => {
    expect(rendered.result.current.error).toBe(expected)
  }

  const verifySendMessageCalled = () => {
    expect(mockedSendMessage).toHaveBeenCalled()
  }

  const verifySendMessageCalledTimes = (times: number) => {
    expect(mockedSendMessage).toHaveBeenCalledTimes(times)
  }

  const verifySendMessageNthCallWith = (n: number, expectedArgs: any) => {
    expect(mockedSendMessage).toHaveBeenNthCalledWith(n, expectedArgs)
  }

  const verifyUpdateDecisionCalledWith = (id: number, decisionObj: any) => {
    expect(mockedUpdateDecision).toHaveBeenCalledWith(id, decisionObj)
  }

  const verifySaveAudioBlobCalledWithBlob = (blob: Blob) => {
    expect(mockedSaveAudioBlob).toHaveBeenCalledWith(expect.stringContaining(':'), blob)
  }

  const verifyAudioKeysTrackedInLocalStorage = () => {
    const storedKeys = Object.keys(localStorage).find(k => k.startsWith('hulyAudioKeys:'))
    expect(storedKeys).toBeDefined()
  }

  const verifyIsSending = (expected: boolean) => {
    expect(rendered.result.current.isSending).toBe(expected)
  }

  const verifyNewConversationIdInLocalStorageNotEqual = (oldId: string) => {
    const newConvId = localStorage.getItem('hulyChatConversationId:1')
    expect(newConvId).not.toBe(oldId)
    expect(newConvId).not.toBeNull()
  }
  const clearLocalStorage = () => {
    localStorage.clear()
  }

  const mockUrlObjectMethods = () => {
    global.URL.createObjectURL = vi.fn().mockReturnValue('blob:fake-url')
    global.URL.revokeObjectURL = vi.fn()
  }
})
