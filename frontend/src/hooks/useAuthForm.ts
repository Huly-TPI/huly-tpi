import { useState, useCallback } from 'react'
import type { ValidationRule } from '../utils/validation'
import { validate } from '../utils/validation'
import { sanitizeInput, sanitizeForSubmit } from '../utils/sanitize'

type FieldRules = Record<string, ValidationRule[]>
type FieldErrors = Record<string, string | undefined>

export function useAuthForm<T extends Record<string, string>>(
  initialValues: T,
  rules: FieldRules,
) {
  const [values, setValues] = useState<T>(initialValues)
  const [errors, setErrors] = useState<FieldErrors>({})

  const handleChange = useCallback((field: string, value: string) => {
    setValues((prev) => ({ ...prev, [field]: sanitizeInput(value) }))
    setErrors((prev) => {
      if (!prev[field]) return prev
      return { ...prev, [field]: undefined }
    })
  }, [])

  const validateAll = useCallback((): boolean => {
    const newErrors: FieldErrors = {}
    let isValid = true

    for (const [field, fieldRules] of Object.entries(rules)) {
      const error = validate(values[field] ?? '', fieldRules, values)
      if (error) {
        newErrors[field] = error
        isValid = false
      }
    }

    setErrors(newErrors)
    return isValid
  }, [values, rules])

  const getSanitizedValues = useCallback((): T => {
    return Object.fromEntries(
      Object.entries(values).map(([key, val]) => [key, sanitizeForSubmit(val)])
    ) as T
  }, [values])

  const reset = useCallback(() => {
    setValues(initialValues)
    setErrors({})
  }, [initialValues])

  const setFieldErrors = useCallback((fieldErrors: Record<string, string>) => {
    setErrors((prev) => ({ ...prev, ...fieldErrors }))
  }, [])

  return { values, errors, handleChange, validateAll, getSanitizedValues, setFieldErrors, reset }
}