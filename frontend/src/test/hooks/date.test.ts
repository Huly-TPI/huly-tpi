import { describe, it, expect } from 'vitest'
import { renderHook } from '@testing-library/react'
import { useTimeFormatter } from '../../hooks/useTimeFormatter'

describe('useTimeFormatter', () => {
  it('retorna "0 seg" para valores cero o negativos', () => {
    setupHook()
    verifyFormattedConsumptionTime(0, '0 seg')
    verifyFormattedConsumptionTime(-10, '0 seg')
  })

  it('formatea segundos menores a 60 como "X seg"', () => {
    setupHook()
    verifyFormattedConsumptionTime(45, '45 seg')
  })

  it('formatea segundos menores a una hora como minutos y segundos exactos', () => {
    setupHook()
    verifyFormattedConsumptionTime(120, '2 min')
    verifyFormattedConsumptionTime(75, '1 min 15 seg')
  })

  it('formatea segundos mayores a una hora como horas, minutos y segundos exactos', () => {
    setupHook()
    verifyFormattedConsumptionTime(3600, '1 h')
    verifyFormattedConsumptionTime(3675, '1 h 1 min 15 seg')
    verifyFormattedConsumptionTime(7205, '2 h 5 seg')
  })
  let rendered: ReturnType<typeof renderHook<ReturnType<typeof useTimeFormatter>, undefined>>

  /* helpers */

  const setupHook = () => {
    rendered = renderHook(() => useTimeFormatter())
  }

  const verifyFormattedConsumptionTime = (seconds: number, expected: string) => {
    expect(rendered.result.current.formatConsumptionTime(seconds)).toBe(expected)
  }
})
