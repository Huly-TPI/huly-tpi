import { useState, useCallback, useEffect } from 'react'
import { chatbotApi, BotConfigResponse } from '../../api/chatbot'

export function useBotConfig() {
  const [config, setConfig] = useState<BotConfigResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const res = await chatbotApi.getBotConfig()
      setConfig(res)
    } catch {
      // config may not exist yet
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  const handleSave = useCallback(async (riskDetectionEnabled: boolean, systemPrompt: string) => {
    try {
      const res = await chatbotApi.updateBotConfig({ riskDetectionEnabled, systemPrompt })
      setConfig(res)
    } catch {
      setError('No se pudo guardar la configuración')
    }
  }, [])

  return {
    config, loading,
    error, clearError: () => setError(null),
    handleSave,
  }
}
