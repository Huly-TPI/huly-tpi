interface ProgressBarProps {
  pct: number
  colorClass: string
  height?: string
  className?: string
}

export function ProgressBar({ pct, colorClass, height = 'h-1', className = '' }: ProgressBarProps) {
  return (
    <div className={`w-full overflow-hidden rounded-full bg-gray-100 dark:bg-gray-800 ${height} ${className}`}>
      <div
        className={`h-full rounded-full ${colorClass}`}
        style={{ width: `${Math.min(100, Math.max(0, pct))}%` }}
      />
    </div>
  )
}
