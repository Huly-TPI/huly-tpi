import { ReactNode } from 'react'

interface SectionCardProps {
  children: ReactNode
  className?: string
  bg?: string
}

export function SectionCard({ children, className = '', bg = 'bg-white' }: SectionCardProps) {
  return (
    <div className={`rounded-[10px] ${bg} p-4 shadow-sm lg:p-5 ${className}`}>
      {children}
    </div>
  )
}

interface CardHeaderProps {
  title: string
  action?: ReactNode
}

export function CardHeader({ title, action }: CardHeaderProps) {
  return (
    <div className="mb-4 flex items-center justify-between gap-2">
      <h2 className="text-sm font-bold text-gray-700">{title}</h2>
      {action}
    </div>
  )
}
