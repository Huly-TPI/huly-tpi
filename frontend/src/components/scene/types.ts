export interface SceneElementImage {
  light: string
  dark?: string
}

export interface SceneElementDefinition {
  id: string
  title: string
  imageAlt: string
  image: SceneElementImage
  imageClassName: string
  imageVariantClassName?: string
  placementClassName: string
  hotspotClassName: string
  clipPath: string
  interactive?: boolean
  tooltipClassName?: string
  to?: string
}
