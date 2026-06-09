import { describe, it, expect, vi } from 'vitest'
import { render, screen, act, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import BubblesActivity from '../../components/Bubbles/BubblesActivity.tsx'
import { registerActivitySession } from '../../api/activities'

vi.mock('../../api/activities', () => ({
  registerActivitySession: vi.fn().mockResolvedValue({}),
  ActivityType: {
    BURBUJA: 'BURBUJA',
  },
}))

const renderActivity = () =>
  render(
    <MemoryRouter>
      <BubblesActivity />
    </MemoryRouter>,
  )

describe('BubblesActivity component', () => {
  it('renderiza el menú de inicio', () => {
    renderActivity()
    expect(screen.getByRole('button', { name: /comenzar/i })).toBeInTheDocument()
  })

  it('renderiza el componente de burbujas al hacer click en comenzar', async () => {
    const user = userEvent.setup()
    renderActivity()
    await user.click(screen.getByRole('button', { name: /comenzar/i }))
    expect(screen.getByTestId('bubbles-container')).toBeInTheDocument()
  })

  it('muestra el botón de volver una vez iniciado el juego', async () => {
    const user = userEvent.setup()
    renderActivity()

    expect(screen.queryByRole('button', { name: /volver/i })).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /comenzar/i }))
    expect(screen.getByRole('button', { name: /volver/i })).toBeInTheDocument()
  })

  it('navega a minijuegos al hacer click en volver', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter initialEntries={['/bubbles']}>
        <Routes>
          <Route path="/minigames" element={<h1>Vista Minijuegos</h1>} />
          <Route path="/bubbles" element={<BubblesActivity />} />
        </Routes>
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: /comenzar/i }))
    await user.click(screen.getByRole('button', { name: /volver/i }))
    expect(screen.getByRole('heading', { name: 'Vista Minijuegos' })).toBeInTheDocument()
  })

  it('no registra la sesión al desmontar si no explotó ninguna burbuja', async () => {
    const { unmount } = render(
      <MemoryRouter>
        <BubblesActivity />
      </MemoryRouter>,
    )

    await fireEvent.click(screen.getByRole('button', { name: /comenzar/i }))

    act(() => {
      unmount()
    })

    expect(registerActivitySession).not.toHaveBeenCalled()
  })

  it('registra la sesión al desmontar si explotó al menos una burbuja', async () => {
    const { unmount } = render(
      <MemoryRouter>
        <BubblesActivity />
      </MemoryRouter>,
    )

    await fireEvent.click(screen.getByRole('button', { name: /comenzar/i }))
    await fireEvent.click(screen.getByTestId('bubble-b-0'))

    act(() => {
      unmount()
    })

    expect(registerActivitySession).toHaveBeenCalledWith(
      {
        activityType: 'BURBUJA',
      },
      {
        keepalive: true,
      },
    )
  })
})
