import { verifyDisplayValuePresent } from '../testHelpers'
import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { StoreProductForm } from '../../components/backoffice/StoreProductForm'

const product = {
  id: 1, name: 'Casa rosa', description: 'desc', category: 'HOUSE', assetKey: 'house-pink',
  priceCoins: 50, price: null, premiumOnly: false, imageUrlLight: null, imageUrlDark: null,
}


describe('StoreProductForm', () => {
  let onSubmitSpy: any

  it('deshabilita crear hasta completar nombre descripción e imágenes', () => {
    renderNewProductForm()
    verifySubmitButtonDisabled('Crear producto')
    
    return fillName('Casa nueva')
      .then(() => fillDescription('una casa'))
      .then(() => verifySubmitButtonDisabled('Crear producto'))
      .then(() => uploadImageClara(file('l.webp')))
      .then(() => uploadImageOscura(file('d.webp')))
      .then(() => verifySubmitButtonEnabled('Crear producto'))
  })

  it('llama a onSubmit con los datos cargados', () => {
    setupOnSubmitSpy(vi.fn().mockResolvedValue(true))
    renderNewProductFormWithSpy()

    return fillName('Casa nueva')
      .then(() => fillDescription('una casa'))
      .then(() => uploadImageClara(file('l.webp')))
      .then(() => uploadImageOscura(file('d.webp')))
      .then(() => clickSubmitButton('Crear producto'))
      .then(() => {
        verifyOnSubmitCalledOnce()
        verifyOnSubmitData('Casa nueva')
      })
  })

  it('precarga los datos en modo edición', () => {
    renderEditProductForm(product)
    verifyDisplayValuePresent('Casa rosa')
    verifySubmitButtonPresent('Guardar cambios')
  })

  /* helpers */

  const renderNewProductForm = () => {
    render(<StoreProductForm product={null} onClose={() => { }} onSubmit={vi.fn()} />)
  }

  const setupOnSubmitSpy = (spy: any) => {
    onSubmitSpy = spy
  }

  const renderNewProductFormWithSpy = () => {
    render(<StoreProductForm product={null} onClose={() => { }} onSubmit={onSubmitSpy} />)
  }

  const renderEditProductForm = (prod: any) => {
    render(<StoreProductForm product={prod} onClose={() => { }} onSubmit={vi.fn()} />)
  }

  const verifySubmitButtonDisabled = (name: string) => {
    expect(screen.getByRole('button', { name })).toBeDisabled()
  }

  const verifySubmitButtonEnabled = (name: string) => {
    expect(screen.getByRole('button', { name })).toBeEnabled()
  }

  const fillName = (text: string) => {
    return userEvent.type(screen.getByLabelText('Nombre'), text)
  }

  const fillDescription = (text: string) => {
    return userEvent.type(screen.getByLabelText('Descripción'), text)
  }

  const uploadImageClara = (f: File) => {
    return userEvent.upload(screen.getByLabelText('Imagen clara'), f)
  }

  const uploadImageOscura = (f: File) => {
    return userEvent.upload(screen.getByLabelText('Imagen oscura'), f)
  }

  const clickSubmitButton = (name: string) => {
    return userEvent.click(screen.getByRole('button', { name }))
  }

  const verifyOnSubmitCalledOnce = () => {
    expect(onSubmitSpy).toHaveBeenCalledOnce()
  }

  const verifyOnSubmitData = (name: string) => {
    const data = onSubmitSpy.mock.calls[0][0]
    expect(data.name).toBe(name)
    expect(data.imageLight).toBeInstanceOf(File)
    expect(data.imageDark).toBeInstanceOf(File)
  }

  

  const verifySubmitButtonPresent = (name: string) => {
    expect(screen.getByRole('button', { name })).toBeInTheDocument()
  }
})

function file(n: string) {
  return new File(['x'], n, { type: 'image/webp' })
}
