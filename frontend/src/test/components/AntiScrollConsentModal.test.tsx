import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AntiScrollConsentModal from '../../components/AntiScrollConsentModal'
import * as extensionApi from '../../api/extension'

vi.mock('../../api/extension', () => ({
  getExtensionSettings: vi.fn(),
  saveExtensionSettings: vi.fn(),
}))

const mockedGetSettings = vi.mocked(extensionApi.getExtensionSettings)
const mockedSaveSettings = vi.mocked(extensionApi.saveExtensionSettings)

describe('AntiScrollConsentModal', () => {
  const defaultSettings = {
    enabled: true,
    pauseIntervalMinutes: 20,
    gardenUrl: 'http://localhost:5173/garden',
    backendUrl: 'http://localhost:8080',
    monitoredDomains: ['twitter.com'],
    dataSharingConsent: false,
  }

  beforeEach(() => {
    vi.clearAllMocks()
    document.documentElement.removeAttribute('data-huly-antiscroll-installed')
    document.documentElement.removeAttribute('data-huly-antiscroll-enabled')
    document.documentElement.removeAttribute('data-huly-antiscroll-consent')
    document.documentElement.removeAttribute('data-huly-antiscroll-id')
    vi.stubGlobal('chrome', {
      runtime: {
        sendMessage: vi.fn(),
      },
    })
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('muestra el estado cargando inicialmente', () => {
    mockedGetSettings.mockReturnValue(new Promise(() => {}))
    render(<AntiScrollConsentModal onClose={vi.fn()} />)
    expect(screen.getByText('Cargando estado...')).toBeInTheDocument()
  })

  it('renderiza correctamente cuando no está instalada la extensión', async () => {
    mockedGetSettings.mockResolvedValueOnce(defaultSettings)

    render(<AntiScrollConsentModal onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.queryByText('Cargando estado...')).not.toBeInTheDocument()
    })

    expect(screen.getByText(/pausa digital: anti-scroll/i)).toBeInTheDocument()
    expect(screen.getByText('Extensión no instalada')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /descargar/i })).toBeInTheDocument()
  })

  it('al hacer click en descargar, se descarga el zip y se muestran instrucciones', async () => {
    const user = userEvent.setup()
    mockedGetSettings.mockResolvedValueOnce(defaultSettings)

    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})

    render(<AntiScrollConsentModal onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.queryByText('Cargando estado...')).not.toBeInTheDocument()
    })

    const downloadBtn = screen.getByRole('button', { name: /descargar/i })
    await user.click(downloadBtn)

    expect(clickSpy).toHaveBeenCalled()
    expect(screen.getByText('¿Cómo instalar la extensión?')).toBeInTheDocument()
  })

  it('renderiza con toggles cuando la extensión está instalada', async () => {
    document.documentElement.setAttribute('data-huly-antiscroll-installed', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-enabled', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-consent', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-id', 'ext-id-123')

    mockedGetSettings.mockResolvedValueOnce(defaultSettings)

    render(<AntiScrollConsentModal onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.queryByText('Cargando estado...')).not.toBeInTheDocument()
    })

    expect(screen.getByText('Extensión anti-scroll')).toBeInTheDocument()
    expect(screen.getByText('Activo y limitando el scroll infinito')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /descargar/i })).not.toBeInTheDocument()
  })

  it('llama a onClose al hacer click en Entendido', async () => {
    const user = userEvent.setup()
    const onClose = vi.fn()
    mockedGetSettings.mockResolvedValueOnce(defaultSettings)

    render(<AntiScrollConsentModal onClose={onClose} />)

    await waitFor(() => {
      expect(screen.queryByText('Cargando estado...')).not.toBeInTheDocument()
    })

    await user.click(screen.getByRole('button', { name: /entendido/i }))

    expect(onClose).toHaveBeenCalledOnce()
  })

  it('cambia el estado de los switches al hacer click y guarda/sincroniza', async () => {
    document.documentElement.setAttribute('data-huly-antiscroll-installed', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-id', 'ext-id-123')
    
    const user = userEvent.setup()
    mockedGetSettings.mockResolvedValueOnce(defaultSettings)

    render(<AntiScrollConsentModal onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.queryByText('Cargando estado...')).not.toBeInTheDocument()
    })

    const checkboxes = screen.getAllByRole('checkbox')
    expect(checkboxes).toHaveLength(2)

    expect(checkboxes[0]).not.toBeChecked()

    await user.click(checkboxes[0])

    expect(checkboxes[0]).toBeChecked()
    expect(mockedSaveSettings).toHaveBeenCalled()
    expect(window.chrome.runtime.sendMessage).toHaveBeenCalledWith(
      'ext-id-123',
      expect.objectContaining({ type: 'SET_ENABLED', enabled: true })
    )
  })
})
