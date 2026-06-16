import { useEffect, useState } from 'react'
import Button from './Buttons/Button/Button'
import colorLogo from '../assets/brand/color-logo.webp'
import { getExtensionSettings, saveExtensionSettings, ExtensionSettings } from '../api/extension'

const CHROME_WEB_STORE_URL =
  'https://chromewebstore.google.com/detail/opafcmdcpkhcfnbfmfipdninpkkpamgh?utm_source=item-share-cb'

interface ToggleSwitchProps {
  checked: boolean
  onChange: () => void
}

function ToggleSwitch({ checked, onChange }: ToggleSwitchProps) {
  return (
    <label className="relative inline-flex items-center cursor-pointer shrink-0">
      <input
        type="checkbox"
        className="sr-only peer"
        checked={checked}
        onChange={onChange}
      />
      <div className="w-11 h-6 bg-gray-300 dark:bg-gray-600 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 dark:after:border-gray-600 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-[#8869AC]"></div>
    </label>
  )
}

interface AntiScrollConsentModalProps {
  onClose: () => void
}

export default function AntiScrollConsentModal({ onClose }: AntiScrollConsentModalProps) {
  const [settings, setSettings] = useState<ExtensionSettings | null>(null)
  const [loading, setLoading] = useState(true)
  const [isInstalled, setIsInstalled] = useState(false)
  const [isEnabled, setIsEnabled] = useState(false)
  const [consent, setConsent] = useState(false)
  const [extId, setExtId] = useState<string | null>(null)

  useEffect(() => {
    const checkInstalled = () => {
      const installed = document.documentElement.hasAttribute('data-huly-antiscroll-installed')
      setIsInstalled(installed)
      if (installed) {
        const id = document.documentElement.getAttribute('data-huly-antiscroll-id')
        setExtId(id)

        const enabledAttr = document.documentElement.getAttribute('data-huly-antiscroll-enabled')
        setIsEnabled(enabledAttr === 'true')

        const consentAttr = document.documentElement.getAttribute('data-huly-antiscroll-consent')
        setConsent(consentAttr === 'true')
      }
    }

    checkInstalled()
    const interval = setInterval(checkInstalled, 500)

    getExtensionSettings()
      .then((data) => {
        setSettings(data)
        if (!document.documentElement.hasAttribute('data-huly-antiscroll-installed')) {
          setIsEnabled(data.enabled)
          setConsent(data.dataSharingConsent)
        }
      })
      .catch((err) => {
        console.error('Error fetching extension settings:', err)
      })
      .finally(() => {
        setLoading(false)
      })

    return () => clearInterval(interval)
  }, [])

  const handleToggleEnabled = async () => {
    const newEnabled = !isEnabled
    setIsEnabled(newEnabled)

    if (settings) {
      const updated = { ...settings, enabled: newEnabled }
      setSettings(updated)
      try {
        await saveExtensionSettings(updated)
      } catch (e) {
        console.error('Error saving settings to backend:', e)
      }
    }

    const chromeObj = (window as any).chrome
    if (isInstalled && extId && chromeObj && chromeObj.runtime) {
      try {
        chromeObj.runtime.sendMessage(extId, { type: 'SET_ENABLED', enabled: newEnabled })
      } catch (e) {
        console.error('Error communicating with extension:', e)
      }
    }
  }

  const handleToggleConsent = async () => {
    const newConsent = !consent
    setConsent(newConsent)

    if (settings) {
      const updated = { ...settings, dataSharingConsent: newConsent }
      setSettings(updated)
      try {
        await saveExtensionSettings(updated)
      } catch (e) {
        console.error('Error saving consent to backend:', e)
      }
    }

    const chromeObj = (window as any).chrome
    if (isInstalled && extId && chromeObj && chromeObj.runtime) {
      try {
        chromeObj.runtime.sendMessage(extId, { type: 'SET_CONSENT', consent: newConsent })
      } catch (e) {
        console.error('Error communicating with extension:', e)
      }
    }
  }

  const handleInstall = () => {
    window.open(CHROME_WEB_STORE_URL, '_blank', 'noopener,noreferrer')
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-[var(--overlay-strong)] backdrop-blur-sm px-4 overflow-y-auto">
      <div className="bg-[var(--surface-primary)] text-[var(--text-primary)] rounded-2xl shadow-xl p-8 max-w-md w-full text-center relative my-8">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-[var(--text-secondary)] hover:text-[var(--text-primary)] text-xl font-bold transition duration-150"
          aria-label="Cerrar modal"
        >
          &times;
        </button>

        <img src={colorLogo} alt="Huly" className="h-12 object-contain mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-[#8869AC] dark:text-violeta-claro mb-4">
          Pausa digital: Anti-Scroll
        </h2>

        <div className="text-[var(--text-secondary)] text-sm leading-relaxed mb-6 text-left whitespace-pre-line">
          {settings?.termsAndConditions || (
            `El modo anti-scroll es simplemente una herramienta para acompanarte cuando sientas que necesitas frenar un poco. No hay reglas estrictas ni metas que cumplir.

Activalo cuando quieras priorizar tu concentracion o desconectar del ruido, y apagalo cuando tengas ganas de explorar libremente. Cero presiones, el ritmo lo marcas vos.`
          )}
        </div>

        {loading ? (
          <div className="text-sm text-[var(--text-secondary)] py-4">Cargando estado...</div>
        ) : (
          <div className="space-y-4 border-t border-b border-[var(--border-soft)] py-4 mb-6">
            <div className="flex items-center justify-between p-3 bg-[var(--surface-secondary)] rounded-xl gap-4">
              <div className="text-left">
                <div className="font-semibold text-sm text-[var(--text-primary)]">
                  {isInstalled ? 'Extension anti-scroll' : 'Extension no instalada'}
                </div>
                <div className="text-xs text-[var(--text-secondary)] mt-0.5">
                  {isInstalled
                    ? isEnabled
                      ? 'Activo y limitando el scroll infinito'
                      : 'Desactivado temporalmente'
                    : 'Instala la extension desde Chrome Web Store'}
                </div>
              </div>

              {isInstalled ? (
                <ToggleSwitch checked={isEnabled} onChange={handleToggleEnabled} />
              ) : (
                <Button variant="primary" size="sm" onClick={handleInstall}>
                  Instalar
                </Button>
              )}
            </div>

            <div className="flex items-center justify-between p-3 bg-[var(--surface-secondary)] rounded-xl gap-4">
              <div className="text-left">
                <div className="font-semibold text-sm text-[var(--text-primary)]">
                  Recopilar estadisticas de bienestar
                </div>
                <div className="text-xs text-[var(--text-secondary)] mt-0.5 leading-normal">
                  Acepto que mis datos de uso (tiempo de scroll en redes) sean recopilados de
                  forma segura e impersonal mientras mi sesion en Huly este abierta.
                </div>
                <div className="text-left text-xs text-bosque dark:text-menta font-semibold mt-1">
                  * Si cierras sesion o desmarcas esta opcion, tus datos no seran recopilados.
                </div>
              </div>
              <ToggleSwitch checked={consent} onChange={handleToggleConsent} />
            </div>
          </div>
        )}

        <Button variant="primary" fullWidth onClick={onClose}>
          Entendido
        </Button>
      </div>
    </div>
  )
}
