import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'

const mockUseTheme = vi.fn()

vi.mock('../../context/theme', () => ({
  useTheme: () => mockUseTheme(),
}))

describe('ThemeBackground', () => {
  beforeEach(() => {
    setupThemeMock('light')
  })

  it('activa el fondo claro y oculta el nocturno en modo día', () => {
    renderDefaultBackground()
    verifyActiveBackground('Fondo de día', 'day.webp')
    verifyHiddenBackground('night.webp')
  })

  it('activa el fondo nocturno y oculta el claro en modo noche', () => {
    setupThemeMock('dark')
    renderDefaultBackground()
    verifyActiveBackground('Fondo nocturno', 'night.webp')
    verifyHiddenBackground('day.webp')
  })

  it('renderiza variantes mobile y desktop cuando recibe fondos mobile', () => {
    renderMobileBackground()
    verifyBackgroundClass('Fondo de día', 'theme-background--desktop')
    verifyBackgroundClass('Fondo de día para celular', 'theme-background--mobile')
    verifyHiddenBackgroundsCount(2)
    verifyHiddenBackgroundsSrc(['night-desktop.webp', 'night-mobile.webp'])
  })

  /* helpers */

  const setupThemeMock = (theme: string) => {
    mockUseTheme.mockReturnValue({ theme })
  }

  const renderDefaultBackground = () => {
    render(
      <ThemeBackground
        lightSrc="day.webp"
        darkSrc="night.webp"
        lightAlt="Fondo de día"
        darkAlt="Fondo nocturno"
      />,
    )
  }

  const renderMobileBackground = () => {
    render(
      <ThemeBackground
        lightSrc="day-desktop.webp"
        darkSrc="night-desktop.webp"
        lightAlt="Fondo de día"
        darkAlt="Fondo nocturno"
        lightMobileSrc="day-mobile.webp"
        darkMobileSrc="night-mobile.webp"
      />,
    )
  }

  const verifyActiveBackground = (altText: string, src: string) => {
    const activeBackground = screen.getByAltText(altText)
    expect(activeBackground).toHaveAttribute('src', src)
    expect(activeBackground).toHaveClass('theme-background--active')
  }

  const verifyHiddenBackground = (src: string) => {
    const hiddenBackground = screen.getByRole('presentation', { hidden: true })
    expect(hiddenBackground).toHaveAttribute('src', src)
    expect(hiddenBackground).toHaveAttribute('aria-hidden', 'true')
  }

  const verifyBackgroundClass = (altText: string, className: string) => {
    expect(screen.getByAltText(altText)).toHaveClass(className)
  }

  const verifyHiddenBackgroundsCount = (count: number) => {
    expect(screen.getAllByRole('presentation', { hidden: true })).toHaveLength(count)
  }

  const verifyHiddenBackgroundsSrc = (srcs: string[]) => {
    const hiddenBackgrounds = screen.getAllByRole('presentation', { hidden: true })
    srcs.forEach((src, idx) => {
      expect(hiddenBackgrounds[idx]).toHaveAttribute('src', src)
    })
  }
})
