import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import HomeOnboarding from '../../components/Onboarding/HomeOnboarding/HomeOnboarding'
import type { HomeOnboardingStep } from '../../components/Onboarding/HomeOnboarding/types'
import type { SceneElementDefinition } from '../../components/Scene/types'
import { clickButton, verifyHeadingPresent } from '../testHelpers'

vi.mock('../../hooks/useDialogFocusTrap', () => ({
  useDialogFocusTrap: () => ({
    dialogRef: { current: null },
    handleDialogKeyDown: vi.fn(),
  }),
}))

const sceneElements: SceneElementDefinition[] = [
  {
    id: 'house',
    title: 'Perfil',
    imageAlt: 'Casa',
    image: { light: '/house-light.webp', dark: '/house-dark.webp' },
    imageClassName: 'w-full',
    placementClassName: 'left-0 top-0',
    hotspotClassName: 'left-0 top-0 h-10 w-10',
    clipPath: 'polygon(0 0, 100% 0, 100% 100%, 0 100%)',
    tooltipClassName: 'bottom-full mb-2',
  },
]

const steps: HomeOnboardingStep[] = [
  {
    id: 'house',
    title: 'Casa',
    description: 'Acá vas a encontrar tu perfil, configuración y tu espacio personal',
    icon: 'C',
    elementIds: ['house'],
    cardClassName: 'left-0 top-0',
  },
]

describe('HomeOnboarding', () => {
  let onStartSpy: any
  let onAdvanceSpy: any

  it('renderiza el modo de introducción como diálogo y llama a onStart al comenzar', () => {
    renderIntroMode()
    verifyDialogIsModal()
    return clickTutorialStartButton().then(() => {
      verifyOnStartCalledTimes(1)
    })
  })

  it('renderiza el modo de pasos con aria-labelledby y llama a onAdvance al avanzar', () => {
    renderStepsMode()
    verifyDialogLabelledBy('home-onboarding-step-title-house')
    verifyHeadingPresent('Casa')
    return clickTutorialFinishButton().then(() => {
      verifyOnAdvanceCalledTimes(1)
    })
  })
  let user: any

  /* helpers */

  const renderIntroMode = () => {
    user = userEvent.setup()
    onStartSpy = vi.fn()
    render(
      <HomeOnboarding
        mode="intro"
        theme="light"
        sceneElements={sceneElements}
        steps={steps}
        currentStepIndex={0}
        onStart={onStartSpy}
        onAdvance={vi.fn()}
      />,
    )
  }

  const renderStepsMode = () => {
    user = userEvent.setup()
    onAdvanceSpy = vi.fn()
    render(
      <HomeOnboarding
        mode="steps"
        theme="light"
        sceneElements={sceneElements}
        steps={steps}
        currentStepIndex={0}
        onStart={vi.fn()}
        onAdvance={onAdvanceSpy}
      />,
    )
  }

  const verifyDialogIsModal = () => {
    expect(screen.getByRole('dialog')).toHaveAttribute('aria-modal', 'true')
  }

  const clickTutorialStartButton = () => {
    return clickButton(user, 'Comenzar con el tutorial')
  }

  const verifyOnStartCalledTimes = (times: number) => {
    expect(onStartSpy).toHaveBeenCalledTimes(times)
  }

  const verifyDialogLabelledBy = (id: string) => {
    expect(screen.getByRole('dialog')).toHaveAttribute('aria-labelledby', id)
  }

  

  const clickTutorialFinishButton = () => {
    return clickButton(user, 'Finalizar tutorial')
  }

  const verifyOnAdvanceCalledTimes = (times: number) => {
    expect(onAdvanceSpy).toHaveBeenCalledTimes(times)
  }
})
