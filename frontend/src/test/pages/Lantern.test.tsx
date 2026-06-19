import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import LanternsActivity from '../../pages/Lanterns/Lanterns'

const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return { ...actual, useNavigate: () => mockNavigate }
})

vi.mock('../../context/theme', () => ({
  useTheme: () => ({ theme: 'light' }),
}))

const mockRequireAuth = vi.fn((action: () => void) => action())
vi.mock('../../context/authGate', () => ({
  useAuthGate: () => ({ requireAuth: mockRequireAuth }),
}))

vi.mock('../../api/lanterns', () => ({
  lanternsApi: {
    list: vi.fn(),
    create: vi.fn(),
    updateStatus: vi.fn(),
    markWorkedOn: vi.fn(),
  },
}))

vi.mock('../../api/client', () => ({
  api: { post: vi.fn() },
}))

import { lanternsApi } from '../../api/lanterns'
import { api } from '../../api/client'

const mockedLanternsApi = vi.mocked(lanternsApi)
const mockedApiPost = vi.mocked(api.post)

const LANTERNS_FIXTURE = [
  { id: 1, text: 'Pensamiento uno', workedOn: false, createdAt: '2026-06-18T00:00:00Z' },
  { id: 2, text: 'Pensamiento dos', workedOn: true, createdAt: '2026-06-17T00:00:00Z' },
]

function renderComponent() {
  return render(
    <MemoryRouter>
      <LanternsActivity />
    </MemoryRouter>,
  )
}

describe('LanternsActivity', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mockedLanternsApi.list.mockResolvedValue([])
  })

  describe('renderizado inicial', () => {
    it('renderiza el título y el subtítulo', () => {
      renderComponent()
      expect(screen.getByText('Farolitos que vuelan')).toBeInTheDocument()
      expect(screen.getByText('Escribe tus pensamientos y déjalos ir al cielo.')).toBeInTheDocument()
    })

    it('renderiza el input y el botón Soltar', () => {
      renderComponent()
      expect(screen.getByPlaceholderText('¿Qué te da vueltas?')).toBeInTheDocument()
      expect(screen.getByRole('button', { name: 'Soltar' })).toBeInTheDocument()
    })

    it('carga los faroles del usuario al montar', async () => {
      mockedLanternsApi.list.mockResolvedValue(LANTERNS_FIXTURE)
      renderComponent()

      await waitFor(() => {
        expect(screen.getAllByText('Pensamiento uno').length).toBeGreaterThan(0)
      })
      expect(mockedLanternsApi.list).toHaveBeenCalledOnce()
    })

    it('renderiza el botón volver', () => {
      renderComponent()
      expect(screen.getByRole('button', { name: /volver/i })).toBeInTheDocument()
    })
  })

  describe('crear farol', () => {
    it('crea un farol al escribir texto y presionar Soltar', async () => {
      const user = userEvent.setup()
      mockedLanternsApi.create.mockResolvedValue({ id: 10, text: 'Nuevo pensamiento', workedOn: false, createdAt: '2026-06-18T00:00:00Z' })
      renderComponent()

      const input = screen.getByPlaceholderText('¿Qué te da vueltas?')
      await user.type(input, 'Nuevo pensamiento')
      await user.click(screen.getByRole('button', { name: 'Soltar' }))

      expect(mockedLanternsApi.create).toHaveBeenCalledWith('Nuevo pensamiento')
    })

    it('crea un farol al presionar Enter', async () => {
      const user = userEvent.setup()
      mockedLanternsApi.create.mockResolvedValue({ id: 11, text: 'Enter test', workedOn: false, createdAt: '2026-06-18T00:00:00Z' })
      renderComponent()

      const input = screen.getByPlaceholderText('¿Qué te da vueltas?')
      await user.type(input, 'Enter test{Enter}')

      expect(mockedLanternsApi.create).toHaveBeenCalledWith('Enter test')
    })

    it('requiere autenticación para crear un farol', async () => {
      const user = userEvent.setup()
      mockRequireAuth.mockImplementation(() => {})
      mockedLanternsApi.create.mockResolvedValue({ id: 12, text: 'Auth test', workedOn: false, createdAt: '2026-06-18T00:00:00Z' })
      renderComponent()

      const input = screen.getByPlaceholderText('¿Qué te da vueltas?')
      await user.type(input, 'Auth test')
      await user.click(screen.getByRole('button', { name: 'Soltar' }))

      expect(mockRequireAuth).toHaveBeenCalled()
      expect(mockedLanternsApi.create).not.toHaveBeenCalled()
    })

    it('no permite crear faroles con texto vacío', async () => {
      const user = userEvent.setup()
      renderComponent()

      await user.click(screen.getByRole('button', { name: 'Soltar' }))
      expect(mockedLanternsApi.create).not.toHaveBeenCalled()
    })

    it('deshabilita el input al llegar al máximo de faroles', async () => {
      const fiveLanterns = Array.from({ length: 5 }, (_, i) => ({
        id: i + 1,
        text: `Farol ${i + 1}`,
        workedOn: false,
        createdAt: '2026-06-18T00:00:00Z',
      }))
      mockedLanternsApi.list.mockResolvedValue(fiveLanterns)
      renderComponent()

      await waitFor(() => {
        expect(screen.getByPlaceholderText('Soltá o completá uno primero')).toBeDisabled()
      })
    })
  })

  describe('acciones sobre faroles', () => {
    beforeEach(() => {
      mockedLanternsApi.list.mockResolvedValue(LANTERNS_FIXTURE)
    })

    it('liberar un farol llama updateStatus con CANCELLED', async () => {
      const user = userEvent.setup()
      mockedLanternsApi.updateStatus.mockResolvedValue(undefined)
      renderComponent()

      await waitFor(() => {
        expect(screen.getAllByText('Pensamiento uno').length).toBeGreaterThan(0)
      })

      const liberarButtons = screen.getAllByRole('button', { name: 'Liberar' })
      await user.click(liberarButtons[0])

      expect(mockedLanternsApi.updateStatus).toHaveBeenCalledWith(1, 'CANCELLED')
    })

    it('completar aparece solo en faroles con workedOn true', async () => {
      renderComponent()

      await waitFor(() => {
        expect(screen.getAllByText('Pensamiento uno').length).toBeGreaterThan(0)
      })

      const completarButtons = screen.queryAllByRole('button', { name: 'Completar' })
      expect(completarButtons.length).toBe(0)
    })

    it('trabajar un farol llama al endpoint de recomendación', async () => {
      const user = userEvent.setup()
      mockedApiPost.mockResolvedValue({
        activity_type: 'diary',
        action_id: '1',
        title: 'Escribí en tu diario',
        description: 'Te recomendamos reflexionar',
        redirect_url: '/diary',
      })
      renderComponent()

      await waitFor(() => {
        expect(screen.getAllByText('Pensamiento uno').length).toBeGreaterThan(0)
      })

      const trabajarButtons = screen.getAllByRole('button', { name: 'Trabajar' })
      await user.click(trabajarButtons[0])

      expect(mockedApiPost).toHaveBeenCalledWith('/lanterns/recommendation', {
        thoughts: ['Pensamiento uno'],
      })
    })

    it('muestra el modal de recomendación tras trabajar un farol', async () => {
      const user = userEvent.setup()
      mockedApiPost.mockResolvedValue({
        activity_type: 'diary',
        action_id: '1',
        title: 'Escribí en tu diario',
        description: 'Te recomendamos reflexionar',
        redirect_url: '/diary',
      })
      renderComponent()

      await waitFor(() => {
        expect(screen.getAllByText('Pensamiento uno').length).toBeGreaterThan(0)
      })

      await user.click(screen.getAllByRole('button', { name: 'Trabajar' })[0])

      await waitFor(() => {
        expect(screen.getByText('Escribí en tu diario')).toBeInTheDocument()
        expect(screen.getByText('Te recomendamos reflexionar')).toBeInTheDocument()
      })
    })

    it('navega a la actividad recomendada al hacer click en Ir', async () => {
      const user = userEvent.setup()
      mockedApiPost.mockResolvedValue({
        activity_type: 'diary',
        action_id: '1',
        title: 'Escribí en tu diario',
        description: 'Te recomendamos reflexionar',
        redirect_url: '/diary',
      })
      renderComponent()

      await waitFor(() => {
        expect(screen.getAllByText('Pensamiento uno').length).toBeGreaterThan(0)
      })

      await user.click(screen.getAllByRole('button', { name: 'Trabajar' })[0])

      await waitFor(() => {
        expect(screen.getByText('Escribí en tu diario')).toBeInTheDocument()
      })

      await user.click(screen.getByRole('button', { name: /ir al diario/i }))
      expect(mockNavigate).toHaveBeenCalledWith('/diary')
    })

    it('cierra el modal al hacer click en Quedarme aquí', async () => {
      const user = userEvent.setup()
      mockedApiPost.mockResolvedValue({
        activity_type: 'diary',
        action_id: '1',
        title: 'Escribí en tu diario',
        description: 'Te recomendamos reflexionar',
        redirect_url: '/diary',
      })
      renderComponent()

      await waitFor(() => {
        expect(screen.getAllByText('Pensamiento uno').length).toBeGreaterThan(0)
      })

      await user.click(screen.getAllByRole('button', { name: 'Trabajar' })[0])

      await waitFor(() => {
        expect(screen.getByText('Escribí en tu diario')).toBeInTheDocument()
      })

      await user.click(screen.getByRole('button', { name: /quedarme aquí/i }))

      await waitFor(() => {
        expect(screen.queryByText('Escribí en tu diario')).not.toBeInTheDocument()
      })
    })
  })

  describe('carrusel mobile', () => {
    it('muestra flechas de navegación cuando hay más de un farol', async () => {
      mockedLanternsApi.list.mockResolvedValue(LANTERNS_FIXTURE)
      renderComponent()

      await waitFor(() => {
        expect(screen.getByLabelText('Farol anterior')).toBeInTheDocument()
        expect(screen.getByLabelText('Farol siguiente')).toBeInTheDocument()
      })
    })

    it('muestra dots indicadores según la cantidad de faroles', async () => {
      mockedLanternsApi.list.mockResolvedValue(LANTERNS_FIXTURE)
      renderComponent()

      await waitFor(() => {
        expect(screen.getByLabelText('Farol 1')).toBeInTheDocument()
        expect(screen.getByLabelText('Farol 2')).toBeInTheDocument()
      })
    })
  })

  describe('navegación', () => {
    it('el botón volver llama navigate(-1)', async () => {
      const user = userEvent.setup()
      renderComponent()

      await user.click(screen.getByRole('button', { name: /volver/i }))
      expect(mockNavigate).toHaveBeenCalledWith(-1)
    })
  })

  describe('loading', () => {
    it('muestra overlay de carga mientras obtiene recomendación', async () => {
      const user = userEvent.setup()
      let resolvePost: (value: unknown) => void
      mockedApiPost.mockReturnValue(new Promise(resolve => { resolvePost = resolve }))
      mockedLanternsApi.list.mockResolvedValue(LANTERNS_FIXTURE)
      renderComponent()

      await waitFor(() => {
        expect(screen.getAllByText('Pensamiento uno').length).toBeGreaterThan(0)
      })

      await user.click(screen.getAllByRole('button', { name: 'Trabajar' })[0])

      expect(screen.getByText('Analizando tu pensamiento...')).toBeInTheDocument()

      resolvePost!({
        activity_type: 'diary',
        action_id: '1',
        title: 'Test',
        description: 'Test desc',
        redirect_url: '/diary',
      })

      await waitFor(() => {
        expect(screen.queryByText('Analizando tu pensamiento...')).not.toBeInTheDocument()
      })
    })
  })
})