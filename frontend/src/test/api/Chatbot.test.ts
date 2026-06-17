import { beforeEach, describe, expect, it, vi } from 'vitest'
import { chatbotApi } from '../../api/chatbot'
import { api } from '../../api/client'

vi.mock('../../api/client', () => ({
  api: {
    get: vi.fn(),
    put: vi.fn(),
  },
}))

describe('chatbotApi configuration', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('sends personalization question flags when updating bot config', async () => {
    vi.mocked(api.put).mockResolvedValueOnce({} as never)

    await chatbotApi.updateBotConfig({
      riskDetectionEnabled: true,
      systemPrompt: 'Prompt',
      preferredNameQuestionEnabled: false,
      communicationStyleQuestionEnabled: true,
    })

    expect(api.put).toHaveBeenCalledWith('/admin/chat/config', {
      riskDetectionEnabled: true,
      systemPrompt: 'Prompt',
      preferredNameQuestionEnabled: false,
      communicationStyleQuestionEnabled: true,
    })
  })
})
