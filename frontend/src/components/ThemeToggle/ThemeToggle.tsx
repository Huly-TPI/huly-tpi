import { useTheme } from '../../context/theme'
import './ThemeToggle.css'

interface ThemeToggleProps {
  compact?: boolean
}

export default function ThemeToggle({ compact = false }: ThemeToggleProps) {
  const { theme, toggleTheme } = useTheme()
  const isDark = theme === 'dark'
  const ariaLabel = isDark ? 'Cambiar a modo dia' : 'Cambiar a modo noche'

  return (
    <button
      type="button"
      className={`theme-toggle ${compact ? 'theme-toggle--compact' : ''} ${isDark ? 'theme-toggle--dark' : 'theme-toggle--light'}`}
      onClick={toggleTheme}
      aria-label={ariaLabel}
      aria-pressed={isDark}
      title={ariaLabel}
    >
      <span aria-hidden="true" className="theme-toggle__track">
        <span className="theme-toggle__layer theme-toggle__layer--one" />
        <span className="theme-toggle__layer theme-toggle__layer--two" />
        <span className="theme-toggle__layer theme-toggle__layer--three" />
        <span className="theme-toggle__layer theme-toggle__layer--four" />
        <span className="theme-toggle__clouds" aria-hidden="true">
          <span className="theme-toggle__cloud theme-toggle__cloud--one" />
          <span className="theme-toggle__cloud theme-toggle__cloud--two" />
          <span className="theme-toggle__cloud theme-toggle__cloud--three" />
        </span>
        <span className="theme-toggle__stars">
          <span />
          <span />
          <span />
          <span />
        </span>
      </span>
      <span aria-hidden="true" className="theme-toggle__thumb">
        <span className="theme-toggle__crater theme-toggle__crater--one" />
        <span className="theme-toggle__crater theme-toggle__crater--two" />
        <span className="theme-toggle__crater theme-toggle__crater--three" />
      </span>
    </button>
  )
}
