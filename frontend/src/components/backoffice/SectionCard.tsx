import { ReactNode } from 'react'

interface SectionCardProps {
  children: ReactNode
  className?: string
  bg?: string
  padding?: string
}

export function SectionCard({ children, className = '', bg = 'bg-white dark:bg-[#172033]', padding = 'p-4 lg:p-5' }: SectionCardProps) {
  return (
    <div className={`rounded-[10px] ${bg} ${padding} shadow-sm ${className}`}>
      {children}
    </div>
  )
}

interface CardHeaderProps {
  title: string
  action?: ReactNode
  subtitle?: string
}

export function CardHeader({ title, action, subtitle }: CardHeaderProps) {
  return (
    <div className="mb-4 flex items-center justify-between gap-2">
      <div>
        <h2 className="text-sm font-bold text-gray-700 dark:text-gray-200">{title}</h2>
        {subtitle && <p className="text-xs text-gray-400 mt-0.5">{subtitle}</p>}
      </div>
      {action}
    </div>
  )
}
