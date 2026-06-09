import { useState } from 'react'
import { createPreference } from '../../api/payment'

export function usePurchase() {
  const [buyingId, setBuyingId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const buy = async (productId: string) => {
    setBuyingId(productId)
    setError(null)
    try {
      const { initPoint } = await createPreference(productId)
      window.location.href = initPoint
    } catch {
      setError('Error al iniciar el pago. Intentá de nuevo.')
      setBuyingId(null)
    }
  }

  return { buyingId, error, buy }
}
