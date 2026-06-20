import { describe, it, expect } from 'vitest'
import { resolveEquippedImages, type CosmeticImage } from '../../components/Scene/cosmeticAssets'
import type { InventoryItemResponse } from '../../api/store'

const owned = (category: string, assetKey: string, equipped: boolean): InventoryItemResponse => ({
  storeItemId: 1, name: 'x', category, assetKey, equipped,
})

const assets: Record<string, CosmeticImage> = {
  'house-pink': { light: '/pink.webp', dark: '/pink-dark.webp' },
  'house-blue': { light: '/blue.webp', dark: '/blue-dark.webp' },
}

describe('resolveEquippedImages', () => {
  it('mapea categoria -> imagen (light+dark) para items equipados con asset existente', () => {
    const result = resolveEquippedImages([owned('HOUSE', 'house-pink', true)], assets)  
    expect(result).toEqual({ HOUSE: { light: '/pink.webp', dark: '/pink-dark.webp' } })
  })

  it('ignora los items no equipados', () => {
    const result = resolveEquippedImages([owned('HOUSE', 'house-pink', false)], assets)  
    expect(result).toEqual({})
  })

  it('ignora los items equipados cuyo asset no existe', () => {
    const result = resolveEquippedImages([owned('HOUSE', 'house-pink', true)], {})  
    expect(result).toEqual({})
  })

  it('la última categoría equipada gana si hay varias del mismo tipo', () => {
    const result = resolveEquippedImages([owned('HOUSE', 'house-pink', true), owned('HOUSE', 'house-blue', true)], assets)  
    expect(result).toEqual({ HOUSE: { light: '/blue.webp', dark: '/blue-dark.webp' } })
  })
  })