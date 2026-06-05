import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useEmotionalOnboarding } from '../../hooks/useEmotionalOnboarding'
import { generateOnboardingOptions, completeOnboarding } from '../../api/onboarding' 

vi.mock('../../api/onboarding', () => ({
    generateOnboardingOptions: vi.fn(),
    completeOnboarding: vi.fn(),
}))

const STEP1_options = ['Calmar mi mente', 'Manejar el estrés', 'Explorar mis emociones', 'Desconectarme un rato', 'Expresar lo que siento',
                        'Encontrar mi espacio de pausa']

       
describe('useEmotionalOnboarding', () => {
    const onComplete = vi.fn()

    beforeEach(() => {
        vi.clearAllMocks()
    })

    it('inicia en el paso 1 con opciones predefinidas', () => {
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        expect(result.current.step).toBe(1)
        expect(result.current.options).toEqual(STEP1_options)
        expect(result.current.isLoading).toBe(false)
    })

    it('avanza al paso 2 y llama a la API cuando se elige una opción del paso 1', async () => {
        vi.mocked(generateOnboardingOptions).mockResolvedValueOnce({ options: ['A', 'B', 'C', 'D'] })
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        act(() => {
            result.current.selectOption('Desestresarme') 
        })

        await waitFor(() => expect(result.current.step).toBe(2))
        expect(generateOnboardingOptions).toHaveBeenCalledWith(2, 'Desestresarme')
        expect(result.current.options).toEqual(['A', 'B', 'C', 'D'] )
    })  

    it('avanza al paso 3 y llama a la API cuando se elige una opción del paso 2', async () => {
        vi.mocked(generateOnboardingOptions) 
            .mockResolvedValueOnce({ options: ['A', 'B', 'C', 'D'] })
            .mockResolvedValueOnce({ options: ['W', 'X', 'Y', 'Z'] })
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete ))
        act(() => {
            result.current.selectOption('Desestresarme') 
        })
        await waitFor(() => expect(result.current.step).toBe(2))
        act(() => {
            result.current.selectOption('A')

        })
        await waitFor(() => expect(result.current.step).toBe(3))
        expect(generateOnboardingOptions).toHaveBeenCalledWith(3, 'A')
        expect(result.current.options).toEqual(['W', 'X', 'Y', 'Z'])
    })

    it('completa el onboarding y llama a onComplete con la opción elegida', async () => {
        vi.mocked(generateOnboardingOptions) 
            .mockResolvedValueOnce({ options: ['A', 'B', 'C', 'D'] })
            .mockResolvedValueOnce({ options: ['W', 'X', 'Y', 'Z'] })
            vi.mocked(completeOnboarding).mockResolvedValueOnce(undefined)

            const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
            act(() => {
                result.current.selectOption('Desestresarme') 
            })
            await waitFor(() => expect(result.current.step).toBe(2))
            act(() => {
                result.current.selectOption('A')
            })
            await waitFor(() => expect(result.current.step).toBe(3))
            act(() => {
                result.current.selectOption('W')
            })
            await waitFor(() => expect(completeOnboarding).toHaveBeenCalledWith('Desestresarme', 'A', 'W'))
            expect(onComplete).toHaveBeenCalledTimes(1)
    })

    it('muestra isLoading mientras espera la respuesta de la API', async () => {
        let resolve: (val: { options: string[] }) => void
        vi.mocked(generateOnboardingOptions).mockReturnValueOnce(new Promise(res => resolve = res))
        const { result } = renderHook(() => useEmotionalOnboarding(onComplete))
        act(() => {
            result.current.selectOption('Desestresarme') 
        })
        expect(result.current.isLoading).toBe(true)
        act(() => resolve!({ options: ['A', 'B', 'C', 'D'] }))
        await waitFor(() => expect(result.current.isLoading).toBe(false))
    })
})