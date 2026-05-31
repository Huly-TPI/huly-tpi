import brandLogoImage from '../../../assets/brand/color-logo.webp'
import Button from '../../Buttons/Button/Button'
import type { HomeOnboardingIntroProps } from './types'

export default function HomeOnboardingIntro({ onStart }: HomeOnboardingIntroProps) {
  return (
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
          Vamos a descubrir el jardín y todo lo que podés hacer en cada rincón
        </p>
        <div className="home-onboarding__intro-actions">
          <Button onClick={onStart}>
            Comenzar con el tutorial
          </Button>
        </div>
      </div>
    </div>
  )
}
