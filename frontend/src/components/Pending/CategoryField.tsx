import type { PendingCategory } from '../../api/pending'

export interface CategoryFieldProps {
  value: PendingCategory | undefined
  onChange: (value: PendingCategory | undefined) => void
}

export const CATEGORY_OPTIONS: { value: PendingCategory; label: string }[] = [
  { value: 'TRABAJO', label: 'Trabajo' },
  { value: 'PERSONAL', label: 'Personal' },
  { value: 'SALUD', label: 'Salud' },
  { value: 'HOGAR', label: 'Hogar' },
]

export default function CategoryField({ value, onChange }: CategoryFieldProps) {
  return (
    <div className="mt-2">
      <label className="text-[0.75rem] font-bold uppercase tracking-[0.06em] text-[rgba(92,61,30,0.8)]">
        Categoría
      </label>
      <div className="mt-1 flex flex-wrap gap-1.5">
        {CATEGORY_OPTIONS.map(option => (
          <button
            key={option.value}
            type="button"
            aria-pressed={value === option.value}
            onClick={() => onChange(value === option.value ? undefined : option.value)}
            className={`rounded-full px-2.5 py-1 text-xs transition-colors ${
              value === option.value ? 'bg-violeta text-white' : 'bg-[rgba(92,61,30,0.08)] text-[#5c3d1e]'
            }`}
          >
            {option.label}
          </button>
        ))}
      </div>
    </div>
  )
}
