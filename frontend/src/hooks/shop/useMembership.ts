import { useCallback, useState, useEffect } from 'react'
import { getMyMembership, type Membership } from '../../api/auth'
import { useAuth } from '../../context/auth'

export function useMembership() {
  const { loading: authLoading } = useAuth()
  const [membership, setMembership] = useState<Membership | null>(null)
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(() => {
    return getMyMembership()
      .then(setMembership)
      .catch(() => setError('No se pudo cargar la membresía.'))
  }, [])

  useEffect(() => {
    if (authLoading) return
    refresh()
  }, [authLoading, refresh])

  return { membership, error, refresh }
}
