import { describe, it, expect } from 'vitest'
import { sanitizeInput, sanitizeForSubmit } from '../../hooks/useForm'

describe('sanitizeInput', () => {
    it('normaliza Unicode a NFC', () => {
        verifySanitizeInput(getNfdString(), getNfcString())
    })

    it('elimina caracteres de control', () => {
        verifySanitizeInput('hola\x00mundo', 'holamundo')
        verifySanitizeInput('hola\x1Fmundo', 'holamundo')
        verifySanitizeInput('hola\x7Fmundo', 'holamundo')
    })

    it('preserva saltos de línea y tabs', () => {
        verifySanitizeInput('hola\nmundo', 'hola\nmundo')
        verifySanitizeInput('hola\tmundo', 'hola\tmundo')
    })

    it('preserva emojis', () => {
        verifySanitizeInput('hola 🌱', 'hola 🌱')
    })

    it('preserva caracteres especiales válidos', () => {
        verifySanitizeInput("O'Brien", "O'Brien")
        verifySanitizeInput('José María', 'José María')
    })

    it('no modifica texto limpio', () => {
        verifySanitizeInput('texto normal', 'texto normal')
    })
    const getNfdString = () => '\u0041\u0301'
    const getNfcString = () => '\u00C1'
    const verifySanitizeInput = (input: string, expected: string) => {
        expect(sanitizeInput(input)).toBe(expected)
    }
})

describe('sanitizeForSubmit', () => {
    it('hace trim del texto', () => {
        verifySanitizeForSubmit('  hola  ', 'hola')
    })

    it('aplica sanitizeInput y trim juntos', () => {
        verifySanitizeForSubmit('  hola\x00  ', 'hola')
    })

    it('no modifica texto ya limpio', () => {
        verifySanitizeForSubmit('texto normal', 'texto normal')
    })
    const verifySanitizeForSubmit = (input: string, expected: string) => {
        expect(sanitizeForSubmit(input)).toBe(expected)
    }
})
