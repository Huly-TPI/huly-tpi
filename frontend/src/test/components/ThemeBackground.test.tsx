import { render, screen } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'

const mockUseTheme = vi.fn()

vi.mock('../../context/theme', () => ({
  useTheme: () => mockUseTheme(),
}))

describe('ThemeBackground', () => {
  beforeEach(() => {
    mockUseTheme.mockReturnValue({ theme: 'light' })
  })

  it('activa el fondo claro y oculta el nocturno en modo día', () => {
    render(
      <ThemeBackground
        lightSrc="day.webp"
        darkSrc="night.webp"
        lightAlt="Fondo de día"
        darkAlt="Fondo nocturno"
      />,
    )

    const activeBackground = screen.getByAltText('Fondo de día')
    const hiddenBackground = screen.getByRole('presentation', { hidden: true })

    expect(activeBackground).toHaveAttribute('src', 'day.webp')
    expect(activeBackground).toHaveClass('theme-background--active')
    expect(hiddenBackground).toHaveAttribute('src', 'night.webp')
    expect(hiddenBackground).toHaveAttribute('aria-hidden', 'true')
  })

  it('activa el fondo nocturno y oculta el claro en modo noche', () => {
    mockUseTheme.mockReturnValue({ theme: 'dark' })

    render(
      <ThemeBackground
        lightSrc="day.webp"
        darkSrc="night.webp"
        lightAlt="Fondo de día"
        darkAlt="Fondo nocturno"
      />,
    )

    const activeBackground = screen.getByAltText('Fondo nocturno')
    const hiddenBackground = screen.getByRole('presentation', { hidden: true })

    expect(activeBackground).toHaveAttribute('src', 'night.webp')
    expect(activeBackground).toHaveClass('theme-background--active')
    expect(hiddenBackground).toHaveAttribute('src', 'day.webp')
    expect(hiddenBackground).toHaveAttribute('aria-hidden', 'true')
  })

  it('renderiza variantes mobile y desktop cuando recibe fondos mobile', () => {
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

    expect(screen.getByAltText('Fondo de día')).toHaveClass('theme-background--desktop')
    expect(screen.getByAltText('Fondo de día para celular')).toHaveClass('theme-background--mobile')

    const hiddenBackgrounds = screen.getAllByRole('presentation', { hidden: true })
    expect(hiddenBackgrounds).toHaveLength(2)
    expect(hiddenBackgrounds[0]).toHaveAttribute('src', 'night-desktop.webp')
    expect(hiddenBackgrounds[1]).toHaveAttribute('src', 'night-mobile.webp')
  })
})
