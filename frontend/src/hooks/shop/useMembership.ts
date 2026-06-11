import { useState, useEffect } from 'react'
import { getMyMembership, type Membership } from '../../api/auth'
import { useAuth } from '../../context/auth'

export function useMembership() {
  const { loading: authLoading } = useAuth()
  const [membership, setMembership] = useState<Membership | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (authLoading) return
    getMyMembership()
      .then(setMembership)
      .catch(() => setError('No se pudo cargar la membresía.'))
  }, [authLoading])

  return { membership, error }
}
