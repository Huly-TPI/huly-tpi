import { useRef, useState } from 'react'
import { CalendarDays } from 'lucide-react'
import { getInputClassName } from './authInputStyles'

interface DateInputProps {
  id: string
  placeholder: string
  value: string
  onChange: (value: string) => void
  hasError: boolean
  errorId?: string
  autoFocus?: boolean
}

export default function DateInput({
  id,
  placeholder,
  value,
  onChange,
  hasError,
  errorId,
  autoFocus = false,
}: DateInputProps) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [focused, setFocused] = useState(false)

  const inputType = value.length > 0 || focused ? 'date' : 'text'

  const openPicker = () => {
    setFocused(true)
    requestAnimationFrame(() => {
      inputRef.current?.showPicker()
    })
  }

  return (
    <div className="relative min-w-0">
      <input
        ref={inputRef}
        id={id}
        type={inputType}
        placeholder={placeholder}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        autoFocus={autoFocus}
        aria-invalid={hasError}
        aria-describedby={errorId}
        className={getInputClassName(
          hasError,
          'pr-12 min-w-0 [&::-webkit-calendar-picker-indicator]:hidden',
        )}
      />
      <button
        type="button"
        onClick={openPicker}
        className="absolute right-3 top-1/2 -translate-y-1/2 text-[#9c8b74] hover:text-[#6b5d4a] transition-colors"
        aria-label="Abrir calendario"
      >
        <CalendarDays className="size-5" strokeWidth={2} />
      </button>
    </div>
  )
}
