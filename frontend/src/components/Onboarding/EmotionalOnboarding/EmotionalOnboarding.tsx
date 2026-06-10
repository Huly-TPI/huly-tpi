import './EmotionalOnboarding.css'
import registerBackground from '../../../assets/register/background.webp'
import cardFrame from '../../../assets/register/cardFrame.webp'
import hulyGreeting from '../../../assets/register/huly-greeting.webp'
import type { Step1Option } from '../../../hooks/useEmotionalOnboarding'

const STEP_CONTENT: Record<1 | 2 | 3, { title: string; subtitle: string }> = {
  1: {
    title: '¿Qué te trae a Huly?',
    subtitle: 'Elegí lo que más te resuena.',
  },
  2: {
    title: '¿Por dónde te gustaría empezar?',
    subtitle: 'Contame un poco más sobre lo que buscás.',
  },
  3: {
    title: '¿Qué queres que sea Huly para vos?',
    subtitle: 'Tu jardín, a tu manera.',
  },
}

type Props = {
  step: 0 | 1 | 2 | 3
  step1Options: Step1Option[]
  pillOptions: string[]
  onAdvance: () => void
  onSelectOption: (option: string) => void
  onSkip: () => Promise<void>
}

export default function EmotionalOnboarding({
  step,
  step1Options,
  pillOptions,
  onAdvance,
  onSelectOption,
  onSkip,
}: Props) {
  const cardStyle = {
    backgroundImage: `url(${cardFrame})`,
    backgroundSize: '100% 100%',
    backgroundRepeat: 'no-repeat',
  }

  return (
    <div
      className="eo-container"
      style={{ backgroundImage: `url(${registerBackground})` }}
    >
      <div className="eo-overlay" />

      {step === 0 && (
        <div className="eo-intro">
          <div className="eo-bubble">
            <div className="eo-mascot-wrapper">
              <img src={hulyGreeting} alt="Huly" className="eo-intro__mascot" />
              <div className="eo-mascot-shadow" />
            </div>
            <p className="eo-bubble__text">
              ¡Hola! Me alegra que estés acá.
              <br />
              Antes de entrar al jardín, te hago unas preguntas cortitas para conocerte mejor y personalizar tu experiencia.
            </p>
            <button className="eo-btn-primary" onClick={onAdvance}>
              Empezar
            </button>
            <button className="eo-btn-skip-link" onClick={onSkip}>
              Saltar por ahora
            </button>
          </div>
        </div>
      )}

      {step > 0 && (
        <div className="eo-card">
          <div className="eo-header">
            <div className="eo-progress" aria-hidden="true">
              <div
                className="eo-progress__bar"
                style={{ width: `${(step / 3) * 100}%` }}
              />
            </div>
            <button className="eo-btn-skip-top" onClick={onSkip}>
              Saltar
            </button>
          </div>

          <h2 className="eo-title">
            {STEP_CONTENT[step as 1 | 2 | 3].title}
          </h2>
          <p className="eo-subtitle">
            {STEP_CONTENT[step as 1 | 2 | 3].subtitle}
          </p>

          {step === 1 && (
            <div className="eo-cards-grid">
              {step1Options.map(opt => (
                <button
                  key={opt.title}
                  className="eo-card-option"
                  onClick={() => onSelectOption(opt.title)}
                >
                  <opt.Icon className="eo-card-option__icon" />
                  <span className="eo-card-option__title">{opt.title}</span>
                  <span className="eo-card-option__subtitle">{opt.subtitle}</span>
                </button>
              ))}
            </div>
          )}

          {(step === 2 || step === 3) && (
            <div className="eo-pills">
              {pillOptions.map(opt => (
                <button
                  key={opt}
                  className="eo-pill"
                  onClick={() => onSelectOption(opt)}
                >
                  {opt}
                </button>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
