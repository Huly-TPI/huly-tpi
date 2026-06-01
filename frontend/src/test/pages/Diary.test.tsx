import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'

vi.mock('../../assets/garden/light-theme/cloud.webp', () => ({ default: 'cloud.webp' }))

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async (importOriginal) => {
  const actual = await importOriginal<typeof import('react-router-dom')>()
  return { ...actual, useNavigate: () => mockNavigate }
})

vi.mock('../../api/journal', () => ({
  journalApi: {
    list: vi.fn(),
    create: vi.fn(),
  },
}))

import Diary from '../../pages/Diary'
import { journalApi } from '../../api/journal'
import type { JournalEntryResponse } from '../../api/journal'

const mockedList = vi.mocked(journalApi.list)
const mockedCreate = vi.mocked(journalApi.create)

const makeEntry = (overrides: Partial<JournalEntryResponse> = {}): JournalEntryResponse => ({
  id: 1,
  content: JSON.stringify({ adentro: 'Mi día', pensamiento: 'Un pensamiento', bien: 'Algo bueno', manana: 'Un deseo' }),
  mood: 'HAPPY',
  createdAt: '2025-01-15T10:00:00Z',
  ...overrides,
})

describe('Diary', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedList.mockResolvedValue([])
  })

  const renderDiary = () => {
    const user = userEvent.setup()
    render(
      <MemoryRouter>
        <Diary />
      </MemoryRouter>
    )
    return { user }
  }


  it('renderiza el formulario de nueva entrada con los 4 campos', () => {
    renderDiary()
    expect(screen.getByPlaceholderText('Hoy me pasó...')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Lo que ya no quiero cargar...')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Hoy logré...')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Mañana quiero...')).toBeInTheDocument()
  })

  it('renderiza los 8 estados de ánimo disponibles', () => {
    renderDiary()
    expect(screen.getByText('Feliz')).toBeInTheDocument()
    expect(screen.getByText('Tranquilo')).toBeInTheDocument()
    expect(screen.getByText('Motivado')).toBeInTheDocument()
    expect(screen.getByText('Neutro')).toBeInTheDocument()
    expect(screen.getByText('Cansado')).toBeInTheDocument()
    expect(screen.getByText('Ansioso')).toBeInTheDocument()
    expect(screen.getByText('Frustrado')).toBeInTheDocument()
    expect(screen.getByText('Triste')).toBeInTheDocument()
  })

  it('carga las entradas del servidor al montar', async () => {
    mockedList.mockResolvedValueOnce([makeEntry({ id: 1 }), makeEntry({ id: 2 })])
    renderDiary()

    await waitFor(() => {
      expect(mockedList).toHaveBeenCalledTimes(1)
    })
  })


  it('el botón Guardar está deshabilitado sin contenido', () => {
    renderDiary()
    expect(screen.getByRole('button', { name: /Guardar/ })).toBeDisabled()
  })

  it('el botón Guardar se habilita al escribir en cualquier campo', async () => {
    const { user } = renderDiary()
    await user.type(screen.getByPlaceholderText('Hoy me pasó...'), 'Algo que sentí')
    expect(screen.getByRole('button', { name: /Guardar/ })).toBeEnabled()
  })


  it('permite seleccionar un mood', async () => {
    const { user } = renderDiary()
    const felizButton = screen.getByText('Feliz').closest('button')!
    await user.click(felizButton)
    expect(felizButton.querySelector('div')).toHaveClass('bg-green-400')
  })

  it('deselecciona el mood al hacer click por segunda vez', async () => {
    const { user } = renderDiary()
    const felizButton = screen.getByText('Feliz').closest('button')!
    await user.click(felizButton)
    await user.click(felizButton)
    expect(felizButton.querySelector('div')).toHaveClass('bg-green-200')
  })


  it('guarda el contenido como JSON con los 4 campos', async () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    const { user } = renderDiary()

    await user.type(screen.getByPlaceholderText('Hoy me pasó...'), 'Lo de adentro')
    await user.type(screen.getByPlaceholderText('Lo que ya no quiero cargar...'), 'Mi pensamiento')
    await user.click(screen.getByRole('button', { name: /Guardar/ }))

    await waitFor(() => {
      const [data] = mockedCreate.mock.calls[0]
      const parsed = JSON.parse(data.content)
      expect(parsed.adentro).toBe('Lo de adentro')
      expect(parsed.pensamiento).toBe('Mi pensamiento')
      expect(parsed.bien).toBe('')
      expect(parsed.manana).toBe('')
    })
  })

  it('incluye el mood seleccionado al guardar', async () => {
    mockedCreate.mockResolvedValueOnce(makeEntry({ mood: 'CALM' }))
    const { user } = renderDiary()

    await user.click(screen.getByText('Tranquilo').closest('button')!)
    await user.type(screen.getByPlaceholderText('Hoy me pasó...'), 'Algo')
    await user.click(screen.getByRole('button', { name: /Guardar/ }))

    await waitFor(() => {
      const [data] = mockedCreate.mock.calls[0]
      expect(data.mood).toBe('CALM')
    })
  })

  it('limpia el formulario tras guardar exitosamente', async () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    const { user } = renderDiary()

    await user.type(screen.getByPlaceholderText('Hoy me pasó...'), 'Algo')
    await user.click(screen.getByRole('button', { name: /Guardar/ }))

    await waitFor(() => {
      expect(screen.getByPlaceholderText('Hoy me pasó...')).toHaveValue('')
    })
  })

  it('muestra error cuando falla el guardado', async () => {
    mockedCreate.mockRejectedValueOnce(new Error('Error del servidor'))
    const { user } = renderDiary()

    await user.type(screen.getByPlaceholderText('Hoy me pasó...'), 'Algo')
    await user.click(screen.getByRole('button', { name: /Guardar/ }))

    await waitFor(() => {
      expect(screen.getByText('No se pudo guardar la entrada. Intentá de nuevo.')).toBeInTheDocument()
    })
  })


  it('navega atrás al hacer click en Cerrar cuaderno', async () => {
    const { user } = renderDiary()
    await user.click(screen.getByText(/Cerrar cuaderno/))
    expect(mockNavigate).toHaveBeenCalledWith(-1)
  })

  it('muestra la paginación correcta al cargar entradas', async () => {
    mockedList.mockResolvedValueOnce([makeEntry({ id: 1 }), makeEntry({ id: 2 })])
    renderDiary()

    await waitFor(() => {
      expect(screen.getByText('1 / 3')).toBeInTheDocument()
    })
  })

  it('navega a una entrada pasada y la muestra en modo lectura', async () => {
    mockedList.mockResolvedValueOnce([makeEntry({ id: 1 })])
    const { user } = renderDiary()

    await waitFor(() => screen.getByText('1 / 2'))
    await user.click(screen.getByText('›'))

    expect(screen.getByText('2 / 2')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Hoy me pasó...')).toHaveAttribute('readonly')
  })


  it('parsea el contenido JSON de una entrada existente', async () => {
    mockedList.mockResolvedValueOnce([
      makeEntry({
        content: JSON.stringify({ adentro: 'Texto adentro', pensamiento: 'Texto pensamiento', bien: '', manana: '' }),
      }),
    ])
    const { user } = renderDiary()

    await waitFor(() => screen.getByText('1 / 2'))
    await user.click(screen.getByText('›'))

    expect(screen.getByDisplayValue('Texto adentro')).toBeInTheDocument()
    expect(screen.getByDisplayValue('Texto pensamiento')).toBeInTheDocument()
  })

  it('usa texto plano como fallback para entradas sin formato JSON', async () => {
    mockedList.mockResolvedValueOnce([
      makeEntry({ content: 'Texto plano sin JSON' }),
    ])
    const { user } = renderDiary()

    await waitFor(() => screen.getByText('1 / 2'))
    await user.click(screen.getByText('›'))

    expect(screen.getByDisplayValue('Texto plano sin JSON')).toBeInTheDocument()
  })
})
