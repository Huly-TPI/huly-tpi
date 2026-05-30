import { useState, useEffect } from 'react'
import { chatbotApi, WellbeingResponse } from '../../api/chatbot'

export function useWellbeing() {
  const [data, setData] = useState<WellbeingResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    chatbotApi.getWellbeing()
      .then(setData)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return { data, loading }
}
