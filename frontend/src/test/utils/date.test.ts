import { describe, it, expect } from 'vitest'
import { formatDate, formatTime, formatConsumptionTime } from '../../utils/date'


describe('formatDate', () => {
  it('formatea correctamente una fecha en español', () => {
    const date = new Date(2026, 4, 19)
    expect(formatDate(date)).toBe('martes, 19 de mayo de 2026')
  })

  it('usa el nombre correcto del día', () => {
    const monday = new Date(2026, 4, 18)
    expect(formatDate(monday).startsWith('lunes')).toBe(true)
  })
})

describe('formatTime', () => {
  it('formatea horas con dos dígitos', () => {
    const date = new Date(2026, 0, 1, 9, 5)
    expect(formatTime(date)).toBe('09:05')
  })

  it('formatea horas de la tarde', () => {
    const date = new Date(2026, 0, 1, 14, 30)
    expect(formatTime(date)).toBe('14:30')
  })
})

describe('formatConsumptionTime', () => {
  it('retorna 0 min para valores nulos, menores o iguales a cero', () => {
    expect(formatConsumptionTime(0)).toBe('0 min')
    expect(formatConsumptionTime(-5)).toBe('0 min')
  })

  it('retorna segundos si es menor a un minuto', () => {
    expect(formatConsumptionTime(45)).toBe('45 seg')
  })

  it('retorna minutos si es menor a una hora', () => {
    expect(formatConsumptionTime(150)).toBe('3 min')
    expect(formatConsumptionTime(3599)).toBe('60 min')
  })

  it('retorna horas y minutos en formato h:mm para una hora o más', () => {
    expect(formatConsumptionTime(3600)).toBe('1:00 h')
    expect(formatConsumptionTime(3720)).toBe('1:02 h')
    expect(formatConsumptionTime(7300)).toBe('2:02 h')
  })
})