import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { ThemeProvider } from '../../context/theme'

vi.mock('../../assets/garden/light-theme/cloud.webp', () => ({ default: 'cloud.webp' }))
vi.mock('../../assets/brand/color-logo.webp', () => ({ default: 'color-logo.webp' }))

const mockAuthState = vi.hoisted(() => ({
  user: { id: 1, name: 'Test', email: 'test@huly.com', role: 'USER' } as { id: number; name: string; email: string; role: string } | null,
}))

vi.mock('../../context/auth', () => ({
  useAuth: () => ({ user: mockAuthState.user }),
}))

const mockRequireAuth = vi.hoisted(() => vi.fn((fn: () => void) => fn()))

vi.mock('../../context/authGate', () => ({
  useAuthGate: () => ({ requireAuth: mockRequireAuth }),
}))

const CONSENT_KEY = 'diaryTextConsent_1'

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

const mockSaveSession = vi.fn().mockResolvedValue({})
const mockStartSession = vi.fn()
const mockStopSession = vi.fn()

vi.mock('../../hooks/useActivitySessionTracker', () => ({
  useActivitySessionTracker: vi.fn(() => ({
    startSession: mockStartSession,
    saveSession: mockSaveSession,
    stopSession: mockStopSession,
    markConditionMet: vi.fn(),
  })),
}))

import Diary from '../../pages/Diary/Diary.tsx'
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
    localStorage.clear()
    mockedList.mockResolvedValue([])
    mockAuthState.user = { id: 1, name: 'Test', email: 'test@huly.com', role: 'USER' }
    mockRequireAuth.mockImplementation((fn: () => void) => fn())
  })

  const renderDiary = (consent: 'true' | 'false' | null = 'true') => {
    if (consent !== null) localStorage.setItem(CONSENT_KEY, consent)
    const user = userEvent.setup()
    render(
      <ThemeProvider>
        <MemoryRouter>
          <Diary />
        </MemoryRouter>
      </ThemeProvider>
    )
    return { user }
  }


  describe('modal de consentimiento', () => {
    it('muestra el modal la primera vez que se abre el diario', () => {
      renderDiary(null)
      expect(screen.getByText('Una pregunta antes de empezar')).toBeInTheDocument()
    })

    it('no muestra el modal si el usuario ya aceptó', () => {
      localStorage.setItem('diaryTextConsent', 'true')
      renderDiary()
      expect(screen.queryByText('Una pregunta antes de empezar')).not.toBeInTheDocument()
    })

    it('no muestra el modal si el usuario ya rechazó', () => {
      localStorage.setItem('diaryTextConsent', 'false')
      renderDiary()
      expect(screen.queryByText('Una pregunta antes de empezar')).not.toBeInTheDocument()
    })

    it('guarda true en localStorage al aceptar', async () => {
      const { user } = renderDiary(null)
      await user.click(screen.getByRole('button', { name: 'Sí, pueden usarlo' }))
      expect(localStorage.getItem(CONSENT_KEY)).toBe('true')
    })

    it('guarda false en localStorage al rechazar', async () => {
      const { user } = renderDiary(null)
      await user.click(screen.getByRole('button', { name: 'Prefiero mantenerlo privado' }))
      expect(localStorage.getItem(CONSENT_KEY)).toBe('false')
    })

    it('oculta el modal después de aceptar', async () => {
      const { user } = renderDiary(null)
      await user.click(screen.getByRole('button', { name: 'Sí, pueden usarlo' }))
      expect(screen.queryByText('Una pregunta antes de empezar')).not.toBeInTheDocument()
    })

    it('oculta el modal después de rechazar', async () => {
      const { user } = renderDiary(null)
      await user.click(screen.getByRole('button', { name: 'Prefiero mantenerlo privado' }))
      expect(screen.queryByText('Una pregunta antes de empezar')).not.toBeInTheDocument()
    })
  })

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

  it('no registra sesión si el usuario no hizo ninguna edición', () => {
    const { unmount } = render(
      <ThemeProvider>
        <MemoryRouter>
          <Diary />
        </MemoryRouter>
      </ThemeProvider>,
    )

    unmount()

    expect(mockStartSession).not.toHaveBeenCalled()
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

  it('registra la sesión de actividad al guardar una entrada', async () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    const { user } = renderDiary()

    await user.type(screen.getByPlaceholderText('Hoy me pasó...'), 'Contenido')
    
    await user.click(screen.getByRole('button', { name: /Guardar/ }))

    await waitFor(() => {
      expect(mockSaveSession).toHaveBeenCalled()
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


  it('navega a home al hacer click en Volver', async () => {
    const { user } = renderDiary()
    await user.click(screen.getByText(/Volver/))
    expect(mockNavigate).toHaveBeenCalledWith('/')
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

  it('manda useTextForAI true al guardar cuando el usuario aceptó el consentimiento', async () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    const { user } = renderDiary('true')

    await user.type(screen.getByPlaceholderText('Hoy me pasó...'), 'Algo')
    await user.click(screen.getByRole('button', { name: /Guardar/ }))

    await waitFor(() => {
      const [data] = mockedCreate.mock.calls[0]
      expect(data.useTextForAI).toBe(true)
    })
  })

  it('manda useTextForAI false al guardar cuando el usuario rechazó el consentimiento', async () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    const { user } = renderDiary('false')

    await user.type(screen.getByPlaceholderText('Hoy me pasó...'), 'Algo')
    await user.click(screen.getByRole('button', { name: /Guardar/ }))

    await waitFor(() => {
      const [data] = mockedCreate.mock.calls[0]
      expect(data.useTextForAI).toBe(false)
    })
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

  describe('acceso sin login', () => {
    beforeEach(() => {
      mockAuthState.user = null
      mockRequireAuth.mockImplementation(() => {})
    })

    it('los 4 campos de texto son de solo lectura cuando no hay sesión', () => {
      renderDiary()
      expect(screen.getByPlaceholderText('Hoy me pasó...')).toHaveAttribute('readonly')
      expect(screen.getByPlaceholderText('Lo que ya no quiero cargar...')).toHaveAttribute('readonly')
      expect(screen.getByPlaceholderText('Hoy logré...')).toHaveAttribute('readonly')
      expect(screen.getByPlaceholderText('Mañana quiero...')).toHaveAttribute('readonly')
    })

    it('llama a requireAuth al hacer foco en el campo "Lo que pasa adentro"', async () => {
      const { user } = renderDiary()
      await user.click(screen.getByPlaceholderText('Hoy me pasó...'))
      expect(mockRequireAuth).toHaveBeenCalled()
    })

    it('llama a requireAuth al hacer foco en el campo de pensamiento', async () => {
      const { user } = renderDiary()
      await user.click(screen.getByPlaceholderText('Lo que ya no quiero cargar...'))
      expect(mockRequireAuth).toHaveBeenCalled()
    })

    it('llama a requireAuth al hacer foco en el campo de logros', async () => {
      const { user } = renderDiary()
      await user.click(screen.getByPlaceholderText('Hoy logré...'))
      expect(mockRequireAuth).toHaveBeenCalled()
    })

    it('llama a requireAuth al hacer foco en el campo de mañana', async () => {
      const { user } = renderDiary()
      await user.click(screen.getByPlaceholderText('Mañana quiero...'))
      expect(mockRequireAuth).toHaveBeenCalled()
    })

    it('llama a requireAuth al hacer click en un emoji de estado de ánimo', async () => {
      const { user } = renderDiary()
      await user.click(screen.getByText('Feliz').closest('button')!)
      expect(mockRequireAuth).toHaveBeenCalled()
    })

    it('no selecciona el mood cuando no hay sesión', async () => {
      const { user } = renderDiary()
      const felizButton = screen.getByText('Feliz').closest('button')!
      await user.click(felizButton)
      expect(felizButton.querySelector('div')).not.toHaveClass('bg-green-400')
    })
  })
})
