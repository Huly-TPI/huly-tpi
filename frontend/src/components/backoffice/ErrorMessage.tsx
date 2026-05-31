interface ErrorMessageProps {
  message: string
  className?: string
}

export function ErrorMessage({ message, className = '' }: ErrorMessageProps) {
  return (
    <div className={`shrink-0 flex items-center gap-2.5 rounded-xl border border-red-200 bg-red-50 px-3 py-2.5 ${className}`}>
      <div className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-red-500">
        <svg className="h-3 w-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </div>
      <p className="text-xs font-semibold text-red-600">{message}</p>
    </div>
  )
}
