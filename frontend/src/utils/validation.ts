type ValidationRule = (value: string, allValues: Record<string, string>) => string | undefined

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

export { required, minLength, validEmail, matchesField, minAge, validate }
export type { ValidationRule }