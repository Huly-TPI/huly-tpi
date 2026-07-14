import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import BreathingAdminPage from '../../pages/Backoffice/BreathingAdminPage'

interface T { id: number; name: string; description: string; inhaleSeconds: number; holdSeconds: number; exhaleSeconds: number; roundsInterval: number; rounds: number; active: boolean }

const mockHook = {
  techniques: [] as T[],
  loading: false,
  error: null as string | null,
  clearError: vi.fn(),
  reload: vi.fn(),
  create: vi.fn(),
  update: vi.fn(),
  setActive: vi.fn(),
}

vi.mock('../../hooks/backoffice/useAdminBreathing', () => ({
  useAdminBreathing: () => mockHook,
}))

const technique: T = { id: 1, name: 'Diafragmática', description: 'd', inhaleSeconds: 4, holdSeconds: 0, exhaleSeconds: 4, roundsInterval: 1, rounds: 4, active: true }

describe('BreathingAdminPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    setupTechniques([])
    setupLoading(false)
    setupError(null)
  })

  it('muestra el estado de carga', () => {
    setupLoading(true)
    renderPage()
    verifyLoadingShown()
  })

  it('muestra el vacío cuando no hay respiraciones', () => {
    renderPage()
    verifyEmptyStateShown()
  })

  it('lista las respiraciones', () => {
    setupTechniques([technique])
    renderPage()
    verifyTechniqueNameShown('Diafragmática')
  })

  it('abre el formulario al tocar "Nueva respiración"', async () => {
    renderPage()
    await clickNewBreathing()
    verifyFormOpened()
  })

  it('abre el formulario de edición precargado', async () => {
    setupTechniques([technique])
    renderPage()
    await clickEditBreathing('Diafragmática')
    verifyEditFormOpenedWith('Diafragmática')
  })

  it('crea una respiración al completar y enviar', async () => {
    setupCreateResolved(true)
    renderPage()
    await clickNewBreathing()
    await typeName('Cuadrada')
    await clickCreateButton()
    verifyCreateCalled()
  })

  it('edita una respiración al guardar', async () => {
    setupTechniques([technique])
    setupUpdateResolved(true)
    renderPage()
    await clickEditBreathing('Diafragmática')
    await clickSaveButton()
    verifyUpdateCalledWith(1, { name: 'Diafragmática' })
  })

  it('desactiva con el botón Eliminar', async () => {
    setupTechniques([technique])
    renderPage()
    await clickActiveToggle('Diafragmática')
    verifySetActiveCalledWith(1, false)
  })

  it('restaura una respiración inactiva', async () => {
    setupTechniques([{ ...technique, active: false }])
    renderPage()
    await clickActiveToggle('Diafragmática')
    verifySetActiveCalledWith(1, true)
  })

  it('muestra el error en un toast', () => {
    setupError('Ups')
    renderPage()
    verifyToastErrorShown('Ups')
  })

  const renderPage = () => {
    render(<BreathingAdminPage />)
  }

  const setupLoading = (loading: boolean) => {
    mockHook.loading = loading
  }

  const setupTechniques = (techniques: T[]) => {
    mockHook.techniques = techniques
  }

  const setupError = (error: string | null) => {
    mockHook.error = error
  }

  const setupCreateResolved = (val: any) => {
    mockHook.create.mockResolvedValue(val)
  }

  const setupUpdateResolved = (val: any) => {
    mockHook.update.mockResolvedValue(val)
  }

  const verifyLoadingShown = () => {
    expect(screen.getByText('Cargando...')).toBeInTheDocument()
  }

  const verifyEmptyStateShown = () => {
    expect(screen.getByText('Todavía no hay respiraciones.')).toBeInTheDocument()
  }

  const verifyTechniqueNameShown = (name: string) => {
    expect(screen.getAllByText(name)[0]).toBeInTheDocument()
  }

  const clickNewBreathing = async () => {
    await userEvent.click(screen.getByRole('button', { name: /Nueva respiración/ }))
  }

  const verifyFormOpened = () => {
    expect(screen.getByText('Inhalar (s)')).toBeInTheDocument()
  }

  const clickEditBreathing = async (name: string) => {
    await userEvent.click(screen.getAllByRole('button', { name: `Editar ${name}` })[0])
  }

  const verifyEditFormOpenedWith = (name: string) => {
    expect(screen.getByText('Editar respiración')).toBeInTheDocument()
    expect(screen.getByDisplayValue(name)).toBeInTheDocument()
  }

  const typeName = async (name: string) => {
    await userEvent.type(screen.getByLabelText('Nombre'), name)
  }

  const clickCreateButton = async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Crear' }))
  }

  const clickSaveButton = async () => {
    await userEvent.click(screen.getByRole('button', { name: 'Guardar' }))
  }

  const clickActiveToggle = async (name: string) => {
    await userEvent.click(screen.getAllByRole('checkbox', { name: `Cambiar estado activo de ${name}` })[0])
  }

  const verifyCreateCalled = () => {
    expect(mockHook.create).toHaveBeenCalledOnce()
  }

  const verifyUpdateCalledWith = (id: number, expected: any) => {
    expect(mockHook.update).toHaveBeenCalledWith(id, expect.objectContaining(expected))
  }

  const verifySetActiveCalledWith = (id: number, active: boolean) => {
    expect(mockHook.setActive).toHaveBeenCalledWith(id, active)
  }

  const verifyToastErrorShown = (msg: string) => {
    expect(screen.getByText(msg)).toBeInTheDocument()
  }
})