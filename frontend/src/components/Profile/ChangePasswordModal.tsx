import { useState } from 'react'
import AuthForm from '../AuthForm/AuthForm'
import { changePassword } from '../../api/auth'
import { ApiError } from '../../api/apiError'

interface ChangePasswordModalProps {
  onClose: () => void
}

const INITIAL_VALUES = { currentPassword: '', newPassword: '' }

function validate(values: typeof INITIAL_VALUES) {
  const errors: Record<string, string | undefined> = {}
  if (!values.currentPassword) errors.currentPassword = 'Ingresá tu contraseña actual'
  if (!values.newPassword) errors.newPassword = 'Ingresá la nueva contraseña'
  else if (values.newPassword.length < 6) errors.newPassword = 'La contraseña debe tener al menos 6 caracteres'
  return errors
}

export default function ChangePasswordModal({ onClose }: ChangePasswordModalProps) {
  const [values, setValues] = useState(INITIAL_VALUES)
  const [errors, setErrors] = useState<Record<string, string | undefined>>({})
  const [loading, setLoading] = useState(false)
  const [apiError, setApiError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)

  const handleChange = (field: string, value: string) => {
    setValues(prev => ({ ...prev, [field]: value }))
    setErrors(prev => ({ ...prev, [field]: undefined }))
    setApiError(null)
  }

  const handleSubmit = async () => {
    const validationErrors = validate(values)
    if (Object.values(validationErrors).some(Boolean)) {
      setErrors(validationErrors)
      return
    }

    setLoading(true)
    setApiError(null)
    try {
      await changePassword(values.currentPassword, values.newPassword)
      setSuccessMessage('¡Contraseña actualizada con éxito!')
      setValues(INITIAL_VALUES)
    } catch (err: unknown) {
      const status = err instanceof ApiError ? err.status : undefined
      if (status === 401 || status === 400) {
        setApiError('La contraseña actual es incorrecta.')
      } else {
        setApiError('Ocurrió un error. Intentá de nuevo.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center overflow-y-auto bg-[var(--overlay-strong)] px-4 backdrop-blur-sm">
      <div
        role="dialog"
        aria-modal="true"
        aria-label="Cambiar contraseña"
        className="relative my-8 w-full max-w-md rounded-2xl bg-[var(--surface-primary)] p-8 shadow-xl"
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 text-xl font-bold text-[var(--text-secondary)] transition duration-150 hover:text-[var(--text-primary)]"
          aria-label="Cerrar modal"
        >
          &times;
        </button>

        <AuthForm
          title="Cambiar contraseña"
          titleClassName="text-[#8869AC] dark:text-violeta-claro"
          subtitle="Ingresá tu contraseña actual y elegí una nueva."
          fields={[
            { name: 'currentPassword', type: 'password', placeholder: 'Contraseña actual' },
            { name: 'newPassword', type: 'password', placeholder: 'Nueva contraseña', showPasswordChecklist: true },
          ]}
          values={values}
          errors={errors}
          onChange={handleChange}
          onSubmit={handleSubmit}
          loading={loading}
          submitLabel="Guardar"
          loadingLabel="Guardando..."
          apiError={apiError}
          successMessage={successMessage}
        />
      </div>
    </div>
  )
}
