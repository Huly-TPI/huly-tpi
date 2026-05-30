interface ProgressBarProps {
  pct: number
  colorClass: string
  height?: string
}

export function ProgressBar({ pct, colorClass, height = 'h-1' }: ProgressBarProps) {
  return (
    <div className={`w-full overflow-hidden rounded-full bg-gray-100 ${height}`}>
      <div
        className={`h-full rounded-full ${colorClass}`}
        style={{ width: `${Math.min(100, Math.max(0, pct))}%` }}
      />
    </div>
  )
}
