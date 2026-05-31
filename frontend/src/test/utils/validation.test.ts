import { describe, it, expect } from 'vitest'
import { required, minLength, validEmail, matchesField, minAge, validate } from '../../utils/validation'

const emptyValues = { email: '', password: '' }

describe('required', () => {
    const rule = required()

    it('retorna error con string vacío', () => {
        expect(rule('', emptyValues)).toBe('Campo requerido')
    })

    it('retorna error con solo espacios', () => {
        expect(rule('   ', emptyValues)).toBe('Campo requerido')
    })

    it('pasa con valor válido', () => {
        expect(rule('hola', emptyValues)).toBeUndefined()
    })

    it('acepta mensaje personalizado', () => {
        const custom = required('Obligatorio')
        expect(custom('', emptyValues)).toBe('Obligatorio')
    })
})

describe('minLength', () => {
    const rule = minLength(6)

    it('retorna error si es más corto', () => {
        expect(rule('abc', emptyValues)).toBe('Mínimo 6 caracteres')
    })

    it('pasa con largo exacto', () => {
        expect(rule('abcdef', emptyValues)).toBeUndefined()
    })

    it('pasa con largo mayor', () => {
        expect(rule('abcdefgh', emptyValues)).toBeUndefined()
    })

    it('acepta mensaje personalizado', () => {
        const custom = minLength(3, 'Muy corto')
        expect(custom('ab', emptyValues)).toBe('Muy corto')
    })
})

describe('validEmail', () => {
    const rule = validEmail()

    it('retorna error con email inválido', () => {
        expect(rule('noesmail', emptyValues)).toBe('Email inválido')
    })

    it('retorna error sin dominio', () => {
        expect(rule('user@', emptyValues)).toBe('Email inválido')
    })

    it('retorna error sin arroba', () => {
        expect(rule('user.com', emptyValues)).toBe('Email inválido')
    })

    it('pasa con email válido', () => {
        expect(rule('user@mail.com', emptyValues)).toBeUndefined()
    })
})

describe('matchesField', () => {
    const rule = matchesField('password')

    it('retorna error si no coinciden', () => {
        const values = { password: '123456', confirmPassword: '654321' }
        expect(rule('654321', values)).toBe('Las contraseñas no coinciden')
    })

    it('pasa si coinciden', () => {
        const values = { password: '123456', confirmPassword: '123456' }
        expect(rule('123456', values)).toBeUndefined()
    })
})

describe('minAge', () => {
    const rule = minAge(13)

    it('retorna undefined con valor vacío (lo maneja required)', () => {
        expect(rule('', emptyValues)).toBeUndefined()
    })

    it('retorna error si no cumple la edad mínima', () => {
        const today = new Date()
        const tooYoung = new Date(today.getFullYear() - 10, today.getMonth(), today.getDate())
        const dateStr = tooYoung.toISOString().split('T')[0]
        expect(rule(dateStr, emptyValues)).toBe('Debés tener al menos 13 años')
    })

    it('pasa con edad exacta', () => {
        const today = new Date()
        const exact = new Date(today.getFullYear() - 13, today.getMonth(), today.getDate())
        const dateStr = exact.toISOString().split('T')[0]
        expect(rule(dateStr, emptyValues)).toBeUndefined()
    })

    it('pasa con edad mayor', () => {
        const today = new Date()
        const older = new Date(today.getFullYear() - 20, today.getMonth(), today.getDate())
        const dateStr = older.toISOString().split('T')[0]
        expect(rule(dateStr, emptyValues)).toBeUndefined()
    })

    it('retorna error si el cumpleaños aún no pasó este año', () => {
        const today = new Date()
        const almostAge = new Date(today.getFullYear() - 13, today.getMonth() + 1, today.getDate())
        const dateStr = almostAge.toISOString().split('T')[0]
        expect(rule(dateStr, emptyValues)).toBe('Debés tener al menos 13 años')
    })

    it('acepta mensaje personalizado', () => {
        const custom = minAge(18, 'Tenés que ser mayor de edad')
        const today = new Date()
        const minor = new Date(today.getFullYear() - 15, today.getMonth(), today.getDate())
        const dateStr = minor.toISOString().split('T')[0]
        expect(custom(dateStr, emptyValues)).toBe('Tenés que ser mayor de edad')
    })
})

describe('validate', () => {
    it('retorna el primer error encontrado', () => {
        const rules = [required(), minLength(6)]
        expect(validate('', rules, emptyValues)).toBe('Campo requerido')
    })

    it('retorna el segundo error si el primero pasa', () => {
        const rules = [required(), minLength(6)]
        expect(validate('abc', rules, emptyValues)).toBe('Mínimo 6 caracteres')
    })

    it('retorna undefined si todas las reglas pasan', () => {
        const rules = [required(), minLength(6)]
        expect(validate('abcdef', rules, emptyValues)).toBeUndefined()
    })
})