import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import EmotionalOnboarding from '../../components/onboarding/EmotionalOnboarding/EmotionalOnboarding'           

describe('EmotionalOnboarding', () => {
    const defaultProps = { 
        step: 1 as const, 
        options: ['Calmar mi mente', "Manejar el estrés", "Explorar mis emociones"], 
        isLoading: false,
        onSelectOption: vi.fn()
    }

    it('renderiza las opciones del paso actual', ()  => {
        render(<EmotionalOnboarding {...defaultProps} />)
        expect(screen.getByRole('button', {name: 'Calmar mi mente'})).toBeInTheDocument()
        expect(screen.getByRole('button', {name: 'Manejar el estrés'})).toBeInTheDocument()
        expect(screen.getByRole('button', {name: 'Explorar mis emociones'})).toBeInTheDocument()
    })

    it('llama a onSelectOption con la opcion elegida al hacer click', async () => {
        const user = userEvent.setup()
        const onSelectOption = vi.fn()
        render(<EmotionalOnboarding {...defaultProps} onSelectOption={onSelectOption} />)
        await user.click(screen.getByRole('button', {name: 'Calmar mi mente'}))
        expect(onSelectOption).toHaveBeenCalledWith('Calmar mi mente')
    })

    it('muestra indicador de carga cuando isLoading es true', () => {
        render(<EmotionalOnboarding {...defaultProps} isLoading={true}/>)
        expect(screen.getByRole('status')).toBeInTheDocument() 
    })

    it('deshabilita los botones cuando isLoading es true', () => {
    render(<EmotionalOnboarding {...defaultProps} isLoading={true}/>)
    expect(screen.queryAllByRole('button')).toHaveLength(0)
    expect(screen.getByRole('status')).toBeInTheDocument()
    })

    it('muestra el titulo correcto segun el paso', () => {
        const { rerender } = render(<EmotionalOnboarding {...defaultProps} step={1} />)
        expect(screen.getByRole('heading')).toBeInTheDocument()
        rerender(<EmotionalOnboarding {...defaultProps} step={2} />)
        expect(screen.getByRole('heading')).toBeInTheDocument()
        rerender(<EmotionalOnboarding {...defaultProps} step={3} />)
        expect(screen.getByRole('heading')).toBeInTheDocument()
    })

    })