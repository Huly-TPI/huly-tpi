import React from 'react'

interface KPICardProps {
  title: string
  value: string | number
  description?: string
  icon?: React.ReactNode
  type?: 'violeta' | 'emerald' | 'blue'
}

export default function KPICard({ title, value, description, icon, type = 'violeta' }: KPICardProps) {
  const themeMap = {
    violeta: {
      card: 'bg-gradient-to-br from-violeta/10 to-violeta-claro/5 dark:from-[#2A233C]/20 dark:to-transparent border border-violeta/20 rounded-xl p-4 flex flex-col justify-between h-full min-h-[110px]',
      valueText: 'text-3xl font-black text-violeta dark:text-violeta-claro mt-2',
    },
    emerald: {
      card: 'bg-gradient-to-br from-emerald-500/10 to-emerald-500/5 dark:from-emerald-955/20 dark:to-transparent border border-emerald-500/20 rounded-xl p-4 flex flex-col justify-between h-full min-h-[110px]',
      valueText: 'text-3xl font-black text-emerald-600 dark:text-emerald-400 mt-2',
    },
    blue: {
      card: 'bg-gradient-to-br from-blue-500/10 to-blue-500/5 dark:from-blue-955/20 dark:to-transparent border border-blue-500/20 rounded-xl p-4 flex flex-col justify-between h-full min-h-[110px]',
      valueText: 'text-3xl font-black text-blue-600 dark:text-blue-400 mt-2',
    },
  }

  const theme = themeMap[type] || themeMap.violeta

  return (
    <div className={theme.card}>
      <div className="flex items-center justify-between gap-2">
        <span className="text-xs font-bold text-gray-400 dark:text-gray-555 uppercase tracking-wider">{title}</span>
        {icon}
      </div>
      <span className={theme.valueText}>
        {value}
      </span>
      {description && (
        <p className="text-xs text-gray-450 dark:text-gray-500 mt-1">{description}</p>
      )}
    </div>
  )
}
