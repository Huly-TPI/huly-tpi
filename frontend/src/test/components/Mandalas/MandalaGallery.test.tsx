import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import MandalaGallery from '../../../components/Mandalas/MandalaGallery'
import { mandalaAssetByKey } from '../../../components/Mandalas/mandalaAssets'
import { clickButton, verifyTextPresent } from '../../testHelpers'

describe('MandalaGallery', () => {
  let onSelectMandalaSpy: any
  let onPageChangeSpy: any
  let user: any

  const availableMandalas = [
    {
      id: 'mandala-01',
      title: 'Mandala 01',
      description: 'Trazos circulares para pintar con calma.',
      src: mandalaAssetByKey['mandala-01'],
      accessStatus: 'available' as const,
      unlockSource: 'free' as const,
      accessType: 'free' as const,
      isLocked: false,
    },
    {
      id: 'mandala-13',
      title: 'Mandala 13',
      description: 'Mandala comprada.',
      src: mandalaAssetByKey['mandala-13'],
      accessStatus: 'available' as const,
      unlockSource: 'purchased' as const,
      accessType: 'purchasable' as const,
      isLocked: false,
    },
    {
      id: 'mandala-20',
      title: 'Mandala 20',
      description: 'Mandala incluida en suscripción.',
      src: mandalaAssetByKey['mandala-20'],
      accessStatus: 'included' as const,
      unlockSource: 'premiumPlan' as const,
      accessType: 'subscription' as const,
      isLocked: false,
    },
  ]

  it('mantiene disponibles los assets locales de las 21 mandalas', () => {
    verifyLocalMandalaAssetsCount(21)
    verifyMandalaAssetPresent('mandala-21')
  })

  it('renderiza solamente las mandalas disponibles recibidas', () => {
    renderDefaultGallery()
    verifyNoHeadingPresent()
    verifyTextPresent('Gratis')
    verifyTextPresent('Comprado')
    verifyTextPresent('Suscripción')
    verifyChooseMandalaButtonsCount(3)
    verifyUniqueSourcesCount(3)
  })

  it('permite seleccionar cualquiera de las mandalas del catálogo', () => {
    renderGalleryWithSelectSpy()
    verifyChooseMandalaButtonsEnabled()
    return clickChooseMandalaButton(0).then(() => {
      verifyOnSelectMandalaCalledWith(availableMandalas[0])
    })
  })

  it('permite navegar entre páginas sin mostrar texto visible', () => {
    renderGalleryWithPageChangeSpy()
    return clickPreviousPageButton()
      .then(() => clickNextPageButton())
      .then(() => {
        verifyOnPageChangeNthCalledWith(1, 0)
        verifyOnPageChangeNthCalledWith(2, 2)
      })
  })

  /* helpers */

  const verifyLocalMandalaAssetsCount = (count: number) => {
    expect(Object.keys(mandalaAssetByKey).sort()).toEqual(
      Array.from({ length: count }, (_, index) => `mandala-${String(index + 1).padStart(2, '0')}`),
    )
  }

  const verifyMandalaAssetPresent = (key: string) => {
    expect(mandalaAssetByKey[key]).toBeTruthy()
  }

  const renderDefaultGallery = () => {
    render(
      <MandalaGallery
        first
        last
        mandalas={availableMandalas}
        onPageChange={vi.fn()}
        onSelectMandala={vi.fn()}
        page={0}
        totalPages={1}
      />,
    )
  }

  const renderGalleryWithSelectSpy = () => {
    user = userEvent.setup()
    onSelectMandalaSpy = vi.fn()
    render(
      <MandalaGallery
        first
        last
        mandalas={availableMandalas}
        onPageChange={vi.fn()}
        onSelectMandala={onSelectMandalaSpy}
        page={0}
        totalPages={1}
      />,
    )
  }

  const renderGalleryWithPageChangeSpy = () => {
    user = userEvent.setup()
    onPageChangeSpy = vi.fn()
    render(
      <MandalaGallery
        first={false}
        last={false}
        mandalas={availableMandalas}
        onPageChange={onPageChangeSpy}
        onSelectMandala={vi.fn()}
        page={1}
        totalPages={3}
      />,
    )
  }

  const verifyNoHeadingPresent = () => {
    expect(screen.queryByRole('heading')).not.toBeInTheDocument()
  }

  const verifyChooseMandalaButtonsCount = (count: number) => {
    expect(screen.getAllByRole('button', { name: (content) => content.includes('Elegir Mandala') })).toHaveLength(count)
  }

  const verifyUniqueSourcesCount = (count: number) => {
    expect(new Set(availableMandalas.map(mandala => mandala.src))).toHaveProperty('size', count)
  }

  const verifyChooseMandalaButtonsEnabled = () => {
    const mandalaButtons = screen.getAllByRole('button', { name: (content) => content.includes('Elegir Mandala') })
    mandalaButtons.forEach(button => expect(button).toBeEnabled())
  }

  const clickChooseMandalaButton = (index: number) => {
    const mandalaButtons = screen.getAllByRole('button', { name: (content) => content.includes('Elegir Mandala') })
    return user.click(mandalaButtons[index])
  }

  const verifyOnSelectMandalaCalledWith = (mandala: any) => {
    expect(onSelectMandalaSpy).toHaveBeenCalledWith(mandala)
  }

  const clickPreviousPageButton = () => {
    return clickButton(user, 'Página anterior')
  }

  const clickNextPageButton = () => {
    return clickButton(user, 'Página siguiente')
  }

  const verifyOnPageChangeNthCalledWith = (nth: number, page: number) => {
    expect(onPageChangeSpy).toHaveBeenNthCalledWith(nth, page)
  }
})
