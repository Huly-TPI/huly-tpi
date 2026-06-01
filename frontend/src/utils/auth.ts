export function getUserIdFromToken(): number | null {
  try {
    const token = localStorage.getItem('token')
    if (!token) return null
    const payload = JSON.parse(atob(token.split('.')[1]))
    return typeof payload.userId === 'number' ? payload.userId : null
  } catch {
    return null
  }
}
