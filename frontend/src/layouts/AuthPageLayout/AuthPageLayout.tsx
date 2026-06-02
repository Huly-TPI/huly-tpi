import type { ReactNode } from 'react'
import background from '../../assets/register/background.webp'
import defaultCharacter from '../../assets/register/huly.webp'
import cardFrame from '../../assets/register/cardFrame.webp'

interface AuthPageLayoutProps {
  children: ReactNode
  reversed?: boolean
  characterImage?: string
  cardHeight?: string
}

export default function AuthPageLayout({ children, reversed = false, characterImage, cardHeight }: AuthPageLayoutProps) {
  const character = characterImage ?? defaultCharacter
  return (
    <div
      className="relative h-full flex items-center justify-center overflow-hidden"
      style={{
        backgroundImage: `url(${background})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }}
    >
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
            backgroundImage: `url(${cardFrame})`,
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
