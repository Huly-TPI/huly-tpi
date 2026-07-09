import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import MandalaToolbar from '../../../components/Mandalas/MandalaToolbar'
import { clickButton } from '../../testHelpers'


const colors = ['#8869AC', '#7BCDBA', '#80B8F0']

describe('MandalaToolbar', () => {
  let onBrushSizeChangeSpy: any
  let onColorChangeSpy: any

  it('permite elegir color y seleccionar herramientas predefinidas', () => {
    renderDefaultToolbar()
    return clickColorButton('#7BCDBA')
      .then(() => verifyOnColorChangeCalledWith('#7BCDBA'))
      .then(() => clickPencilToolButton())
      .then(() => verifyOnBrushSizeChangeCalledWith(3))
      .then(() => clickBrushToolButton())
      .then(() => verifyOnBrushSizeChangeCalledWith(40))
  })
  let user: any

  /* helpers */

  const renderDefaultToolbar = () => {
    user = userEvent.setup()
    onBrushSizeChangeSpy = vi.fn()
    onColorChangeSpy = vi.fn()

    render(
      <MandalaToolbar
        brushSize={40}
        colors={colors}
        onBrushSizeChange={onBrushSizeChangeSpy}
        onColorChange={onColorChangeSpy}
        selectedColor="#8869AC"
      />,
    )
  }

  const clickColorButton = (color: string) => {
    return clickButton(user, `Usar color ${color}`)
  }

  const verifyOnColorChangeCalledWith = (color: string) => {
    expect(onColorChangeSpy).toHaveBeenCalledWith(color)
  }

  const clickPencilToolButton = () => {
    return clickButton(user, 'Lápiz')
  }

  const clickBrushToolButton = () => {
    return clickButton(user, 'Pincel')
  }

  const verifyOnBrushSizeChangeCalledWith = (size: number) => {
    expect(onBrushSizeChangeSpy).toHaveBeenCalledWith(size)
  }
})
