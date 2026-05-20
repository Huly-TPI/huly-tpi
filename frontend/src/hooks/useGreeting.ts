import { useState, useEffect } from 'react'
import { getGreeting } from '../utils/greeting'

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