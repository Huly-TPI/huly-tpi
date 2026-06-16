import { useState, useEffect } from 'react'
import { getProducts, type Product } from '../../api/payment'
import { useAuth } from '../../context/auth'

export function useProducts() {
  const { loading: authLoading } = useAuth()
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (authLoading) return
    getProducts()
      .then(setProducts)
      .catch(() => setError('No se pudieron cargar los productos.'))
      .finally(() => setLoading(false))
  }, [authLoading])

  return { products, loading, error }
}
