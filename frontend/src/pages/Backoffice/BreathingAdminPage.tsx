import { useState } from 'react'
import { Plus, Pencil } from 'lucide-react'
import { useAdminBreathing } from '../../hooks/backoffice/useAdminBreathing'
import type { AdminBreathingTechnique, BreathingFormData } from '../../api/adminBreathing'
import { Toast } from '../../components/backoffice/Toast'
import Button from '../../components/Buttons/Button/Button'
import { Table, Column } from '../../components/backoffice/Table'
import { SectionCard } from '../../components/backoffice/SectionCard'
import { Switch } from '../../components/backoffice/Switch'
import PageHeader from '../../components/backoffice/PageHeader'

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

  const breathingColumns: Column<AdminBreathingTechnique>[] = [
    {
      header: 'Nombre',
      render: (t) => <span className="font-semibold text-[#4A5568] dark:text-gray-200">{t.name}</span>,
    },
    {
      header: 'Inhalar',
      render: (t) => <span className="text-[#4A5568] dark:text-gray-300">{t.inhaleSeconds}s</span>,
    },
    {
      header: 'Sostener',
      render: (t) => <span className="text-[#4A5568] dark:text-gray-300">{t.holdSeconds}s</span>,
    },
    {
      header: 'Exhalar',
      render: (t) => <span className="text-[#4A5568] dark:text-gray-300">{t.exhaleSeconds}s</span>,
    },
    {
      header: 'Rondas',
      render: (t) => <span className="text-[#4A5568] dark:text-gray-300">{t.rounds}</span>,
    },
    {
      header: 'Estado',
      render: (t) => (
        <span className={`rounded-full px-2 py-0.5 text-[12px] font-semibold ${t.active ? 'bg-[#E9F1EA] text-[#4C7C64]' : 'bg-gray-200 text-gray-500'}`}>
          {t.active ? 'Activa' : 'Inactiva'}
        </span>
      ),
    },
    {
      header: 'ACTIVO',
      render: (t) => (
        <Switch
          checked={t.active}
          onChange={() => setActive(t.id, !t.active)}
          ariaLabel={`Cambiar estado activo de ${t.name}`}
        />
      ),
    },
    {
      header: 'Acciones',
      className: 'text-right',
      render: (t) => (
        <div className="flex justify-end gap-2">
          <button
            onClick={() => openEdit(t)}
            className="inline-flex h-8 w-8 items-center justify-center rounded-lg hover:bg-[#D1CAEF]/30 dark:hover:bg-[#D1CAEF]/10 text-gray-400 dark:text-gray-555 hover:text-violeta dark:hover:text-violeta-claro transition duration-150"
            aria-label={`Editar ${t.name}`}
          >
            <Pencil className="h-5 w-5" strokeWidth={2} />
          </button>
        </div>
      ),
    },
  ]

  return (
    <div className="flex flex-col gap-4">
      <PageHeader
        title="Respiraciones guiadas"
        subtitle="Alta, edición y listado de respiraciones guiadas"
        action={
          <Button variant="primary" onClick={openCreate}>
            <Plus className="h-4 w-4" strokeWidth={2} /> Nueva respiración
          </Button>
        }
      />

      <SectionCard className="bg-white dark:bg-[#172033] flex-1 min-h-0 flex flex-col">
        <div className="flex flex-col gap-4 flex-1 min-h-0">
          {loading ? (
            <p className="py-8 text-center text-sm text-[#A0AEC0]">Cargando...</p>
          ) : techniques.length === 0 ? (
            <p className="py-8 text-center text-sm text-[#A0AEC0]">Todavía no hay respiraciones.</p>
          ) : (
            <Table
              data={techniques}
              columns={breathingColumns}
              keyExtractor={(t) => t.id}
            />
          )}
        </div>
      </SectionCard>

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