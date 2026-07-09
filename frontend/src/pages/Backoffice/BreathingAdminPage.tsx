import { useState } from 'react'
import { Plus, Pencil } from 'lucide-react'
import { useAdminBreathing } from '../../hooks/backoffice/useAdminBreathing'
import type { AdminBreathingTechnique, BreathingFormData } from '../../api/adminBreathing'
import { Toast } from '../../components/backoffice/Toast'
import Button from '../../components/Buttons/Button/Button'

const inputClass = 'rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 focus:border-violeta focus:outline-none dark:border-gray-800 dark:bg-[#09111f] dark:text-gray-200'
const labelClass = 'flex flex-col gap-1 text-sm font-semibold text-[#4A5568] dark:text-gray-300'

function BreathingForm({ technique, onClose, onSubmit }: {
  technique: AdminBreathingTechnique | null
  onClose: () => void
  onSubmit: (data: BreathingFormData) => Promise<boolean>
}) {
  const isEdit = technique != null
  const [name, setName] = useState(technique?.name ?? '')
  const [description, setDescription] = useState(technique?.description ?? '')
  const [inhaleSeconds, setInhaleSeconds] = useState(String(technique?.inhaleSeconds ?? 4))
  const [holdSeconds, setHoldSeconds] = useState(String(technique?.holdSeconds ?? 0))
  const [exhaleSeconds, setExhaleSeconds] = useState(String(technique?.exhaleSeconds ?? 4))
  const [roundsInterval, setRoundsInterval] = useState(String(technique?.roundsInterval ?? 1))
  const [rounds, setRounds] = useState(String(technique?.rounds ?? 4))
  const [saving, setSaving] = useState(false)

  const canSubmit = Boolean(name.trim())

  async function handleSubmit() {
    if (!canSubmit || saving) return
    setSaving(true)
    try {
      await onSubmit({
        name: name.trim(),
        description: description.trim(),
        inhaleSeconds: Number(inhaleSeconds) || 0,
        holdSeconds: Number(holdSeconds) || 0,
        exhaleSeconds: Number(exhaleSeconds) || 0,
        roundsInterval: Number(roundsInterval) || 0,
        rounds: Number(rounds) || 0,
      })
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
      <button type="button" aria-label="Cerrar" className="absolute inset-0 cursor-default" onClick={onClose} />
      <div className="relative z-10 flex max-h-[90vh] w-full max-w-md flex-col gap-3 overflow-y-auto rounded-2xl bg-white p-5 shadow-2xl dark:bg-[#172033]">
        <h2 className="text-lg font-bold text-[#8869AC]">{isEdit ? 'Editar respiración' : 'Nueva respiración'}</h2>
        <label className={labelClass}>Nombre
          <input value={name} onChange={e => setName(e.target.value)} className={inputClass} />
        </label>
        <label className={labelClass}>Descripción
          <input value={description} onChange={e => setDescription(e.target.value)} className={inputClass} />
        </label>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
          <label className={labelClass}>Inhalar (s)
            <input type="number" min="0" value={inhaleSeconds} onChange={e => setInhaleSeconds(e.target.value)} className={inputClass} />
          </label>
          <label className={labelClass}>Sostener (s)
            <input type="number" min="0" value={holdSeconds} onChange={e => setHoldSeconds(e.target.value)} className={inputClass} />
          </label>
          <label className={labelClass}>Exhalar (s)
            <input type="number" min="0" value={exhaleSeconds} onChange={e => setExhaleSeconds(e.target.value)} className={inputClass} />
          </label>
        </div>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <label className={labelClass}>Intervalo
            <input type="number" min="0" value={roundsInterval} onChange={e => setRoundsInterval(e.target.value)} className={inputClass} />
          </label>
          <label className={labelClass}>Rondas
            <input type="number" min="0" value={rounds} onChange={e => setRounds(e.target.value)} className={inputClass} />
          </label>
        </div>
        <div className="mt-1 flex flex-col gap-2 sm:flex-row">
          <Button variant="primary" onClick={handleSubmit} isLoading={saving} loadingLabel="Guardando..." disabled={!canSubmit || saving} className="flex-1">
            {isEdit ? 'Guardar' : 'Crear'}
          </Button>
          <Button variant="secondary" onClick={onClose}>Cancelar</Button>
        </div>
      </div>
    </div>
  )
}

export default function BreathingAdminPage() {
  const { techniques, loading, error, clearError, create, update, setActive } = useAdminBreathing()
  const [editing, setEditing] = useState<AdminBreathingTechnique | null>(null)
  const [showForm, setShowForm] = useState(false)

  const openCreate = () => { setEditing(null); setShowForm(true) }
  const openEdit = (t: AdminBreathingTechnique) => { setEditing(t); setShowForm(true) }
  const closeForm = () => { setShowForm(false); setEditing(null) }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl font-extrabold leading-tight text-[#8869AC] sm:text-[30px]">Respiraciones guiadas</h1>
          <p className="text-sm text-[#A0AEC0] sm:text-[16px]">Alta, edición y listado de respiraciones guiadas</p>
        </div>
        <Button variant="primary" className="self-start" onClick={openCreate}>
          <Plus className="h-4 w-4" strokeWidth={2} /> Nueva respiración
        </Button>
      </div>

      {loading ? (
        <p className="py-8 text-center text-sm text-[#A0AEC0]">Cargando...</p>
      ) : techniques.length === 0 ? (
        <p className="py-8 text-center text-sm text-[#A0AEC0]">Todavía no hay respiraciones.</p>
      ) : (
        <>
        <ul className="flex flex-col gap-3 sm:hidden">
          {techniques.map(t => (
            <li key={t.id} className="flex flex-col gap-3 rounded-2xl border border-[#EDF2ED] bg-white p-4 dark:border-gray-800 dark:bg-[#172033]">
              <div className="flex items-start justify-between gap-2">
                <span className="font-semibold text-[#4A5568] dark:text-gray-200">{t.name}</span>
                <span className={`shrink-0 rounded-full px-2 py-0.5 text-[11px] font-semibold ${t.active ? 'bg-[#E9F1EA] text-[#4C7C64]' : 'bg-gray-200 text-gray-500'}`}>{t.active ? 'Activa' : 'Inactiva'}</span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="flex flex-col"><span className="text-[11px] font-semibold uppercase text-[#8869AC]">Inhalar</span><span className="text-[#4A5568] dark:text-gray-300">{t.inhaleSeconds}s</span></div>
                <div className="flex flex-col"><span className="text-[11px] font-semibold uppercase text-[#8869AC]">Sostener</span><span className="text-[#4A5568] dark:text-gray-300">{t.holdSeconds}s</span></div>
                <div className="flex flex-col"><span className="text-[11px] font-semibold uppercase text-[#8869AC]">Exhalar</span><span className="text-[#4A5568] dark:text-gray-300">{t.exhaleSeconds}s</span></div>
                <div className="flex flex-col"><span className="text-[11px] font-semibold uppercase text-[#8869AC]">Rondas</span><span className="text-[#4A5568] dark:text-gray-300">{t.rounds}</span></div>
              </div>
              <div className="flex gap-2 border-t border-[#EDF2ED] pt-2 dark:border-gray-800">
                <button onClick={() => openEdit(t)} className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold text-violeta hover:bg-[#D1CAEF]/30" aria-label={`Editar ${t.name}`}><Pencil className="h-3.5 w-3.5" /> Editar</button>
                <button onClick={() => setActive(t.id, !t.active)} className={`rounded-lg px-2 py-1 text-xs font-semibold ${t.active ? 'text-anaranjado hover:bg-anaranjado/10' : 'text-[#4C7C64] hover:bg-[#E9F1EA]'}`} aria-label={t.active ? `Eliminar ${t.name}` : `Restaurar ${t.name}`}>{t.active ? 'Eliminar' : 'Restaurar'}</button>
              </div>
            </li>
          ))}
        </ul>
        <div className="hidden overflow-x-auto rounded-2xl border border-[#EDF2ED] dark:border-gray-800 sm:block">
          <table className="w-full min-w-[720px] text-left text-sm">
            <thead className="bg-[#F4EFFB] text-xs uppercase text-[#8869AC] dark:bg-[#1a1324]">
              <tr>
                <th className="px-4 py-3">Nombre</th>
                <th className="px-4 py-3">Inhalar</th>
                <th className="px-4 py-3">Sostener</th>
                <th className="px-4 py-3">Exhalar</th>
                <th className="px-4 py-3">Rondas</th>
                <th className="px-4 py-3">Estado</th>
                <th className="px-4 py-3 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {techniques.map(t => (
                <tr key={t.id} className="border-t border-[#EDF2ED] dark:border-gray-800">
                  <td className="px-4 py-3 font-semibold text-[#4A5568] dark:text-gray-200">{t.name}</td>
                  <td className="px-4 py-3 text-[#4A5568] dark:text-gray-300">{t.inhaleSeconds}s</td>
                  <td className="px-4 py-3 text-[#4A5568] dark:text-gray-300">{t.holdSeconds}s</td>
                  <td className="px-4 py-3 text-[#4A5568] dark:text-gray-300">{t.exhaleSeconds}s</td>
                  <td className="px-4 py-3 text-[#4A5568] dark:text-gray-300">{t.rounds}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${t.active ? 'bg-[#E9F1EA] text-[#4C7C64]' : 'bg-gray-200 text-gray-500'}`}>
                      {t.active ? 'Activa' : 'Inactiva'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      <button onClick={() => openEdit(t)} className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold text-violeta hover:bg-[#D1CAEF]/30" aria-label={`Editar ${t.name}`}>
                        <Pencil className="h-3.5 w-3.5" /> Editar
                      </button>
                      <button onClick={() => setActive(t.id, !t.active)} className={`rounded-lg px-2 py-1 text-xs font-semibold ${t.active ? 'text-anaranjado hover:bg-anaranjado/10' : 'text-[#4C7C64] hover:bg-[#E9F1EA]'}`} aria-label={t.active ? `Eliminar ${t.name}` : `Restaurar ${t.name}`}>
                        {t.active ? 'Eliminar' : 'Restaurar'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        </>
      )}

      {showForm && (
        <BreathingForm
          technique={editing}
          onClose={closeForm}
          onSubmit={async (data) => {
            const ok = editing ? await update(editing.id, data) : await create(data)
            if (ok) closeForm()
            return ok
          }}
        />
      )}

      {error && <Toast message={error} onClose={clearError} />}
    </div>
  )
}