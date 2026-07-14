import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, waitFor, act } from '@testing-library/react'
import { useAdminBreathing } from '../../hooks/backoffice/useAdminBreathing'

const list = vi.fn()
const create = vi.fn()
const update = vi.fn()
const setActive = vi.fn()

vi.mock('../../api/adminBreathing', () => ({
    adminBreathingApi: {
        list: (...a: unknown[]) => list(...a),
        create: (...a: unknown[]) => create(...a),
        update: (...a: unknown[]) => update(...a),
        setActive: (...a: unknown[]) => setActive(...a),
    },
}))

const technique = { id: 1, name: 'Diafragmática', description: 'd', inhaleSeconds: 4, holdSeconds: 0, exhaleSeconds: 4, roundsInterval: 1, rounds: 4, active: true }

describe('useAdminBreathing', () => {
    beforeEach(() => {
        vi.clearAllMocks()
        setupListSuccess([technique])
    })

    it('carga las respiraciones al cargar', async () => {
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        verifyTechniques(result, [technique])
        verifyListCalledOnce()
    })

    it('setea error si falla la carga', async () => {
        setupListFailure(new Error('x'))
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        verifyError(result, 'No se pudieron cargar las respiraciones')
    })

    it('create llama a la api y devuelve true', async () => {
        setupCreateSuccess()
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        const ok = await callCreate(result, {} as never)
        verifyCreateCalledOnce()
        expect(ok).toBe(true)
    })

    it('create devuelve false y setea error si falla', async () => {
        setupCreateFailure(new Error('boom'))
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        const ok = await callCreate(result, {} as never)
        expect(ok).toBe(false)
        verifyError(result, 'boom')
    })

    it('update llama a la api y devuelve true', async () => {
        setupUpdateSuccess()
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        const ok = await callUpdate(result, 1, {} as never)
        verifyUpdateCalledWith(1, {})
        expect(ok).toBe(true)
    })

    it('update devuelve false y setea error si falla', async () => {
        setupUpdateFailure(new Error('boom'))
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        const ok = await callUpdate(result, 1, {} as never)
        expect(ok).toBe(false)
        verifyError(result, 'boom')
    })

    it('setActive llama a la api con el id y estado', async () => {
        setupSetActiveSuccess()
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        await callSetActive(result, 1, false)
        verifySetActiveCalledWith(1, false)
    })

    it('setActive devuelve false y setea error si falla', async () => {
        setupSetActiveFailure(new Error('nope'))
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        const ok = await callSetActive(result, 1, false)
        expect(ok).toBe(false)
        verifyError(result, 'nope')
    })

    it('usa mensaje por defecto si el error de create no es Error', async () => {
        setupCreateFailure('x')
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        await callCreate(result, {} as never)
        verifyError(result, 'No se pudo crear la respiración')
    })

    it('usa mensaje por defecto si el error de update no es Error', async () => {
        setupUpdateFailure('x')
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        await callUpdate(result, 1, {} as never)
        verifyError(result, 'No se pudo editar la respiración')
    })

    it('usa mensaje por defecto si el error de setActive no es Error', async () => {
        setupSetActiveFailure('x')
        const { result } = renderAdminBreathing()
        await verifyLoadingFinished(result)
        await callSetActive(result, 1, false)
        verifyError(result, 'No se pudo cambiar el estado')
    })

    const renderAdminBreathing = () => {
        return renderHook(() => useAdminBreathing())
    }

    const setupListSuccess = (val: any) => {
        list.mockResolvedValue(val)
    }

    const setupListFailure = (err: any) => {
        list.mockRejectedValueOnce(err)
    }

    const setupCreateSuccess = () => {
        create.mockResolvedValue(undefined)
    }

    const setupCreateFailure = (err: any) => {
        create.mockRejectedValue(err)
    }

    const setupUpdateSuccess = () => {
        update.mockResolvedValue(undefined)
    }

    const setupUpdateFailure = (err: any) => {
        update.mockRejectedValue(err)
    }

    const setupSetActiveSuccess = () => {
        setActive.mockResolvedValue(undefined)
    }

    const setupSetActiveFailure = (err: any) => {
        setActive.mockRejectedValue(err)
    }

    const verifyLoadingFinished = async (result: any) => {
        await waitFor(() => expect(result.current.loading).toBe(false))
    }

    const verifyTechniques = (result: any, expected: any[]) => {
        expect(result.current.techniques).toEqual(expected)
    }

    const verifyListCalledOnce = () => {
        expect(list).toHaveBeenCalledOnce()
    }

    const verifyError = (result: any, expectedError: string | null) => {
        expect(result.current.error).toBe(expectedError)
    }

    const callCreate = async (result: any, d: any) => {
        let ok: boolean | undefined
        await act(async () => { ok = await result.current.create(d) })
        return ok
    }

    const callUpdate = async (result: any, id: number, d: any) => {
        let ok: boolean | undefined
        await act(async () => { ok = await result.current.update(id, d) })
        return ok
    }

    const callSetActive = async (result: any, id: number, active: boolean) => {
        let ok: boolean | undefined
        await act(async () => { ok = await result.current.setActive(id, active) })
        return ok
    }

    const verifyCreateCalledOnce = () => {
        expect(create).toHaveBeenCalledOnce()
    }

    const verifyUpdateCalledWith = (id: number, d: any) => {
        expect(update).toHaveBeenCalledWith(id, d)
    }

    const verifySetActiveCalledWith = (id: number, active: boolean) => {
        expect(setActive).toHaveBeenCalledWith(id, active)
    }
})