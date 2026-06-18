import { useCallback } from 'react'

export function useTimeFormatter() {
  const formatConsumptionTime = useCallback((seconds: number): string => {
    if (seconds <= 0) 
      return '0 seg'
    
    if (seconds < 60) 
      return `${seconds} seg`
    
    if (seconds < 3600) {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      if (secs === 0) 
        return `${mins} min`
      
      return `${mins} min ${secs} seg`
    }
    
    const h = Math.floor(seconds / 3600)
    const remainder = seconds % 3600
    const mins = Math.floor(remainder / 60)
    const secs = remainder % 60
    
    let result = `${h} h`
    if (mins > 0) 
      result += ` ${mins} min`
    
    if (secs > 0) 
      result += ` ${secs} seg`
    
    return result
  }, [])

  return { formatConsumptionTime }
}
