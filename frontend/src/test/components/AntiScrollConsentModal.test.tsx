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
    pauseIntervalSeconds: 20,
    gardenUrl: 'http://localhost:5173/',
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

  it('renderiza correctamente cuando no esta instalada la extension', async () => {
    mockedGetSettings.mockResolvedValueOnce(defaultSettings)

    render(<AntiScrollConsentModal onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.queryByText('Cargando estado...')).not.toBeInTheDocument()
    })

    expect(screen.getByText(/pausa digital: anti-scroll/i)).toBeInTheDocument()
    expect(screen.getByText('Extension no instalada')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /instalar/i })).toBeInTheDocument()
  })

  it('al hacer click en instalar, abre la Chrome Web Store', async () => {
    const user = userEvent.setup()
    mockedGetSettings.mockResolvedValueOnce(defaultSettings)
    const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)

    render(<AntiScrollConsentModal onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.queryByText('Cargando estado...')).not.toBeInTheDocument()
    })

    await user.click(screen.getByRole('button', { name: /instalar/i }))

    expect(openSpy).toHaveBeenCalledWith(
      'https://chromewebstore.google.com/detail/opafcmdcpkhcfnbfmfipdninpkkpamgh?utm_source=item-share-cb',
      '_blank',
      'noopener,noreferrer',
    )
  })

  it('renderiza con toggles cuando la extension esta instalada', async () => {
    document.documentElement.setAttribute('data-huly-antiscroll-installed', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-enabled', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-consent', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-id', 'ext-id-123')

    mockedGetSettings.mockResolvedValueOnce(defaultSettings)

    render(<AntiScrollConsentModal onClose={vi.fn()} />)

    await waitFor(() => {
      expect(screen.queryByText('Cargando estado...')).not.toBeInTheDocument()
    })

    expect(screen.getByText('Extension anti-scroll')).toBeInTheDocument()
    expect(screen.getByText('Activo y limitando el scroll infinito')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /instalar/i })).not.toBeInTheDocument()
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
    expect((window as any).chrome.runtime.sendMessage).toHaveBeenCalledWith(
      'ext-id-123',
      expect.objectContaining({ type: 'SET_ENABLED', enabled: true }),
    )
  })
})
