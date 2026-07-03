import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { StoreProductForm } from '../../components/backoffice/StoreProductForm'

const product = {
  id: 1, name: 'Casa rosa', description: 'desc', category: 'HOUSE', assetKey: 'house-pink',
  priceCoins: 50, price: null, premiumOnly: false, imageUrlLight: null, imageUrlDark: null,
}
const file = (n: string) => new File(['x'], n, { type: 'image/webp' })

describe('StoreProductForm', () => {
  it('deshabilita crear hasta completar nombre, descripción e imágenes', async () => {
    render(<StoreProductForm product={null} onClose={() => { }} onSubmit={vi.fn()} />)
    const submit = screen.getByRole('button', { name: 'Crear producto' })
    expect(submit).toBeDisabled()

    await userEvent.type(screen.getByLabelText('Nombre'), 'Casa nueva')
    await userEvent.type(screen.getByLabelText('Descripción'), 'una casa')
    expect(submit).toBeDisabled()

    await userEvent.upload(screen.getByLabelText('Imagen clara'), file('l.webp'))
    await userEvent.upload(screen.getByLabelText('Imagen oscura'), file('d.webp'))
    expect(submit).toBeEnabled()
  })

  it('llama onSubmit con los datos cargados', async () => {
    const onSubmit = vi.fn().mockResolvedValue(true)
    render(<StoreProductForm product={null} onClose={() => { }} onSubmit={onSubmit} />)

    await userEvent.type(screen.getByLabelText('Nombre'), 'Casa nueva')
    await userEvent.type(screen.getByLabelText('Descripción'), 'una casa')
    await userEvent.upload(screen.getByLabelText('Imagen clara'), file('l.webp'))
    await userEvent.upload(screen.getByLabelText('Imagen oscura'), file('d.webp'))
    await userEvent.click(screen.getByRole('button', { name: 'Crear producto' }))

    expect(onSubmit).toHaveBeenCalledOnce()
    const data = onSubmit.mock.calls[0][0]
    expect(data.name).toBe('Casa nueva')
    expect(data.imageLight).toBeInstanceOf(File)
    expect(data.imageDark).toBeInstanceOf(File)
  })

  it('precarga los datos en modo edición', () => {
    render(<StoreProductForm product={product} onClose={() => { }} onSubmit={vi.fn()} />)
    expect(screen.getByDisplayValue('Casa rosa')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Guardar cambios' })).toBeInTheDocument()
  })
})