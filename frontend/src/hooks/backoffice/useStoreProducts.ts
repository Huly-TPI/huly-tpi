import { useState, useCallback, useEffect } from 'react'
import { storeApi, type StoreItemResponse } from '../../api/store'
import { adminStoreApi, type StoreItemFormData } from '../../api/adminStore'

export function useStoreProducts() {
    const [products, setProducts] = useState<StoreItemResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const load = useCallback(async () => {
        setLoading(true)
        try {
            setProducts(await storeApi.getItems())
        } catch {
            setError('No se pudieron cargar los productos')
        } finally {
            setLoading(false)
        }
    }, [])

    useEffect(() => {
        load()
    }, [load])

    const create = useCallback(async (data: StoreItemFormData): Promise<boolean> => {
        try {
            await adminStoreApi.create(data)
            await load()
            return true
        } catch (e) {
            setError(e instanceof Error ? e.message : 'No se pudo crear el producto')
            return false
        }
    }, [load])

    const update = useCallback(async (id: number, data: StoreItemFormData): Promise<boolean> => {
        try {
            await adminStoreApi.update(id, data)
            await load()
            return true
        } catch (e) {
            setError(e instanceof Error ? e.message : 'No se pudo editar el producto')
            return false
        }
    }, [load])

    const remove = useCallback(async (id: number): Promise<boolean> => {
        try {
            await adminStoreApi.remove(id)
            await load()
            return true
        } catch (e) {
            setError(e instanceof Error ? e.message : 'No se pudo eliminar el producto')
            return false
        }
    }, [load])

    return { products, loading, error, clearError: () => setError(null), reload: load, create, update, remove }
}