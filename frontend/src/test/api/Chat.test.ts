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
    vi.clearAllMocks()
  })

  it('sendMessage calls POST /chat with dto', async () => {
    mockedPost.mockResolvedValueOnce({
      huly_reply: 'ok',
    } as never)

    await chatApi.sendMessage({
      message: 'hola',
      conversationId: 'conv-1',
    })

    expect(mockedPost).toHaveBeenCalledWith('/chat', {
      message: 'hola',
      conversationId: 'conv-1',
    })
  })

  it('getHistory calls GET /chat/{conversationId}/messages with defaults', async () => {
    mockedGet.mockResolvedValueOnce({
      content: [],
      page_number: 0,
      page_size: 20,
      total_elements: 0,
      total_pages: 0,
      first: true,
      last: true,
    } as never)

    await chatApi.getHistory('conv-1')

    expect(mockedGet).toHaveBeenCalledWith('/chat/conv-1/messages?page=0&size=20')
  })

  it('sendAudioMessage calls postMultipart /chat/audio with blob and conversationId', async () => {
    mockedPostMultipart.mockResolvedValueOnce({
      huly_reply: 'entendí tu audio',
    } as never)
    const blob = new Blob(['audio-data'], { type: 'audio/webm' })

    await chatApi.sendAudioMessage(blob, 'conv-1')

    expect(mockedPostMultipart).toHaveBeenCalledWith(
      '/chat/audio',
      expect.any(FormData),
      undefined,
    )
    const formData: FormData = mockedPostMultipart.mock.calls[0][1] as FormData
    expect(formData.get('conversationId')).toBe('conv-1')
    expect(formData.get('audio')).toBeInstanceOf(Blob)
  })

  it('sendAudioMessage forwards AbortSignal to postMultipart', async () => {
    mockedPostMultipart.mockResolvedValueOnce({ huly_reply: 'ok' } as never)
    const blob = new Blob(['audio'], { type: 'audio/webm' })
    const signal = new AbortController().signal

    await chatApi.sendAudioMessage(blob, 'conv-1', signal)

    expect(mockedPostMultipart).toHaveBeenCalledWith(
      '/chat/audio',
      expect.any(FormData),
      signal,
    )
  })

  it('getHistory encodes conversationId and forwards custom pagination', async () => {
    mockedGet.mockResolvedValueOnce({
      content: [],
      page_number: 1,
      page_size: 5,
      total_elements: 0,
      total_pages: 0,
      first: false,
      last: true,
    } as never)

    await chatApi.getHistory('conv with spaces', 1, 5)

    expect(mockedGet).toHaveBeenCalledWith('/chat/conv%20with%20spaces/messages?page=1&size=5')
  })
})

