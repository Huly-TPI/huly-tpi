import { Check, X } from 'lucide-react'
import { useTrainingLogs } from '../../hooks/backoffice/useTrainingLogs'
import { SectionCard, CardHeader } from './SectionCard'
import { Skeleton } from './Skeleton'

const EMOTION_CLS: Record<string, string> = {
  'Estrés': 'bg-orange-100 text-orange-700 dark:bg-orange-950/30 dark:text-orange-400',
  Ansiedad: 'bg-yellow-100 text-yellow-700 dark:bg-yellow-950/30 dark:text-yellow-400',
  Tristeza: 'bg-blue-100 text-blue-700 dark:bg-blue-950/30 dark:text-blue-400',
  Miedo: 'bg-pink-100 text-pink-700 dark:bg-pink-950/30 dark:text-pink-400',
  Enojo: 'bg-red-100 text-red-700 dark:bg-red-950/30 dark:text-red-400',
  Soledad: 'bg-stone-100 text-stone-600 dark:bg-stone-800/40 dark:text-stone-300',
}

export function TrainingLogsSection() {
  const { logs, loading } = useTrainingLogs()

  return (
    <SectionCard>
      <CardHeader title="Entrenamiento del sistema (Logs)" />
      {loading ? (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[480px]">
            <thead>
              <tr className="border-b border-gray-100 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-400 dark:border-gray-800 dark:text-gray-500">
                <th className="w-1/2 pb-3 pr-4">Mensaje del usuario</th>
                <th className="pb-3 pr-4">Emoción IA</th>
                <th className="pb-3 pr-4">Confianza</th>
                <th className="pb-3">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 dark:divide-gray-800/40">
              {Array.from({ length: 5 }).map((_, i) => (
                <tr key={i}>
                  <td className="py-3 pr-4"><Skeleton className="h-4 w-full" /></td>
                  <td className="py-3 pr-4"><Skeleton className="h-5 w-16 rounded-full" /></td>
                  <td className="py-3 pr-4"><Skeleton className="h-4 w-12" /></td>
                  <td className="py-3"><Skeleton className="h-6 w-14 rounded-lg" /></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[480px]">
            <thead>
              <tr className="border-b border-gray-100 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-400 dark:border-gray-800 dark:text-gray-500">
                <th className="w-1/2 pb-3 pr-4">Mensaje del usuario</th>
                <th className="pb-3 pr-4">Emoción IA</th>
                <th className="pb-3 pr-4">Confianza</th>
                <th className="pb-3">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 dark:divide-gray-800/40">
              {logs.map((log, i) => (
                <tr key={i}>
                  <td className="py-3 pr-4 text-sm font-medium text-gray-700 dark:text-gray-200">{log.message}</td>
                  <td className="py-3 pr-4">
                    <span className={`inline-block w-24 rounded-full py-0.5 text-center text-xs font-semibold ${EMOTION_CLS[log.emotion] ?? 'bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300'}`}>
                      {log.emotion}
                    </span>
                  </td>
                  <td className="py-3 pr-4 text-sm font-semibold text-gray-500 dark:text-gray-400">{log.confidence}%</td>
                  <td className="py-3">
                    <div className="flex gap-2">
                      <button className="rounded-lg p-1 text-bosque transition-colors hover:bg-green-50 dark:hover:bg-green-950/20">
                        <Check className="h-4 w-4" strokeWidth={2} />
                      </button>
                      <button className="rounded-lg p-1 text-red-400 transition-colors hover:bg-red-50 dark:hover:bg-red-950/20">
                        <X className="h-4 w-4" strokeWidth={2} />
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
