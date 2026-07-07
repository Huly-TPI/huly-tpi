import { useState } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'

export interface DueDateFieldProps {
  value: string
  onChange: (value: string) => void
}

const WEEKDAY_LABELS = ['Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sá', 'Do']

function toDateOnly(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function parseDateOnly(value: string): Date | null {
  if (!value) return null
  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day)
}

function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

export default function DueDateField({ value, onChange }: DueDateFieldProps) {
  const today = startOfDay(new Date())
  const selected = parseDateOnly(value)
  const [viewDate, setViewDate] = useState(() => startOfDay(selected ?? today))

  const rawMonthLabel = viewDate.toLocaleDateString('es-AR', { month: 'long', year: 'numeric' })
  const monthLabel = rawMonthLabel.charAt(0).toUpperCase() + rawMonthLabel.slice(1)
  const isAtEarliestMonth = viewDate.getFullYear() === today.getFullYear() && viewDate.getMonth() === today.getMonth()

  const firstOfMonth = new Date(viewDate.getFullYear(), viewDate.getMonth(), 1)
  const startOffset = (firstOfMonth.getDay() + 6) % 7
  const daysInMonth = new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 0).getDate()

  const cells: (Date | null)[] = [
    ...Array.from({ length: startOffset }, () => null),
    ...Array.from({ length: daysInMonth }, (_, i) => new Date(viewDate.getFullYear(), viewDate.getMonth(), i + 1)),
  ]

  const goToMonth = (delta: number) => {
    setViewDate(prev => new Date(prev.getFullYear(), prev.getMonth() + delta, 1))
  }

  return (
    <div className="mt-2">
      <label className="text-[0.65rem] font-bold uppercase tracking-[0.06em] text-[rgba(92,61,30,0.5)]">
        Fecha límite
      </label>
      <div className="mt-1 rounded-lg border border-[rgba(92,61,30,0.25)] bg-[#fffdf5] p-2">
        <div className="flex items-center justify-between px-1">
          <button
            type="button"
            onClick={() => goToMonth(-1)}
            disabled={isAtEarliestMonth}
            aria-label="Mes anterior"
            className="rounded-full p-1 text-[#5c3d1e] transition-colors hover:bg-[rgba(92,61,30,0.1)] disabled:cursor-not-allowed disabled:opacity-30 disabled:hover:bg-transparent"
          >
            <ChevronLeft className="h-4 w-4" />
          </button>
          <span className="text-xs font-bold text-[#3b2510]">{monthLabel}</span>
          <button
            type="button"
            onClick={() => goToMonth(1)}
            aria-label="Mes siguiente"
            className="rounded-full p-1 text-[#5c3d1e] transition-colors hover:bg-[rgba(92,61,30,0.1)]"
          >
            <ChevronRight className="h-4 w-4" />
          </button>
        </div>

        <div className="mt-2 grid grid-cols-7 gap-1 text-center text-[0.6rem] font-bold text-[rgba(92,61,30,0.5)]">
          {WEEKDAY_LABELS.map(day => (
            <span key={day}>{day}</span>
          ))}
        </div>

        <div className="mt-1 grid grid-cols-7 gap-1">
          {cells.map((date, index) => {
            if (!date) return <span key={`empty-${index}`} />
            const isPast = date < today
            const isSelected = selected !== null && date.getTime() === selected.getTime()
            const isToday = date.getTime() === today.getTime()
            return (
              <button
                key={date.getTime()}
                type="button"
                disabled={isPast}
                onClick={() => onChange(toDateOnly(date))}
                className={`aspect-square rounded-full text-[0.7rem] transition-colors ${
                  isSelected
                    ? 'bg-violeta font-bold text-white'
                    : isPast
                      ? 'cursor-not-allowed text-[rgba(92,61,30,0.3)]'
                      : `text-[#3b2510] hover:bg-[rgba(92,61,30,0.12)] ${isToday ? 'ring-1 ring-inset ring-violeta' : ''}`
                }`}
              >
                {date.getDate()}
              </button>
            )
          })}
        </div>

        {value && (
          <button
            type="button"
            onClick={() => onChange('')}
            className="mt-2 w-full text-center text-[0.65rem] text-[#7a5c38] underline"
          >
            Quitar fecha
          </button>
        )}
      </div>
    </div>
  )
}
