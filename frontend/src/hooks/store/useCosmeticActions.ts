import { useState } from 'react'
import { storeApi } from '../../api/store'
import { ApiError } from '../../api/apiError'

export function useCosmeticActions() {
    const [busyId, setBusyId] = useState<number | null>(null)
    const [error, setError] = useState<string | null>(null)

    const buy = async (id: number) => {
        setBusyId(id)
        setError(null)

        try {
            await storeApi.buy(id)
            return true
        } catch (err) {
            setError(err instanceof ApiError ? err.message : 'No se pudo comprar el item.')
            return false
        } finally {
            setBusyId(null)
        }
    }

    const equip = async (id: number): Promise<boolean> => {
        setBusyId(id)
        setError(null)

        try {
            await storeApi.equip(id)
            return true
        } catch (err) {
            setError(err instanceof ApiError ? err.message : 'No se pudo equipar el item.')
            return false
        } finally {
            setBusyId(null)
        }
    }

    const unequip = async (itemId: number): Promise<boolean> => {
        setBusyId(itemId)
        setError(null)
        try {
            await storeApi.unequip(itemId)
            return true
        } catch (err) {
            setError(err instanceof ApiError ? err.message : 'No se pudo quitar el item.')
            return false
        } finally {
            setBusyId(null)
        }
    }

    return { busyId, error, buy, equip, unequip }
}