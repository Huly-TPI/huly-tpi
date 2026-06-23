import { X } from 'lucide-react'
import { RiskWordResponse } from '../../api/chatbot'

const RISK_STYLE: Record<'HIGH' | 'MEDIUM' | 'LOW', {
  rowBg: string
  badgeBg: string
  badgeText: string
  label: string
}> = {
  HIGH: { rowBg: 'bg-[#FEE2E2] dark:bg-red-950/20', badgeBg: 'bg-[#EF4444]', badgeText: 'text-white', label: 'CRÍTICO' },
  MEDIUM: { rowBg: 'bg-[#FFFBEB] dark:bg-amber-950/20', badgeBg: 'bg-[#F59E0B]', badgeText: 'text-white', label: 'ALTO' },
  LOW: { rowBg: 'bg-white dark:bg-[#09111f]', badgeBg: 'bg-[#A0AEC0] dark:bg-gray-700', badgeText: 'text-white dark:text-gray-200', label: 'INFO' },
}

interface RiskWordItemProps {
  word: RiskWordResponse
  onDelete: (id: number) => void
}

export function RiskWordItem({ word, onDelete }: RiskWordItemProps) {
  const s = RISK_STYLE[word.severity]

  return (
    <li className={`flex items-center justify-between gap-3 rounded-xl px-4 py-3 ${s.rowBg}`}>
      <span className="flex-1 truncate text-sm font-semibold text-[#4A5568] dark:text-gray-200">
        "{word.word}"
      </span>
      <div className="flex shrink-0 items-center gap-2">
        <span className={`w-[62px] rounded-full px-3 py-0.5 text-center text-[10px] font-bold uppercase tracking-wide ${s.badgeBg} ${s.badgeText}`}>
          {s.label}
        </span>
        <button
          onClick={() => onDelete(word.id)}
          className="text-[#A0AEC0] transition-colors hover:text-[#EF4444] dark:text-gray-500 dark:hover:text-red-400"
          aria-label={`Eliminar "${word.word}"`}
        >
          <X className="h-3.5 w-3.5" strokeWidth={2} />
        </button>
      </div>
    </li>
  )
}
