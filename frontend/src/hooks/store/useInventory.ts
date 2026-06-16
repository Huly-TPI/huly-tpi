import { useState, useEffect, useCallback } from 'react'
import { storeApi, type InventoryItemResponse } from '../../api/store'
import { useAuth } from '../../context/auth'

export function useInventory() {
    const { loading: authLoading } = useAuth()
    const [inventory, setInventory] = useState<InventoryItemResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const refetch = useCallback(() => {
        return storeApi.getInventory()
            .then(setInventory)
            .catch(() => setError('No se pudo cargar tu inventario.'))
    }, [])

    useEffect(() => {
        if (authLoading) return
        refetch()
            .finally(() => setLoading(false))
    }, [authLoading, refetch])

    return { inventory, loading, error, refetch }
}