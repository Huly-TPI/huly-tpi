const criticalModules = import.meta.glob(
  [
    '../assets/garden/**/*.{webp,avif,png,jpg,jpeg,gif,svg}',
    '../assets/shop-seed/**/*.{webp,avif,png,jpg,jpeg,gif,svg}',
  ],
  { eager: true, query: '?url', import: 'default' },
)

export const criticalImageUrls: string[] = Object.values(criticalModules) as string[]
