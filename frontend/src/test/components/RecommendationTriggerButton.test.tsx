import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import RecommendationTriggerButton from '../../components/Pending/RecommendationTriggerButton'

describe('RecommendationTriggerButton', () => {
  it('llama a onClick al presionarlo', async () => {
    const onClick = vi.fn().mockResolvedValue(undefined)
    render(<RecommendationTriggerButton onClick={onClick} />)

    fireEvent.click(screen.getByLabelText('Generar combinación recomendada'))

    await waitFor(() => expect(onClick).toHaveBeenCalled())
  })

  it('muestra un tooltip explicando la acción al pasar el mouse', () => {
    render(<RecommendationTriggerButton onClick={vi.fn()} />)

    fireEvent.mouseOver(screen.getByLabelText('Generar combinación recomendada'))

    expect(screen.getByText('Generar combinación recomendada')).toBeInTheDocument()
  })

  it('se deshabilita mientras espera la respuesta', async () => {
    let resolvePromise: () => void = () => {}
    const onClick = vi.fn(() => new Promise<void>(resolve => { resolvePromise = resolve }))
    render(<RecommendationTriggerButton onClick={onClick} />)

    fireEvent.click(screen.getByLabelText('Generar combinación recomendada'))

    await waitFor(() => expect(screen.getByLabelText('Generar combinación recomendada')).toBeDisabled())

    resolvePromise()
    await waitFor(() => expect(screen.getByLabelText('Generar combinación recomendada')).not.toBeDisabled())
  })
})
