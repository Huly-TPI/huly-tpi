import { useState, useCallback, useEffect } from 'react'
import { adminProductsApi, type AdminProduct, type ProductFormData } from '../../api/adminProducts'

export function useAdminProducts(type = 'COIN_PACK') {
    const [products, setProducts] = useState<AdminProduct[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const load = useCallback(async () => {
        setLoading(true)
        try {
            setProducts(await adminProductsApi.list(type))
        } catch {
            setError('No se pudieron cargar los productos')
        } finally {
            setLoading(false)
        }
    }, [type])

    useEffect(() => { load() }, [load])

    const create = useCallback(async (data: ProductFormData): Promise<boolean> => {
        try {
            await adminProductsApi.create(data)
            await load()
            return true
        } catch (e) {
            setError(e instanceof Error ? e.message : 'No se pudo crear el producto')
            return false
        }
    }, [load])

    const update = useCallback(async (id: number, data: ProductFormData): Promise<boolean> => {
        try {
            await adminProductsApi.update(id, data)
            await load()
            return true
        } catch (e) {
            setError(e instanceof Error ? e.message : 'No se pudo editar el producto')
            return false
        }
    }, [load])

    const setActive = useCallback(async (id: number, active: boolean): Promise<boolean> => {
        try {
            await adminProductsApi.setActive(id, active)
            await load()
            return true
        } catch (e) {
            setError(e instanceof Error ? e.message : 'No se pudo cambiar el estado')
            return false
        }
    }, [load])

    return { products, loading, error, clearError: () => setError(null), reload: load, create, update, setActive }
}