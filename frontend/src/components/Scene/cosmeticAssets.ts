import type { InventoryItemResponse } from '../../api/store'
import housePinkLight from '../../assets/garden/light-theme/house-pink.webp'
import housePinkDark from '../../assets/garden/dark-theme/house-pink.webp'
import houseBlueLight from '../../assets/garden/light-theme/house-blue.webp'
import houseBlueDark from '../../assets/garden/dark-theme/house-blue.webp'

export interface CosmeticImage{
    light: string
    dark: string
}

export const cosmeticAssets: Record<string, CosmeticImage> = {
  'house-pink': { light: housePinkLight, dark: housePinkDark },
  'house-blue': { light: houseBlueLight, dark: houseBlueDark },
}

export function resolveEquippedImages(
  inventory: InventoryItemResponse[],
assets: Record<string, CosmeticImage> = cosmeticAssets,
):  Record<string, CosmeticImage> {
  const result: Record<string, CosmeticImage> = {}
  for (const item of inventory) {
    if (item.equipped && assets[item.assetKey]) {
      result[item.category] = assets[item.assetKey]
    }
  }
  return result
}