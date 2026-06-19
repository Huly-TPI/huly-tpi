import { useState, useEffect } from 'react'
import { storeApi, type StoreItemResponse } from '../../api/store'
import { useAuth } from '../../context/auth'

export function useStoreItems() { 
    const {loading: authLoading} = useAuth()
    const [items, setItems] = useState<StoreItemResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)
  
    useEffect(() => {
      if (authLoading) return
      storeApi.getItems()
        .then(setItems)
        .catch(() => setError('No se pudieron cargar los items de la tienda.'))
        .finally(() => setLoading(false))
    }, [authLoading])
  
    return { items, loading, error }
}