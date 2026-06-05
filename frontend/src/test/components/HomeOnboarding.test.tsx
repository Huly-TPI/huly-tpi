import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import HomeOnboarding from '../../components/Onboarding/HomeOnboarding/HomeOnboarding'
import type { HomeOnboardingStep } from '../../components/Onboarding/HomeOnboarding/types'
import type { SceneElementDefinition } from '../../components/Scene/types'

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
  it('renderiza el modo intro como dialog y dispara onStart', async () => {
    const user = userEvent.setup()
    const onStart = vi.fn()

    render(
      <HomeOnboarding
        mode="intro"
        theme="light"
        sceneElements={sceneElements}
        steps={steps}
        currentStepIndex={0}
        onStart={onStart}
        onAdvance={vi.fn()}
      />,
    )

    expect(screen.getByRole('dialog')).toHaveAttribute('aria-modal', 'true')

    await user.click(screen.getByRole('button', { name: 'Comenzar con el tutorial' }))

    expect(onStart).toHaveBeenCalledTimes(1)
  })

  it('renderiza el modo step con aria-labelledby y CTA de avance', async () => {
    const user = userEvent.setup()
    const onAdvance = vi.fn()

    render(
      <HomeOnboarding
        mode="steps"
        theme="light"
        sceneElements={sceneElements}
        steps={steps}
        currentStepIndex={0}
        onStart={vi.fn()}
        onAdvance={onAdvance}
      />,
    )

    const dialog = screen.getByRole('dialog')
    expect(dialog).toHaveAttribute('aria-labelledby', 'home-onboarding-step-title-house')
    expect(screen.getByRole('heading', { name: 'Casa' })).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Finalizar tutorial' }))

    expect(onAdvance).toHaveBeenCalledTimes(1)
  })
})
