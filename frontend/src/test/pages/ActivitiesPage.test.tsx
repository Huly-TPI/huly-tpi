import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ActivitiesPage from '../../pages/Backoffice/ActivitiesPage'

const mockHook = {
  activeTab: 'metrics',
  setActiveTab: vi.fn(),
  timeframe: 'month',
  setTimeframe: vi.fn(),
  configs: [] as any[],
  kpis: null as any,
  popularity: [] as any[],
  correlation: [] as any[],
  impact: [] as any[],
  loading: false,
  editingConfig: null as any,
  setEditingConfig: vi.fn(),
  saveLoading: false,
  handleEditClick: vi.fn(),
  handleUpdateConfigSubmit: vi.fn(),
}

vi.mock('../../components/backoffice/activities/useAdminActivities', () => ({
  useAdminActivities: () => mockHook,
}))

vi.mock('../../components/backoffice/activities/ActivitiesMetricsTab', () => ({
  default: () => <div data-testid="metrics-tab">Metrics Tab Content</div>,
}))

vi.mock('../../components/backoffice/activities/ActivitiesConfigTab', () => ({
  default: () => <div data-testid="config-tab">Config Tab Content</div>,
}))

vi.mock('../../components/backoffice/ActivityConfigModal', () => ({
  default: () => <div data-testid="config-modal">Config Modal Content</div>,
}))

describe('ActivitiesPage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockHook.activeTab = 'metrics'
    mockHook.timeframe = 'month'
    mockHook.loading = false
    mockHook.editingConfig = null
  })

  it('renderiza la cabecera, pestañas y selector de periodo', () => {
    render(<ActivitiesPage />)
    expect(screen.getByText('Actividades')).toBeInTheDocument()
    expect(screen.getByText('Rendimiento y Métricas')).toBeInTheDocument()
    expect(screen.getByText('Configuración Dinámica')).toBeInTheDocument()
    expect(screen.getByRole('combobox')).toBeInTheDocument()
  })

  it('muestra estado cargando cuando loading es true', () => {
    mockHook.loading = true
    render(<ActivitiesPage />)
    expect(screen.getByText('Cargando datos...')).toBeInTheDocument()
  })

  it('renderiza la pestaña de métricas por defecto', () => {
    render(<ActivitiesPage />)
    expect(screen.getByTestId('metrics-tab')).toBeInTheDocument()
    expect(screen.queryByTestId('config-tab')).not.toBeInTheDocument()
  })

  it('renderiza la pestaña de configuración si activeTab es config', () => {
    mockHook.activeTab = 'config'
    render(<ActivitiesPage />)
    expect(screen.getByTestId('config-tab')).toBeInTheDocument()
    expect(screen.queryByTestId('metrics-tab')).not.toBeInTheDocument()
  })

  it('cambia de pestaña al hacer click en Configuración Dinámica', async () => {
    render(<ActivitiesPage />)
    const configBtn = screen.getByRole('button', { name: /Configuración Dinámica/ })
    await userEvent.click(configBtn)
    expect(mockHook.setActiveTab).toHaveBeenCalledWith('config')
  })

  it('cambia el periodo al seleccionar otra opcion en el combobox', async () => {
    render(<ActivitiesPage />)
    const select = screen.getByRole('combobox')
    await userEvent.selectOptions(select, 'today')
    expect(mockHook.setTimeframe).toHaveBeenCalledWith('today')
  })

  it('muestra el modal de edicion cuando editingConfig está seteado', () => {
    mockHook.editingConfig = { id: 1, title: 'Respiracion' }
    render(<ActivitiesPage />)
    expect(screen.getByTestId('config-modal')).toBeInTheDocument()
  })
})
