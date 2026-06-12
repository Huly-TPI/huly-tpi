import { useState } from 'react'
import { createPreference } from '../../api/payment'
import { ApiError } from '../../api/apiError'

export function usePurchase() {
  const [buyingId, setBuyingId] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)

  const buy = async (productId: string) => {
    setBuyingId(productId)
    setError(null)

    // Abrimos la pestaña SINCRÓNICAMENTE dentro del gesto del usuario para que
    // los bloqueadores de popups no la bloqueen; le seteamos la URL tras el await.
    const popup = window.open('', '_blank')

    try {
      const { initPoint } = await createPreference(productId)
      if (popup) {
        popup.location.href = initPoint
      } else {
        // El popup fue bloqueado: redirigimos en la misma pestaña (comportamiento previo).
        window.location.href = initPoint
        return
      }
    } catch (err) {
      // Si el backend rechazó con una regla de negocio (ej. ya tenés un plan activo),
      // mostramos su mensaje; si no, un genérico.
      setError(err instanceof ApiError ? err.message : 'Error al iniciar el pago. Intentá de nuevo.')
      if (popup) popup.close()
    } finally {
      setBuyingId(null)
    }
  }

  return { buyingId, error, buy }
}
