import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { CosmeticCard } from '../../components/Shop/CosmeticCard'
import { verifyTextPresent } from '../testHelpers'



describe('CosmeticCard', () => {
    let onBuyMock: any
    let onBuyWithMoneyMock: any
    let onEquipMock: any
    let onUnequipMock: any

    beforeEach(() => {
        onBuyMock = vi.fn()
        onBuyWithMoneyMock = vi.fn()
        onEquipMock = vi.fn()
        onUnequipMock = vi.fn()
    })

    it('muestra el nombre, descripción y precio', () => {
        renderCard({})
        verifyCardDetails()
    })

    it('muestra Comprar cuando no lo tenés y llama onBuy con el id', () => {
        renderCard({ owned: false })
        return clickButtonByName('Comprar').then(() => {
            verifyOnBuyCalledWithId(10)
        })
    })

    it('muestra Equipar cuando lo tenés y llama onEquip con el id', () => {
        renderCard({ owned: true, equipped: false })
        return clickButtonByName('Equipar').then(() => {
            verifyOnEquipCalledWithId(10)
        })
    })

    it('muestra Quitar cuando esta equipado y llama onUnequip con el id', () => {
        renderCard({ owned: true, equipped: true })
        return clickButtonByName(/Quitar/).then(() => {
            verifyOnUnequipCalledWithId(10)
        })
    })

    it('muestra "Comprar con dinero" cuando el item tiene precio en dinero y llama onBuyWithMoney', () => {
        renderCard({ item: makeItem({ price: 1000 }), owned: false })
        return clickButtonByName('Comprar con MercadoPago').then(() => {
            verifyOnBuyWithMoneyCalledWithId(10)
        })
    })

    it('muestra badge "Solo premium" cuando el item es premiumOnly', () => {
        renderCard({ item: makeItem({ premiumOnly: true }), userIsPremium: false })
        verifyPremiumBadgesLength(2)
    })

    it('muestra botón deshabilitado cuando item es premiumOnly y usuario no es premium', () => {
        renderCard({ item: makeItem({ premiumOnly: true }), userIsPremium: false })
        verifyPremiumButtonDisabled()
    })

    it('usa la imagen subida cuando el item tiene imageUrlLight', () => {
        renderCard({ item: makeItem({ imageUrlLight: 'http://cdn/light-theme/u.webp' }) })
        verifyImageLightSource('http://cdn/light-theme/u.webp')
    })
    const renderCard = (props: {
        item?: any
        owned?: boolean
        equipped?: boolean
        busy?: boolean
        disabled?: boolean
        userIsPremium?: boolean
    }) => {
        const defaultProps = {
            item: makeItem(),
            owned: false,
            equipped: false,
            busy: false,
            disabled: false,
            onBuy: onBuyMock,
            onBuyWithMoney: onBuyWithMoneyMock,
            onEquip: onEquipMock,
            onUnequip: onUnequipMock,
            userIsPremium: false,
        }
        render(<CosmeticCard {...defaultProps} {...props} />)
    }

    const verifyCardDetails = () => {
        verifyTextPresent('Casa rosa')
        verifyTextPresent('Pinta tu casa de rosa')
        verifyTextPresent('50 semillas')
    }

    const clickButtonByName = (name: string | RegExp) => {
        const user = userEvent.setup()
        return user.click(screen.getByRole('button', { name }))
    }

    const verifyOnBuyCalledWithId = (id: number) => {
        expect(onBuyMock).toHaveBeenCalledWith(id)
    }

    const verifyOnEquipCalledWithId = (id: number) => {
        expect(onEquipMock).toHaveBeenCalledWith(id)
    }

    const verifyOnUnequipCalledWithId = (id: number) => {
        expect(onUnequipMock).toHaveBeenCalledWith(id)
    }

    const verifyOnBuyWithMoneyCalledWithId = (id: number) => {
        expect(onBuyWithMoneyMock).toHaveBeenCalledWith(id)
    }

    const verifyPremiumBadgesLength = (length: number) => {
        expect(screen.getAllByText('Solo premium')).toHaveLength(length)
    }

    const verifyPremiumButtonDisabled = () => {
        const btn = screen.getByRole('button', { name: 'Solo premium' })
        expect(btn).toBeDisabled()
    }

    const verifyImageLightSource = (src: string) => {
        const img = screen.getByAltText('Casa rosa') as HTMLImageElement
        expect(img.src).toContain(src)
    }
})

function makeItem(overrides = {}) {
  return ({
    id: 10,
    name: 'Casa rosa',
    description: 'Pinta tu casa de rosa',
    category: 'HOUSE',
    assetKey: 'house-pink',
    priceCoins: 50,
    price: null as number | null,
    premiumOnly: false,
    imageUrlLight: null as string | null,
    imageUrlDark: null as string | null,
    ...overrides,
})
}
