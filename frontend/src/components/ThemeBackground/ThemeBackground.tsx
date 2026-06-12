import { useTheme } from '../../context/theme'
import './ThemeBackground.css'

interface ThemeBackgroundProps {
  lightSrc: string
  darkSrc: string
  lightAlt: string
  darkAlt: string
  lightMobileSrc?: string
  darkMobileSrc?: string
}

export default function ThemeBackground({
  lightSrc,
  darkSrc,
  lightAlt,
  darkAlt,
  lightMobileSrc,
  darkMobileSrc,
}: ThemeBackgroundProps) {
  const { theme } = useTheme()
  const mobileLightSrc = lightMobileSrc
  const mobileDarkSrc = darkMobileSrc

  if (mobileLightSrc && mobileDarkSrc) {
    return (
      <>
        <ThemeBackgroundImage src={lightSrc} alt={lightAlt} active={theme === 'light'} variant="desktop" />
        <ThemeBackgroundImage src={mobileLightSrc} alt={`${lightAlt} para celular`} active={theme === 'light'} variant="mobile" />
        <ThemeBackgroundImage src={darkSrc} alt={darkAlt} active={theme === 'dark'} variant="desktop" />
        <ThemeBackgroundImage src={mobileDarkSrc} alt={`${darkAlt} para celular`} active={theme === 'dark'} variant="mobile" />
      </>
    )
  }

  return (
    <>
      <ThemeBackgroundImage src={lightSrc} alt={lightAlt} active={theme === 'light'} />
      <ThemeBackgroundImage src={darkSrc} alt={darkAlt} active={theme === 'dark'} />
    </>
  )
}

interface ThemeBackgroundImageProps {
  src: string
  alt: string
  active: boolean
  variant?: 'desktop' | 'mobile'
}

function ThemeBackgroundImage({ src, alt, active, variant }: ThemeBackgroundImageProps) {
  const variantClassName = variant ? ` theme-background--${variant}` : ''

  return (
    <img
      src={src}
      alt={active ? alt : ''}
      aria-hidden={!active}
      className={`theme-background${variantClassName} ${active ? 'theme-background--active' : ''}`}
    />
  )
}
