import { describe, it, expect } from 'vitest'
import { resolveEquippedImages, type CosmeticImage } from '../../components/Scene/CosmeticAssets'
import type { InventoryItemResponse } from '../../api/store'

const assets: Record<string, CosmeticImage> = {
  'house-pink': { light: '/pink.webp', dark: '/pink-dark.webp' },
  'house-blue': { light: '/blue.webp', dark: '/blue-dark.webp' },
}

describe('resolveEquippedImages', () => {
  it('mapea categoria -> imagen (light+dark) para items equipados con asset existente', () => {
    verifyResolvedImages([owned('HOUSE', 'house-pink', true)], assets, {
      HOUSE: { light: '/pink.webp', dark: '/pink-dark.webp' }
    })
  })

  it('ignora los items no equipados', () => {
    verifyResolvedImages([owned('HOUSE', 'house-pink', false)], assets, {})
  })

  it('ignora los items equipados cuyo asset no existe', () => {
    verifyResolvedImages([owned('HOUSE', 'house-pink', true)], {}, {})
  })

  it('la última categoría equipada gana si hay varias del mismo tipo', () => {
    verifyResolvedImages([
      owned('HOUSE', 'house-pink', true),
      owned('HOUSE', 'house-blue', true)
    ], assets, {
      HOUSE: { light: '/blue.webp', dark: '/blue-dark.webp' }
    })
  })

  it('usa las URLs subidas cuando el item tiene imageUrl', () => {
    verifyResolvedImagesWithImageUrl({
      storeItemId: 1,
      name: 'x',
      category: 'HOUSE',
      assetKey: null,
      equipped: true,
      imageUrlLight: 'http://cdn/light-theme/u.webp',
      imageUrlDark: 'http://cdn/dark-theme/u.webp',
    }, {
      light: 'http://cdn/light-theme/u.webp',
      dark: 'http://cdn/dark-theme/u.webp',
    })
  })
})

/* ─── Private Helpers ─── */

function owned(category: string, assetKey: string, equipped: boolean): InventoryItemResponse {
  return {
    storeItemId: 1,
    name: 'x',
    category,
    assetKey,
    equipped,
    imageUrlLight: null,
    imageUrlDark: null
  }
}

function verifyResolvedImages(
  items: InventoryItemResponse[],
  assetsMap: Record<string, CosmeticImage>,
  expected: Record<string, CosmeticImage>
) {
  const result = resolveEquippedImages(items, assetsMap)
  expect(result).toEqual(expected)
}

function verifyResolvedImagesWithImageUrl(item: any, expected: CosmeticImage) {
  const result = resolveEquippedImages([item])
  expect(result.HOUSE).toEqual(expected)
}
