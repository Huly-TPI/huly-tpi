import { clearAllMocks, setupMockedPostResponse, setupMockedGetResponse, setupMockedPostMultipartResponse } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { chatApi } from '../../api/chat'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
  api: {
    post: vi.fn(),
    get: vi.fn(),
    postMultipart: vi.fn(),
  },
}))

const mockedPost = vi.mocked(api.post)
const mockedGet = vi.mocked(api.get)
const mockedPostMultipart = vi.mocked(api.postMultipart)

describe('chatApi', () => {
  beforeEach(() => {
    clearAllMocks()
  })

  it('sendMessage llama a POST /chat con el DTO', () => {
    setupMockedPostResponse({ huly_reply: 'ok' })
    return callSendMessage('hola', 'conv-1').then(() => {
      verifyPostCalledWith('/chat', { message: 'hola', conversationId: 'conv-1' })
    })
  })

  it('getHistory llama a GET /chat/{conversationId}/messages con los valores por defecto', () => {
    setupMockedGetResponse({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    })
    return callGetHistory('conv-1').then(() => {
      verifyGetCalledWith('/chat/conv-1/messages?page=0&size=20')
    })
  })

  it('sendAudioMessage llama a postMultipart /chat/audio con el blob y conversationId', () => {
    setupMockedPostMultipartResponse({ huly_reply: 'entendí tu audio' })
    return callSendAudioMessageWithDummyBlob('conv-1').then(() => {
      verifyPostMultipartCalledWithAudioAndConv('conv-1')
    })
  })

  it('sendAudioMessage reenvía el AbortSignal a postMultipart', () => {
    setupMockedPostMultipartResponse({ huly_reply: 'ok' })
    return callSendAudioMessageWithAbortSignal('conv-1').then(() => {
      verifyPostMultipartCalledWithSignal()
    })
  })

  it('getHistory codifica el conversationId y reenvía la paginación personalizada', () => {
    setupMockedGetResponse({
      content: [],
      page_number: 1,
      page_size: 5,
      total_elements: 0,
      total_pages: 0,
      first: false,
      last: true,
    })
    return callGetHistory('conv with spaces', 1, 5).then(() => {
      verifyGetCalledWith('/chat/conv%20with%20spaces/messages?page=1&size=5')
    })
  })

  /* helpers */

  

  

  

  const callSendMessage = (message: string, conversationId: string) => {
    return chatApi.sendMessage({ message, conversationId })
  }

  const callGetHistory = (conversationId: string, page?: number, size?: number) => {
    return chatApi.getHistory(conversationId, page, size)
  }

  const callSendAudioMessageWithDummyBlob = (conversationId: string) => {
    const blob = new Blob(['audio-data'], { type: 'audio/webm' })
    return chatApi.sendAudioMessage(blob, conversationId)
  }

  const callSendAudioMessageWithAbortSignal = (conversationId: string) => {
    const blob = new Blob(['audio'], { type: 'audio/webm' })
    const signal = new AbortController().signal
    return chatApi.sendAudioMessage(blob, conversationId, signal)
  }

  const verifyPostCalledWith = (url: string, body: any) => {
    expect(mockedPost).toHaveBeenCalledWith(url, body)
  }

  const verifyGetCalledWith = (url: string) => {
    expect(mockedGet).toHaveBeenCalledWith(url)
  }

  const verifyPostMultipartCalledWithAudioAndConv = (expectedConvId: string) => {
    expect(mockedPostMultipart).toHaveBeenCalledWith(
      '/chat/audio',
      expect.any(FormData),
      undefined,
    )
    const formData: FormData = mockedPostMultipart.mock.calls[0][1] as FormData
    expect(formData.get('conversationId')).toBe(expectedConvId)
    expect(formData.get('audio')).toBeInstanceOf(Blob)
  }

  const verifyPostMultipartCalledWithSignal = () => {
    expect(mockedPostMultipart).toHaveBeenCalledWith(
      '/chat/audio',
      expect.any(FormData),
      expect.any(AbortSignal),
    )
  }
})

