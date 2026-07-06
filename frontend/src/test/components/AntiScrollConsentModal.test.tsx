import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import AntiScrollConsentModal from '../../components/AntiScrollConsentModal'
import * as extensionApi from '../../api/extension'
import { clickButton, verifyTextPresent, verifyTextNotPresent, clearAllMocks } from '../testHelpers'

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

  let openSpy: any
  let onCloseMock: any

  beforeEach(() => {
    clearAllMocks()
    resetDOMAttributes()
    setupChromeStub()
    setupWindowOpenMock()
    setupCloseMock()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('muestra el estado cargando inicialmente', () => {
    setupGetSettingsPromise()
    renderModal()
    verifyLoadingStateIsShown()
  })

  it('renderiza correctamente cuando no está instalada la extensión', () => {
    setupGetSettingsResolved(defaultSettings)
    renderModal()
    return waitForLoadingStateToDisappear().then(() => {
      verifyExtensionNotInstalledRendered()
    })
  })

  it('al hacer click en instalar, abre la Chrome Web Store', () => {
    setupGetSettingsResolved(defaultSettings)
    renderModal()
    return waitForLoadingStateToDisappear()
      .then(() => clickInstallButton())
      .then(() => verifyChromeWebStoreOpened())
  })

  it('renderiza con toggles cuando la extensión está instalada', () => {
    setupDOMAttributesForInstalled()
    setupGetSettingsResolved(defaultSettings)
    renderModal()
    return waitForLoadingStateToDisappear().then(() => {
      verifyExtensionInstalledRendered()
    })
  })

  it('llama a onClose al hacer click en Entendido', () => {
    setupGetSettingsResolved(defaultSettings)
    renderModalWithOnClose()
    return waitForLoadingStateToDisappear()
      .then(() => clickEntendidoButton())
      .then(() => verifyOnCloseCalled())
  })

  it('cambia el estado de los switches al hacer click y guarda/sincroniza', () => {
    setupDOMAttributesForInstalledWithoutConsent()
    setupGetSettingsResolved(defaultSettings)
    renderModal()
    return waitForLoadingStateToDisappear()
      .then(() => clickFirstCheckbox())
      .then(() => verifyFirstCheckboxCheckedAndSaved())
  })

  it('sincroniza el estado visual cuando la extensión actualiza atributos del DOM', () => {
    setupDOMAttributesForInstalledDisabled()
    setupGetSettingsResolved(defaultSettings)
    renderModal()
    return waitForLoadingStateToDisappear()
      .then(() => verifyCheckboxesAreNotChecked())
      .then(() => updateDOMAttributesForEnabled())
      .then(() => waitForCheckboxesToBeChecked())
  })

  /* helpers */

  const setupGetSettingsPromise = () => {
    mockedGetSettings.mockReturnValue(new Promise(() => {}))
  }

  const setupGetSettingsResolved = (settings: any) => {
    mockedGetSettings.mockResolvedValueOnce(settings)
  }

  const renderModal = () => {
    render(<AntiScrollConsentModal onClose={vi.fn()} />)
  }

  const renderModalWithOnClose = () => {
    render(<AntiScrollConsentModal onClose={onCloseMock} />)
  }

  const verifyLoadingStateIsShown = () => {
    verifyTextPresent('Cargando estado...')
  }

  const waitForLoadingStateToDisappear = () => {
    return waitFor(() => {
      verifyTextNotPresent('Cargando estado...')
    })
  }

  const verifyExtensionNotInstalledRendered = () => {
    verifyTextPresent('Pausa digital: Anti-Scroll')
    verifyTextPresent('Extension no instalada')
    expect(screen.getByRole('button', { name: 'Instalar' })).toBeInTheDocument()
  }

  const clickInstallButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Instalar')
  }

  const clickEntendidoButton = () => {
    const user = userEvent.setup()
    return clickButton(user, 'Entendido')
  }

  const verifyChromeWebStoreOpened = () => {
    expect(openSpy).toHaveBeenCalledWith(
      'https://chromewebstore.google.com/detail/opafcmdcpkhcfnbfmfipdninpkkpamgh?utm_source=item-share-cb',
      '_blank',
      'noopener,noreferrer',
    )
  }

  const setupDOMAttributesForInstalled = () => {
    document.documentElement.setAttribute('data-huly-antiscroll-installed', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-enabled', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-consent', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-id', 'ext-id-123')
  }

  const setupDOMAttributesForInstalledWithoutConsent = () => {
    document.documentElement.setAttribute('data-huly-antiscroll-installed', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-id', 'ext-id-123')
  }

  const setupDOMAttributesForInstalledDisabled = () => {
    document.documentElement.setAttribute('data-huly-antiscroll-installed', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-enabled', 'false')
    document.documentElement.setAttribute('data-huly-antiscroll-consent', 'false')
  }

  const updateDOMAttributesForEnabled = () => {
    document.documentElement.setAttribute('data-huly-antiscroll-enabled', 'true')
    document.documentElement.setAttribute('data-huly-antiscroll-consent', 'true')
  }

  const verifyExtensionInstalledRendered = () => {
    verifyTextPresent('Extension anti-scroll')
    verifyTextPresent('Activo y limitando el scroll infinito')
    expect(screen.queryByRole('button', { name: 'Instalar' })).not.toBeInTheDocument()
  }

  const verifyOnCloseCalled = () => {
    expect(onCloseMock).toHaveBeenCalledOnce()
  }

  const clickFirstCheckbox = () => {
    const user = userEvent.setup()
    const checkboxes = screen.getAllByRole('checkbox')
    expect(checkboxes).toHaveLength(2)
    expect(checkboxes[0]).not.toBeChecked()
    return user.click(checkboxes[0])
  }

  const verifyFirstCheckboxCheckedAndSaved = () => {
    const checkboxes = screen.getAllByRole('checkbox')
    expect(checkboxes[0]).toBeChecked()
    expect(mockedSaveSettings).toHaveBeenCalled()
    expect((window as any).chrome.runtime.sendMessage).toHaveBeenCalledWith(
      'ext-id-123',
      expect.objectContaining({ type: 'SET_ENABLED', enabled: true }),
    )
  }

  const verifyCheckboxesAreNotChecked = () => {
    const checkboxes = screen.getAllByRole('checkbox')
    expect(checkboxes[0]).not.toBeChecked()
    expect(checkboxes[1]).not.toBeChecked()
  }

  const waitForCheckboxesToBeChecked = () => {
    const checkboxes = screen.getAllByRole('checkbox')
    return waitFor(() => {
      expect(checkboxes[0]).toBeChecked()
      expect(checkboxes[1]).toBeChecked()
    })
  }

  const resetDOMAttributes = () => {
    document.documentElement.removeAttribute('data-huly-antiscroll-installed')
    document.documentElement.removeAttribute('data-huly-antiscroll-enabled')
    document.documentElement.removeAttribute('data-huly-antiscroll-consent')
    document.documentElement.removeAttribute('data-huly-antiscroll-id')
  }

  const setupChromeStub = () => {
    vi.stubGlobal('chrome', {
      runtime: {
        sendMessage: vi.fn(),
      },
    })
  }

  const setupWindowOpenMock = () => {
    openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)
  }

  const setupCloseMock = () => {
    onCloseMock = vi.fn()
  }
})
