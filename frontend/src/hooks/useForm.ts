import { useState, useCallback } from 'react'

export type ValidationRule = (value: string, allValues: Record<string, string>) => string | undefined

export const required = (message = 'Campo requerido'): ValidationRule =>
  (value) => {
    if (value.trim()) {
      return undefined
    }
    return message
  }

export const minLength = (min: number, message?: string): ValidationRule =>
  (value) => {
    if (value.length >= min) {
      return undefined
    }
    if (message) {
      return message
    }
    return `Mínimo ${min} caracteres`
  }

export const maxLength = (max: number, message?: string): ValidationRule =>
  (value) => {
    if (value.length <= max) {
      return undefined
    }
    if (message) {
      return message
    }
    return `Máximo ${max} caracteres`
  }

export const validEmail = (message = 'Email inválido'): ValidationRule =>
  (value) => {
    if (/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
      return undefined
    }
    return message
  }

export const matchesField = (
  fieldName: string,
  message = 'Las contraseñas no coinciden',
): ValidationRule =>
  (value, allValues) => {
    if (value === allValues[fieldName]) {
      return undefined
    }
    return message
  }

export const noHtml: ValidationRule =
  (value) => {
    if (/<\/?[a-z][\s\S]*>/i.test(value) || /javascript:/i.test(value)) {
      return 'El texto no puede contener HTML'
    }
    return undefined
  }

export const safeText: ValidationRule =
  (value) => {
    const xss = /<[a-z][\s\S]*>/i.test(value) || /javascript:/i.test(value) || /on\w+\s*=/i.test(value)
    const sql = /('\s*(or|and)\s*('|[0-9]))|(--)|(;\s*drop\s+table)|(union\s+select)/i.test(value)
    if (xss || sql) {
      return 'El texto contiene caracteres no permitidos'
    }
    return undefined
  }

export function validate(
  value: string,
  rules: ValidationRule[],
  allValues: Record<string, string>,
): string | undefined {
  for (const rule of rules) {
    const error = rule(value, allValues)
    if (error) {
      return error
    }
  }
  return undefined
}

export function sanitizeInput(value: string): string {
  return value
    .normalize('NFC')
    .replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, '')
}

export function sanitizeForSubmit(value: string): string {
  return sanitizeInput(value).trim()
}

type FieldRules = Record<string, ValidationRule[]>
type FieldErrors = Record<string, string | undefined>

export function useForm<T extends Record<string, string>>(
  initialValues: T,
  rules: FieldRules,
) {
  const [values, setValues] = useState<T>(initialValues)
  const [errors, setErrors] = useState<FieldErrors>({})

  const handleChange = useCallback((field: string, value: string) => {
    setValues((prev) => ({
      ...prev,
      [field]: sanitizeInput(value),
    }))
    setErrors((prev) => {
      if (!prev[field]) {
        return prev
      }
      const next = { ...prev }
      next[field] = undefined
      return next
    })
  }, [])

  const validateAll = useCallback((): boolean => {
    const newErrors: FieldErrors = {}
    let isValid = true

    for (const [field, fieldRules] of Object.entries(rules)) {
      const error = validate(values[field], fieldRules, values)
      if (error) {
        newErrors[field] = error
        isValid = false
      }
    }

    setErrors(newErrors)
    return isValid
  }, [values, rules])

  const getSanitizedValues = useCallback((): T => {
    const result: Record<string, string> = {}
    for (const [key, val] of Object.entries(values)) {
      result[key] = sanitizeForSubmit(val)
    }
    return result as T
  }, [values])

  const reset = useCallback(() => {
    setValues(initialValues)
    setErrors({})
  }, [initialValues])

  const setFieldErrors = useCallback((fieldErrors: Record<string, string>) => {
    setErrors((prev) => {
      const next = { ...prev }
      for (const [field, err] of Object.entries(fieldErrors)) {
        next[field] = err
      }
      return next
    })
  }, [])

  return { values, errors, handleChange, validateAll, getSanitizedValues, setFieldErrors, reset }
}
