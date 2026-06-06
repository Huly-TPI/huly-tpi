const imageModules = import.meta.glob('../assets/images/*.png', { eager: false })

export function preloadImages() {
    Object.values(imageModules).forEach(loader => {
    (loader as () => Promise<{ default: string }>)().then(mod => {
      const img = new Image()
      img.src = mod.default
    }).catch(() => {})
  })
}