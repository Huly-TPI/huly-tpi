export function sanitizeInput(value: string): string {
  return value
    .normalize('NFC')
    .replace(/[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]/g, '')
}

export function sanitizeForSubmit(value: string): string {
  return sanitizeInput(value).trim()
}