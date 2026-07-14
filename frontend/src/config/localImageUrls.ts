import { criticalImageUrls } from './criticalImageUrls'

const assetModules = import.meta.glob(
  '../assets/**/*.{webp,avif,png,jpg,jpeg,gif,svg}',
  { eager: true, query: '?url', import: 'default' },
)

const assetUrls = Object.values(assetModules) as string[]

const publicUrls = [
  '/badges/badge_apertura.webp',
  '/badges/badge_calma.webp',
  '/badges/badge_constancia.webp',
  '/badges/badge_explorador.webp',
  '/badges/badge_launcher.webp',
  '/badges/badge_luz_interior.webp',
  '/badges/badge_primer_paso.webp',
  '/badges/badge_valentia.webp',
  '/badges/badge_voz_propia.webp',
]

export const localImageUrls: string[] = [...assetUrls, ...publicUrls]
const criticalSet = new Set(criticalImageUrls)
export const restImageUrls: string[] = localImageUrls.filter(
  (url) => !criticalSet.has(url),
)
