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
import purpleShirt from '../../assets/avatar/remera-violeta-completa.webp'
import pinkShirt from '../../assets/avatar/remera-rosa-completa.webp'
import blueShirt from '../../assets/avatar/remera-azul-completa.webp'
import mustardShirt from '../../assets/avatar/remera-mostaza-completa.webp'
import tealShirt from '../../assets/avatar/remera-turquesa-completa.webp'
import crimsonShirt from '../../assets/avatar/remera-carmesi-completa.webp'
import lavenderShirt from '../../assets/avatar/remera-lavanda-completa.webp'
import charcoalShirt from '../../assets/avatar/remera-carbon-completa.webp'

import overallsPink from '../../assets/avatar/jardinero-rosa.webp'
import overallsGrey from '../../assets/avatar/jardinero-gris.webp'
import overallsMustard from '../../assets/avatar/jardinero-mostaza.png'
import overallsCharcoal from '../../assets/avatar/jardinero-carbon.webp'

import blueCap from '../../assets/avatar/vicera-azul.webp'
import brownCap from '../../assets/avatar/vicera-cafe.webp'
import pinkCap from '../../assets/avatar/vicera-rosa.webp'
import tealCap from '../../assets/avatar/vicera-turquesa.webp'
import crimsonCap from '../../assets/avatar/vicera-carmesi.webp'
import charcoalCap from '../../assets/avatar/vicera-carbon.webp'

import whiteSneakers from '../../assets/avatar/par-zapatilla-blanca.webp'
import redSneakers from '../../assets/avatar/par-zapatillas-rojas.webp'
import blackSneakers from '../../assets/avatar/par-zapatillas-negras.webp'
import greenSneakers from '../../assets/avatar/par-zapatillas-verdes.webp'

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
  'remera-rosa': { light: pinkShirt, dark: pinkShirt },
  'remera-azul': { light: blueShirt, dark: blueShirt },
  'remera-mostaza': { light: mustardShirt, dark: mustardShirt },
  'remera-turquesa': { light: tealShirt, dark: tealShirt },
  'remera-carmesi': { light: crimsonShirt, dark: crimsonShirt },
  'remera-lavanda': { light: lavenderShirt, dark: lavenderShirt },
  'remera-carbon': { light: charcoalShirt, dark: charcoalShirt },

  'jardinero-rosa': { light: overallsPink, dark: overallsPink },
  'jardinero-gris': { light: overallsGrey, dark: overallsGrey },
  'jardinero-mostaza': { light: overallsMustard, dark: overallsMustard },
  'jardinero-carbon': { light: overallsCharcoal, dark: overallsCharcoal },

  'vicera-azul': { light: blueCap, dark: blueCap },
  'vicera-cafe': { light: brownCap, dark: brownCap },
  'vicera-rosa': { light: pinkCap, dark: pinkCap },
  'vicera-turquesa': { light: tealCap, dark: tealCap },
  'vicera-carmesi': { light: crimsonCap, dark: crimsonCap },
  'vicera-carbon': { light: charcoalCap, dark: charcoalCap },

  'zapatillas-blancas': { light: whiteSneakers, dark: whiteSneakers },
  'zapatillas-rojas': { light: redSneakers, dark: redSneakers },
  'zapatillas-negras': { light: blackSneakers, dark: blackSneakers },
  'zapatillas-verdes': { light: greenSneakers, dark: greenSneakers },
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