import { RiskWordResponse } from '../../api/chatbot'

// Colors from Figma design file
const RISK_STYLE: Record<'HIGH' | 'MEDIUM' | 'LOW', {
  rowBg: string
  badgeBg: string
  badgeText: string
  label: string
}> = {
  HIGH:   { rowBg: 'bg-[#FEE2E2]', badgeBg: 'bg-[#EF4444]', badgeText: 'text-white', label: 'CRÍTICO' },
  MEDIUM: { rowBg: 'bg-[#FFFBEB]', badgeBg: 'bg-[#F59E0B]', badgeText: 'text-white', label: 'ALTO'    },
  LOW:    { rowBg: 'bg-white',      badgeBg: 'bg-[#A0AEC0]', badgeText: 'text-white', label: 'INFO'    },
}

interface RiskWordItemProps {
  word: RiskWordResponse
  onDelete: (id: number) => void
}

export function RiskWordItem({ word, onDelete }: RiskWordItemProps) {
  const s = RISK_STYLE[word.severity]

  return (
    <li className={`flex items-center justify-between gap-3 rounded-xl px-4 py-3 ${s.rowBg}`}>
      <span className="flex-1 truncate text-sm font-semibold text-[#4A5568]">
        "{word.word}"
      </span>
      <div className="flex shrink-0 items-center gap-2">
        <span className={`rounded-full px-3 py-0.5 text-[10px] font-bold uppercase tracking-wide ${s.badgeBg} ${s.badgeText}`}>
          {s.label}
        </span>
        <button
          onClick={() => onDelete(word.id)}
          className="text-[#A0AEC0] hover:text-[#EF4444] transition-colors"
          aria-label={`Eliminar "${word.word}"`}
        >
          <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </li>
  )
}
