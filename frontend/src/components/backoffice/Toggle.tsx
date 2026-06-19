interface ToggleProps {
  enabled: boolean
  onChange: (val: boolean) => void
  label?: string
  description?: string
}

export function Toggle({ enabled, onChange, label, description }: ToggleProps) {
  return (
    <div className="flex items-center justify-between gap-4 rounded-xl bg-gray-50 dark:bg-[#09111f] px-4 py-3">
      {(label || description) && (
        <div>
          {label && <p className="text-sm font-semibold text-gray-700 dark:text-gray-200">{label}</p>}
          {description && <p className="text-xs text-gray-400 dark:text-gray-500">{description}</p>}
        </div>
      )}
      <button
        type="button"
        role="switch"
        aria-checked={enabled}
        onClick={() => onChange(!enabled)}
        className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-violeta ${enabled ? 'bg-violeta' : 'bg-gray-300 dark:bg-gray-700'}`}
      >
        <span
          className={`inline-block h-4 w-4 rounded-full bg-white shadow transition-transform ${enabled ? 'translate-x-6' : 'translate-x-1'}`}
        />
      </button>
    </div>
  )
}
