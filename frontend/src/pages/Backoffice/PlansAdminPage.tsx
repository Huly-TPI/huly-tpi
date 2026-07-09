import { useState } from 'react'
import { Pencil, Plus } from 'lucide-react'
import { useAdminProducts } from '../../hooks/backoffice/useAdminProducts'
import type { AdminProduct, ProductFormData } from '../../api/adminProducts'
import { Toast } from '../../components/backoffice/Toast'
import Button from '../../components/Buttons/Button/Button'

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

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="flex flex-col gap-1">
          <h1 className="text-2xl font-extrabold leading-tight text-[#8869AC] sm:text-[30px]">Suscripciones</h1>
          <p className="text-sm text-[#A0AEC0] sm:text-[16px]">Precios y configuración de los planes</p>
        </div>
        <Button variant="primary" className="self-start" onClick={() => { setEditing(null); setShowForm(true) }}>
          <Plus className="h-4 w-4" strokeWidth={2} /> Nuevo plan
        </Button>
      </div>

      {loading ? (
        <p className="py-8 text-center text-sm text-[#A0AEC0]">Cargando...</p>
      ) : products.length === 0 ? (
        <p className="py-8 text-center text-sm text-[#A0AEC0]">No hay planes.</p>
      ) : (
        <>
        <ul className="flex flex-col gap-3 sm:hidden">
          {products.map(p => (
            <li key={p.id} className="flex flex-col gap-3 rounded-2xl border border-[#EDF2ED] bg-white p-4 dark:border-gray-800 dark:bg-[#172033]">
              <div className="flex items-start justify-between gap-2">
                <span className="font-semibold text-[#4A5568] dark:text-gray-200">{p.name}</span>
                <span className={`shrink-0 rounded-full px-2 py-0.5 text-[11px] font-semibold ${p.active ? 'bg-[#E9F1EA] text-[#4C7C64]' : 'bg-gray-200 text-gray-500'}`}>{p.active ? 'Activo' : 'Inactivo'}</span>
              </div>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div className="flex flex-col"><span className="text-[11px] font-semibold uppercase text-[#8869AC]">Código</span><span className="text-[#A0AEC0]">{p.planCode ?? '—'}</span></div>
                <div className="flex flex-col"><span className="text-[11px] font-semibold uppercase text-[#8869AC]">Precio</span><span className="text-[#4A5568] dark:text-gray-300">${p.price.toLocaleString('es-AR')}</span></div>
                <div className="flex flex-col"><span className="text-[11px] font-semibold uppercase text-[#8869AC]">Chat/día</span><span className="text-[#4A5568] dark:text-gray-300">{p.chatDailyLimit ?? '—'}</span></div>
              </div>
              <div className="flex gap-2 border-t border-[#EDF2ED] pt-2 dark:border-gray-800">
                <button onClick={() => { setEditing(p); setShowForm(true) }} className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold text-violeta hover:bg-[#D1CAEF]/30" aria-label={`Editar ${p.name}`}><Pencil className="h-3.5 w-3.5" /> Editar</button>
                <button onClick={() => setActive(p.id, !p.active)} className={`rounded-lg px-2 py-1 text-xs font-semibold ${p.active ? 'text-anaranjado hover:bg-anaranjado/10' : 'text-[#4C7C64] hover:bg-[#E9F1EA]'}`} aria-label={p.active ? `Desactivar ${p.name}` : `Activar ${p.name}`}>{p.active ? 'Desactivar' : 'Activar'}</button>
              </div>
            </li>
          ))}
        </ul>
        <div className="hidden overflow-x-auto rounded-2xl border border-[#EDF2ED] dark:border-gray-800 sm:block">
          <table className="w-full min-w-[720px] text-left text-sm">
            <thead className="bg-[#F4EFFB] text-xs uppercase text-[#8869AC] dark:bg-[#1a1324]">
              <tr>
                <th className="px-4 py-3">Plan</th>
                <th className="px-4 py-3">Código</th>
                <th className="px-4 py-3">Precio</th>
                <th className="px-4 py-3">Chat/día</th>
                <th className="px-4 py-3">Estado</th>
                <th className="px-4 py-3 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {products.map(p => (
                <tr key={p.id} className="border-t border-[#EDF2ED] dark:border-gray-800">
                  <td className="px-4 py-3 font-semibold text-[#4A5568] dark:text-gray-200">{p.name}</td>
                  <td className="px-4 py-3 text-[#A0AEC0]">{p.planCode ?? '—'}</td>
                  <td className="px-4 py-3 text-[#4A5568] dark:text-gray-300">${p.price.toLocaleString('es-AR')}</td>
                  <td className="px-4 py-3 text-[#4A5568] dark:text-gray-300">{p.chatDailyLimit ?? '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${p.active ? 'bg-[#E9F1EA] text-[#4C7C64]' : 'bg-gray-200 text-gray-500'}`}>
                      {p.active ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      <button onClick={() => { setEditing(p); setShowForm(true) }} className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold text-violeta hover:bg-[#D1CAEF]/30" aria-label={`Editar ${p.name}`}>
                        <Pencil className="h-3.5 w-3.5" /> Editar
                      </button>
                      <button onClick={() => setActive(p.id, !p.active)} className={`rounded-lg px-2 py-1 text-xs font-semibold ${p.active ? 'text-anaranjado hover:bg-anaranjado/10' : 'text-[#4C7C64] hover:bg-[#E9F1EA]'}`} aria-label={p.active ? `Desactivar ${p.name}` : `Activar ${p.name}`}>
                        {p.active ? 'Desactivar' : 'Activar'}
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