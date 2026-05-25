import brandLogoImage from '../../../assets/brand/color-logo.webp'
import Button from '../../buttons/Button/Button'
import './HomeOnboarding.css'
import type { HomeOnboardingProps } from './types'

export default function HomeOnboarding({
  mode,
  theme,
  sceneElements,
  steps,
  currentStepIndex,
  onStart,
  onAdvance,
}: HomeOnboardingProps) {
  if (mode === 'intro') {
    return (
      <div
        className="home-onboarding"
        role="dialog"
        aria-modal="true"
        aria-labelledby="home-onboarding-title"
        aria-describedby="home-onboarding-subtitle"
      >
        <div className="home-onboarding__overlay" />
        <div className="home-onboarding__intro">
          <div className="home-onboarding__intro-card">
            <p className="home-onboarding__intro-kicker">Bienvenido a</p>
            <img
              src={brandLogoImage}
              alt="Huly"
              className="home-onboarding__intro-logo"
            />
            <h1 id="home-onboarding-title" className="home-onboarding__intro-title">
              Tu espacio para cuidar tus pensamientos
            </h1>
            <p id="home-onboarding-subtitle" className="home-onboarding__intro-subtitle">
              Vamos a descubrir el jardin y todo lo que podes hacer en cada rincon.
            </p>
            <div className="home-onboarding__intro-actions">
              <Button onClick={onStart}>
                Comenzar con el tutorial
              </Button>
            </div>
          </div>
        </div>
      </div>
    )
  }

  const step = steps[currentStepIndex]
  const highlightedElements = sceneElements.filter(element => step.elementIds.includes(element.id))
  const isLastStep = currentStepIndex === steps.length - 1

  return (
    <div
      className="home-onboarding"
      role="dialog"
      aria-modal="true"
      aria-labelledby={`home-onboarding-step-title-${step.id}`}
      aria-describedby={`home-onboarding-step-description-${step.id}`}
    >
      <div className="home-onboarding__overlay" />

      {highlightedElements.map(element => {
        const imageSrc = theme === 'dark' && element.image.dark ? element.image.dark : element.image.light

        return (
          <div
            key={element.id}
            className={`home-onboarding__highlight ${element.placementClassName}`}
            aria-hidden="true"
          >
            <img
              src={imageSrc}
              alt=""
              className={`home-onboarding__highlight-image pointer-events-none select-none ${element.imageVariantClassName ?? ''} ${element.imageClassName}`}
            />
          </div>
        )
      })}

      <div className={`home-onboarding__step-card ${step.cardClassName}`}>
        <div className="home-onboarding__step-card-inner">
          <div className="home-onboarding__step-icon" aria-hidden="true">
            {step.icon}
          </div>
          <div>
            <h2 id={`home-onboarding-step-title-${step.id}`} className="home-onboarding__step-title">
              {step.title}
            </h2>
            <p
              id={`home-onboarding-step-description-${step.id}`}
              className="home-onboarding__step-description"
            >
              {step.description}
            </p>
          </div>
        </div>
      </div>
      <div className="home-onboarding__step-actions">
        <Button onClick={onAdvance}>
          {isLastStep ? 'Finalizar tutorial' : 'Siguiente'}
        </Button>
      </div>
    </div>
  )
}
