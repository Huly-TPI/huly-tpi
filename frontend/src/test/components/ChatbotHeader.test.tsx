import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ChatbotHeader from '../../components/Chatbot/ChatbotHeader'

describe('ChatbotHeader', () => {
  it('renders title, options button, and close button', () => {
    render(<ChatbotHeader onClose={vi.fn()} onReset={vi.fn()} />)
    expect(screen.getByText('Huly')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Abrir opciones del chat' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cerrar' })).toBeInTheDocument()
  })

  it('calls onClose when close is clicked', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()
    render(<ChatbotHeader onClose={onClose} onReset={vi.fn()} />)

    await user.click(screen.getByRole('button', { name: 'Cerrar' }))
    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('calls onReset from the options menu', async () => {
    const user = userEvent.setup()
    const onReset = vi.fn()
    render(<ChatbotHeader onClose={vi.fn()} onReset={onReset} />)

    await user.click(screen.getByRole('button', { name: 'Abrir opciones del chat' }))
    await user.click(screen.getByRole('menuitem', { name: 'Limpiar chat' }))

    expect(onReset).toHaveBeenCalledTimes(1)
  })
})

