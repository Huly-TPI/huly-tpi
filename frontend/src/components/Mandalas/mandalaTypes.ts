export type MandalaAccessStatus =
  | 'available'
  | 'included'

export type MandalaUnlockSource =
  | 'free'
  | 'premiumPlan'
  | 'purchased'

export interface MandalaCatalogItem {
  id: string
  title: string
  description?: string
  src: string
  previewSrc?: string
  accessStatus: MandalaAccessStatus
  unlockSource: MandalaUnlockSource
}
