import { useState, useEffect } from 'react'
import { chatbotApi, WellbeingResponse } from '../../api/chatbot'
import { SectionCard, CardHeader } from './SectionCard'
import { WellbeingChart } from './WellbeingChart'

export function WellbeingSection() {
  const [data, setData] = useState<WellbeingResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    chatbotApi.getWellbeing()
      .then(setData)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return (
    <SectionCard>
      <CardHeader title="Evolución del bienestar general" />
      {loading ? (
        <div className="flex items-center justify-center py-6">
          <div className="h-5 w-5 animate-spin rounded-full border-2 border-[#8869AC] border-t-transparent" />
        </div>
      ) : data ? (
        <WellbeingChart points={data.points} labels={data.labels} />
      ) : null}
    </SectionCard>
  )
}
