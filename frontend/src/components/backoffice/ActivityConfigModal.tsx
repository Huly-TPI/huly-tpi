import React, { useEffect, useState } from 'react'
import Modal from './Modal'
import { AdminActivityConfig } from '../../api/adminActivities'
import Button from '../Buttons/Button/Button'
import { Smile, Star, Save, CirclePlay } from 'lucide-react'
import { ACTIVITY_ICONS } from './activities/activityUiMetadata'

interface ActivityConfigModalProps {
  isOpen: boolean
  onClose: () => void
  config: AdminActivityConfig | null
  onSave: (e: React.FormEvent, config: AdminActivityConfig) => void
  saveLoading: boolean
}

const inputClass =
  'rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm font-normal text-gray-700 focus:border-violeta focus:outline-none dark:border-gray-800 dark:bg-[#09111f] dark:text-gray-200 w-full'
const labelClass = 'flex flex-col gap-1 text-sm font-semibold text-[#4A5568] dark:text-gray-300'

export default function ActivityConfigModal({
  isOpen,
  onClose,
  config,
  onSave,
  saveLoading,
}: ActivityConfigModalProps) {
  const [localConfig, setLocalConfig] = useState<AdminActivityConfig | null>(null)

  useEffect(() => {
    if (config) {
      setLocalConfig({ ...config })
    }
  }, [config, isOpen])

  if (!localConfig) return null

  const handleSubmit = (e: React.FormEvent) => {
    onSave(e, localConfig)
  }

  const activitySpanish = localConfig.title
  const Icon = ACTIVITY_ICONS[localConfig.type] || CirclePlay

  // Helper format value for display
  const formatVal = (val: number) => {
    return val >= 0 ? `+${val.toFixed(2)}` : val.toFixed(2)
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Configurar Actividad"
      maxWidthClass="max-w-xl"
      hideDivider={true}
    >
      <form onSubmit={handleSubmit} className="flex flex-col gap-4 text-sm">
        {/* Header Icon + Name */}
        <div className="flex items-center gap-3 bg-[#F4EFFB] p-3 rounded-2xl dark:bg-[#0f1524] border border-[#EDF2ED] dark:border-gray-800/40">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#D1CAEF]/40 text-violeta dark:bg-[#D1CAEF]/10">
            <Icon className="h-5 w-5" />
          </div>
          <div>
            <h4 className="font-bold text-gray-800 dark:text-gray-100">{activitySpanish}</h4>
            <span className="text-[11px] font-bold text-violeta dark:text-violeta-claro uppercase tracking-widest">{localConfig.type}</span>
          </div>
        </div>

        {/* Title & Route */}
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <label className={labelClass}>
            Título
            <input
              type="text"
              value={localConfig.title}
              onChange={(e) => setLocalConfig({ ...localConfig, title: e.target.value })}
              className={inputClass}
            />
          </label>
          <label className={labelClass}>
            Ruta de Redirección
            <input
              type="text"
              value={localConfig.routePath}
              onChange={(e) => setLocalConfig({ ...localConfig, routePath: e.target.value })}
              className={`${inputClass} font-mono text-xs`}
            />
          </label>
        </div>

        {/* Description */}
        <label className={labelClass}>
          Descripción
          <textarea
            value={localConfig.description}
            onChange={(e) => setLocalConfig({ ...localConfig, description: e.target.value })}
            rows={2}
            className={inputClass}
          />
        </label>

        {/* Goal keywords */}
        <label className={labelClass}>
          Palabras Clave (Separadas por Coma)
          <input
            type="text"
            value={localConfig.goalKeywords}
            onChange={(e) => setLocalConfig({ ...localConfig, goalKeywords: e.target.value })}
            className={`${inputClass} text-xs`}
          />
        </label>

        {/* 6 Slider VAD Activation panel */}
        <div className="flex flex-col gap-3 rounded-2xl bg-gray-50/50 p-4 dark:bg-gray-900/20 border border-gray-100 dark:border-gray-800/40">
          <h4 className="font-bold text-gray-700 dark:text-gray-200 text-xs uppercase tracking-wider text-gray-400 flex items-center gap-1.5">
            <Smile className="h-4 w-4 text-violeta" /> Límites de Activación VAD <span className="text-[11px] lowercase text-gray-450 font-normal">(-1.0 a +1.0)</span>
          </h4>
          
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            {/* Valence */}
            <div className="flex flex-col gap-3 p-3 rounded-xl bg-white dark:bg-gray-950/20 border border-gray-50 dark:border-gray-800/20">
              <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                <span>VALENCIA MÍN.</span>
                <span className="font-mono text-violeta dark:text-violeta-claro">{formatVal(localConfig.valenceMin)}</span>
              </div>
              <input
                type="range"
                min="-1"
                max="1"
                step="0.05"
                value={localConfig.valenceMin}
                onChange={(e) => setLocalConfig({ ...localConfig, valenceMin: parseFloat(e.target.value) })}
                className="w-full accent-violeta bg-gray-200 rounded-lg appearance-none h-1.5 cursor-pointer dark:bg-gray-800"
              />
            </div>
            <div className="flex flex-col gap-3 p-3 rounded-xl bg-white dark:bg-gray-950/20 border border-gray-50 dark:border-gray-800/20">
              <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                <span>VALENCIA MÁX.</span>
                <span className="font-mono text-violeta dark:text-violeta-claro">{formatVal(localConfig.valenceMax)}</span>
              </div>
              <input
                type="range"
                min="-1"
                max="1"
                step="0.05"
                value={localConfig.valenceMax}
                onChange={(e) => setLocalConfig({ ...localConfig, valenceMax: parseFloat(e.target.value) })}
                className="w-full accent-violeta bg-gray-200 rounded-lg appearance-none h-1.5 cursor-pointer dark:bg-gray-800"
              />
            </div>

            {/* Arousal */}
            <div className="flex flex-col gap-3 p-3 rounded-xl bg-white dark:bg-gray-950/20 border border-gray-50 dark:border-gray-800/20">
              <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                <span>AROUSAL MÍN.</span>
                <span className="font-mono text-violeta dark:text-violeta-claro">{formatVal(localConfig.arousalMin)}</span>
              </div>
              <input
                type="range"
                min="-1"
                max="1"
                step="0.05"
                value={localConfig.arousalMin}
                onChange={(e) => setLocalConfig({ ...localConfig, arousalMin: parseFloat(e.target.value) })}
                className="w-full accent-violeta bg-gray-200 rounded-lg appearance-none h-1.5 cursor-pointer dark:bg-gray-800"
              />
            </div>
            <div className="flex flex-col gap-3 p-3 rounded-xl bg-white dark:bg-gray-950/20 border border-gray-50 dark:border-gray-800/20">
              <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                <span>AROUSAL MÁX.</span>
                <span className="font-mono text-violeta dark:text-violeta-claro">{formatVal(localConfig.arousalMax)}</span>
              </div>
              <input
                type="range"
                min="-1"
                max="1"
                step="0.05"
                value={localConfig.arousalMax}
                onChange={(e) => setLocalConfig({ ...localConfig, arousalMax: parseFloat(e.target.value) })}
                className="w-full accent-violeta bg-gray-200 rounded-lg appearance-none h-1.5 cursor-pointer dark:bg-gray-800"
              />
            </div>

            {/* Dominance */}
            <div className="flex flex-col gap-3 p-3 rounded-xl bg-white dark:bg-gray-950/20 border border-gray-50 dark:border-gray-800/20">
              <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                <span>DOMINANCIA MÍN.</span>
                <span className="font-mono text-violeta dark:text-violeta-claro">{formatVal(localConfig.dominanceMin)}</span>
              </div>
              <input
                type="range"
                min="-1"
                max="1"
                step="0.05"
                value={localConfig.dominanceMin}
                onChange={(e) => setLocalConfig({ ...localConfig, dominanceMin: parseFloat(e.target.value) })}
                className="w-full accent-violeta bg-gray-200 rounded-lg appearance-none h-1.5 cursor-pointer dark:bg-gray-800"
              />
            </div>
            <div className="flex flex-col gap-3 p-3 rounded-xl bg-white dark:bg-gray-950/20 border border-gray-50 dark:border-gray-800/20">
              <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                <span>DOMINANCIA MÁX.</span>
                <span className="font-mono text-violeta dark:text-violeta-claro">{formatVal(localConfig.dominanceMax)}</span>
              </div>
              <input
                type="range"
                min="-1"
                max="1"
                step="0.05"
                value={localConfig.dominanceMax}
                onChange={(e) => setLocalConfig({ ...localConfig, dominanceMax: parseFloat(e.target.value) })}
                className="w-full accent-violeta bg-gray-200 rounded-lg appearance-none h-1.5 cursor-pointer dark:bg-gray-800"
              />
            </div>
          </div>
        </div>

        {/* 3 Slider VAD Expected Effects panel */}
        <div className="flex flex-col gap-3 rounded-2xl bg-gray-50/50 p-4 dark:bg-gray-900/20 border border-gray-100 dark:border-gray-800/40">
          <h4 className="font-bold text-gray-700 dark:text-gray-200 text-xs uppercase tracking-wider text-gray-400 flex items-center gap-1.5">
            <Star className="h-4 w-4 text-violeta" /> Efectos Esperados VAD <span className="text-[11px] lowercase text-gray-450 font-normal">(deltas de cambio)</span>
          </h4>
          
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <div className="flex flex-col gap-3 p-3 rounded-xl bg-white dark:bg-gray-950/20 border border-gray-50 dark:border-gray-800/20">
              <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                <span>Δ VALENCIA</span>
                <span className="font-mono text-violeta dark:text-violeta-claro">{formatVal(localConfig.effectValence)}</span>
              </div>
              <input
                type="range"
                min="-1"
                max="1"
                step="0.05"
                value={localConfig.effectValence}
                onChange={(e) => setLocalConfig({ ...localConfig, effectValence: parseFloat(e.target.value) })}
                className="w-full accent-violeta bg-gray-200 rounded-lg appearance-none h-1.5 cursor-pointer dark:bg-gray-800"
              />
            </div>
            <div className="flex flex-col gap-3 p-3 rounded-xl bg-white dark:bg-gray-950/20 border border-gray-50 dark:border-gray-800/20">
              <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                <span>Δ AROUSAL</span>
                <span className="font-mono text-violeta dark:text-violeta-claro">{formatVal(localConfig.effectArousal)}</span>
              </div>
              <input
                type="range"
                min="-1"
                max="1"
                step="0.05"
                value={localConfig.effectArousal}
                onChange={(e) => setLocalConfig({ ...localConfig, effectArousal: parseFloat(e.target.value) })}
                className="w-full accent-violeta bg-gray-200 rounded-lg appearance-none h-1.5 cursor-pointer dark:bg-gray-800"
              />
            </div>
            <div className="flex flex-col gap-3 p-3 rounded-xl bg-white dark:bg-gray-950/20 border border-gray-50 dark:border-gray-800/20">
              <div className="flex items-center justify-between text-xs font-bold text-gray-500">
                <span>Δ DOMINANCIA</span>
                <span className="font-mono text-violeta dark:text-violeta-claro">{formatVal(localConfig.effectDominance)}</span>
              </div>
              <input
                type="range"
                min="-1"
                max="1"
                step="0.05"
                value={localConfig.effectDominance}
                onChange={(e) => setLocalConfig({ ...localConfig, effectDominance: parseFloat(e.target.value) })}
                className="w-full accent-violeta bg-gray-200 rounded-lg appearance-none h-1.5 cursor-pointer dark:bg-gray-800"
              />
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-3 mt-2">
          <Button variant="secondary" onClick={onClose} type="button">
            Cancelar
          </Button>
          <Button variant="primary" disabled={saveLoading} type="submit" className="flex items-center gap-1.5">
            <Save className="h-4 w-4" />
            {saveLoading ? 'Guardando...' : 'Guardar Ajustes'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}
