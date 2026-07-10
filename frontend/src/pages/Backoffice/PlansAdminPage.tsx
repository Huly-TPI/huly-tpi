import { useState } from 'react'
import { Pencil, Plus } from 'lucide-react'
import { useAdminProducts } from '../../hooks/backoffice/useAdminProducts'
import type { AdminProduct, ProductFormData } from '../../api/adminProducts'
import { Toast } from '../../components/backoffice/Toast'
import Button from '../../components/Buttons/Button/Button'
import { Table, Column } from '../../components/backoffice/Table'
import { SectionCard } from '../../components/backoffice/SectionCard'
import { Switch } from '../../components/backoffice/Switch'
import PageHeader from '../../components/backoffice/PageHeader'

const inputClass = 'rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 focus:border-violeta focus:outline-none dark:border-gray-800 dark:bg-[#09111f] dark:text-gray-200'
const labelClass = 'flex flex-col gap-1 text-sm font-semibold text-[#4A5568] dark:text-gray-300'
const PLAN_CODES = ['PREMIUM', 'BASIC']

function PlanForm({ product, onClose, onSubmit }: {
  product: AdminProduct | null
  onClose: () => void
  onSubmit: (data: ProductFormData) => Promise<boolean>
}) {
  const isEdit = product != null
  const [name, setName] = useState(product?.name ?? '')
  const [description, setDescription] = useState(product?.description ?? '')
  const [price, setPrice] = useState(product?.price != null ? String(product.price) : '')
  const [planCode, setPlanCode] = useState(product?.planCode ?? 'PREMIUM')
  const [chatDailyLimit, setChatDailyLimit] = useState(product?.chatDailyLimit != null ? String(product.chatDailyLimit) : '')
  const [audioDailyLimit, setAudioDailyLimit] = useState(product?.audioDailyLimit != null ? String(product.audioDailyLimit) : '')
  const [saving, setSaving] = useState(false)

  const canSubmit = Boolean(name.trim() && description.trim() && price.trim())

  async function handleSubmit() {
    if (!canSubmit || saving) return
    setSaving(true)
    try {
      await onSubmit({
        name: name.trim(),
        description: description.trim(),
        price: Number(price),
        coinsAmount: product?.coinsAmount ?? 0,
        type: 'PLAN',
        planCode,
        chatDailyLimit: chatDailyLimit.trim() ? Number(chatDailyLimit) : null,
        audioDailyLimit: audioDailyLimit.trim() ? Number(audioDailyLimit) : null,
      })
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
      <button type="button" aria-label="Cerrar" className="absolute inset-0 cursor-default" onClick={onClose} />
      <div className="relative z-10 flex max-h-[90vh] w-full max-w-md flex-col gap-3 overflow-y-auto rounded-2xl bg-white p-5 shadow-2xl dark:bg-[#172033]">
        <h2 className="text-lg font-bold text-[#8869AC]">{isEdit ? `Editar plan (${product.planCode})` : 'Nuevo plan'}</h2>

        {isEdit ? (
          <p className="text-sm text-[#A0AEC0]">Código del plan: <strong className="text-[#4A5568] dark:text-gray-300">{product.planCode}</strong></p>
        ) : (
          <label className={labelClass}>Código del plan
            <select value={planCode} onChange={e => setPlanCode(e.target.value)} className={inputClass}>
              {PLAN_CODES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
            <span className="text-[11px] font-normal text-[#A0AEC0]">Hereda los beneficios de ese plan (items premium, mandalas).</span>
          </label>
        )}

        <label className={labelClass}>Nombre
          <input value={name} onChange={e => setName(e.target.value)} className={inputClass} />
        </label>
        <label className={labelClass}>Descripción
          <input value={description} onChange={e => setDescription(e.target.value)} className={inputClass} />
        </label>
        <label className={labelClass}>Precio ($)
          <input type="number" min="0" step="0.01" value={price} onChange={e => setPrice(e.target.value)} className={inputClass} />
        </label>
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <label className={labelClass}>Límite chat/día
            <input type="number" min="0" value={chatDailyLimit} onChange={e => setChatDailyLimit(e.target.value)} className={inputClass} />
          </label>
          <label className={labelClass}>Límite audio/día
            <input type="number" min="0" value={audioDailyLimit} onChange={e => setAudioDailyLimit(e.target.value)} className={inputClass} />
          </label>
        </div>
        <div className="mt-1 flex flex-col gap-2 sm:flex-row">
          <Button variant="primary" onClick={handleSubmit} isLoading={saving} loadingLabel="Guardando..." disabled={!canSubmit || saving} className="flex-1">{isEdit ? 'Guardar' : 'Crear'}</Button>
          <Button variant="secondary" onClick={onClose}>Cancelar</Button>
        </div>
      </div>
    </div>
  )
}

export default function PlansAdminPage() {
  const { products, loading, error, clearError, create, update, setActive } = useAdminProducts('PLAN')
  const [editing, setEditing] = useState<AdminProduct | null>(null)
  const [showForm, setShowForm] = useState(false)

  const planColumns: Column<AdminProduct>[] = [
    {
      header: 'Plan',
      render: (p) => <span className="font-semibold text-[#4A5568] dark:text-gray-200">{p.name}</span>,
    },
    {
      header: 'Código',
      render: (p) => <span className="text-[#A0AEC0]">{p.planCode}</span>,
    },
    {
      header: 'Precio',
      render: (p) => <span className="text-[#4A5568] dark:text-gray-300">${p.price.toLocaleString('es-AR')}</span>,
    },
    {
      header: 'Chat/día',
      render: (p) => <span className="text-[#4A5568] dark:text-gray-300">{p.chatDailyLimit}</span>,
    },
    {
      header: 'Estado',
      render: (p) => (
        <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${p.active ? 'bg-[#E9F1EA] text-[#4C7C64]' : 'bg-gray-200 text-gray-500'}`}>
          {p.active ? 'Activo' : 'Inactivo'}
        </span>
      ),
    },
    {
      header: 'ACTIVO',
      render: (p) => (
        <Switch
          checked={p.active}
          onChange={() => setActive(p.id, !p.active)}
          ariaLabel={`Cambiar estado activo de ${p.name}`}
        />
      ),
    },
    {
      header: 'Acciones',
      className: 'text-right',
      render: (p) => (
        <div className="flex justify-end gap-2">
          <button
            onClick={() => { setEditing(p); setShowForm(true) }}
            className="inline-flex h-8 w-8 items-center justify-center rounded-lg hover:bg-[#D1CAEF]/30 dark:hover:bg-[#D1CAEF]/10 text-gray-400 dark:text-gray-555 hover:text-violeta dark:hover:text-violeta-claro transition duration-150"
            aria-label={`Editar ${p.name}`}
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
        title="Suscripciones"
        subtitle="Precios y configuración de los planes"
        action={
          <Button variant="primary" onClick={() => { setEditing(null); setShowForm(true) }}>
            <Plus className="h-4 w-4" strokeWidth={2} /> Nuevo plan
          </Button>
        }
      />

      <SectionCard className="bg-white dark:bg-[#172033] flex-1 min-h-0 flex flex-col">
        <div className="flex flex-col gap-4 flex-1 min-h-0">
          {loading ? (
            <p className="py-8 text-center text-sm text-[#A0AEC0]">Cargando...</p>
          ) : products.length === 0 ? (
            <p className="py-8 text-center text-sm text-[#A0AEC0]">No hay planes.</p>
          ) : (
            <Table
              data={products}
              columns={planColumns}
              keyExtractor={(p) => p.id}
            />
          )}
        </div>
      </SectionCard>

      {showForm && (
        <PlanForm
          product={editing}
          onClose={() => { setShowForm(false); setEditing(null) }}
          onSubmit={async (data) => {
            const ok = editing ? await update(editing.id, data) : await create(data)
            if (ok) { setShowForm(false); setEditing(null) }
            return ok
          }}
        />
      )}

      {error && <Toast message={error} onClose={clearError} />}
    </div>
  )
}