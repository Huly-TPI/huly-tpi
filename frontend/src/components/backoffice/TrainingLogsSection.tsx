import { useState, useEffect } from 'react'
import { chatbotApi, TrainingLogResponse } from '../../api/chatbot'
import { SectionCard, CardHeader } from './SectionCard'

const EMOTION_CLS: Record<string, string> = {
  'Estrés':   'bg-orange-100 text-orange-700',
  'Ansiedad': 'bg-yellow-100 text-yellow-700',
  'Tristeza': 'bg-blue-100 text-blue-700',
  'Miedo':    'bg-pink-100 text-pink-700',
  'Enojo':    'bg-red-100 text-red-700',
  'Soledad':  'bg-stone-100 text-stone-600',
}

export function TrainingLogsSection() {
  const [logs, setLogs] = useState<TrainingLogResponse[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    chatbotApi.getTrainingLogs()
      .then(setLogs)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return (
    <SectionCard>
      <CardHeader title="Entrenamiento del sistema (Logs)" />
      {loading ? (
        <div className="flex items-center justify-center py-6">
          <div className="h-5 w-5 animate-spin rounded-full border-2 border-[#8869AC] border-t-transparent" />
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[480px]">
            <thead>
              <tr className="border-b border-gray-100 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                <th className="pb-3 pr-4 w-1/2">Mensaje del usuario</th>
                <th className="pb-3 pr-4">Emoción IA</th>
                <th className="pb-3 pr-4">Confianza</th>
                <th className="pb-3">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50">
              {logs.map((log, i) => (
                <tr key={i}>
                  <td className="py-3 pr-4 text-sm font-medium text-gray-700">{log.message}</td>
                  <td className="py-3 pr-4">
                    <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${EMOTION_CLS[log.emotion] ?? 'bg-gray-100 text-gray-600'}`}>
                      {log.emotion}
                    </span>
                  </td>
                  <td className="py-3 pr-4 text-sm font-semibold text-gray-500">{log.confidence}%</td>
                  <td className="py-3">
                    <div className="flex gap-2">
                      <button className="rounded-lg p-1 text-bosque hover:bg-green-50 transition-colors">
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                        </svg>
                      </button>
                      <button className="rounded-lg p-1 text-red-400 hover:bg-red-50 transition-colors">
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </SectionCard>
  )
}
