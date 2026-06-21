export type MandalaAccessStatus =
  | 'available'
  | 'locked'
  | 'premium'
  | 'included'
  | 'comingSoon'

export type MandalaUnlockSource =
  | 'free'
  | 'mockUnlocked'
  | 'premiumPlan'
  | 'futureRelease'

export interface MandalaCatalogItem {
  id: string
  title: string
  description?: string
  src: string
  previewSrc?: string
  accessStatus: MandalaAccessStatus
  unlockSource: MandalaUnlockSource
}

export function isMandalaPaintable(mandala: MandalaCatalogItem) {
  return mandala.accessStatus === 'available' || mandala.accessStatus === 'included'
}
