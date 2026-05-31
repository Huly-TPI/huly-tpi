import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import HomeOnboardingStep from '../../components/Onboarding/HomeOnboarding/HomeOnboardingStep'
import type { HomeOnboardingStep as HomeOnboardingStepDefinition } from '../../components/Onboarding/HomeOnboarding/types'
import type { SceneElementDefinition } from '../../components/Scene/types'

const baseElement: SceneElementDefinition = {
  id: 'notebook',
  title: 'Diario',
  imageAlt: 'Banco con cuaderno',
  image: {
    light: '/light-notebook.webp',
    dark: '/dark-notebook.webp',
  },
  imageClassName: 'w-full',
  imageVariantClassName: 'scene-element__image--mirror-mobile',
  placementClassName: 'left-0 top-0',
  hotspotClassName: 'left-0 top-0 h-10 w-10',
  clipPath: 'polygon(0 0, 100% 0, 100% 100%, 0 100%)',
  tooltipClassName: 'bottom-full mb-2',
}

describe('HomeOnboardingStep', () => {
  it('renderiza mascota anclada, usa imagen dark y avanza al clickear', async () => {
    const user = userEvent.setup()
    const onAdvance = vi.fn()
    const step: HomeOnboardingStepDefinition = {
      id: 'notebook',
      title: 'Diario emocional',
      description: 'Escribí, reflexioná y registrá cómo te sentiste en tu día',
      icon: 'D',
      elementIds: ['notebook'],
      cardClassName: 'left-0 top-0',
      mascot: {
        imageSrc: '/mascot-notebook.webp',
        imageAlt: 'Mascota escribiendo',
        anchorElementId: 'notebook',
        placementClassName: 'right-0 top-0 w-10',
        imageClassName: 'w-full',
      },
    }

    const { container } = render(
      <HomeOnboardingStep
        currentStepIndex={0}
        onAdvance={onAdvance}
        sceneElements={[baseElement]}
        step={step}
        theme="dark"
        totalSteps={2}
      />,
    )

    expect(screen.getByRole('heading', { name: 'Diario emocional' })).toBeInTheDocument()
    expect(screen.getByText('Escribí, reflexioná y registrá cómo te sentiste en tu día')).toBeInTheDocument()
    expect(screen.getByAltText('Mascota escribiendo')).toBeInTheDocument()
    expect(container.querySelector('.scene-element__image--mirror-mobile')).toBeInTheDocument()

    const highlightedImage = container.querySelector('.home-onboarding__highlight-image') as HTMLImageElement | null
    expect(highlightedImage?.getAttribute('src')).toBe('/dark-notebook.webp')

    await user.click(screen.getByRole('button', { name: 'Siguiente' }))

    expect(onAdvance).toHaveBeenCalledTimes(1)
  })

  it('renderiza mascota global cuando no tiene anchorElementId', () => {
    const step: HomeOnboardingStepDefinition = {
      id: 'clouds',
      title: 'Respiraciones guiadas',
      description: 'Tomá unos minutos para frenar, respirar y bajar un cambio',
      icon: 'N',
      elementIds: ['cloud-1'],
      cardClassName: 'left-0 top-0',
      mascot: {
        imageSrc: '/mascot-clouds.webp',
        imageAlt: 'Mascota respirando',
        placementClassName: 'left-0 top-0 w-10',
        imageClassName: 'w-full',
      },
    }

    const { container } = render(
      <HomeOnboardingStep
        currentStepIndex={0}
        onAdvance={vi.fn()}
        sceneElements={[
          {
            ...baseElement,
            id: 'cloud-1',
            image: { light: '/cloud.webp' },
          },
        ]}
        step={step}
        theme="light"
        totalSteps={1}
      />,
    )

    expect(screen.getByAltText('Mascota respirando')).toBeInTheDocument()
    expect(container.querySelectorAll('.home-onboarding__mascot')).toHaveLength(1)
    expect(screen.getByRole('button', { name: 'Finalizar tutorial' })).toBeInTheDocument()
  })
})
