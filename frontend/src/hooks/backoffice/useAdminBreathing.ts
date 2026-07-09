import { useState, useCallback, useEffect } from 'react'
import { adminBreathingApi, type AdminBreathingTechnique, type BreathingFormData } from '../../api/adminBreathing'

export function useAdminBreathing() {
    const [techniques, setTechniques] = useState<AdminBreathingTechnique[]>([])
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    const load = useCallback(async () => {
        setLoading(true)
        try {
            setTechniques(await adminBreathingApi.list())
        } catch {
            setError('No se pudieron cargar las respiraciones')
        } finally {
            setLoading(false)
        }
    }, [])

    useEffect(() => { load() }, [load])

    const create = useCallback(async (data: BreathingFormData): Promise<boolean> => {
        try {
            await adminBreathingApi.create(data)
            await load()
            return true
        } catch (e) {
            setError(e instanceof Error ? e.message : 'No se pudo crear la respiración')
            return false
        }
    }, [load])

    const update = useCallback(async (id: number, data: BreathingFormData): Promise<boolean> => {
        try {
            await adminBreathingApi.update(id, data)
            await load()
            return true
        } catch (e) {
            setError(e instanceof Error ? e.message : 'No se pudo editar la respiración')
            return false
        }
    }, [load])

    const setActive = useCallback(async (id: number, active: boolean): Promise<boolean> => {
        try {
            await adminBreathingApi.setActive(id, active)
            await load()
            return true
        } catch (e) {
            setError(e instanceof Error ? e.message : 'No se pudo cambiar el estado')
            return false
        }
    }, [load])

    return { techniques, loading, error, clearError: () => setError(null), reload: load, create, update, setActive }
}