import { useDialogFocusTrap } from '../../../hooks/useDialogFocusTrap'
import './HomeOnboarding.css'
import HomeOnboardingIntro from './HomeOnboardingIntro'
import HomeOnboardingStep from './HomeOnboardingStep'
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
  const { dialogRef, handleDialogKeyDown } = useDialogFocusTrap({
    focusKey: `${mode}:${currentStepIndex}`,
  })

  if (mode === 'intro') {
    return (
      <div
        ref={dialogRef}
        className="home-onboarding"
        role="dialog"
        aria-modal="true"
        aria-labelledby="home-onboarding-title"
        aria-describedby="home-onboarding-subtitle"
        tabIndex={-1}
        onKeyDown={handleDialogKeyDown}
      >
        <div className="home-onboarding__overlay" />
        <HomeOnboardingIntro onStart={onStart} />
      </div>
    )
  }

  const step = steps[currentStepIndex]

  return (
    <div
      ref={dialogRef}
      className="home-onboarding"
      role="dialog"
      aria-modal="true"
      aria-labelledby={`home-onboarding-step-title-${step.id}`}
      aria-describedby={`home-onboarding-step-description-${step.id}`}
      tabIndex={-1}
      onKeyDown={handleDialogKeyDown}
    >
      <HomeOnboardingStep
        currentStepIndex={currentStepIndex}
        onAdvance={onAdvance}
        sceneElements={sceneElements}
        step={step}
        theme={theme}
        totalSteps={steps.length}
      />
    </div>
  )
}
