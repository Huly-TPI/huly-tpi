type ValidationRule = (value: string, allValues: Record<string, string>) => string | undefined

const REGISTER_NAME_PATTERN = /^(?=(?:.*\p{L}){3,})[\p{L}]+(?:\s+[\p{L}]+)*$/u

const required = (message = 'Campo requerido'): ValidationRule =>
  (value) => (value.trim() ? undefined : message)

const minLength = (min: number, message?: string): ValidationRule =>
  (value) =>
    value.length >= min ? undefined : (message ?? `Mínimo ${min} caracteres`)

const validEmail = (message = 'Email inválido'): ValidationRule =>
  (value) =>
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ? undefined : message

const matchesField = (
  fieldName: string,
  message = 'Las contraseñas no coinciden',
): ValidationRule =>
  (value, allValues) =>
    value === allValues[fieldName] ? undefined : message

const minAge = (min: number, message?: string): ValidationRule =>
  (value) => {
    if (!value) return undefined
    const birth = new Date(value)
    const today = new Date()
    let age = today.getFullYear() - birth.getFullYear()
    const monthDiff = today.getMonth() - birth.getMonth()
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
      age -= 1
    }
    return age >= min ? undefined : (message ?? `Debés tener al menos ${min} años`)
  }

const maxLength = (max: number, message?: string): ValidationRule =>
  (value) =>
    value.length <= max ? undefined : (message ?? `Máximo ${max} caracteres`)

const noHtml: ValidationRule =
  (value) =>
    /<\/?[a-z][\s\S]*>/i.test(value) || /javascript:/i.test(value)
      ? 'El texto no puede contener HTML'
      : undefined

const safeText: ValidationRule =
  (value) => {
    const xss = /<[a-z][\s\S]*>/i.test(value) || /javascript:/i.test(value) || /on\w+\s*=/i.test(value)
    const sql = /('\s*(or|and)\s*('|[0-9]))|(--)|(;\s*drop\s+table)|(union\s+select)/i.test(value)
    return xss || sql ? 'El texto contiene caracteres no permitidos' : undefined
  }

const validRegisterName = (
  message = 'El nombre debe tener al menos 3 letras. Solo puede contener letras y espacios',
): ValidationRule =>
  (value) => {
    const normalizedValue = value.trim()
    return REGISTER_NAME_PATTERN.test(normalizedValue) ? undefined : message
  }

function validate(
  value: string,
  rules: ValidationRule[],
  allValues: Record<string, string>,
): string | undefined {
  for (const rule of rules) {
    const error = rule(value, allValues)
    if (error) return error
  }
  return undefined
}

export { required, minLength, maxLength, validEmail, matchesField, minAge, noHtml, safeText, validRegisterName, validate }
export type { ValidationRule }
