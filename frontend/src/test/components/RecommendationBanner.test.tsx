import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import RecommendationBanner from '../../components/Pending/RecommendationBanner'

describe('RecommendationBanner', () => {
  it('muestra las tareas recomendadas', () => {
    render(<RecommendationBanner taskTitles={['Lavar los platos', 'Escribir el informe']} onAccept={vi.fn()} onReject={vi.fn()} />)

    expect(screen.getByText('Lavar los platos')).toBeInTheDocument()
    expect(screen.getByText('Escribir el informe')).toBeInTheDocument()
  })

  it('el botón de tilde llama a onAccept', async () => {
    const onAccept = vi.fn().mockResolvedValue(undefined)
    render(<RecommendationBanner taskTitles={['Tarea']} onAccept={onAccept} onReject={vi.fn()} />)

    fireEvent.click(screen.getByLabelText('Aceptar recomendación'))

    await waitFor(() => expect(onAccept).toHaveBeenCalled())
  })

  it('el botón de cruz llama a onReject', async () => {
    const onReject = vi.fn().mockResolvedValue(undefined)
    render(<RecommendationBanner taskTitles={['Tarea']} onAccept={vi.fn()} onReject={onReject} />)

    fireEvent.click(screen.getByLabelText('Rechazar recomendación'))

    await waitFor(() => expect(onReject).toHaveBeenCalled())
  })

  it('no tiene un botón de cierre neutral: solo aceptar o rechazar', () => {
    render(<RecommendationBanner taskTitles={['Tarea']} onAccept={vi.fn()} onReject={vi.fn()} />)

    expect(screen.queryByLabelText(/^cerrar$/i)).not.toBeInTheDocument()
    expect(screen.getAllByRole('button')).toHaveLength(2)
  })

  it('se muestra como un modal por encima del contenido (con backdrop)', () => {
    render(<RecommendationBanner taskTitles={['Tarea']} onAccept={vi.fn()} onReject={vi.fn()} />)

    expect(screen.getByRole('dialog')).toBeInTheDocument()
  })
})
