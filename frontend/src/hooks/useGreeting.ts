import { useState, useEffect } from 'react'
export function getGreeting(hour: number): string {
  if (hour >= 6 && hour < 12) 
    return 'Buenos días'
  
  if (hour >= 12 && hour < 19) 
    return 'Buenas tardes'
  
  return 'Buenas noches'
}

export function useGreeting() {
  const [greeting, setGreeting] = useState(getGreeting(new Date().getHours()))

  useEffect(() => {
    const interval = setInterval(() => {
      setGreeting(getGreeting(new Date().getHours()))
    }, 60000)
    return () => clearInterval(interval)
  }, [])

  return greeting
}