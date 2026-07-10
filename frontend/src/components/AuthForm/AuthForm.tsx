import PasswordInput from './Passwordinput'
import DateInput from './Dateinput'
import { getInputClassName } from './authInputStyles'
import Button from '../Buttons/Button/Button'
import { InlineError } from '../feedback/InlineError'

export type FieldType = 'text' | 'email' | 'password' | 'date'

export interface AuthFormField {
  name: string
  type: FieldType
  placeholder: string
  showPasswordChecklist?: boolean
}

interface AuthFormProps {
  title: string
  titleClassName?: string
  subtitle?: string
  fields: AuthFormField[]
  values: Record<string, string>
  errors: Record<string, string | undefined>
  onChange: (field: string, value: string) => void
  onSubmit: () => void
  loading: boolean
  submitLabel: string
  loadingLabel: string
  apiError: string | null
  switchText?: string
  switchLabel?: string
  onSwitchMode?: () => void
  termsAccepted?: boolean
  onTermsChange?: (accepted: boolean) => void
  onForgotPassword?: () => void
  successMessage?: string | null
}

export default function AuthForm({
  title,
  titleClassName,
  subtitle,
  fields,
  values,
  errors,
  onChange,
  onSubmit,
  loading,
  submitLabel,
  loadingLabel,
  apiError,
  switchText,
  switchLabel,
  onSwitchMode,
  termsAccepted,
  onTermsChange,
  onForgotPassword,
  successMessage,
}: AuthFormProps) {
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit()
  }

  const renderField = (field: AuthFormField, index: number) => {
    const fieldError = errors[field.name]
    const fieldId = `field-${field.name}`
    const errorId = fieldError ? `error-${field.name}` : undefined
    const isFirst = index === 0
    const commonProps = {
      id: fieldId,
      placeholder: field.placeholder,
      value: values[field.name] ?? '',
      hasError: !!fieldError,
      errorId,
      autoFocus: isFirst,
    }

    switch (field.type) {
      case 'password':
        return (
          <PasswordInput
            {...commonProps}
            onChange={(value) => onChange(field.name, value)}
            showChecklist={field.showPasswordChecklist}
          />
        )
      case 'date':
        return (
          <DateInput
            {...commonProps}
            onChange={(value) => onChange(field.name, value)}
          />
        )
      default:
        return (
          <input
            id={fieldId}
            type={field.type}
            placeholder={field.placeholder}
            value={values[field.name] ?? ''}
            onChange={(e) => onChange(field.name, e.target.value)}
            autoFocus={isFirst}
            aria-invalid={!!fieldError}
            aria-describedby={errorId}
            className={getInputClassName(!!fieldError)}
          />
        )
    }
  }

  return (
    <div className="w-full font-nunito">
      <h2 className={`mt-8 mb-2 text-center text-2xl md:text-3xl font-bold ${titleClassName ?? 'text-[#4C7C64]'}`}>
        {title}
      </h2>

      {subtitle && (
        <p className="mb-5 text-center text-sm font-semibold italic text-[#6b5a45]">
          {subtitle}
        </p>
      )}

      <form onSubmit={handleSubmit} className="flex flex-col gap-3" noValidate>
        {fields.map((field, index) => (
          <div key={field.name} className="flex flex-col gap-1 min-w-0">
            <label htmlFor={`field-${field.name}`} className="sr-only">
              {field.placeholder}
            </label>
            {renderField(field, index)}
            {errors[field.name] && (
              <p
                id={`error-${field.name}`}
                role="alert"
                className="ml-2 max-sm:hidden flex items-center gap-1.5 text-xs font-semibold text-red-600 dark:text-red-400"
              >
                <span className="h-1.5 w-1.5 rounded-full bg-red-600 dark:bg-red-400 shrink-0" />
                {errors[field.name]}
              </p>
            )}
          </div>
        ))}

        {apiError && (
          <InlineError message={apiError} className="mt-1" />
        )}

        {successMessage && (
          <p className="mt-3 text-center text-sm font-semibold text-[#4C7C64]">
            {successMessage}
          </p>
        )}

        {onTermsChange !== undefined && (
          <label className="mt-1 flex items-center gap-2 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={termsAccepted ?? false}
              onChange={(e) => onTermsChange(e.target.checked)}
              className="size-4 rounded border-[#d4c4a8] accent-[#5a8a50]"
              style={{ colorScheme: 'light' }}
            />
            <span className="text-xs text-[#8c7b66]">
              Acepto los{' '}
              <a
                href="/privacy/terms"
                target="_blank"
                rel="noopener noreferrer"
                className="font-bold text-[#4C7C64] hover:underline"
              >
                términos y condiciones
              </a>
            </span>
          </label>
        )}

        <Button
          type="submit"
          variant="primary"
          fullWidth
          disabled={loading || (onTermsChange !== undefined && !termsAccepted)}
          isLoading={loading}
          loadingLabel={loadingLabel}
          className="mt-2"
        >
          {submitLabel}
        </Button>

        {onForgotPassword && (
          <button
            type="button"
            onClick={onForgotPassword}
            className="mt-1 w-full text-center text-sm text-[#8c7b66] hover:text-[#4C7C64] hover:underline focus-visible:outline-none focus-visible:underline underline-offset-4 transition-colors"
          >
            ¿Olvidaste tu contraseña?
          </button>
        )}
      </form>


      {switchText && switchLabel && onSwitchMode && (
        <p className="mt-4 mb-4 text-center text-sm text-[#8c7b66]">
          <span className="hidden sm:inline">{switchText} </span>
          <button
            type="button"
            onClick={onSwitchMode}
            className="font-semibold text-[#4C7C64] hover:underline focus-visible:outline-none focus-visible:underline"
          >
            {switchLabel}
          </button>
        </p>
      )}
    </div>
  )
}