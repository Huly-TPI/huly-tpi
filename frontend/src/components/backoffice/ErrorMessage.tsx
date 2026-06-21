import { X } from 'lucide-react'

interface ErrorMessageProps {
  message: string
  className?: string
}

export function ErrorMessage({ message, className = '' }: ErrorMessageProps) {
  return (
    <div className={`shrink-0 flex items-center gap-2.5 rounded-xl border border-red-200 bg-red-50 px-3 py-2.5 dark:border-red-900/30 dark:bg-red-950/10 ${className}`}>
      <div className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-500">
        <X className="h-3 w-3 text-white" strokeWidth={3} />
      </div>
      <p className="text-xs font-semibold text-red-600 dark:text-red-400">{message}</p>
    </div>
  )
}
