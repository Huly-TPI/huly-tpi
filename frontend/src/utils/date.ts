export function formatDate(date: Date): string {
  const days = ['domingo', 'lunes', 'martes', 'miércoles', 'jueves', 'viernes', 'sábado']
  const months = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio',
                  'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre']
  
  const dayName = days[date.getDay()]
  const day = date.getDate()
  const month = months[date.getMonth()]
  const year = date.getFullYear()
  
  return `${dayName}, ${day} de ${month} de ${year}`
}

export function formatTime(date: Date): string {
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

export function getToday(): string {
  return formatDate(new Date())
}

export function formatConsumptionTime(seconds: number): string {
  if (!seconds || seconds <= 0) return '0 min'
  if (seconds < 60) return `${seconds} seg`
  if (seconds < 3600) {
    return `${Math.round(seconds / 60)} min`
  }
  if (seconds < 86400) {
    return `${(seconds / 3600).toFixed(1)} h`
  }
  return `${(seconds / 86400).toFixed(1)} días`
}