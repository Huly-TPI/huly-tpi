import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { CosmeticCard } from '../../components/Shop/CosmeticCard'

const makeItem = (overrides = {}) => ({
    id: 10,
    name: 'Casa rosa',
    description: 'Pinta tu casa de rosa',
    category: 'HOUSE',
    assetKey: 'house-pink',
    priceCoins: 50,
    price: null as number | null,
    ...overrides,
})

const noop = () => { }

describe('CosmeticCard', () => {
    it('muestra el nombre, descripción y precio', () => {
        render(<CosmeticCard item={makeItem()} owned={false} equipped={false} busy={false} disabled={false} onBuy={noop} onBuyWithMoney={noop} onEquip={noop} onUnequip={noop} />)
        expect(screen.getByText('Casa rosa')).toBeInTheDocument()
        expect(screen.getByText('Pinta tu casa de rosa')).toBeInTheDocument()
        expect(screen.getByText('50')).toBeInTheDocument()
    })

    it('muestra Comprar cuando no lo tenés y llama onBuy con el id', async () => {
        const onBuy = vi.fn()
        render(<CosmeticCard item={makeItem()} owned={false} equipped={false} busy={false} disabled={false} onBuy={onBuy} onBuyWithMoney={noop} onEquip={noop} onUnequip={noop} />)

        await userEvent.click(screen.getByRole('button', { name: 'Comprar' }))

        expect(onBuy).toHaveBeenCalledWith(10)
    })

    it('muestra Equipar cuando lo tenés y llama onEquip con el id', async () => {
        const onEquip = vi.fn()
        render(<CosmeticCard item={makeItem()} owned={true} equipped={false} busy={false} disabled={false} onBuy={noop} onBuyWithMoney={noop} onEquip={onEquip} onUnequip={noop} />)

        await userEvent.click(screen.getByRole('button', { name: 'Equipar' }))

        expect(onEquip).toHaveBeenCalledWith(10)
    })

    it('muestra Quitar cuando esta equipado y llama onUnequip con el id', async () => {
        const onUnequip = vi.fn()
        render(<CosmeticCard item={makeItem()} owned={true} equipped={true} busy={false} disabled={false} onBuy={noop} onBuyWithMoney={noop} onEquip={noop} onUnequip={onUnequip} />)
        await userEvent.click(screen.getByRole('button', { name: /Quitar/ }))
        expect(onUnequip).toHaveBeenCalledWith(10)
    })

    it('muestra "Comprar con dinero" cuando el item tiene precio en dinero y llama onBuyWithMoney', async () => {
        const onBuyWithMoney = vi.fn()
        render(<CosmeticCard item={makeItem({ price: 1000 })} owned={false} equipped={false} busy={false} disabled={false} onBuy={noop} onBuyWithMoney={onBuyWithMoney} onEquip={noop} onUnequip={noop} />)

        await userEvent.click(screen.getByRole('button', { name: 'Comprar con MercadoPago' }))

        expect(onBuyWithMoney).toHaveBeenCalledWith(10)
    })


})
