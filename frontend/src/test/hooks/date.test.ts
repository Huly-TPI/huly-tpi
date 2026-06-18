import { describe, it, expect } from 'vitest'
import { renderHook } from '@testing-library/react'
import { useTimeFormatter } from '../../hooks/useTimeFormatter'

describe('useTimeFormatter', () => {
  it('returns "0 seg" for zero or negative values', () => {
    const { result } = renderHook(() => useTimeFormatter())
    expect(result.current.formatConsumptionTime(0)).toBe('0 seg')
    expect(result.current.formatConsumptionTime(-10)).toBe('0 seg')
  })

  it('formats seconds under 60 as "X seg"', () => {
    const { result } = renderHook(() => useTimeFormatter())
    expect(result.current.formatConsumptionTime(45)).toBe('45 seg')
  })

  it('formats seconds under an hour as exact minutes and seconds', () => {
    const { result } = renderHook(() => useTimeFormatter())
    expect(result.current.formatConsumptionTime(120)).toBe('2 min')
    expect(result.current.formatConsumptionTime(75)).toBe('1 min 15 seg')
  })

  it('formats seconds over an hour as exact hours, minutes, and seconds', () => {
    const { result } = renderHook(() => useTimeFormatter())
    expect(result.current.formatConsumptionTime(3600)).toBe('1 h')
    expect(result.current.formatConsumptionTime(3675)).toBe('1 h 1 min 15 seg')
    expect(result.current.formatConsumptionTime(7205)).toBe('2 h 5 seg')
  })
})
