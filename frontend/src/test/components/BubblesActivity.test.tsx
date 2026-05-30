import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import BubblesActivity from '../../components/Bubbles/BubblesActivity.tsx'


describe('BubblesActivity component', () => {
  it('renderiza el menú de inicio', () => {
    render(<BubblesActivity />)
    expect(screen.getByRole('button', { name: /comenzar/i })).toBeInTheDocument()
  })

  it('renderiza el botón de volver en el menú', () => {
    render(<BubblesActivity />)
    expect(screen.getByRole('button', { name: /volver/i })).toBeInTheDocument()
  })

  it('llama a onBack al hacer click en el botón de volver', async () => {
    const onBack = vi.fn()
    render(<BubblesActivity onBack={onBack} />)
    await userEvent.click(screen.getByRole('button', { name: /volver/i }))
    expect(onBack).toHaveBeenCalled()
  })

  it('renderiza el componente de burbujas al hacer click en comenzar', async () => {
    render(<BubblesActivity />)
    await userEvent.click(screen.getByRole('button', { name: /comenzar/i }))
    expect(screen.getByTestId('bubbles-container')).toBeInTheDocument()
  })





})