import { useState, useEffect } from 'react'
import { chatbotApi, TrainingLogResponse } from '../../api/chatbot'

export function useTrainingLogs() {
  const [logs, setLogs] = useState<TrainingLogResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    chatbotApi.getTrainingLogs()
      .then(setLogs)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return { logs, loading }
}
