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
  placementClassName: string
  hotspotClassName: string
  clipPath: string
  tooltipClassName?: string
  to?: string
}
