import { useState } from 'react'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { useStoreProducts } from '../../hooks/backoffice/useStoreProducts'
import type { StoreItemResponse } from '../../api/store'
import { Toast } from '../../components/backoffice/Toast'
import { StoreProductForm } from '../../components/backoffice/StoreProductForm'
import Button from '../../components/Buttons/Button/Button'

const CATEGORY_LABELS: Record<string, string> = {
    HOUSE: 'Casa', NOTEBOOK: 'Diario', TREE: 'Árbol', MANDALA: 'Mandala',
}

export default function ProductsPage() {
    const { products, loading, error, clearError, create, update, remove } = useStoreProducts()
    const [editing, setEditing] = useState<StoreItemResponse | null>(null)
    const [showForm, setShowForm] = useState(false)

    const openCreate = () => { setEditing(null); setShowForm(true) }
    const openEdit = (p: StoreItemResponse) => { setEditing(p); setShowForm(true) }
    const closeForm = () => { setShowForm(false); setEditing(null) }

    return (
        <div className="flex flex-col gap-4 animate-fadeIn">
            <div className="flex items-start justify-between gap-3">
                <div className="flex flex-col gap-1">
                    <h1 className="text-[30px] font-extrabold leading-tight text-violeta dark:text-violeta-claro">Productos de la tienda</h1>
                    <p className="text-[16px] text-[#A0AEC0] dark:text-gray-400">Alta, edición y baja de los cosméticos de la tienda</p>
                </div>
                <Button variant="primary" onClick={openCreate}>
                    <Plus className="h-4 w-4" strokeWidth={2} /> Nuevo producto
                </Button>
            </div>

            {loading ? (
                <p className="py-8 text-center text-sm text-[#A0AEC0]">Cargando productos...</p>
            ) : products.length === 0 ? (
                <p className="py-8 text-center text-sm text-[#A0AEC0]">Todavía no hay productos.</p>
            ) : (
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
                    {products.map(p => (
                        <div key={p.id} className="flex gap-3 rounded-2xl border border-[#EDF2ED] bg-white p-3 dark:border-gray-800 dark:bg-[#172033]">
                            <div className="flex h-16 w-16 shrink-0 items-center justify-center overflow-hidden rounded-xl bg-[#F4EFFB] dark:bg-[#0f1524]">
                                {p.imageUrlLight
                                    ? <img src={p.imageUrlLight} alt={p.name} className="h-full w-full object-contain" />
                                    : <span className="px-1 text-center text-[9px] text-[#A0AEC0]">{p.assetKey}</span>}
                            </div>
                            <div className="min-w-0 flex-1">
                                <div className="flex items-center gap-1.5">
                                    <h3 className="truncate font-bold text-[#4A5568] dark:text-gray-200">{p.name}</h3>
                                    <span className="shrink-0 rounded-full bg-[#D1CAEF]/40 px-2 py-0.5 text-[10px] font-semibold text-violeta">{CATEGORY_LABELS[p.category] ?? p.category}</span>
                                </div>
                                <p className="mt-0.5 line-clamp-2 text-xs text-[#A0AEC0]">{p.description}</p>
                                <p className="mt-1 text-xs font-semibold text-[#4A5568] dark:text-gray-300">
                                    {p.price != null ? `$${p.price.toLocaleString('es-AR')}` : `${p.priceCoins.toLocaleString('es-AR')} semillas`}
                                    {p.premiumOnly && <span className="ml-1 text-violeta">· Premium</span>}
                                </p>
                                <div className="mt-2 flex gap-2">
                                    <button onClick={() => openEdit(p)} className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold text-violeta hover:bg-[#D1CAEF]/30" aria-label={`Editar ${p.name}`}>
                                        <Pencil className="h-3.5 w-3.5" /> Editar
                                    </button>
                                    <button onClick={() => { if (window.confirm(`¿Eliminar "${p.name}"?`)) remove(p.id) }} className="flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-semibold text-anaranjado hover:bg-anaranjado/10" aria-label={`Eliminar ${p.name}`}>
                                        <Trash2 className="h-3.5 w-3.5" /> Eliminar
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {showForm && (
                <StoreProductForm
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