import { mapEmotionToSpanish } from '../../../types/ai'
import { User, Heart, ThumbsUp, ThumbsDown, Brain, Sparkles } from 'lucide-react'
import { UserAiDiagnosticsResponse } from '../../../api/admin'

interface AiDiagnosticsTabProps {
  aiDiagnostics: UserAiDiagnosticsResponse | null
  aiLoading: boolean
  aiError: string | null
}

export function AiDiagnosticsTab({
  aiDiagnostics,
  aiLoading,
  aiError,
}: AiDiagnosticsTabProps) {
  if (aiLoading && !aiDiagnostics) {
    return (
      <div className="flex flex-col items-center justify-center py-20 gap-3">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-violeta border-t-transparent"></div>
        <p className="text-xs text-gray-550 dark:text-gray-400 font-bold">Cargando diagnóstico de IA...</p>
      </div>
    )
  }

  if (aiError) {
    return (
      <div className="py-12 text-center text-sm text-red-500 font-semibold flex items-center justify-center">
        {aiError}
      </div>
    )
  }

  if (!aiDiagnostics) return null

  return (
    <div className="flex flex-col gap-6 animate-fadeIn lg:flex-grow lg:min-h-0">
      {/* Grid 1: Synthesized Summary & Personality Profile */}
      <div className="bg-gradient-to-br from-violeta/10 to-violeta-claro/5 dark:from-[#2A233C]/20 dark:to-transparent border border-violeta/20 rounded-2xl p-5 flex flex-col md:flex-row gap-6 shadow-sm shrink-0">
        <div className="flex-grow flex flex-col gap-3 justify-center">
          <div className="flex items-center gap-2 mb-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
              <User className="h-5 w-5" strokeWidth={1.8} />
            </div>
            <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Perfil Psicológico y Conductual (Síntesis de IA)</h2>
          </div>
          <p className="text-sm text-gray-750 dark:text-gray-305 leading-relaxed font-medium">
            {aiDiagnostics.personalitySummary}
          </p>
          
          <div className="flex flex-wrap gap-2 mt-2">
            {aiDiagnostics.topicsDetected?.map((topic, i) => (
              <span key={i} className="px-2.5 py-1 text-xs font-bold rounded-lg bg-red-50 dark:bg-red-955/20 text-red-700 dark:text-red-400 border border-red-100 dark:border-red-955/30">
                Foco: {topic}
              </span>
            ))}
            {aiDiagnostics.copingStrategies?.map((strategy, i) => (
              <span key={i} className="px-2.5 py-1 text-xs font-bold rounded-lg bg-blue-50 dark:bg-blue-955/20 text-blue-700 dark:text-blue-400 border border-blue-100 dark:border-blue-955/30">
                Preferencia: {strategy}
              </span>
            ))}
            {(!aiDiagnostics.topicsDetected?.length && !aiDiagnostics.copingStrategies?.length) && (
              <span className="text-xs text-gray-400 font-semibold italic">Esperando más interacciones para extraer preferencias específicas.</span>
            )}
          </div>
        </div>
      </div>

      {/* Grid 2: Decision Tracking (Acceptance), Assistant Preferences & Emotion Distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 lg:flex-grow lg:min-h-0">
        
        {/* Receptivity Card */}
        <div className="bg-gray-55/50 dark:bg-[#09111f]/40 border border-gray-100 dark:border-gray-800/40 rounded-xl p-5 flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
                <Heart className="h-5 w-5" strokeWidth={1.8} />
              </div>
              <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Receptividad a las Sugerencias</h2>
            </div>
            <div className="flex items-center gap-4 mb-4">
              {(() => {
                const score = aiDiagnostics.receptivityScore ?? 0
                const radius = 24
                const circumference = 2 * Math.PI * radius
                const strokeDashoffset = circumference - (score / 100) * circumference
                return (
                  <div className="relative flex items-center justify-center h-16 w-16 shrink-0">
                    <svg className="h-16 w-16 transform -rotate-90">
                      <circle
                        className="text-gray-150 dark:text-gray-800"
                        strokeWidth="4.5"
                        stroke="currentColor"
                        fill="transparent"
                        r={radius}
                        cx="32"
                        cy="32"
                      />
                      <circle
                        className="text-violeta transition-all duration-500"
                        strokeWidth="4.5"
                        strokeDasharray={circumference}
                        strokeDashoffset={strokeDashoffset}
                        strokeLinecap="round"
                        stroke="currentColor"
                        fill="transparent"
                        r={radius}
                        cx="32"
                        cy="32"
                      />
                    </svg>
                    <span className="absolute text-xs font-black text-violeta dark:text-violeta-claro">{score}%</span>
                  </div>
                )
              })()}
              <div>
                <div className="text-sm font-extrabold text-gray-800 dark:text-gray-100 capitalize">
                  {aiDiagnostics.receptivityLabel}
                </div>
                <div className="text-xs text-gray-400 dark:text-gray-555 mt-0.5">
                  Tasa de aceptación de las recomendaciones y retos sugeridos.
                </div>
              </div>
            </div>

            <div className="space-y-3 pt-2 border-t border-gray-100 dark:border-gray-800/40">
              <div>
                <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block mb-1">Suele Aceptar:</span>
                {(aiDiagnostics.acceptedActivities?.length ?? 0) > 0 ? (
                  <div className="text-xs text-gray-600 dark:text-gray-300 font-medium flex items-start gap-1.5">
                    <ThumbsUp className="h-4 w-4 text-green-600 dark:text-green-400 shrink-0 mt-0.5" strokeWidth={2.2} />
                    <span>
                      {(() => {
                        const text = aiDiagnostics.acceptedActivities?.join(', ') ?? '';
                        return text ? text.charAt(0).toUpperCase() + text.slice(1) : '';
                      })()}
                    </span>
                  </div>
                ) : (
                  <span className="text-xs text-gray-400 italic">No se registran recomendaciones aceptadas.</span>
                )}
              </div>

              <div className="pt-1">
                <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider block mb-1">Suele Ignorar/Rechazar:</span>
                {(aiDiagnostics.ignoredActivities?.length ?? 0) > 0 ? (
                  <div className="text-xs text-gray-600 dark:text-gray-300 font-medium flex items-start gap-1.5">
                    <ThumbsDown className="h-4 w-4 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" strokeWidth={2.2} />
                    <span>
                      {(() => {
                        const text = aiDiagnostics.ignoredActivities?.join(', ') ?? '';
                        return text ? text.charAt(0).toUpperCase() + text.slice(1) : '';
                      })()}
                    </span>
                  </div>
                ) : (
                  <span className="text-xs text-gray-400 italic">No se registran recomendaciones rechazadas.</span>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Assistant Preferences Card */}
        <div className="bg-gray-55/50 dark:bg-[#09111f]/40 border border-gray-100 dark:border-gray-800/40 rounded-xl p-5 flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
                <Brain className="h-5 w-5" strokeWidth={1.8} />
              </div>
              <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">Preferencias de Personalización</h2>
            </div>
            <div className="space-y-3">
              <p className="text-xs text-gray-400 dark:text-gray-555 font-semibold mb-2">Configuraciones del chatbot para este usuario:</p>
              <div className="bg-white dark:bg-[#172033] p-3.5 rounded-xl border border-gray-100 dark:border-gray-800/60 shadow-sm flex flex-col gap-1">
                <span className="text-[10px] font-bold text-gray-400 dark:text-gray-555 uppercase tracking-wider">Apodo Deseado</span>
                <span className="text-sm font-extrabold text-gray-750 dark:text-gray-200">
                  {aiDiagnostics.preferredName || 'sin nombre'}
                </span>
              </div>
              <div className="bg-white dark:bg-[#172033] p-3.5 rounded-xl border border-gray-100 dark:border-gray-800/60 shadow-sm flex flex-col gap-1">
                <span className="text-[10px] font-bold text-gray-400 dark:text-gray-555 uppercase tracking-wider">Tono del Chatbot</span>
                <span className="text-sm font-extrabold text-gray-750 dark:text-gray-200">
                  {aiDiagnostics.communicationStyle ? aiDiagnostics.communicationStyle.charAt(0).toUpperCase() + aiDiagnostics.communicationStyle.slice(1) : 'Amigo'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Emotion Distribution Card */}
        <div className="bg-gray-55/50 dark:bg-[#09111f]/40 border border-gray-100 dark:border-gray-800/40 rounded-xl p-5 flex flex-col">
          <div className="flex items-center gap-2 mb-4">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro">
              <Sparkles className="h-5 w-5" strokeWidth={1.8} />
            </div>
            <h2 className="text-base font-bold text-gray-700 dark:text-gray-200">
              Estado emocional predominante: <span className="text-violeta dark:text-violeta-claro font-extrabold">{mapEmotionToSpanish(aiDiagnostics.dominantEmotion ?? null)}</span>
            </h2>
          </div>
          {(() => {
            const emotionCounts = aiDiagnostics.emotionDistribution ?? {}
            const totalEmotions = Object.values(emotionCounts).reduce((a, b) => a + b, 0)
            if (totalEmotions === 0) {
              return <p className="text-xs text-gray-400 py-4 text-center">No hay registros emocionales capturados por la IA todavía.</p>
            }

            const sortedEmotions = Object.entries(emotionCounts).sort((a, b) => b[1] - a[1])
            const maxCount = Math.max(...Object.values(emotionCounts), 1)

            return (
              <div className="space-y-4">
                <p className="text-xs text-gray-400 dark:text-gray-500 font-semibold mb-2">Emociones detectadas en sus interacciones:</p>
                <div className="space-y-3.5">
                  {sortedEmotions.map(([emotion, count]) => {
                    const percentage = Math.round((count / totalEmotions) * 100)
                    const progressPct = (count / maxCount) * 100
                    return (
                      <div key={emotion} className="space-y-1 animate-fadeIn">
                        <div className="flex justify-between text-xs font-bold text-gray-600 dark:text-gray-400">
                          <span className="capitalize">{mapEmotionToSpanish(emotion)}</span>
                          <span className="text-gray-850 dark:text-gray-200">{percentage}% ({count})</span>
                        </div>
                        <div className="w-full h-2.5 bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-gradient-to-r from-violeta to-violeta-claro rounded-full transition-all duration-500"
                            style={{ width: `${progressPct}%` }}
                          />
                        </div>
                      </div>
                    )
                  })}
                </div>
              </div>
            )
          })()}
        </div>
      </div>
    </div>
  )
}
