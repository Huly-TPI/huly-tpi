import { useState, useEffect } from 'react'
import { BotConfigResponse } from '../../api/chatbot'
import { SectionCard, CardHeader } from './SectionCard'
import { Toggle } from './Toggle'

interface BotConfigSectionProps {
  config: BotConfigResponse | null
  loading: boolean
  onSave: (riskDetectionEnabled: boolean, systemPrompt: string) => Promise<void>
}

export function BotConfigSection({ config, loading, onSave }: BotConfigSectionProps) {
  const [prompt, setPrompt] = useState(config?.system_prompt ?? '')
  const [riskEnabled, setRiskEnabled] = useState(config?.risk_detection_enabled ?? true)
  const [saving, setSaving] = useState(false)
  const [saved, setSaved] = useState(false)

  useEffect(() => {
    if (config) {
      setPrompt(config.system_prompt)
      setRiskEnabled(config.risk_detection_enabled)
    }
  }, [config])

  async function handleSave() {
    setSaving(true)
    setSaved(false)
    try {
      await onSave(riskEnabled, prompt)
      setSaved(true)
      setTimeout(() => setSaved(false), 2500)
    } finally {
      setSaving(false)
    }
  }

  return (
    <SectionCard>
      <CardHeader title="Configuración del bot" />
      {loading ? (
        <div className="flex items-center justify-center py-8">
          <div className="h-6 w-6 animate-spin rounded-full border-2 border-violeta border-t-transparent" />
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          <Toggle
            enabled={riskEnabled}
            onChange={setRiskEnabled}
            label="Detección de riesgo activa"
            description="El bot evaluará frases de riesgo en cada mensaje"
          />
          <div>
            <label className="mb-1.5 block text-xs font-semibold text-gray-500">Prompt del sistema</label>
            <textarea
              rows={5}
              value={prompt}
              onChange={e => setPrompt(e.target.value)}
              placeholder="Eres Huly, un asistente de bienestar emocional..."
              className="w-full resize-none rounded-xl border border-gray-100 bg-gray-50 px-3 py-2.5 text-sm text-gray-700 focus:outline-none focus:border-violeta focus:bg-white focus:ring-1 focus:ring-violeta/20 transition-all"
            />
          </div>
          <button
            onClick={handleSave}
            disabled={saving || !prompt.trim()}
            className="self-end rounded-xl bg-violeta px-5 py-2 text-sm font-semibold text-white shadow-sm disabled:opacity-40 hover:bg-violeta/90 transition-colors"
          >
            {saving ? 'Guardando...' : saved ? '✓ Guardado' : 'Guardar cambios'}
          </button>
        </div>
      )}
    </SectionCard>
  )
}
