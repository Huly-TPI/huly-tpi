import brandLogoImage from '../../../assets/brand/color-logo.webp'
import Button from '../../Buttons/Button/Button'
import type { HomeOnboardingIntroProps } from './types'

export default function HomeOnboardingIntro({
  kicker = 'Bienvenido a',
  showBrand = true,
  title = 'Tu espacio para cuidar tus pensamientos',
  subtitle = 'Vamos a descubrir el jardín y todo lo que podés hacer en cada rincón',
  startLabel = 'Comenzar con el tutorial',
  onStart,
}: HomeOnboardingIntroProps) {
  return (
    <div className="home-onboarding__intro">
      <div className="home-onboarding__intro-card">
        {showBrand ? (
          <>
            <p className="home-onboarding__intro-kicker">{kicker}</p>
            <img
              src={brandLogoImage}
              alt="Huly"
              className="home-onboarding__intro-logo"
            />
          </>
        ) : null}
        <h1 id="home-onboarding-title" className="home-onboarding__intro-title">
          {title}
        </h1>
        <p id="home-onboarding-subtitle" className="home-onboarding__intro-subtitle">{subtitle}</p>
        <div className="home-onboarding__intro-actions">
          <Button onClick={onStart}>
            {startLabel}
          </Button>
        </div>
      </div>
    </div>
  )
}
