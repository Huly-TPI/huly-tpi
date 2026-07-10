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
    mockHook.techniques = []
    mockHook.loading = false
    mockHook.error = null
  })

  it('muestra el estado de carga', () => {
    mockHook.loading = true
    render(<BreathingAdminPage />)
    expect(screen.getByText('Cargando...')).toBeInTheDocument()
  })

  it('muestra el vacío cuando no hay respiraciones', () => {
    render(<BreathingAdminPage />)
    expect(screen.getByText('Todavía no hay respiraciones.')).toBeInTheDocument()
  })

  it('lista las respiraciones', () => {
    mockHook.techniques = [technique]
    render(<BreathingAdminPage />)
    expect(screen.getAllByText('Diafragmática')[0]).toBeInTheDocument()
  })

  it('abre el formulario al tocar "Nueva respiración"', async () => {
    render(<BreathingAdminPage />)
    await userEvent.click(screen.getByRole('button', { name: /Nueva respiración/ }))
    expect(screen.getByText('Inhalar (s)')).toBeInTheDocument()
  })

  it('abre el formulario de edición precargado', async () => {
    mockHook.techniques = [technique]
    render(<BreathingAdminPage />)
    await userEvent.click(screen.getAllByRole('button', { name: 'Editar Diafragmática' })[0])
    expect(screen.getByText('Editar respiración')).toBeInTheDocument()
    expect(screen.getByDisplayValue('Diafragmática')).toBeInTheDocument()
  })

  it('crea una respiración al completar y enviar', async () => {
    mockHook.create.mockResolvedValue(true)
    render(<BreathingAdminPage />)
    await userEvent.click(screen.getByRole('button', { name: /Nueva respiración/ }))
    await userEvent.type(screen.getByLabelText('Nombre'), 'Cuadrada')
    await userEvent.click(screen.getByRole('button', { name: 'Crear' }))
    expect(mockHook.create).toHaveBeenCalledOnce()
  })

  it('edita una respiración al guardar', async () => {
    mockHook.techniques = [technique]
    mockHook.update.mockResolvedValue(true)
    render(<BreathingAdminPage />)
    await userEvent.click(screen.getAllByRole('button', { name: 'Editar Diafragmática' })[0])
    await userEvent.click(screen.getByRole('button', { name: 'Guardar' }))
    expect(mockHook.update).toHaveBeenCalledWith(1, expect.objectContaining({ name: 'Diafragmática' }))
  })

  it('desactiva con el botón Eliminar', async () => {
    mockHook.techniques = [technique]
    render(<BreathingAdminPage />)
    await userEvent.click(screen.getAllByRole('checkbox', { name: 'Cambiar estado activo de Diafragmática' })[0])
    expect(mockHook.setActive).toHaveBeenCalledWith(1, false)
  })

  it('restaura una respiración inactiva', async () => {
    mockHook.techniques = [{ ...technique, active: false }]
    render(<BreathingAdminPage />)
    await userEvent.click(screen.getAllByRole('checkbox', { name: 'Cambiar estado activo de Diafragmática' })[0])
    expect(mockHook.setActive).toHaveBeenCalledWith(1, true)
  })

  it('muestra el error en un toast', () => {
    mockHook.error = 'Ups'
    render(<BreathingAdminPage />)
    expect(screen.getByText('Ups')).toBeInTheDocument()
  })
})