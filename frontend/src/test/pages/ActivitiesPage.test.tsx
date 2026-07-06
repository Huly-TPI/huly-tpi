import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ActivitiesPage from '../../pages/Backoffice/ActivitiesPage'
import { verifyTextPresent, clearAllMocks } from '../testHelpers'

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
    clearAllMocks()
    mockHook.activeTab = 'metrics'
    mockHook.timeframe = 'month'
    mockHook.loading = false
    mockHook.editingConfig = null
  })

  it('renderiza la cabecera, pestañas y selector de periodo', () => {
    renderActivitiesPage()
    verifyHeaderTabsAndPeriodSelector()
  })

  it('muestra estado cargando cuando loading es true', () => {
    setupLoadingState(true)
    renderActivitiesPage()
    verifyLoadingStateVisible()
  })

  it('renderiza la pestaña de métricas por defecto', () => {
    renderActivitiesPage()
    verifyMetricsTabVisible()
    verifyConfigTabNotVisible()
  })

  it('renderiza la pestaña de configuración si activeTab es config', () => {
    setupActiveTab('config')
    renderActivitiesPage()
    verifyConfigTabVisible()
    verifyMetricsTabNotVisible()
  })

  it('cambia de pestaña al hacer click en Configuración Dinámica', () => {
    renderActivitiesPage()
    return clickConfigTabButton().then(() => {
      verifySetActiveTabCalledWith('config')
    })
  })

  it('cambia el periodo al seleccionar otra opcion en el combobox', () => {
    renderActivitiesPage()
    return selectTimeframeOption('today').then(() => {
      verifySetTimeframeCalledWith('today')
    })
  })

  it('muestra el modal de edicion cuando editingConfig está seteado', () => {
    setupEditingConfig({ id: 1, title: 'Respiracion' })
    renderActivitiesPage()
    verifyConfigModalVisible()
  })

  /* helpers */

  const renderActivitiesPage = () => {
    render(<ActivitiesPage />)
  }

  const setupLoadingState = (loading: boolean) => {
    mockHook.loading = loading
  }

  const setupActiveTab = (tab: string) => {
    mockHook.activeTab = tab
  }

  const setupEditingConfig = (config: any) => {
    mockHook.editingConfig = config
  }

  const verifyHeaderTabsAndPeriodSelector = () => {
    verifyTextPresent('Actividades')
    verifyTextPresent('Rendimiento y Métricas')
    verifyTextPresent('Configuración Dinámica')
    expect(screen.getByRole('combobox')).toBeInTheDocument()
  }

  const verifyLoadingStateVisible = () => {
    verifyTextPresent('Cargando datos...')
  }

  const verifyMetricsTabVisible = () => {
    expect(screen.getByTestId('metrics-tab')).toBeInTheDocument()
  }

  const verifyConfigTabNotVisible = () => {
    expect(screen.queryByTestId('config-tab')).not.toBeInTheDocument()
  }

  const verifyConfigTabVisible = () => {
    expect(screen.getByTestId('config-tab')).toBeInTheDocument()
  }

  const verifyMetricsTabNotVisible = () => {
    expect(screen.queryByTestId('metrics-tab')).not.toBeInTheDocument()
  }

  const clickConfigTabButton = () => {
    const configBtn = screen.getByRole('button', { name: 'Configuración Dinámica' })
    return userEvent.click(configBtn)
  }

  const verifySetActiveTabCalledWith = (tab: string) => {
    expect(mockHook.setActiveTab).toHaveBeenCalledWith(tab)
  }

  const selectTimeframeOption = (option: string) => {
    const select = screen.getByRole('combobox')
    return userEvent.selectOptions(select, option)
  }

  const verifySetTimeframeCalledWith = (timeframe: string) => {
    expect(mockHook.setTimeframe).toHaveBeenCalledWith(timeframe)
  }

  const verifyConfigModalVisible = () => {
    expect(screen.getByTestId('config-modal')).toBeInTheDocument()
  }
})
