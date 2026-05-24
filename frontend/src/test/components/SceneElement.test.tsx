import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import SceneElement from '../../components/scene/SceneElement/SceneElement'

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

  it('renderiza imagen y tooltip con el titulo', () => {
    render(
      <MemoryRouter>
        <SceneElement {...baseProps} to="/profile" />
      </MemoryRouter>
    )

    expect(screen.getByAltText('Casa de perfil')).toBeInTheDocument()
    expect(screen.getByText('Perfil')).toBeInTheDocument()
  })

  it('renderiza un link cuando recibe destino', () => {
    render(
      <MemoryRouter>
        <SceneElement {...baseProps} to="/profile" />
      </MemoryRouter>
    )

    expect(screen.getByLabelText('Perfil').closest('a')).toHaveAttribute('href', '/profile')
  })

  it('renderiza un button cuando no recibe destino', () => {
    render(
      <MemoryRouter>
        <SceneElement {...baseProps} />
      </MemoryRouter>
    )

    expect(screen.getByRole('button', { name: 'Perfil' })).toBeInTheDocument()
  })

  it('usa la imagen dark cuando el theme es dark', () => {
    render(
      <MemoryRouter>
        <SceneElement {...baseProps} theme="dark" />
      </MemoryRouter>
    )

    expect(screen.getByAltText('Casa de perfil')).toHaveAttribute('src', '/dark-house.webp')
  })

  it('desactiva navegacion cuando interactive es false', () => {
    render(
      <MemoryRouter>
        <SceneElement {...baseProps} to="/profile" interactive={false} />
      </MemoryRouter>
    )

    expect(screen.getByRole('button', { name: 'Perfil' })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: 'Perfil' })).not.toBeInTheDocument()
  })
})
