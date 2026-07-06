import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import HomeOnboardingIntro from '../../components/Onboarding/HomeOnboarding/HomeOnboardingIntro'
import { clickButton, verifyTextPresent, verifyTextNotPresent } from '../testHelpers'


describe('HomeOnboardingIntro', () => {
  let onStartSpy: any

  it('renderiza el contenido de introducción y llama a onStart al comenzar', () => {
    renderWithStartSpy()
    verifyIntroductionContent()
    return clickTutorialStartButton().then(() => {
      verifyOnStartCalledTimes(1)
    })
  })

  it('puede ocultar el kicker y el logo de la marca', () => {
    renderWithoutBrand()
    verifyKickerAndLogoNotPresent()
  })
  let user: any

  /* helpers */

  const renderWithStartSpy = () => {
    user = userEvent.setup()
    onStartSpy = vi.fn()
    render(<HomeOnboardingIntro onStart={onStartSpy} />)
  }

  const renderWithoutBrand = () => {
    render(<HomeOnboardingIntro showBrand={false} onStart={vi.fn()} />)
  }

  const verifyIntroductionContent = () => {
    verifyTextPresent('Bienvenido a')
    expect(screen.getByRole('heading', { name: 'Tu espacio para cuidar tus pensamientos' })).toBeInTheDocument()
    verifyTextPresent('Vamos a descubrir el jardín y todo lo que podés hacer en cada rincón')
  }

  const clickTutorialStartButton = () => {
    return clickButton(user, 'Comenzar con el tutorial')
  }

  const verifyOnStartCalledTimes = (times: number) => {
    expect(onStartSpy).toHaveBeenCalledTimes(times)
  }

  const verifyKickerAndLogoNotPresent = () => {
    verifyTextNotPresent('Bienvenido a')
    expect(screen.queryByRole('img', { name: 'Huly' })).not.toBeInTheDocument()
  }
})
