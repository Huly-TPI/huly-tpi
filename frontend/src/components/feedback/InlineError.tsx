import { AlertCircle } from 'lucide-react'

interface InlineErrorProps {
  message: string
  className?: string
}

export function InlineError({ message, className = '' }: InlineErrorProps) {
  return (
    <div
      className={`flex items-start gap-2.5 rounded-xl border border-red-200 bg-red-50/50 p-3 text-sm text-red-700 dark:border-red-900/30 dark:bg-red-950/10 dark:text-red-400 ${className}`}
    >
      <AlertCircle className="h-4 w-4 shrink-0 mt-0.5 text-red-500 dark:text-red-400" />
      <span role="alert" className="font-medium leading-tight">{message}</span>
    </div>
  )
}
