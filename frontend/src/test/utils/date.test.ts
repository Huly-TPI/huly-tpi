import { describe, it, expect } from 'vitest'
import { formatDate, formatTime } from '../../utils/date'


describe('formatDate', () => {
  it('formatea correctamente una fecha en español', () => {
    const date = new Date(2026, 4, 19) // 19 de mayo de 2026 (martes)
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