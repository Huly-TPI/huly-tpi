import { useEffect, useRef, useState, type FormEvent } from 'react'
import { CalendarDays } from 'lucide-react'
import { ApiError } from '../../api/apiError'
import { getAccountSettings, updateAccountSettings, type AccountSettings } from '../../api/auth'
import Button from '../Buttons/Button/Button'
import { InlineError } from '../feedback/InlineError'

interface AccountSettingsModalProps {
  onClose: () => void
  onSaved: () => Promise<unknown> | unknown
}

interface FormValues {
  name: string
  email: string
  birthDate: string
}

const INITIAL_VALUES: FormValues = {
  name: '',
  email: '',
  birthDate: '',
}

const MAX_NAME_LENGTH = 80

const toFormValues = (settings: AccountSettings): FormValues => ({
  name: settings.name ?? '',
  email: settings.email ?? '',
  birthDate: settings.birthDate ?? '',
})

const validate = (values: FormValues) => {
  const errors: Record<string, string | undefined> = {}
  const today = new Date().toISOString().slice(0, 10)

  if (!values.name.trim()) {
    errors.name = 'Ingresá tu nombre'
  } else if (values.name.trim().length > MAX_NAME_LENGTH) {
    errors.name = 'El nombre no puede superar los 80 caracteres'
  }

  if (values.birthDate && values.birthDate >= today) {
    errors.birthDate = 'La fecha de nacimiento debe ser anterior a hoy'
  }

  return errors
}

const getApiMessage = (error: unknown) => {
  if (error instanceof ApiError) {
    return error.message || 'No se pudieron guardar los cambios'
  }
  return 'No se pudieron guardar los cambios. Intentá de nuevo.'
}

export default function AccountSettingsModal({ onClose, onSaved }: AccountSettingsModalProps) {
  const [values, setValues] = useState<FormValues>(INITIAL_VALUES)
  const [errors, setErrors] = useState<Record<string, string | undefined>>({})
  const [loadError, setLoadError] = useState<string | null>(null)
  const [apiError, setApiError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const birthDateRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    let active = true

    const loadSettings = async () => {
      setLoading(true)
      setLoadError(null)
      try {
        const settings = await getAccountSettings()
        if (active) {
          setValues(toFormValues(settings))
        }
      } catch {
        if (active) {
          setLoadError('No se pudieron cargar tus datos. Intentá de nuevo.')
        }
      } finally {
        if (active) {
          setLoading(false)
        }
      }
    }

    void loadSettings()

    return () => {
      active = false
    }
  }, [])

  const handleChange = (field: keyof FormValues, value: string) => {
    setValues(prev => ({ ...prev, [field]: value }))
    setErrors(prev => ({ ...prev, [field]: undefined }))
    setApiError(null)
    setSuccessMessage(null)
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault()
    const validationErrors = validate(values)
    if (Object.values(validationErrors).some(Boolean)) {
      setErrors(validationErrors)
      return
    }

    setSaving(true)
    setApiError(null)
    setSuccessMessage(null)
    try {
      const updatedSettings = await updateAccountSettings({
        name: values.name.trim(),
        birthDate: values.birthDate || null,
      })
      setValues(toFormValues(updatedSettings))
      await onSaved()
      setSuccessMessage('Tus datos se guardaron correctamente.')
    } catch (error) {
      if (error instanceof ApiError && Object.keys(error.fieldErrors).length > 0) {
        setErrors(error.fieldErrors)
      }
      setApiError(getApiMessage(error))
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center overflow-y-auto bg-[var(--overlay-strong)] px-4 backdrop-blur-sm">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="account-settings-title"
        className="relative my-8 w-full max-w-md rounded-2xl bg-[var(--surface-primary)] p-8 text-[var(--text-primary)] shadow-xl"
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 text-xl font-bold text-[var(--text-secondary)] transition duration-150 hover:text-[var(--text-primary)]"
          aria-label="Cerrar modal"
        >
          &times;
        </button>

        <h2 id="account-settings-title" className="mb-3 text-center text-2xl font-bold text-[#8869AC] dark:text-violeta-claro">
          Configuración de cuenta
        </h2>

        {loading ? (
          <div className="rounded-xl bg-[var(--surface-secondary)] p-4 text-center text-sm font-semibold text-[var(--text-secondary)]">
            Cargando tus datos...
          </div>
        ) : loadError ? (
          <InlineError message={loadError} />
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="space-y-1.5">
              <label htmlFor="account-email" className="text-sm font-semibold text-[var(--text-primary)]">
                Correo electrónico
              </label>
              <input
                id="account-email"
                type="email"
                value={values.email}
                readOnly
                className="w-full rounded-xl border border-[var(--border-soft)] bg-[var(--surface-secondary)] px-3 py-2 text-sm text-[var(--text-secondary)]"
                placeholder="Correo electrónico"
              />
            </div>

            <div className="space-y-1.5">
              <label htmlFor="account-name" className="text-sm font-semibold text-[var(--text-primary)]">
                Nombre
              </label>
              <input
                id="account-name"
                type="text"
                value={values.name}
                onChange={event => handleChange('name', event.target.value)}
                aria-invalid={!!errors.name}
                aria-describedby={errors.name ? 'account-name-error' : undefined}
                className="w-full rounded-xl border border-[var(--border-soft)] bg-[var(--surface-primary)] px-3 py-2 text-sm text-[var(--text-primary)]"
                placeholder="Tu nombre"
                autoFocus
              />
              {errors.name && <p id="account-name-error" role="alert" className="text-xs font-semibold text-red-600 dark:text-red-400">{errors.name}</p>}
            </div>

            <div className="space-y-1.5">
              <label htmlFor="account-birth-date" className="text-sm font-semibold text-[var(--text-primary)]">
                Fecha de nacimiento
              </label>
              <div className="relative">
                <input
                  ref={birthDateRef}
                  id="account-birth-date"
                  type="date"
                  value={values.birthDate}
                  onChange={event => handleChange('birthDate', event.target.value)}
                  aria-invalid={!!errors.birthDate}
                  aria-describedby={errors.birthDate ? 'account-birth-date-error' : undefined}
                  className="w-full appearance-none rounded-xl border border-[var(--border-soft)] bg-[var(--surface-primary)] px-3 py-2 pr-10 text-sm text-[var(--text-primary)] [&::-webkit-calendar-picker-indicator]:hidden"
                />
                <button
                  type="button"
                  onClick={() => birthDateRef.current?.showPicker()}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-[var(--text-secondary)] hover:text-[var(--text-primary)] transition-colors"
                  aria-label="Abrir calendario"
                >
                  <CalendarDays className="size-5" strokeWidth={2} />
                </button>
              </div>
              {errors.birthDate && <p id="account-birth-date-error" role="alert" className="text-xs font-semibold text-red-600 dark:text-red-400">{errors.birthDate}</p>}
            </div>

            {apiError && <InlineError message={apiError} />}
            {successMessage && (
              <p className="text-center text-sm font-semibold text-[#4C7C64]">
                {successMessage}
              </p>
            )}

            <div className="flex flex-col gap-3 pt-2 sm:flex-row">
              <Button type="button" variant="secondary" fullWidth onClick={onClose} disabled={saving}>
                Cancelar
              </Button>
              <Button type="submit" variant="primary" fullWidth isLoading={saving} loadingLabel="Guardando...">
                Guardar cambios
              </Button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}
