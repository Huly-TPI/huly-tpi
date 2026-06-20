import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import HomeOnboardingIntro from '../../components/Onboarding/HomeOnboarding/HomeOnboardingIntro'

describe('HomeOnboardingIntro', () => {
  it('renderiza el contenido introductorio y dispara onStart', async () => {
    const user = userEvent.setup()
    const onStart = vi.fn()

    render(<HomeOnboardingIntro onStart={onStart} />)

    expect(screen.getByText('Bienvenido a')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Tu espacio para cuidar tus pensamientos' })).toBeInTheDocument()
    expect(screen.getByText('Vamos a descubrir el jardín y todo lo que podés hacer en cada rincón')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Comenzar con el tutorial' }))

    expect(onStart).toHaveBeenCalledTimes(1)
  })

  it('puede ocultar el kicker y el logo de marca', () => {
    render(<HomeOnboardingIntro showBrand={false} onStart={vi.fn()} />)

    expect(screen.queryByText('Bienvenido a')).not.toBeInTheDocument()
    expect(screen.queryByRole('img', { name: 'Huly' })).not.toBeInTheDocument()
  })
})
