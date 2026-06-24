import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import MandalaToolbar from '../../../components/Mandalas/MandalaToolbar'

const colors = ['#8869AC', '#7BCDBA', '#80B8F0']

describe('MandalaToolbar', () => {
  it('permite elegir color y seleccionar herramientas predefinidas', async () => {
    const user = userEvent.setup()
    const onBrushSizeChange = vi.fn()
    const onColorChange = vi.fn()

    render(
      <MandalaToolbar
        brushSize={40}
        colors={colors}
        onBrushSizeChange={onBrushSizeChange}
        onColorChange={onColorChange}
        selectedColor="#8869AC"
      />,
    )

    // Elegir un color
    await user.click(screen.getByRole('button', { name: 'Usar color #7BCDBA' }))
    expect(onColorChange).toHaveBeenCalledWith('#7BCDBA')

    // Seleccionar la herramienta lápiz (grosor 3)
    await user.click(screen.getByRole('button', { name: 'Lápiz' }))
    expect(onBrushSizeChange).toHaveBeenCalledWith(3)

    // Seleccionar la herramienta pincel (grosor 40)
    await user.click(screen.getByRole('button', { name: 'Pincel' }))
    expect(onBrushSizeChange).toHaveBeenCalledWith(40)
  })
})
