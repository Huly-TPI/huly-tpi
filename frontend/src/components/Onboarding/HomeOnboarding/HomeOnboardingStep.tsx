import Button from '../../Buttons/Button/Button'
import type { HomeOnboardingStepProps } from './types'

export default function HomeOnboardingStep({
  currentStepIndex,
  onAdvance,
  sceneElements,
  step,
  theme,
  totalSteps,
}: HomeOnboardingStepProps) {
  const highlightedElements = sceneElements.filter(element => step.elementIds.includes(element.id))
  const isLastStep = currentStepIndex === totalSteps - 1
  const mascotAnchorElementId = step.mascot?.anchorElementId
  const globalMascot = step.mascot && !step.mascot.anchorElementId ? step.mascot : null

  return (
    <>
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
              className={`home-onboarding__highlight-image scene-element__image pointer-events-none select-none ${element.imageVariantClassName ?? ''} ${element.imageClassName}`}
            />

            {step.mascot && mascotAnchorElementId === element.id ? (
              <div className={`home-onboarding__mascot ${step.mascot.placementClassName}`}>
                <img
                  src={step.mascot.imageSrc}
                  alt={step.mascot.imageAlt ?? ''}
                  className={`home-onboarding__mascot-image pointer-events-none select-none ${step.mascot.imageClassName}`}
                  aria-hidden={step.mascot.imageAlt ? undefined : 'true'}
                />
              </div>
            ) : null}
          </div>
        )
      })}

      {globalMascot ? (
        <div
          className={`home-onboarding__mascot ${globalMascot.placementClassName}`}
          aria-hidden={globalMascot.imageAlt ? undefined : 'true'}
        >
          <img
            src={globalMascot.imageSrc}
            alt={globalMascot.imageAlt ?? ''}
            className={`home-onboarding__mascot-image pointer-events-none select-none ${globalMascot.imageClassName}`}
          />
        </div>
      ) : null}

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
    </>
  )
}
