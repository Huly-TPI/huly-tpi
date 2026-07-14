import type { EstimatedDuration } from '../../api/pending'

export interface DurationFieldProps {
  value: EstimatedDuration | undefined
  onChange: (value: EstimatedDuration | undefined) => void
}

export const DURATION_OPTIONS: { value: EstimatedDuration; label: string }[] = [
  { value: 'FIFTEEN_MIN', label: '15 min' },
  { value: 'ONE_HOUR', label: '1 hora' },
  { value: 'HALF_DAY', label: 'Medio día' },
  { value: 'FULL_DAY', label: 'Día completo' },
]

export default function DurationField({ value, onChange }: DurationFieldProps) {
  return (
    <div className="mt-2">
      <label className="text-[0.75rem] font-bold uppercase tracking-[0.06em] text-[rgba(92,61,30,0.8)] dark:text-slate-400">
        Duración estimada
      </label>
      <div className="mt-1 flex flex-wrap gap-1.5">
        {DURATION_OPTIONS.map(option => (
          <button
            key={option.value}
            type="button"
            aria-pressed={value === option.value}
            onClick={() => onChange(value === option.value ? undefined : option.value)}
            className={`rounded-full px-2.5 py-1 text-xs transition-colors ${
              value === option.value ? 'bg-violeta text-white' : 'bg-[rgba(92,61,30,0.08)] dark:bg-slate-800 text-[#5c3d1e] dark:text-slate-300'
            }`}
          >
            {option.label}
          </button>
        ))}
      </div>
    </div>
  )
}
