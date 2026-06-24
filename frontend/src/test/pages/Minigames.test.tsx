import { beforeEach, describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
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
  beforeEach(() => {
    vi.clearAllMocks()
    mockUseInventory.mockReturnValue({
      inventory: [],
      loading: false,
      error: null,
      refetch: vi.fn(),
    })
  })

  const renderWithRouter = () => {
    return render(
      <ThemeProvider>
        <MemoryRouter>
          <Minigames />
        </MemoryRouter>
      </ThemeProvider>,
    )
  }

  it('renderiza los fondos de escena desktop y mobile', () => {
    renderWithRouter()
    expect(screen.getByAltText('Fondo de minijuegos')).toBeInTheDocument()
    expect(screen.getByAltText('Fondo de minijuegos para celular')).toBeInTheDocument()
  })

  it('renderiza las nubes como elementos decorativos sin links', () => {
    const { container } = renderWithRouter()
    const clouds = container.querySelectorAll('img[src*="cloud.webp"]')
    expect(clouds).toHaveLength(3)
    clouds.forEach(cloud => {
      expect(cloud.closest('a')).toBeNull()
    })
  })

  it('renderiza los faroles navegables hacia la actividad de farolitos', () => {
    renderWithRouter()
    const lanternLinks = screen.getAllByLabelText('Farolitos que vuelan')
    expect(lanternLinks).toHaveLength(4)
    lanternLinks.forEach(lantern => {
      expect(lantern.closest('a')).toHaveAttribute('href', '/lanterns')
    })
  })

  it('renderiza los hotspots de cada minijuego con su ruta', () => {
    renderWithRouter()
    expect(screen.getByLabelText('Burbujas').closest('a')).toHaveAttribute('href', '/bubbles')
    expect(screen.getByLabelText('Colorear mandalas').closest('a')).toHaveAttribute('href', '/mandalas')
    expect(screen.getByLabelText('Arena zen').closest('a')).toHaveAttribute('href', '/zen-sand-garden')
  })

  it('usa el arbol cosmetico equipado para volver al jardin', () => {
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

    renderWithRouter()

    const treeImage = screen.getByAltText('Fragmento del arbol del jardin') as HTMLImageElement
    expect(treeImage.src).toContain(treeBlueLight)
  })

  it('el boton volver navega a /', async () => {
    const user = userEvent.setup()

    render(
      <ThemeProvider>
        <MemoryRouter initialEntries={['/minigames']}>
          <Routes>
            <Route path="/" element={<h1>Vista Garden</h1>} />
            <Route path="/minigames" element={<Minigames />} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>,
    )

    await user.click(screen.getByRole('link', { name: /volver/i }))
    expect(await screen.findByRole('heading', { name: 'Vista Garden' })).toBeInTheDocument()
  })

  it('redirige a burbujas al hacer click en el hotspot del pez', async () => {
    const user = userEvent.setup()

    render(
      <ThemeProvider>
        <MemoryRouter initialEntries={['/minigames']}>
          <Routes>
            <Route path="/minigames" element={<Minigames />} />
            <Route path="/bubbles" element={<h1>Vista Burbujas</h1>} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>,
    )

    await user.click(screen.getByLabelText('Burbujas'))
    expect(screen.getByRole('heading', { name: 'Vista Burbujas' })).toBeInTheDocument()
  })

  it('redirige a farolitos al hacer click en un farol', async () => {
    const user = userEvent.setup()

    render(
      <ThemeProvider>
        <MemoryRouter initialEntries={['/minigames']}>
          <Routes>
            <Route path="/minigames" element={<Minigames />} />
            <Route path="/lanterns" element={<h1>Vista Farolitos</h1>} />
          </Routes>
        </MemoryRouter>
      </ThemeProvider>,
    )

    await user.click(screen.getAllByLabelText('Farolitos que vuelan')[0])
    expect(screen.getByRole('heading', { name: 'Vista Farolitos' })).toBeInTheDocument()
  })
})