import { useState } from 'react'
import { Globe } from 'lucide-react'

interface DomainFaviconProps {
  domain: string
  size?: number
  className?: string
}

export default function DomainFavicon({ domain, size = 16, className = '' }: DomainFaviconProps) {
  const [error, setError] = useState(false)
  const isOther = !domain || domain === 'Otros sitios'

  if (isOther || error) {
    return <Globe className={`${className} text-gray-400`} size={size} strokeWidth={1.8} />
  }

  return (
    <img
      src={`https://www.google.com/s2/favicons?sz=64&domain=${domain}`}
      alt=""
      className={`${className} object-contain rounded-sm`}
      style={{ width: `${size}px`, height: `${size}px` }}
      onError={() => setError(true)}
    />
  )
}
