import { useState } from 'react'
import { Plus, Pencil } from 'lucide-react'
import { useAdminProducts } from '../../hooks/backoffice/useAdminProducts'
import type { AdminProduct, ProductFormData } from '../../api/adminProducts'
import { Toast } from '../../components/backoffice/Toast'
import Button from '../../components/Buttons/Button/Button'

const inputClass = 'rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm text-gray-700 focus:border-violeta focus:outline-none dark:border-gray-800 dark:bg-[#09111f] dark:text-gray-200'
const labelClass = 'flex flex-col gap-1 text-sm font-semibold text-[#4A5568] dark:text-gray-300'

function ProductForm({ product, onClose, onSubmit }: {
  product: AdminProduct | null
  onClose: () => void
  onSubmit: (data: ProductFormData) => Promise<boolean>
}) {
  const isEdit = product != null
  const [name, setName] = useState(product?.name ?? '')
  const [description, setDescription] = useState(product?.description ?? '')
  const [price, setPrice] = useState(product?.price != null ? String(product.price) : '')
  const [coinsAmount, setCoinsAmount] = useState(product?.coinsAmount != null ? String(product.coinsAmount) : '')
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
        coinsAmount: coinsAmount.trim() ? Number(coinsAmount) : null,
        type: 'COIN_PACK',
      })
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
      <button type="button" aria-label="Cerrar" className="absolute inset-0 cursor-default" onClick={onClose} />
      <div className="relative z-10 flex w-full max-w-md flex-col gap-3 rounded-2xl bg-white p-5 shadow-2xl dark:bg-[#172033]">
        <h2 className="text-lg font-bold text-[#8869AC]">{isEdit ? 'Editar paquete' : 'Nuevo paquete'}</h2>
        <label className={labelClass}>Nombre
          <input value={name} onChange={e => setName(e.target.value)} className={inputClass} />
        </label>
        <label className={labelClass}>Descripción
          <input value={description} onChange={e => setDescription(e.target.value)} className={inputClass} />
        </label>
        <div className="grid grid-cols-2 gap-3">
          <label className={labelClass}>Precio ($)
            <input type="number" min="0" step="0.01" value={price} onChange={e => setPrice(e.target.value)} className={inputClass} />
          </label>
          <label className={labelClass}>Semillas
            <input type="number" min="0" value={coinsAmount} onChange={e => setCoinsAmount(e.target.value)} className={inputClass} />
          </label>
        </div>
        <div className="mt-1 flex gap-2">
          <Button variant="primary" onClick={handleSubmit} isLoading={saving} loadingLabel="Guardando..." disabled={!canSubmit || saving} className="flex-1">
            {isEdit ? 'Guardar' : 'Crear'}
          </Button>
          <Button variant="secondary" onClick={onClose}>Cancelar</Button>
        </div>
      </div>
    </div>
  )
}

export default function SeedProductsPage() {
  const { products, loading, error, clearError, create, update, setActive } = useAdminProducts('COIN_PACK')
  const [editing, setEditing] = useState<AdminProduct | null>(null)
  const [showForm, setShowForm] = useState(false)

  const openCreate = () => { setEditing(null); setShowForm(true) }
  const openEdit = (p: AdminProduct) => { setEditing(p); setShowForm(true) }
  const closeForm = () => { setShowForm(false); setEditing(null) }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-start justify-between gap-3">
        <div className="flex flex-col gap-1">
          <h1 className="text-[30px] font-extrabold leading-tight text-[#8869AC]">Paquetes de semillas</h1>
          <p className="text-[16px] text-[#A0AEC0]">Precios y paquetes de semillas comprables</p>
        </div>
        <Button variant="primary" onClick={openCreate}>
          <Plus className="h-4 w-4" strokeWidth={2} /> Nuevo paquete
        </Button>
      </div>

      {loading ? (
        <p className="py-8 text-center text-sm text-[#A0AEC0]">Cargando...</p>
      ) : products.length === 0 ? (
        <p className="py-8 text-center text-sm text-[#A0AEC0]">Todavía no hay paquetes.</p>
      ) : (
        <div className="overflow-hidden rounded-2xl border border-[#EDF2ED] dark:border-gray-800">
          <table className="w-full text-left text-sm">
            <thead className="bg-[#F4EFFB] text-xs uppercase text-[#8869AC] dark:bg-[#1a1324]">
              <tr>
                <th className="px-4 py-3">Nombre</th>
                <th className="px-4 py-3">Semillas</th>
                <th className="px-4 py-3">Precio</th>
                <th className="px-4 py-3">Estado</th>
                <th className="px-4 py-3 text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {products.map(p => (
                <tr key={p.id} className="border-t border-[#EDF2ED] dark:border-gray-800">
                  <td className="px-4 py-3 font-semibold text-[#4A5568] dark:text-gray-200">{p.name}</td>
                  <td className="px-4 py-3 text-[#4A5568] dark:text-gray-300">{p.coinsAmount?.toLocaleString('es-AR') ?? '—'}</td>
                  <td className="px-4 py-3 text-[#4A5568] dark:text-gray-300">${p.price.toLocaleString('es-AR')}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-[11px] font-semibold ${p.active ? 'bg-[#E9F1EA] text-[#4C7C64]' : 'bg-gray-200 text-gray-500'}`}>
                      {p.active ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      <button onClick={() => openEdit(p)} className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold text-violeta hover:bg-[#D1CAEF]/30" aria-label={`Editar ${p.name}`}>
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
      )}

      {showForm && (
        <ProductForm
          product={editing}
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