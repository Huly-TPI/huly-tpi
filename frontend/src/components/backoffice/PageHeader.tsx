import { ReactNode } from 'react'
import { ChevronLeft } from 'lucide-react'

interface PageHeaderProps {
  title: string
  subtitle?: string
  action?: ReactNode
  showBackButton?: boolean
  onBackButtonClick?: () => void
}

export default function PageHeader({
  title,
  subtitle,
  action,
  showBackButton = false,
  onBackButtonClick,
}: PageHeaderProps) {
  return (
    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 shrink-0">
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-2">
          {showBackButton && onBackButtonClick && (
            <button
              onClick={onBackButtonClick}
              className="text-gray-400 dark:text-gray-555 hover:text-violeta dark:hover:text-violeta-claro transition duration-150 flex items-center justify-center p-0.5"
              aria-label="volver"
            >
              <ChevronLeft className="h-5 w-5" strokeWidth={2} />
            </button>
          )}
          <h1 className="text-[30px] font-extrabold leading-tight text-violeta dark:text-violeta-claro">
            {title}
          </h1>
        </div>
        {subtitle && (
          <p className="text-[16px] text-[#A0AEC0] dark:text-gray-400">
            {subtitle}
          </p>
        )}
      </div>
      {action && (
        <div className="self-start sm:self-auto flex items-center gap-2">
          {action}
        </div>
      )}
    </div>
  )
}
