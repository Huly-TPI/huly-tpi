import type { InventoryItemResponse } from '../../api/store'
import { getBackendOrigin } from '../../api/client'
import housePinkLight from '../../assets/garden/light-theme/house-pink.webp'
import housePinkDark from '../../assets/garden/dark-theme/house-pink.webp'
import houseBlueLight from '../../assets/garden/light-theme/house-blue.webp'
import houseBlueDark from '../../assets/garden/dark-theme/house-blue.webp'
import notebookPinkLight from '../../assets/garden/light-theme/notebook-pink.webp'
import notebookPinkDark from '../../assets/garden/dark-theme/notebook-pink.webp'
import notebookBlueLight from '../../assets/garden/light-theme/notebook-blue.webp'
import notebookBlueDark from '../../assets/garden/dark-theme/notebook-blue.webp'
import treeBlueLight from '../../assets/garden/light-theme/tree-blue.webp'
import treeBlueDark from '../../assets/garden/dark-theme/tree-blue.webp'
import treeSakuraLight from '../../assets/garden/light-theme/tree-sakura.webp'
import treeSakuraDark from '../../assets/garden/dark-theme/tree-sakura.webp'
import purpleShirt from '../../assets/avatar/remera-violeta.webp'
import whiteSneakers from '../../assets/avatar/par-zapatilla-blanca.webp'
import blueCap from '../../assets/avatar/vicera-azul.webp'


export interface CosmeticImage {
  light: string
  dark: string
}

export const cosmeticAssets: Record<string, CosmeticImage> = {
  'house-pink': { light: housePinkLight, dark: housePinkDark },
  'house-blue': { light: houseBlueLight, dark: houseBlueDark },
  'notebook-pink': { light: notebookPinkLight, dark: notebookPinkDark },
  'notebook-blue': { light: notebookBlueLight, dark: notebookBlueDark },
  'tree-sakura': { light: treeSakuraLight, dark: treeSakuraDark },
  'tree-blue': { light: treeBlueLight, dark: treeBlueDark },
  'remera-violeta': { light: purpleShirt, dark: purpleShirt },
  'zapatillas-blancas': { light: whiteSneakers, dark: whiteSneakers },
  'vicera-azul': { light: blueCap, dark: blueCap },
}

const resolveUrl = (u: string) => u.startsWith('http') ? u : `${getBackendOrigin()}${u}`

export function resolveEquippedImages(
  inventory: InventoryItemResponse[],
  assets: Record<string, CosmeticImage> = cosmeticAssets,
): Record<string, CosmeticImage> {
  const result: Record<string, CosmeticImage> = {}
  for (const item of inventory) {
    if (!item.equipped) continue
    if (item.imageUrlLight && item.imageUrlDark) {
      result[item.category] = { light: resolveUrl(item.imageUrlLight), dark: resolveUrl(item.imageUrlDark) }
    } else if (item.assetKey && assets[item.assetKey]) {
      result[item.category] = assets[item.assetKey]
    }
  }
  return result
}