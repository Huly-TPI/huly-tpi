import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import SceneElement from '../../components/Scene/SceneElement/SceneElement'
import { verifyTextPresent } from '../testHelpers'


describe('SceneElement', () => {
  const baseProps = {
    id: 'house',
    title: 'Perfil',
    imageAlt: 'Casa de perfil',
    image: {
      light: '/light-house.webp',
      dark: '/dark-house.webp',
    },
    imageClassName: 'w-full',
    placementClassName: 'left-0 top-0',
    hotspotClassName: 'left-0 top-0 h-10 w-10',
    clipPath: 'polygon(0 0, 100% 0, 100% 100%, 0 100%)',
    tooltipClassName: 'bottom-full mb-2',
  }

  it('renderiza la imagen y el tooltip con el título', () => {
    renderWithRouterAndProps({ to: '/profile' })
    verifyAltTextPresent('Casa de perfil')
    verifyTextPresent('Perfil')
  })

  it('renderiza un enlace cuando recibe un destino', () => {
    renderWithRouterAndProps({ to: '/profile' })
    verifyLinkDestination('Perfil', '/profile')
  })

  it('renderiza un botón cuando no recibe un destino', () => {
    renderWithRouterAndProps({})
    verifyButtonWithNamePresent('Perfil')
  })

  it('usa la imagen oscura cuando el tema es oscuro', () => {
    renderWithRouterAndProps({ theme: 'dark' })
    verifyImageSrc('Casa de perfil', '/dark-house.webp')
  })

  it('aplica la variante visual adicional cuando se especifica', () => {
    renderWithRouterAndProps({
      imageAlt: 'Banco espejado',
      imageVariantClassName: 'scene-element__image--mirror-mobile',
    })
    verifyImageHasClass('Banco espejado', 'scene-element__image--mirror-mobile')
  })

  it('desactiva la navegación cuando la propiedad interactiva es falsa', () => {
    renderWithRouterAndProps({ to: '/profile', interactive: false })
    verifyButtonWithNamePresent('Perfil')
    verifyLinkWithNameNotPresent('Perfil')
  })

  /* helpers */

  const renderWithRouterAndProps = (props: Record<string, any>) => {
    render(
      <MemoryRouter>
        <SceneElement {...baseProps} {...props} />
      </MemoryRouter>
    )
  }

  const verifyAltTextPresent = (alt: string) => {
    expect(screen.getByAltText(alt)).toBeInTheDocument()
  }

  const verifyLinkDestination = (label: string, href: string) => {
    expect(screen.getByLabelText(label).closest('a')).toHaveAttribute('href', href)
  }

  const verifyButtonWithNamePresent = (name: string) => {
    expect(screen.getByRole('button', { name })).toBeInTheDocument()
  }

  const verifyImageSrc = (alt: string, src: string) => {
    expect(screen.getByAltText(alt)).toHaveAttribute('src', src)
  }

  const verifyImageHasClass = (alt: string, className: string) => {
    expect(screen.getByAltText(alt)).toHaveClass(className)
  }

  const verifyLinkWithNameNotPresent = (name: string) => {
    expect(screen.queryByRole('link', { name })).not.toBeInTheDocument()
  }
})
