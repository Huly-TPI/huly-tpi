import { describe, it, expect } from 'vitest'
import { sanitizeInput, sanitizeForSubmit } from '../../hooks/useForm'

describe('sanitizeInput', () => {
    it('normaliza Unicode a NFC', () => {
        const nfd = '\u0041\u0301'
        const nfc = '\u00C1'
        expect(sanitizeInput(nfd)).toBe(nfc)
    })

    it('elimina caracteres de control', () => {
        expect(sanitizeInput('hola\x00mundo')).toBe('holamundo')
        expect(sanitizeInput('hola\x1Fmundo')).toBe('holamundo')
        expect(sanitizeInput('hola\x7Fmundo')).toBe('holamundo')
    })

    it('preserva saltos de línea y tabs', () => {
        expect(sanitizeInput('hola\nmundo')).toBe('hola\nmundo')
        expect(sanitizeInput('hola\tmundo')).toBe('hola\tmundo')
    })

    it('preserva emojis', () => {
        expect(sanitizeInput('hola 🌱')).toBe('hola 🌱')
    })

    it('preserva caracteres especiales válidos', () => {
        expect(sanitizeInput("O'Brien")).toBe("O'Brien")
        expect(sanitizeInput('José María')).toBe('José María')
    })

    it('no modifica texto limpio', () => {
        expect(sanitizeInput('texto normal')).toBe('texto normal')
    })
})

describe('sanitizeForSubmit', () => {
    it('hace trim del texto', () => {
        expect(sanitizeForSubmit('  hola  ')).toBe('hola')
    })

    it('aplica sanitizeInput y trim juntos', () => {
        expect(sanitizeForSubmit('  hola\x00  ')).toBe('hola')
    })

    it('no modifica texto ya limpio', () => {
        expect(sanitizeForSubmit('texto normal')).toBe('texto normal')
    })
})
