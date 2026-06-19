import { describe, it, expect } from 'vitest'
import { getGreeting } from '../../hooks/useGreeting'

describe('getGreeting', () => {
  it('devuelve "Buenos días" entre las 6 y las 11', () => {
    expect(getGreeting(6)).toBe('Buenos días')
    expect(getGreeting(9)).toBe('Buenos días')
    expect(getGreeting(11)).toBe('Buenos días')
  })

  it('devuelve "Buenas tardes" entre las 12 y las 18', () => {
    expect(getGreeting(12)).toBe('Buenas tardes')
    expect(getGreeting(15)).toBe('Buenas tardes')
    expect(getGreeting(18)).toBe('Buenas tardes')
  })

  it('devuelve "Buenas noches" en la madrugada o de noche', () => {
    expect(getGreeting(0)).toBe('Buenas noches')
    expect(getGreeting(3)).toBe('Buenas noches')
    expect(getGreeting(20)).toBe('Buenas noches')
    expect(getGreeting(23)).toBe('Buenas noches')
  })
})
