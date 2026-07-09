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
        list.mockResolvedValue([technique])
    })

    it('carga las respiraciones al cargar', async () => {
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        expect(result.current.techniques).toHaveLength(1)
        expect(list).toHaveBeenCalledOnce()
    })

    it('setea error si falla la carga', async () => {
        list.mockRejectedValueOnce(new Error('x'))
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        expect(result.current.error).toBe('No se pudieron cargar las respiraciones')
    })

    it('create llama a la api y devuelve true', async () => {
        create.mockResolvedValue(undefined)
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        let ok: boolean | undefined
        await act(async () => { ok = await result.current.create({} as never) })
        expect(create).toHaveBeenCalledOnce()
        expect(ok).toBe(true)
    })

    it('create devuelve false y setea error si falla', async () => {
        create.mockRejectedValue(new Error('boom'))
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        let ok: boolean | undefined
        await act(async () => { ok = await result.current.create({} as never) })
        expect(ok).toBe(false)
        expect(result.current.error).toBe('boom')
    })

    it('update llama a la api y devuelve true', async () => {
        update.mockResolvedValue(undefined)
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        let ok: boolean | undefined
        await act(async () => { ok = await result.current.update(1, {} as never) })
        expect(update).toHaveBeenCalledWith(1, {})
        expect(ok).toBe(true)
    })

    it('update devuelve false y setea error si falla', async () => {
        update.mockRejectedValue(new Error('boom'))
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        let ok: boolean | undefined
        await act(async () => { ok = await result.current.update(1, {} as never) })
        expect(ok).toBe(false)
        expect(result.current.error).toBe('boom')
    })

    it('setActive llama a la api con el id y estado', async () => {
        setActive.mockResolvedValue(undefined)
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        await act(async () => { await result.current.setActive(1, false) })
        expect(setActive).toHaveBeenCalledWith(1, false)
    })

    it('setActive devuelve false y setea error si falla', async () => {
        setActive.mockRejectedValue(new Error('nope'))
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        let ok: boolean | undefined
        await act(async () => { ok = await result.current.setActive(1, false) })
        expect(ok).toBe(false)
        expect(result.current.error).toBe('nope')
    })

    it('usa mensaje por defecto si el error de create no es Error', async () => {
        create.mockRejectedValue('x')
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        await act(async () => { await result.current.create({} as never) })
        expect(result.current.error).toBe('No se pudo crear la respiración')
    })

    it('usa mensaje por defecto si el error de update no es Error', async () => {
        update.mockRejectedValue('x')
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        await act(async () => { await result.current.update(1, {} as never) })
        expect(result.current.error).toBe('No se pudo editar la respiración')
    })

    it('usa mensaje por defecto si el error de setActive no es Error', async () => {
        setActive.mockRejectedValue('x')
        const { result } = renderHook(() => useAdminBreathing())
        await waitFor(() => expect(result.current.loading).toBe(false))
        await act(async () => { await result.current.setActive(1, false) })
        expect(result.current.error).toBe('No se pudo cambiar el estado')
    })
})