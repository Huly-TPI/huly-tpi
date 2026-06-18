import { useCallback, useEffect, useRef } from 'react'

interface Options {
  /** Cuántas veces ejecutar onReturn al volver (para tolerar latencia del webhook). */
  attempts?: number
  /** Milisegundos entre reintentos. */
  intervalMs?: number
}

/**
 * Para acciones que llevan al usuario fuera de la pestaña (ej. pago en MercadoPago).
 *
 * Devuelve `markPending()`: llamalo al iniciar la acción. Cuando el usuario vuelve
 * a la pestaña (focus/visibilitychange), ejecuta `onReturn` varias veces espaciadas,
 * porque el resultado (webhook que acredita monedas / activa el plan) puede tardar
 * unos segundos en impactar en el backend.
 */
export function useRefreshOnReturn(
  onReturn: () => void,
  { attempts = 4, intervalMs = 2000 }: Options = {},
) {
  const pending = useRef(false)
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null)
  const onReturnRef = useRef(onReturn)
  onReturnRef.current = onReturn

  const markPending = useCallback(() => {
    pending.current = true
  }, [])

  useEffect(() => {
    const handleReturn = () => {
      if (document.visibilityState !== 'visible' || !pending.current) return
      pending.current = false

      let count = 0
      const tick = () => {
        onReturnRef.current()
        count += 1
        if (count < attempts) {
          timer.current = setTimeout(tick, intervalMs)
        }
      }
      tick()
    }

    document.addEventListener('visibilitychange', handleReturn)
    window.addEventListener('focus', handleReturn)
    return () => {
      document.removeEventListener('visibilitychange', handleReturn)
      window.removeEventListener('focus', handleReturn)
      if (timer.current) clearTimeout(timer.current)
    }
  }, [attempts, intervalMs])

  return markPending
}
