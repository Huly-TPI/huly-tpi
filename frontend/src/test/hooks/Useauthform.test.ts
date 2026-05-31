import { describe, it, expect } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useAuthForm } from '../../hooks/useAuthForm'
import { required, minLength, matchesField } from '../../utils/validation'

const INITIAL_VALUES = {
    email: '',
    password: '',
    confirmPassword: '',
}

const RULES = {
    email: [required()],
    password: [required(), minLength(6)],
    confirmPassword: [required(), matchesField('password')],
}

describe('useAuthForm', () => {
    it('inicializa con los valores dados', () => {
        const { result } = renderHook(() => useAuthForm(INITIAL_VALUES, RULES))

        expect(result.current.values).toEqual(INITIAL_VALUES)
        expect(result.current.errors).toEqual({})
    })

    it('actualiza un campo con handleChange', () => {
        const { result } = renderHook(() => useAuthForm(INITIAL_VALUES, RULES))

        act(() => {
            result.current.handleChange('email', 'user@mail.com')
        })

        expect(result.current.values.email).toBe('user@mail.com')
    })

    it('limpia el error del campo al escribir', () => {
        const { result } = renderHook(() => useAuthForm(INITIAL_VALUES, RULES))

        act(() => {
            result.current.validateAll()
        })

        expect(result.current.errors.email).toBeDefined()

        act(() => {
            result.current.handleChange('email', 'user@mail.com')
        })

        expect(result.current.errors.email).toBeUndefined()
    })

    it('validateAll retorna false con campos inválidos', () => {
        const { result } = renderHook(() => useAuthForm(INITIAL_VALUES, RULES))

        let isValid = false
        act(() => {
            isValid = result.current.validateAll()
        })

        expect(isValid).toBe(false)
        expect(result.current.errors.email).toBe('Campo requerido')
        expect(result.current.errors.password).toBe('Campo requerido')
    })

    it('validateAll retorna true con campos válidos', () => {
        const { result } = renderHook(() => useAuthForm(INITIAL_VALUES, RULES))

        act(() => {
            result.current.handleChange('email', 'user@mail.com')
            result.current.handleChange('password', '123456')
            result.current.handleChange('confirmPassword', '123456')
        })

        let isValid = false
        act(() => {
            isValid = result.current.validateAll()
        })

        expect(isValid).toBe(true)
        expect(result.current.errors).toEqual({})
    })

    it('detecta contraseñas que no coinciden', () => {
        const { result } = renderHook(() => useAuthForm(INITIAL_VALUES, RULES))

        act(() => {
            result.current.handleChange('email', 'user@mail.com')
            result.current.handleChange('password', '123456')
            result.current.handleChange('confirmPassword', '654321')
        })

        act(() => {
            result.current.validateAll()
        })

        expect(result.current.errors.confirmPassword).toBe('Las contraseñas no coinciden')
    })

    it('reset vuelve a los valores iniciales', () => {
        const { result } = renderHook(() => useAuthForm(INITIAL_VALUES, RULES))

        act(() => {
            result.current.handleChange('email', 'user@mail.com')
            result.current.validateAll()
        })

        act(() => {
            result.current.reset()
        })

        expect(result.current.values).toEqual(INITIAL_VALUES)
        expect(result.current.errors).toEqual({})
    })
})