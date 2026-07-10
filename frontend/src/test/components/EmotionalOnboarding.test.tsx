import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import EmotionalOnboarding from '../../components/Onboarding/EmotionalOnboarding/EmotionalOnboarding'
import type { Step1Option } from '../../hooks/useEmotionalOnboarding'
import { clickButton, verifyTextPresent } from '../testHelpers'
import { ThemeProvider } from '../../context/theme'



const mockStep1Options: Step1Option[] = [
    { Icon: MockIcon, title: 'Un momento para mí',    subtitle: 'Para pausar y respirar' },
    { Icon: MockIcon, title: 'Soltar lo que cargo',   subtitle: 'Tengo cosas en la cabeza' },
    { Icon: MockIcon, title: 'Entender lo que siento',subtitle: 'Quiero explorar mis emociones' },
    { Icon: MockIcon, title: 'Descansar un rato',     subtitle: 'Busco desconectarme un poco' },
]

const mockPillOptions = ['Opción A', 'Opción B', 'Opción C', 'Opción D']

describe('EmotionalOnboarding', () => {
    let onAdvanceSpy: any
    let onSkipSpy: any
    let onSelectOptionSpy: any

    const baseProps = {
        step1Options: mockStep1Options,
        pillOptions: [],
        onAdvance: vi.fn(),
        onSelectOption: vi.fn(),
        onSkip: vi.fn(),
    }

    describe('Step 0 - intro', () => {
        it('muestra el mensaje de bienvenida', () => {
            renderStep(0)
            expect(screen.getByText('Me alegra que estés acá', { exact: false })).toBeInTheDocument()
        })
        
        it('el botón Empezar llama a onAdvance', () => {
            renderStepWithAdvanceSpy(0)
            return clickButtonWithText('Empezar').then(() => {
                verifyOnAdvanceCalledTimes(1)
            })
        })

        it('el botón Saltar por ahora llama a onSkip', () => {
            renderStepWithSkipSpy(0)
            return clickButtonWithText('Saltar por ahora').then(() => {
                verifyOnSkipCalledTimes(1)
            })
        })
    })

    describe('Step 1 - tarjetas', () => {
        it('renderiza las 4 tarjetas con título y subtítulo', () => {
            renderStep(1)
            verifyTextPresent('Un momento para mí')
            verifyTextPresent('Para pausar y respirar')
            verifyTextPresent('Descansar un rato')
        })

        it('click en una tarjeta llama a onSelectOption con el título', () => {
            renderStepWithSelectOptionSpy(1)
            return clickButtonWithText('Un momento para mí Para pausar y respirar').then(() => {
                verifyOnSelectOptionCalledTimes(1)
                verifyOnSelectOptionCalledWith('Un momento para mí')
            })
        })

        it('muestra el botón Saltar que llama a onSkip', () => {
            renderStepWithSkipSpy(1)
            return clickButtonWithText('Saltar').then(() => {
                verifyOnSkipCalledTimes(1)
            })
        })
    })

    describe('Steps 2 y 3 - pills', () => {
        it('muestra las opciones de las pilas', () => {
            renderStepWithPillOptions(2, mockPillOptions)
            verifyTextPresent('Opción A')
            verifyTextPresent('Opción D')
        })

        it('click en una pill lama a onSelectOption con el título', () => {
            renderStepWithPillOptionsAndSelectOptionSpy(2, mockPillOptions)
            return clickButtonWithText('Opción A').then(() => {
                verifyOnSelectOptionCalledTimes(1)
                verifyOnSelectOptionCalledWith('Opción A')
            })
        })

        it('muestra el botón Saltar en Step3 llama a onSkip', () => {
            renderStepWithSkipSpy(2)
            return clickButtonWithText('Saltar').then(() => {
                verifyOnSkipCalledTimes(1)
            })
        })
    })
    let user: any

    const renderStep = (step: 0 | 1 | 2 | 3) => {
        render(
            <ThemeProvider>
                <EmotionalOnboarding {...baseProps} step={step} />
            </ThemeProvider>
        )
    }

    const renderStepWithAdvanceSpy = (step: 0 | 1 | 2 | 3) => {
        user = userEvent.setup()
        onAdvanceSpy = vi.fn()
        render(
            <ThemeProvider>
                <EmotionalOnboarding {...baseProps} step={step} onAdvance={onAdvanceSpy} />
            </ThemeProvider>
        )
    }

    const renderStepWithSkipSpy = (step: 0 | 1 | 2 | 3) => {
        user = userEvent.setup()
        onSkipSpy = vi.fn()
        render(
            <ThemeProvider>
                <EmotionalOnboarding {...baseProps} step={step} onSkip={onSkipSpy} />
            </ThemeProvider>
        )
    }

    const renderStepWithSelectOptionSpy = (step: 0 | 1 | 2 | 3) => {
        user = userEvent.setup()
        onSelectOptionSpy = vi.fn()
        render(
            <ThemeProvider>
                <EmotionalOnboarding {...baseProps} step={step} onSelectOption={onSelectOptionSpy} />
            </ThemeProvider>
        )
    }

    const renderStepWithPillOptions = (step: 0 | 1 | 2 | 3, pillOptions: string[]) => {
        render(
            <ThemeProvider>
                <EmotionalOnboarding {...baseProps} step={step} pillOptions={pillOptions} />
            </ThemeProvider>
        )
    }

    const renderStepWithPillOptionsAndSelectOptionSpy = (step: 0 | 1 | 2 | 3, pillOptions: string[]) => {
        user = userEvent.setup()
        onSelectOptionSpy = vi.fn()
        render(
            <ThemeProvider>
                <EmotionalOnboarding {...baseProps} step={step} pillOptions={pillOptions} onSelectOption={onSelectOptionSpy} />
            </ThemeProvider>
        )
    }

    const clickButtonWithText = (text: string) => {
        return clickButton(user, text)
    }

    const verifyOnAdvanceCalledTimes = (times: number) => {
        expect(onAdvanceSpy).toHaveBeenCalledTimes(times)
    }

    const verifyOnSkipCalledTimes = (times: number) => {
        expect(onSkipSpy).toHaveBeenCalledTimes(times)
    }

    const verifyOnSelectOptionCalledTimes = (times: number) => {
        expect(onSelectOptionSpy).toHaveBeenCalledTimes(times)
    }

    const verifyOnSelectOptionCalledWith = (value: string) => {
        expect(onSelectOptionSpy).toHaveBeenCalledWith(value)
    }
})

function MockIcon() {
  return <svg />
}
