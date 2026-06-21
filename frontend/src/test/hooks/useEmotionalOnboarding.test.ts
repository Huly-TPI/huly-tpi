import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useEmotionalOnboarding } from '../../hooks/useEmotionalOnboarding'
import { completeOnboarding } from '../../api/onboarding'

vi.mock('../../api/onboarding', () => ({
    completeOnboarding: vi.fn(),
}))

describe('useEmotionalOnboarding', () => {
    const onComplete = vi.fn()

    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('inicia en el step 0 (intro)', () => {
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        expect(result.current.step).toBe(0)
    })

    it('advance() pasa al step 1 con las 4 tarjetas', () => {
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        act(() => { result.current.advance() })
        expect(result.current.step).toBe(1)
        expect(result.current.step1Options).toHaveLength(4)
    })

    it('selectOption en step 1 avanza al step 2 con pills mapeadas según la elección', async () => {
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        await act(async () => { result.current.advance() })
        await act(async () => { result.current.selectOption('Soltar lo que cargo') })
        expect(result.current.step).toBe(2)
        expect(result.current.pillOptions).toContain('Poner en palabras lo que siento')
        expect(result.current.pillOptions).toHaveLength(4)
    })

    it('selectOption en step 2 avanza al step 3 con las opciones de cierre', async () => {
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        await act(async () => { result.current.advance() })
        await act(async () => { result.current.selectOption('Un momento para mí') })
        await waitFor(() => { expect(result.current.step).toBe(2) })
        await act(async () => { result.current.selectOption('Bajar el ritmo') })
        expect(result.current.step).toBe(3)
        expect(result.current.pillOptions).toContain('Mi espacio de pausa diaria')
        expect(result.current.pillOptions).toHaveLength(4)
    })

    it('selectOption en step 3 llama a completeOnboarding con las 3 respuestas y luego onComplete', async () => {
        vi.mocked(completeOnboarding).mockResolvedValueOnce(undefined)
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        await act(async () => { result.current.advance() })
        await act(async () => { result.current.selectOption('Descansar un rato') })
        await waitFor(() => expect(result.current.step).toBe(2))
        await act(async () => { result.current.selectOption('Jugar algo tranquilo') })
        await waitFor(() => expect(result.current.step).toBe(3))
        await act(async () => { result.current.selectOption('Todavía lo estoy descubriendo') })
        expect(completeOnboarding).toHaveBeenCalledWith(
            'Descansar un rato',
            'Jugar algo tranquilo',
            'Todavía lo estoy descubriendo'
        )
        expect(onComplete).toHaveBeenCalledTimes(1)
    })

    it('skip() llama a completeOnboarding con strings vacios y luego onComplete', async () => {
        vi.mocked(completeOnboarding).mockResolvedValueOnce(undefined)
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        await act(async () => { result.current.skip() })
        await waitFor(() => {
            expect(completeOnboarding).toHaveBeenCalledWith(
                'Prefiero no decirlo',
                'Prefiero no decirlo',
                'Todavía lo estoy descubriendo'
            )
        })
        expect(onComplete).toHaveBeenCalledTimes(1)


    })

    it('hacer más de un click en step 3 no dispara completeOnboarding más de una vez', async () => {
        vi.mocked(completeOnboarding).mockResolvedValueOnce(undefined)
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        await act(async () => { result.current.advance() })
        await act(async () => { result.current.selectOption('Descansar un rato') })
        await waitFor(() => expect(result.current.step).toBe(2))
        await act(async () => { result.current.selectOption('Jugar algo tranquilo') })
        await waitFor(() => expect(result.current.step).toBe(3))
        await act(async () => {
            result.current.selectOption('Todavía lo estoy descubriendo')
            result.current.selectOption('Todavía lo estoy descubriendo')
        })
        expect(completeOnboarding).toHaveBeenCalledTimes(1)
    })

    it('un segundo skip mientras carga otro no dispara de nuevo completeOnboarding', async () => {
        vi.mocked(completeOnboarding).mockResolvedValue(undefined)
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        await act(async () => {
            result.current.skip()
            result.current.skip()
        })

        expect(completeOnboarding).toHaveBeenCalledTimes(1)
    })

})
