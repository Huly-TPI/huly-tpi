import { clearAllMocks } from '../testHelpers'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act, waitFor } from '@testing-library/react'
import { useEmotionalOnboarding } from '../../hooks/useEmotionalOnboarding'
import { completeOnboarding } from '../../api/onboarding'

vi.mock('../../api/onboarding', () => ({
  completeOnboarding: vi.fn(),
}))

const mockedCompleteOnboarding = vi.mocked(completeOnboarding)

describe('useEmotionalOnboarding', () => {
  const onComplete = vi.fn()
  let rendered: ReturnType<typeof renderHook<ReturnType<typeof useEmotionalOnboarding>, undefined>>

  beforeEach(() => {
    clearAllMocks()
  })

  // --- CASOS DE PRUEBA (TEST SUITE) ---

  it('inicia en el step 0 (intro)', () => {
    setupHook()
    verifyStep(0)
  })

  it('advance() pasa al step 1 con las 4 tarjetas', () => {
    setupHook()
    callAdvance()
    verifyStep(1)
    verifyStep1OptionsLength(4)
  })

  it('selectOption en step 1 avanza al step 2 con pills mapeadas según la elección', () => {
    setupHook()
    callAdvance()
    return callSelectOption('Soltar lo que cargo').then(() => {
      verifyStep(2)
      verifyPillOptionsContains('Poner en palabras lo que siento')
      verifyPillOptionsLength(4)
    })
  })

  it('selectOption en step 2 avanza al step 3 con las opciones de cierre', () => {
    setupHook()
    callAdvance()
    return callSelectOption('Un momento para mí')
      .then(() => waitForStep(2))
      .then(() => callSelectOption('Bajar el ritmo'))
      .then(() => {
        verifyStep(3)
        verifyPillOptionsContains('Mi espacio de pausa diaria')
        verifyPillOptionsLength(4)
      })
  })

  it('selectOption en step 3 llama a completeOnboarding con las 3 respuestas y luego onComplete', () => {
    setupCompleteOnboardingResolved()
    setupHook()
    callAdvance()
    return callSelectOption('Descansar un rato')
      .then(() => waitForStep(2))
      .then(() => callSelectOption('Jugar algo tranquilo'))
      .then(() => waitForStep(3))
      .then(() => callSelectOption('Todavía lo estoy descubriendo'))
      .then(() => {
        return waitFor(() => {
          verifyCompleteOnboardingCalledWith(
            'Descansar un rato',
            'Jugar algo tranquilo',
            'Todavía lo estoy descubriendo'
          )
          verifyOnCompleteCalledTimes(1)
        })
      })
  })

  it('skip() llama a completeOnboarding con strings vacios y luego onComplete', () => {
    setupCompleteOnboardingResolved()
    setupHook()
    return callSkip().then(() => {
      return waitFor(() => {
        verifyCompleteOnboardingCalledWith(
          'Prefiero no decirlo',
          'Prefiero no decirlo',
          'Todavía lo estoy descubriendo'
        )
        verifyOnCompleteCalledTimes(1)
      })
    })
  })

  it('hacer más de un click en step 3 no dispara completeOnboarding más de una vez', () => {
    setupCompleteOnboardingResolved()
    setupHook()
    callAdvance()
    return callSelectOption('Descansar un rato')
      .then(() => waitForStep(2))
      .then(() => callSelectOption('Jugar algo tranquilo'))
      .then(() => waitForStep(3))
      .then(() => {
        return callMultipleSelectOptions([
          'Todavía lo estoy descubriendo',
          'Todavía lo estoy descubriendo',
        ])
      })
      .then(() => {
        return waitFor(() => {
          verifyCompleteOnboardingCalledTimes(1)
        })
      })
  })

  it('un segundo skip mientras carga otro no dispara de nuevo completeOnboarding', () => {
    setupCompleteOnboardingResolved()
    setupHook()
    return callMultipleSkips().then(() => {
      return waitFor(() => {
        verifyCompleteOnboardingCalledTimes(1)
      })
    })
  })

  /* helpers */

  const setupHook = () => {
    rendered = renderHook(() => useEmotionalOnboarding(onComplete))
  }

  const setupCompleteOnboardingResolved = () => {
    mockedCompleteOnboarding.mockResolvedValue(undefined)
  }

  const callAdvance = () => {
    act(() => {
      rendered.result.current.advance()
    })
  }

  const callSelectOption = async (option: string) => {
    await act(async () => {
      await rendered.result.current.selectOption(option)
    })
  }

  const callMultipleSelectOptions = async (options: string[]) => {
    await act(async () => {
      for (const option of options) {
        rendered.result.current.selectOption(option)
      }
    })
  }

  const callSkip = async () => {
    await act(async () => {
      await rendered.result.current.skip()
    })
  }

  const callMultipleSkips = async () => {
    await act(async () => {
      rendered.result.current.skip()
      rendered.result.current.skip()
    })
  }

  const waitForStep = (step: number) => {
    return waitFor(() => {
      expect(rendered.result.current.step).toBe(step)
    })
  }

  const verifyStep = (step: number) => {
    expect(rendered.result.current.step).toBe(step)
  }

  const verifyStep1OptionsLength = (len: number) => {
    expect(rendered.result.current.step1Options).toHaveLength(len)
  }

  const verifyPillOptionsContains = (option: string) => {
    expect(rendered.result.current.pillOptions).toContain(option)
  }

  const verifyPillOptionsLength = (len: number) => {
    expect(rendered.result.current.pillOptions).toHaveLength(len)
  }

  const verifyCompleteOnboardingCalledWith = (first: string, second: string, third: string) => {
    expect(mockedCompleteOnboarding).toHaveBeenCalledWith(first, second, third)
  }

  const verifyCompleteOnboardingCalledTimes = (times: number) => {
    expect(mockedCompleteOnboarding).toHaveBeenCalledTimes(times)
  }

  const verifyOnCompleteCalledTimes = (times: number) => {
    expect(onComplete).toHaveBeenCalledTimes(times)
  }
})
