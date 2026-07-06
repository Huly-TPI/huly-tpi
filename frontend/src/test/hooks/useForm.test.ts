import { describe, it, expect } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useForm } from '../../hooks/useForm'
import { required, minLength, matchesField } from '../../hooks/useForm'

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

describe('useForm', () => {
    it('inicializa con los valores dados', () => {
        setupHook()
        verifyValues(INITIAL_VALUES)
        verifyErrors({})
    })

    it('actualiza un campo con handleChange', () => {
        setupHook()
        callHandleChange('email', 'user@mail.com')
        verifyValue('email', 'user@mail.com')
    })

    it('limpia el error del campo al escribir', () => {
        setupHook()
        callValidateAll()
        verifyErrorDefined('email')
        callHandleChange('email', 'user@mail.com')
        verifyErrorUndefined('email')
    })

    it('validateAll retorna false con campos inválidos', () => {
        setupHook()
        verifyValidateAllResult(false)
        verifyErrorMessage('email', 'Campo requerido')
        verifyErrorMessage('password', 'Campo requerido')
    })

    it('validateAll retorna true con campos válidos', () => {
        setupHook()
        fillValidForm()
        verifyValidateAllResult(true)
        verifyErrors({})
    })

    it('detecta contraseñas que no coinciden', () => {
        setupHook()
        fillMismatchedPasswords()
        callValidateAll()
        verifyErrorMessage('confirmPassword', 'Las contraseñas no coinciden')
    })

    it('reset vuelve a los valores iniciales', () => {
        setupHook()
        callHandleChange('email', 'user@mail.com')
        callValidateAll()
        callReset()
        verifyValues(INITIAL_VALUES)
        verifyErrors({})
    })
    let rendered: ReturnType<typeof renderHook<ReturnType<typeof useForm>, undefined>>

    const setupHook = () => {
        rendered = renderHook(() => useForm(INITIAL_VALUES, RULES))
    }

    const callHandleChange = (field: keyof typeof INITIAL_VALUES, value: string) => {
        act(() => {
            rendered.result.current.handleChange(field, value)
        })
    }

    const callValidateAll = () => {
        let res = false
        act(() => {
            res = rendered.result.current.validateAll()
        })
        return res
    }

    const callReset = () => {
        act(() => {
            rendered.result.current.reset()
        })
    }

    const fillValidForm = () => {
        act(() => {
            rendered.result.current.handleChange('email', 'user@mail.com')
            rendered.result.current.handleChange('password', '123456')
            rendered.result.current.handleChange('confirmPassword', '123456')
        })
    }

    const fillMismatchedPasswords = () => {
        act(() => {
            rendered.result.current.handleChange('email', 'user@mail.com')
            rendered.result.current.handleChange('password', '123456')
            rendered.result.current.handleChange('confirmPassword', '654321')
        })
    }

    const verifyValues = (expected: typeof INITIAL_VALUES) => {
        expect(rendered.result.current.values).toEqual(expected)
    }

    const verifyValue = (field: keyof typeof INITIAL_VALUES, expected: string) => {
        expect(rendered.result.current.values[field]).toBe(expected)
    }

    const verifyErrors = (expected: any) => {
        expect(rendered.result.current.errors).toEqual(expected)
    }

    const verifyErrorDefined = (field: keyof typeof INITIAL_VALUES) => {
        expect(rendered.result.current.errors[field]).toBeDefined()
    }

    const verifyErrorUndefined = (field: keyof typeof INITIAL_VALUES) => {
        expect(rendered.result.current.errors[field]).toBeUndefined()
    }

    const verifyErrorMessage = (field: keyof typeof INITIAL_VALUES, expectedMsg: string) => {
        expect(rendered.result.current.errors[field]).toBe(expectedMsg)
    }

    const verifyValidateAllResult = (expected: boolean) => {
        let res = false
        act(() => {
            res = rendered.result.current.validateAll()
        })
        expect(res).toBe(expected)
    }
})
