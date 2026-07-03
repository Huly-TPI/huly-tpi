import { useState } from 'react'
import { X } from 'lucide-react'
import type { StoreItemResponse } from '../../api/store'
import type { StoreItemFormData } from '../../api/adminStore'
import Button from '../Buttons/Button/Button'

const CATEGORIES = [
    { value: 'HOUSE', label: 'Casa' },
    { value: 'NOTEBOOK', label: 'Diario' },
    { value: 'TREE', label: 'Árbol' },
    { value: 'MANDALA', label: 'Mandala' },
]

interface Props {
    product: StoreItemResponse | null
    onClose: () => void
    onSubmit: (data: StoreItemFormData) => Promise<boolean>
}

const inputClass =
    'rounded-xl border border-gray-200 bg-white px-3 py-2 text-sm font-normal text-gray-700 focus:border-violeta focus:outline-none dark:border-gray-800 dark:bg-[#09111f] dark:text-gray-200'
const labelClass = 'flex flex-col gap-1 text-sm font-semibold text-[#4A5568] dark:text-gray-300'

export function StoreProductForm({ product, onClose, onSubmit }: Props) {
    const isEdit = product != null
    const [name, setName] = useState(product?.name ?? '')
    const [description, setDescription] = useState(product?.description ?? '')
    const [category, setCategory] = useState(product?.category ?? 'HOUSE')
    const [priceCoins, setPriceCoins] = useState(String(product?.priceCoins ?? 0))
    const [price, setPrice] = useState(product?.price != null ? String(product.price) : '')
    const [premiumOnly, setPremiumOnly] = useState(product?.premiumOnly ?? false)
    const [imageLight, setImageLight] = useState<File | null>(null)
    const [imageDark, setImageDark] = useState<File | null>(null)
    const [saving, setSaving] = useState(false)

    const canSubmit = Boolean(name.trim() && description.trim() && (isEdit || (imageLight && imageDark)))

    async function handleSubmit() {
        if (!canSubmit || saving) return
        setSaving(true)
        try {
            await onSubmit({
                name: name.trim(),
                description: description.trim(),
                category,
                priceCoins: Number(priceCoins) || 0,
                price: price.trim() ? Number(price) : null,
                premiumOnly,
                imageLight,
                imageDark,
            })
        } finally {
            setSaving(false)
        }
    }

    return (
        <div className="fixed inset-0 z-[400] flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
            <button type="button" aria-label="Cerrar" className="absolute inset-0 cursor-default" onClick={onClose} />
            <div className="relative z-10 flex max-h-[85dvh] w-full max-w-lg flex-col gap-3 overflow-y-auto rounded-2xl bg-white p-5 shadow-2xl dark:bg-[#172033]">
                <div className="flex items-center justify-between">
                    <h2 className="text-lg font-bold text-[#8869AC]">{isEdit ? 'Editar producto' : 'Nuevo producto'}</h2>
                    <button type="button" onClick={onClose} aria-label="Cerrar" className="rounded-full p-1.5 text-[#A0AEC0] hover:bg-gray-100 dark:hover:bg-gray-800">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <label className={labelClass}>
                    Nombre
                    <input value={name} onChange={e => setName(e.target.value)} className={inputClass} />
                </label>

                <label className={labelClass}>
                    Descripción
                    <textarea value={description} onChange={e => setDescription(e.target.value)} rows={2} className={inputClass} />
                </label>

                <label className={labelClass}>
                    Categoría
                    <select value={category} onChange={e => setCategory(e.target.value)} className={inputClass}>
                        {CATEGORIES.map(c => <option key={c.value} value={c.value}>{c.label}</option>)}
                    </select>
                </label>

                <div className="grid grid-cols-2 gap-3">
                    <label className={labelClass}>
                        Precio en semillas
                        <input type="number" min="0" value={priceCoins} onChange={e => setPriceCoins(e.target.value)} className={inputClass} />
                    </label>
                    <label className={labelClass}>
                        Precio en $ (opcional)
                        <input type="number" min="0" step="0.01" value={price} onChange={e => setPrice(e.target.value)} placeholder="—" className={inputClass} />
                    </label>
                </div>

                <label className="flex items-center gap-2 text-sm font-semibold text-[#4A5568] dark:text-gray-300">
                    <input type="checkbox" checked={premiumOnly} onChange={e => setPremiumOnly(e.target.checked)} className="h-4 w-4 accent-violeta" />
                    Solo premium
                </label>

                <div className="grid grid-cols-2 gap-3">
                    <label className={labelClass}>
                        Imagen clara
                        <input type="file" accept="image/*" onChange={e => setImageLight(e.target.files?.[0] ?? null)} className="text-xs text-[#4A5568] dark:text-gray-400 file:mr-2 file:rounded-lg file:border-0 file:bg-[#D1CAEF]/40 file:px-3 file:py-1.5 file:text-xs file:font-semibold file:text-violeta" />
                    </label>
                    <label className={labelClass}>
                        Imagen oscura
                        <input type="file" accept="image/*" onChange={e => setImageDark(e.target.files?.[0] ?? null)} className="text-xs text-[#4A5568] dark:text-gray-400 file:mr-2 file:rounded-lg file:border-0 file:bg-[#D1CAEF]/40 file:px-3 file:py-1.5 file:text-xs file:font-semibold file:text-violeta" />
                    </label>
                </div>
                {isEdit && (
                    <p className="text-[11px] text-[#A0AEC0]">Dejá las imágenes vacías para conservar las actuales.</p>
                )}

                <div className="mt-1 flex gap-2">
                    <Button variant="primary" onClick={handleSubmit} isLoading={saving} loadingLabel="Guardando..." disabled={!canSubmit || saving} className="flex-1">
                        {isEdit ? 'Guardar cambios' : 'Crear producto'}
                    </Button>
                    <Button variant="secondary" onClick={onClose}>Cancelar</Button>
                </div>
            </div>
        </div>
    )
}