import type { ReactNode } from 'react'
import background from '../../assets/register/background.webp'
import backgroundNight from '../../assets/register/background-night.webp'
import defaultCharacter from '../../assets/register/huly.webp'
import cardFrame from '../../assets/register/cardFrame.webp'
import cardFrameLogin from '../../assets/register/cardFrame-login.webp'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'
import BackButton from '../../components/Buttons/BackButton/BackButton'

interface AuthPageLayoutProps {
  children: ReactNode
  reversed?: boolean
  characterImage?: string
  cardHeight?: string
  variant?: 'default' | 'login'
}

export default function AuthPageLayout({ children, reversed = false, characterImage, cardHeight, variant = 'default' }: AuthPageLayoutProps) {
  const character = characterImage ?? defaultCharacter
  const frame = variant === 'login' ? cardFrameLogin : cardFrame
  return (
    <div className="relative h-full flex items-center justify-center overflow-hidden">
      <BackButton to="/" label="Volver al jardín" />
      <ThemeBackground
        lightSrc={background}
        darkSrc={backgroundNight}
        lightAlt="Fondo de autenticación"
        darkAlt="Fondo nocturno de autenticación"
      />
      <div className="absolute inset-0 backdrop-blur-md" />

      <div className={`relative z-10 flex w-full max-w-5xl items-center justify-center gap-12 px-4 md:px-10 pt-0 pb-8 ${reversed ? 'flex-row-reverse' : ''}`}>
        <img
          src={character}
          alt="Mascota de Huly"
          className="hidden lg:block h-[360px] object-contain self-end mb-6"
        />

        <div
          className="relative w-full max-w-[calc(100%-2rem)] sm:max-w-md"
          style={{
            backgroundImage: `url(${frame})`,
            backgroundSize: '100% 100%',
            backgroundRepeat: 'no-repeat',
            height: cardHeight,
          }}
        >
          <div className="h-full overflow-y-auto px-8 py-8 md:px-14 md:py-10">
            {children}
          </div>
        </div>
      </div>
    </div>
  )
}
