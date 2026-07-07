import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { ThemeProvider } from '../../context/theme'

// --- SIMULACIONES GLOBALES (MOCKS) ---
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

const mockShowToast = vi.hoisted(() => vi.fn())

vi.mock('../../context/toast', () => ({
  useToast: () => ({ showToast: mockShowToast }),
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
import { clickButton, typePlaceholder, verifyTextPresent, verifyTextNotPresent, verifyPlaceholderPresent, verifyButtonDisabled, verifyButtonEnabled, clearAllMocks, verifyDisplayValuePresent } from '../testHelpers'

const mockedList = vi.mocked(journalApi.list)
const mockedCreate = vi.mocked(journalApi.create)



describe('Diary', () => {
  let user: ReturnType<typeof userEvent.setup>

  beforeEach(() => {
    clearAllMocks()
    localStorage.clear()
    mockedList.mockResolvedValue([])
    mockAuthState.user = { id: 1, name: 'Test', email: 'test@huly.com', role: 'USER' }
    mockRequireAuth.mockImplementation((fn: () => void) => fn())
  })

  // --- CASOS DE PRUEBA (TEST SUITE) ---

  describe('modal de consentimiento', () => {
    it('muestra el modal la primera vez que se abre el diario', () => {
      renderDiary(null)
      verifyTextPresent('Una pregunta antes de empezar')
    })

    it('no muestra el modal si el usuario ya aceptó', () => {
      localStorage.setItem('diaryTextConsent', 'true')
      renderDiary()
      verifyTextNotPresent('Una pregunta antes de empezar')
    })

    it('no muestra el modal si el usuario ya rechazó', () => {
      localStorage.setItem('diaryTextConsent', 'false')
      renderDiary()
      verifyTextNotPresent('Una pregunta antes de empezar')
    })

    it('guarda true en localStorage al aceptar', () => {
      renderDiary(null)
      return clickDiaryButton('Sí, pueden usarlo').then(() => {
        verifyLocalStorageConsent('true')
      })
    })

    it('guarda false en localStorage al rechazar', () => {
      renderDiary(null)
      return clickDiaryButton('Prefiero mantenerlo privado').then(() => {
        verifyLocalStorageConsent('false')
      })
    })

    it('oculta el modal después de aceptar', () => {
      renderDiary(null)
      return clickDiaryButton('Sí, pueden usarlo').then(() => {
        verifyTextNotPresent('Una pregunta antes de empezar')
      })
    })

    it('oculta el modal después de rechazar', () => {
      renderDiary(null)
      return clickDiaryButton('Prefiero mantenerlo privado').then(() => {
        verifyTextNotPresent('Una pregunta antes de empezar')
      })
    })
  })

  it('renderiza el formulario de nueva entrada con los 4 campos', () => {
    renderDiary()
    verifyPlaceholderPresent('Hoy me pasó...')
    verifyPlaceholderPresent('Lo que ya no quiero cargar...')
    verifyPlaceholderPresent('Hoy logré...')
    verifyPlaceholderPresent('Mañana quiero...')
  })

  it('renderiza los 8 estados de ánimo disponibles', () => {
    renderDiary()
    verifyTextPresent('Feliz')
    verifyTextPresent('Tranquilo')
    verifyTextPresent('Motivado')
    verifyTextPresent('Neutro')
    verifyTextPresent('Cansado')
    verifyTextPresent('Ansioso')
    verifyTextPresent('Frustrado')
    verifyTextPresent('Triste')
  })

  it('carga las entradas del servidor al montar', () => {
    mockedList.mockResolvedValueOnce([makeEntry({ id: 1 }), makeEntry({ id: 2 })])
    renderDiary()
    return verifyListWasCalled()
  })

  it('el botón Guardar está deshabilitado sin contenido', () => {
    renderDiary()
    verifyButtonDisabled(/Guardar/)
  })

  it('no registra sesión si el usuario no hizo ninguna edición', () => {
    unmountDiaryComponent()
    verifySessionNotStarted()
  })

  it('el botón Guardar se habilita al escribir en cualquier campo', () => {
    renderDiary()
    return writeInPlaceholder('Hoy me pasó...', 'Algo que sentí').then(() => {
      verifyButtonEnabled(/Guardar/)
    })
  })

  it('permite seleccionar un mood', () => {
    renderDiary()
    return selectMood('Feliz').then(() => {
      verifyMoodSelected('Feliz', true)
    })
  })

  it('deselecciona el mood al hacer click por segunda vez', () => {
    renderDiary()
    return selectMood('Feliz')
      .then(() => selectMood('Feliz'))
      .then(() => {
        verifyMoodSelected('Feliz', false)
      })
  })

  it('guarda el contenido como JSON con los 4 campos', () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    renderDiary()
    return writeInPlaceholder('Hoy me pasó...', 'Lo de adentro')
      .then(() => writeInPlaceholder('Lo que ya no quiero cargar...', 'Mi pensamiento'))
      .then(() => clickDiaryButton(/Guardar/))
      .then(() => {
        return verifyCreatePayloadContains({
          adentro: 'Lo de adentro',
          pensamiento: 'Mi pensamiento',
          bien: '',
          manana: ''
        })
      })
  })

  it('incluye el mood seleccionado al guardar', () => {
    mockedCreate.mockResolvedValueOnce(makeEntry({ mood: 'CALM' }))
    renderDiary()
    return selectMood('Tranquilo')
      .then(() => writeInPlaceholder('Hoy me pasó...', 'Algo'))
      .then(() => clickDiaryButton(/Guardar/))
      .then(() => {
        return verifyCreatePayloadContains({}, 'CALM')
      })
  })

  it('limpia el formulario tras guardar exitosamente', () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    renderDiary()
    return writeInPlaceholder('Hoy me pasó...', 'Algo')
      .then(() => clickDiaryButton(/Guardar/))
      .then(() => {
        return verifyPlaceholderValue('Hoy me pasó...', '')
      })
  })

  it('registra la sesión de actividad al guardar una entrada', () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    renderDiary()
    return writeInPlaceholder('Hoy me pasó...', 'Contenido')
      .then(() => clickDiaryButton(/Guardar/))
      .then(() => {
        return verifySessionSaved()
      })
  })

  it.skip('muestra toast de exito cuando guarda una entrada', () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    renderDiary()
    return writeInPlaceholder('Hoy me pasó...', 'Algo')
      .then(() => clickDiaryButton(/Guardar/))
      .then(() => {
        return verifyToastShown('Entrada al diario guardada correctamente.', 'success')
      })
  })

  it('muestra error cuando falla el guardado', () => {
    mockedCreate.mockRejectedValueOnce(new Error('Error del servidor'))
    renderDiary()
    return writeInPlaceholder('Hoy me pasó...', 'Algo')
      .then(() => clickDiaryButton(/Guardar/))
      .then(() => {
        return verifyToastShown('No se pudo guardar la entrada. Intentá de nuevo.', 'error')
      })
  })

  it('muestra toast de exito al guardar usando el formulario nuevo', () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    renderDiary()
    return typeInFirstTextbox('Algo')
      .then(() => clickDiaryButton(/Guardar/))
      .then(() => {
        return verifyToastShown('Entrada al diario guardada correctamente.', 'success')
      })
  })

  it('navega a home al hacer click en Volver', () => {
    renderDiary()
    return clickDiaryButton(/Volver/).then(() => {
      verifyNavigateTo('/')
    })
  })

  it('muestra la paginación correcta al cargar entradas', () => {
    mockedList.mockResolvedValueOnce([makeEntry({ id: 1 }), makeEntry({ id: 2 })])
    renderDiary()
    return verifyPaginationTextShown('1 / 3')
  })

  it('navega a una entrada pasada y la muestra en modo lectura', () => {
    mockedList.mockResolvedValueOnce([makeEntry({ id: 1 })])
    renderDiary()
    return waitAndClickPaginationButton('›', '1 / 2')
      .then(() => {
        verifyTextPresent('2 / 2')
        verifyPlaceholderReadonly('Hoy me pasó...', true)
      })
  })

  it('parsea el contenido JSON de una entrada existente', () => {
    mockedList.mockResolvedValueOnce([
      makeEntry({
        content: JSON.stringify({ adentro: 'Texto adentro', pensamiento: 'Texto pensamiento', bien: '', manana: '' }),
      }),
    ])
    renderDiary()
    return waitAndClickPaginationButton('›', '1 / 2')
      .then(() => {
        verifyDisplayValuePresent('Texto adentro')
        verifyDisplayValuePresent('Texto pensamiento')
      })
  })

  it('manda useTextForAI true al guardar cuando el usuario aceptó el consentimiento', () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    renderDiary('true')
    return writeInPlaceholder('Hoy me pasó...', 'Algo')
      .then(() => clickDiaryButton(/Guardar/))
      .then(() => {
        return verifyCreatePayloadContains({}, undefined, true)
      })
  })

  it('manda useTextForAI false al guardar cuando el usuario rechazó el consentimiento', () => {
    mockedCreate.mockResolvedValueOnce(makeEntry())
    renderDiary('false')
    return writeInPlaceholder('Hoy me pasó...', 'Algo')
      .then(() => clickDiaryButton(/Guardar/))
      .then(() => {
        return verifyCreatePayloadContains({}, undefined, false)
      })
  })

  it('usa texto plano como fallback para entradas sin formato JSON', () => {
    mockedList.mockResolvedValueOnce([
      makeEntry({ content: 'Texto plano sin JSON' }),
    ])
    renderDiary()
    return waitAndClickPaginationButton('›', '1 / 2')
      .then(() => {
        verifyDisplayValuePresent('Texto plano sin JSON')
      })
  })

  describe('acceso sin login', () => {
    beforeEach(() => {
      mockAuthState.user = null
      mockRequireAuth.mockImplementation(() => {})
    })

    it('los 4 campos de texto son de solo lectura cuando no hay sesión', () => {
      renderDiary()
      verifyPlaceholderReadonly('Hoy me pasó...', true)
      verifyPlaceholderReadonly('Lo que ya no quiero cargar...', true)
      verifyPlaceholderReadonly('Hoy logré...', true)
      verifyPlaceholderReadonly('Mañana quiero...', true)
    })

    it('llama a requireAuth al hacer foco en el campo "Lo que pasa adentro"', () => {
      renderDiary()
      return focusOnField('Hoy me pasó...').then(() => {
        verifyRequireAuthCalled()
      })
    })

    it('llama a requireAuth al hacer foco en el campo de pensamiento', () => {
      renderDiary()
      return focusOnField('Lo que ya no quiero cargar...').then(() => {
        verifyRequireAuthCalled()
      })
    })

    it('llama a requireAuth al hacer foco en el campo de logros', () => {
      renderDiary()
      return focusOnField('Hoy logré...').then(() => {
        verifyRequireAuthCalled()
      })
    })

    it('llama a requireAuth al hacer foco en el campo de mañana', () => {
      renderDiary()
      return focusOnField('Mañana quiero...').then(() => {
        verifyRequireAuthCalled()
      })
    })

    it('llama a requireAuth al hacer click en un emoji de estado de ánimo', () => {
      renderDiary()
      return selectMood('Feliz').then(() => {
        verifyRequireAuthCalled()
      })
    })

    it('no selecciona el mood cuando no hay sesión', () => {
      renderDiary()
      return selectMood('Feliz').then(() => {
        verifyMoodSelectedBackground('Feliz', false)
      })
    })
  })

  /* helpers */

  const renderDiary = (consent: 'true' | 'false' | null = 'true') => {
    if (consent !== null) localStorage.setItem(CONSENT_KEY, consent)
    user = userEvent.setup()
    render(
      <ThemeProvider>
        <MemoryRouter>
          <Diary />
        </MemoryRouter>
      </ThemeProvider>
    )
  }

  const writeInPlaceholder = async (placeholder: string, text: string) => {
    await typePlaceholder(user, placeholder, text)
  }

  const clickDiaryButton = async (name: string | RegExp) => {
    await clickButton(user, name)
  }

  const selectMood = async (name: string) => {
    const button = screen.getByText(name).closest('button')!
    await user.click(button)
  }

  const focusOnField = async (placeholder: string) => {
    await user.click(screen.getByPlaceholderText(placeholder))
  }

  const typeInFirstTextbox = async (text: string) => {
    const [firstTextbox] = screen.getAllByRole('textbox')
    await user.type(firstTextbox, text)
  }

  const waitAndClickPaginationButton = async (btnText: string, waitText: string) => {
    await screen.findByText(waitText)
    await user.click(screen.getByText(btnText))
  }

  const unmountDiaryComponent = () => {
    const { unmount } = render(
      <ThemeProvider>
        <MemoryRouter>
          <Diary />
        </MemoryRouter>
      </ThemeProvider>,
    )
    unmount()
  }

  const verifyLocalStorageConsent = (value: string | null) => {
    expect(localStorage.getItem(CONSENT_KEY)).toBe(value)
  }

  const verifyMoodSelected = (name: string, isSelected: boolean) => {
    const button = screen.getByText(name).closest('button')!
    if (isSelected) {
      expect(button.querySelector('div')).toHaveClass('ring-2')
    } else {
      expect(button.querySelector('div')).not.toHaveClass('ring-2')
    }
  }

  const verifyMoodSelectedBackground = (name: string, isSelected: boolean) => {
    const button = screen.getByText(name).closest('button')!
    if (isSelected) {
      expect(button.querySelector('div')).toHaveClass('bg-green-400')
    } else {
      expect(button.querySelector('div')).not.toHaveClass('bg-green-400')
    }
  }

  const verifyPlaceholderReadonly = (placeholder: string, isReadonly: boolean) => {
    const input = screen.getByPlaceholderText(placeholder)
    if (isReadonly) {
      expect(input).toHaveAttribute('readonly')
    } else {
      expect(input).not.toHaveAttribute('readonly')
    }
  }

  

  const verifyCreatePayloadContains = async (
    expectedContentPartial: Record<string, string>,
    expectedMood?: string,
    expectedUseTextForAI?: boolean
  ) => {
    await waitFor(() => {
      const [data] = mockedCreate.mock.calls[0]
      if (expectedMood !== undefined) {
        expect(data.mood).toBe(expectedMood)
      }
      if (expectedUseTextForAI !== undefined) {
        expect(data.useTextForAI).toBe(expectedUseTextForAI)
      }
      if (expectedContentPartial && Object.keys(expectedContentPartial).length > 0) {
        const parsed = JSON.parse(data.content)
        Object.keys(expectedContentPartial).forEach(key => {
          expect(parsed[key]).toBe(expectedContentPartial[key])
        })
      }
    })
  }

  const verifyListWasCalled = async () => {
    await waitFor(() => {
      expect(mockedList).toHaveBeenCalledTimes(1)
    })
  }

  const verifySessionSaved = async () => {
    await waitFor(() => {
      expect(mockSaveSession).toHaveBeenCalled()
    })
  }

  const verifySessionNotStarted = () => {
    expect(mockStartSession).not.toHaveBeenCalled()
  }

  const verifyToastShown = async (message: string, type: 'success' | 'error') => {
    await waitFor(() => {
      expect(mockShowToast).toHaveBeenCalledWith(message, type)
    })
  }

  const verifyNavigateTo = (path: string) => {
    expect(mockNavigate).toHaveBeenCalledWith(path)
  }

  const verifyRequireAuthCalled = () => {
    expect(mockRequireAuth).toHaveBeenCalled()
  }

  const verifyPlaceholderValue = async (placeholder: string, expectedValue: string) => {
    await waitFor(() => {
      expect(screen.getByPlaceholderText(placeholder)).toHaveValue(expectedValue)
    })
  }

  const verifyPaginationTextShown = async (text: string) => {
    await waitFor(() => {
      verifyTextPresent(text)
    })
  }
})

function makeEntry(overrides: Partial<JournalEntryResponse> = {}): JournalEntryResponse {
  return ({
  id: 1,
  content: JSON.stringify({ adentro: 'Mi día', pensamiento: 'Un pensamiento', bien: 'Algo bueno', manana: 'Un deseo' }),
  mood: 'HAPPY',
  createdAt: '2025-01-15T10:00:00Z',
  ...overrides,
})
}
