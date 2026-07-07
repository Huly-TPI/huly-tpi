import { describe, it, expect } from 'vitest'
import { getGreeting } from '../../hooks/useGreeting'

describe('getGreeting', () => {
  it('devuelve "Buenos días" entre las 6 y las 11', () => {
    verifyGreeting(6, 'Buenos días')
    verifyGreeting(9, 'Buenos días')
    verifyGreeting(11, 'Buenos días')
  })

  it('devuelve "Buenas tardes" entre las 12 y las 18', () => {
    verifyGreeting(12, 'Buenas tardes')
    verifyGreeting(15, 'Buenas tardes')
    verifyGreeting(18, 'Buenas tardes')
  })

  it('devuelve "Buenas noches" en la madrugada o de noche', () => {
    verifyGreeting(0, 'Buenas noches')
    verifyGreeting(3, 'Buenas noches')
    verifyGreeting(20, 'Buenas noches')
    verifyGreeting(23, 'Buenas noches')
  })

  /* helpers */

  const verifyGreeting = (hour: number, expected: string) => {
    expect(getGreeting(hour)).toBe(expected)
  }
})
