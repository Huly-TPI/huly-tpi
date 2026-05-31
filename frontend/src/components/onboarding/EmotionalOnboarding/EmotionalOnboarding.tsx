import './EmotionalOnboarding.css'
type Props = {
    step: 1 | 2 | 3
    options: string[]
    isLoading: boolean
    onSelectOption: (option: string) => void
}

const TITLES: Record<1 | 2 | 3, string> = {
  1: '¿Qué querés lograr?',
  2: '¿Qué aspecto te llama más?',
  3: '¿Cómo querés empezar?',
}

const SUBTITLES: Record<1 | 2 | 3, string> = {
  1: 'Elegí lo que más se acerca a lo que buscás hoy.',
  2: 'Basándonos en tu elección, ¿qué te llama más la atención?',
  3: 'Casi listo. Elegí cómo dar el primer paso.',
}

export default function EmotionalOnboarding({ step, options, isLoading, onSelectOption }: Props) {
  return (
    <div className="emotional-onboarding">
      <div className="emotional-onboarding-overlay" />
      <div className="emotional-onboarding-card">
        <div className="emotional-onboarding-steps" aria-hidden="true">
          {[1, 2, 3].map(s => (
            <div
              key={s}
              className={`emotional-onboarding-step-dot ${s === step ? 'emotional-onboarding-step-dot-active' : ''}`}
            />
          ))}
        </div>

        <h2 className="emotional-onboarding-title">{TITLES[step]}</h2>
        <p className="emotional-onboarding-subtitle">{SUBTITLES[step]}</p>

        <div className="emotional-onboarding-options">
          {isLoading ? (
            <div className="emotional-onboarding-loading" role="status">
              Cargando opciones...
            </div>
          ) : (
            options.map(option => (
              <button
                key={option}
                className="emotional-onboarding-option"
                onClick={() => onSelectOption(option)}
                disabled={isLoading}
              >
                {option}
              </button>
            ))
          )}
        </div>
      </div>
    </div>
  )
}