import { clearAllMocks } from '../testHelpers'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import treeBlueLight from '../../assets/garden/light-theme/tree-blue.webp'
import Minigames from '../../pages/Minigames/Minigames'
import { ThemeProvider } from '../../context/theme'

const mockUseInventory = vi.fn()

vi.mock('../../hooks/store/useInventory', () => ({
  useInventory: () => mockUseInventory(),
}))

describe('Minigames', () => {
  let user: ReturnType<typeof userEvent.setup>
  let activeContainer: HTMLElement

  beforeEach(() => {
    clearAllMocks()
    setupDefaultInventory()
  })

  it('renderiza los fondos de escena desktop y mobile', () => {
    renderMinigamesPage()
    verifyDesktopAndMobileBackgroundsVisible()
  })

  it('renderiza las nubes como elementos decorativos sin links', () => {
    renderMinigamesPage()
    verifyDecorativeCloudsExistWithoutLinks()
  })

  it('renderiza los faroles navegables hacia la actividad de farolitos', () => {
    renderMinigamesPage()
    verifyLanternLinksNavigateTo('/lanterns')
  })

  it('renderiza los hotspots de cada minijuego con su ruta', () => {
    renderMinigamesPage()
    verifyHotspotLinkTarget('Burbujas', '/bubbles')
    verifyHotspotLinkTarget('Colorear mandalas', '/mandalas')
    verifyHotspotLinkTarget('Arena zen', '/zen-sand-garden')
    verifyHotspotLinkTarget('Piedras del lago', '/stones')
  })

  it('redirige a piedras al hacer click en el hotspot de las piedras', () => {
    renderMinigamesWithRoutes('/stones', <h1>Vista Piedras</h1>)
    return clickHotspot('Piedras del lago').then(() => {
      return verifyHeadingVisible('Vista Piedras')
    })
  })

  it('usa el arbol cosmetico equipado para volver al jardin', () => {
    setupEquippedTreeInventory()
    renderMinigamesPage()
    verifyEquippedTreeImageSrcContains(treeBlueLight)
  })

  it('el boton volver navega a /', () => {
    renderMinigamesWithRoutes('/', <h1>Vista Garden</h1>)
    return clickVolverLink().then(() => {
      return verifyHeadingVisible('Vista Garden')
    })
  })

  it('redirige a burbujas al hacer click en el hotspot del pez', () => {
    renderMinigamesWithRoutes('/bubbles', <h1>Vista Burbujas</h1>)
    return clickHotspot('Burbujas').then(() => {
      return verifyHeadingVisible('Vista Burbujas')
    })
  })

  it('redirige a farolitos al hacer click en un farol', () => {
    renderMinigamesWithRoutes('/lanterns', <h1>Vista Farolitos</h1>)
    return clickFirstLanternLink().then(() => {
      return verifyHeadingVisible('Vista Farolitos')
    })
  })

  /* helpers */

  const renderMinigamesPage = () => {
    user = userEvent.setup()
    const { container } = render(
      <ThemeProvider>
        <MemoryRouter>
          <Minigames />
        </MemoryRouter>
      </ThemeProvider>
    )
    activeContainer = container
  }

  const renderMinigamesWithRoutes = (targetPath: string, targetElement: React.ReactNode) => {
    user = userEvent.setup()
    const { container } = render(
      <ThemeProvider>
        <MemoryRouter initialEntries={['/minigames']}>
          <Routes>
            <Route path="/minigames" element={<Minigames />} />
            <Route path={targetPath} element={targetElement} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>
    )
    activeContainer = container
  }

  const setupEquippedTreeInventory = () => {
    mockUseInventory.mockReturnValue({
      inventory: [
        {
          storeItemId: 7,
          name: 'Arbol azul',
          category: 'TREE',
          assetKey: 'tree-blue',
          equipped: true,
        },
      ],
      loading: false,
      error: null,
      refetch: vi.fn(),
    })
  }

  const verifyDesktopAndMobileBackgroundsVisible = () => {
    expect(screen.getByAltText('Fondo de minijuegos')).toBeInTheDocument()
    expect(screen.getByAltText('Fondo de minijuegos para celular')).toBeInTheDocument()
  }

  const verifyDecorativeCloudsExistWithoutLinks = () => {
    const clouds = activeContainer.querySelectorAll('img[src*="cloud.webp"]')
    expect(clouds).toHaveLength(3)
    clouds.forEach(cloud => {
      expect(cloud.closest('a')).toBeNull()
    })
  }

  const verifyLanternLinksNavigateTo = (path: string) => {
    const lanternLinks = screen.getAllByLabelText('Farolitos que vuelan')
    expect(lanternLinks).toHaveLength(4)
    lanternLinks.forEach(lantern => {
      expect(lantern.closest('a')).toHaveAttribute('href', path)
    })
  }

  const verifyHotspotLinkTarget = (labelText: string, path: string) => {
    expect(screen.getByLabelText(labelText).closest('a')).toHaveAttribute('href', path)
  }

  const verifyEquippedTreeImageSrcContains = (srcPart: string) => {
    const treeImage = screen.getByAltText('Fragmento del arbol del jardin') as HTMLImageElement
    expect(treeImage.src).toContain(srcPart)
  }

  const clickVolverLink = () => {
    return user.click(screen.getByRole('link', { name: 'Volver al jardin' }))
  }

  const clickHotspot = (labelText: string) => {
    return user.click(screen.getByLabelText(labelText))
  }

  const clickFirstLanternLink = () => {
    return user.click(screen.getAllByLabelText('Farolitos que vuelan')[0])
  }

  const verifyHeadingVisible = async (text: string) => {
    await waitFor(() => {
      expect(screen.getByRole('heading', { name: text })).toBeInTheDocument()
    })
  }
  const setupDefaultInventory = () => {
    mockUseInventory.mockReturnValue({
      inventory: [],
      loading: false,
      error: null,
      refetch: vi.fn(),
    })
  }
})