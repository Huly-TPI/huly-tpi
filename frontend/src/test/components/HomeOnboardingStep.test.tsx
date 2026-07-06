import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import HomeOnboardingStep from '../../components/Onboarding/HomeOnboarding/HomeOnboardingStep'
import type { HomeOnboardingStep as HomeOnboardingStepDefinition } from '../../components/Onboarding/HomeOnboarding/types'
import type { SceneElementDefinition } from '../../components/Scene/types'
import { clickButton, verifyTextPresent, verifyHeadingPresent } from '../testHelpers'


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
  let onAdvanceSpy: any

  it('renderiza la mascota anclada, usa imagen oscura y avanza al hacer click', () => {
    renderWithAnchoredMascotAndDarkTheme()
    verifyHeadingPresent('Diario emocional')
    verifyDescriptionPresent('Escribí, reflexioná y registrá cómo te sentiste en tu día')
    verifyMascotAltText('Mascota escribiendo')
    verifyImageMirrorClassPresent()
    verifyHighlightedImageSrc('/dark-notebook.webp')
    return clickNextButton().then(() => {
      verifyOnAdvanceCalledTimes(1)
    })
  })

  it('renderiza la mascota global cuando no tiene identificador de anclaje', () => {
    renderWithGlobalMascotAndLightTheme()
    verifyMascotAltText('Mascota respirando')
    verifyMascotCount(1)
    verifyFinishButtonPresent()
  })
  let user: any
  let renderResult: any

  /* helpers */

  const renderWithAnchoredMascotAndDarkTheme = () => {
    user = userEvent.setup()
    onAdvanceSpy = vi.fn()
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
    renderResult = render(
      <HomeOnboardingStep
        currentStepIndex={0}
        onAdvance={onAdvanceSpy}
        sceneElements={[baseElement]}
        step={step}
        theme="dark"
        totalSteps={2}
      />,
    )
  }

  const renderWithGlobalMascotAndLightTheme = () => {
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
    renderResult = render(
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
  }

  

  const verifyDescriptionPresent = (text: string) => {
    verifyTextPresent(text)
  }

  const verifyMascotAltText = (alt: string) => {
    expect(screen.getByAltText(alt)).toBeInTheDocument()
  }

  const verifyImageMirrorClassPresent = () => {
    expect(renderResult.container.querySelector('.scene-element__image--mirror-mobile')).toBeInTheDocument()
  }

  const verifyHighlightedImageSrc = (src: string) => {
    const highlightedImage = renderResult.container.querySelector('.home-onboarding__highlight-image') as HTMLImageElement | null
    expect(highlightedImage?.getAttribute('src')).toBe(src)
  }

  const clickNextButton = () => {
    return clickButton(user, 'Siguiente')
  }

  const verifyOnAdvanceCalledTimes = (times: number) => {
    expect(onAdvanceSpy).toHaveBeenCalledTimes(times)
  }

  const verifyMascotCount = (count: number) => {
    expect(renderResult.container.querySelectorAll('.home-onboarding__mascot')).toHaveLength(count)
  }

  const verifyFinishButtonPresent = () => {
    expect(screen.getByRole('button', { name: 'Finalizar tutorial' })).toBeInTheDocument()
  }
})
