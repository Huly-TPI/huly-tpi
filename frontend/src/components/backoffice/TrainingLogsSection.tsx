import { SectionCard, CardHeader } from './SectionCard'

// TODO: replace with real training logs / message history endpoint
const TRAINING_LOGS = [
  { message: '"Me siento muy agobiado por el trabajo..."', emotion: 'Estrés',   emotionCls: 'bg-orange-100 text-orange-700', confidence: 98.4 },
  { message: '"No sé qué hacer, mi corazón late rápido."', emotion: 'Ansiedad', emotionCls: 'bg-yellow-100 text-yellow-700', confidence: 84.2 },
  { message: '"Hoy ha sido un día realmente gris."',        emotion: 'Tristeza', emotionCls: 'bg-blue-100 text-blue-700',    confidence: 92.7 },
]

export function TrainingLogsSection() {
  return (
    <SectionCard>
      <CardHeader title="Entrenamiento del sistema (Logs)" />
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
            {TRAINING_LOGS.map((log, i) => (
              <tr key={i}>
                <td className="py-3 pr-4 text-sm font-medium text-gray-700">{log.message}</td>
                <td className="py-3 pr-4">
                  <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${log.emotionCls}`}>
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
    </SectionCard>
  )
}
