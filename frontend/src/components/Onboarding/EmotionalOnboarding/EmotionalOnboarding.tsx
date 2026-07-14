import { useState, useEffect } from 'react'
import './EmotionalOnboarding.css'
import registerBackground from '../../../assets/register/background.webp'
import registerBackgroundNight from '../../../assets/register/background-night.webp'
import hulyGreeting from '../../../assets/register/huly-greeting.webp'
import type { Step1Option } from '../../../hooks/useEmotionalOnboarding'
import { useTheme } from '../../../context/theme'

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
    title: '¿Qué querés que sea Huly para vos?',
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
  submitting?: boolean
}

export default function EmotionalOnboarding({
  step,
  step1Options,
  pillOptions,
  onAdvance,
  onSelectOption,
  onSkip,
  submitting = false,
}: Props) {
  const { theme } = useTheme()
  const isDark = theme === 'dark'
  const [selectedOption, setSelectedOption] = useState<string | null>(null)
  const [isSkipping, setIsSkipping] = useState(false)

  useEffect(() => {
    setSelectedOption(null)
  }, [step])

  const handleSelectOption = (option: string) => {
    setSelectedOption(option)
    onSelectOption(option)
  }

  const handleSkip = async () => {
    setIsSkipping(true)
    try {
      await onSkip()
    } finally {
      setIsSkipping(false)
    }
  }

  return (
    <div
      className="eo-container"
      style={{ backgroundImage: `url(${isDark ? registerBackgroundNight : registerBackground})` }}
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
             <button className="eo-btn-primary" onClick={onAdvance} disabled={submitting || isSkipping}>
              Empezar
            </button>
            <button className="eo-btn-skip-link" onClick={handleSkip} disabled={submitting || isSkipping}>
              {isSkipping ? 'Saltar por ahora...' : 'Saltar por ahora'}
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
            <button className="eo-btn-skip-top" onClick={handleSkip} disabled={submitting || isSkipping}>
              {isSkipping ? 'Saltar...' : 'Saltar'}
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
              {step1Options.map(opt => {
                const isSelected = selectedOption === opt.title
                return (
                  <button
                    key={opt.title}
                    className={`eo-card-option ${isSelected && submitting ? 'eo-card-option--loading' : ''}`}
                    onClick={() => handleSelectOption(opt.title)}
                    disabled={submitting || isSkipping}
                  >
                    <opt.Icon className="eo-card-option__icon" />
                    <span className="eo-card-option__title">
                      {isSelected && submitting ? 'Cargando...' : opt.title}
                    </span>
                    <span className="eo-card-option__subtitle">{opt.subtitle}</span>
                  </button>
                )
              })}
            </div>
          )}

          {(step === 2 || step === 3) && (
            <div className="eo-pills">
              {pillOptions.map(opt => {
                const isSelected = selectedOption === opt
                return (
                  <button
                    key={opt}
                    className={`eo-pill ${isSelected && submitting ? 'eo-pill--loading' : ''}`}
                    onClick={() => handleSelectOption(opt)}
                    disabled={submitting || isSkipping}
                  >
                    {isSelected && submitting ? 'Cargando...' : opt}
                  </button>
                )
              })}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
