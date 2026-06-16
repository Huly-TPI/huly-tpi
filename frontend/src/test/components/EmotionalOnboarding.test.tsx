import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import EmotionalOnboarding from '../../components/Onboarding/EmotionalOnboarding/EmotionalOnboarding'
import type { Step1Option } from '../../hooks/useEmotionalOnboarding'

const MockIcon = () => <svg />

const mockStep1Options: Step1Option[] = [
    { Icon: MockIcon, title: 'Un momento para mí',    subtitle: 'Para pausar y respirar' },
    { Icon: MockIcon, title: 'Soltar lo que cargo',   subtitle: 'Tengo cosas en la cabeza' },
    { Icon: MockIcon, title: 'Entender lo que siento',subtitle: 'Quiero explorar mis emociones' },
    { Icon: MockIcon, title: 'Descansar un rato',     subtitle: 'Busco desconectarme un poco' },
]

const mockPillOptions = ['Opción A', 'Opción B', 'Opción C', 'Opción D']

describe('EmotionalOnboarding', () => {
    const baseProps = {
        step1Options: mockStep1Options,
        pillOptions: [],
        onAdvance: vi.fn(),
        onSelectOption: vi.fn(),
        onSkip: vi.fn(),
    }

    describe('Step 0 - intro', () => {
        it('muestra el mensaje de bienvenida', () => {
            render(<EmotionalOnboarding {...baseProps} step={0}/>)
            expect(screen.getByText(/Me alegra que estés acá/i)).toBeInTheDocument()
        })
        
        it('el botón Empezar llama a onAdvance', async () => {
            const user = userEvent.setup()
            const onAdvance = vi.fn()
            render(<EmotionalOnboarding {...baseProps} step={0} onAdvance={onAdvance}/>)
            await user.click(screen.getByRole('button', { name: /empezar/i }))

            expect(onAdvance).toHaveBeenCalledTimes(1)
        })

        it('el botón Saltar por ahora llama a onSkip', async () => {
            const user = userEvent.setup()
            const onSkip = vi.fn()
            render(<EmotionalOnboarding {...baseProps} step={0} onSkip={onSkip}/>)
            await user.click(screen.getByRole('button', { name: /saltar por ahora/i }))
            expect(onSkip).toHaveBeenCalledTimes(1)
        })
})

    describe('Step 1 - tarjetas', () => {
        it('renderiza las 4 tarjetas con título y subtítulo', () => {
            render(<EmotionalOnboarding {...baseProps} step={1} />)
            expect(screen.getByText('Un momento para mí')).toBeInTheDocument()
            expect(screen.getByText('Para pausar y respirar')).toBeInTheDocument()
            expect(screen.getByText('Descansar un rato')).toBeInTheDocument()
        })

        it('click en una tarjeta llama a onSelectOption con el título', async () => {
            const user = userEvent.setup()
            const onSelectOption = vi.fn()
            render(<EmotionalOnboarding {...baseProps} step={1} onSelectOption={onSelectOption}/>)
            await user.click(screen.getByRole('button', { name: /Un momento para mí/i }))
            expect(onSelectOption).toHaveBeenCalledTimes(1)
            expect(onSelectOption).toHaveBeenCalledWith('Un momento para mí')
        })

        it('muestra el botón Saltar que llama a onSkip', async () => {
            const user = userEvent.setup()
            const onSkip = vi.fn()
            render(<EmotionalOnboarding {...baseProps} step={1} onSkip={onSkip}/>)
            await user.click(screen.getByRole('button', { name: /saltar/i }))
            expect(onSkip).toHaveBeenCalledTimes(1)
        })
    })

    describe('Steps 2 y 3 - pills', () => {
        it('muestra las opciones de las pilas', () => {
            render(<EmotionalOnboarding {...baseProps} step={2} pillOptions={mockPillOptions}/>)
            expect(screen.getByText('Opción A')).toBeInTheDocument()
            expect(screen.getByText('Opción D')).toBeInTheDocument()
        })

        it('click en una pill lama a onSelectOption con el título', async () => {
            const user = userEvent.setup()
            const onSelectOption = vi.fn()
            render(<EmotionalOnboarding {...baseProps} step={2} pillOptions={mockPillOptions} onSelectOption={onSelectOption}/>)
            await user.click(screen.getByRole('button', { name: /Opción A/i }))
            expect(onSelectOption).toHaveBeenCalledTimes(1)
            expect(onSelectOption).toHaveBeenCalledWith('Opción A')
        })

        it('muestra el botón Saltar en Step3 llama a onSkip', async () => {
            const user = userEvent.setup()
            const onSkip = vi.fn()
            render(<EmotionalOnboarding {...baseProps} step={2} onSkip={onSkip}/>)
            await user.click(screen.getByRole('button', { name: /saltar/i }))
            expect(onSkip).toHaveBeenCalledTimes(1)
        })
    })
})
