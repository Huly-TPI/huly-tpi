import { clearAllMocks, setupMockedPutResponse } from '../testHelpers'
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
    clearAllMocks()
  })

  it('envía flags de preguntas de personalización al actualizar la configuración del bot', () => {
    setupMockedPutResponse({})
    return callUpdateBotConfig({
      riskDetectionEnabled: true,
      systemPrompt: 'Prompt',
      preferredNameQuestionEnabled: false,
      communicationStyleQuestionEnabled: true,
    }).then(() => {
      verifyPutCalledWith('/admin/chat/config', {
        riskDetectionEnabled: true,
        systemPrompt: 'Prompt',
        preferredNameQuestionEnabled: false,
        communicationStyleQuestionEnabled: true,
      })
    })
  })

  /* helpers */

  

  const callUpdateBotConfig = (config: any) => {
    return chatbotApi.updateBotConfig(config)
  }

  const verifyPutCalledWith = (url: string, body: any) => {
    expect(api.put).toHaveBeenCalledWith(url, body)
  }
})
