import dayBackgroundImage from '../../assets/garden/light-theme/background/day-background.webp'
import { useEffect } from 'react'
import cloudImage from '../../assets/garden/light-theme/cloud.webp'
import houseImage from '../../assets/garden/light-theme/house.webp'
import notebookImage from '../../assets/garden/light-theme/notebook.webp'
import todoBoardImage from '../../assets/garden/light-theme/to-do-board.webp'
import treeImage from '../../assets/garden/light-theme/tree.webp'
import wateringCanImage from '../../assets/garden/light-theme/watering-can.webp'
import nightBackgroundImage from '../../assets/garden/dark-theme/background/night-background.webp'
import darkHouseImage from '../../assets/garden/dark-theme/house.webp'
import darkNotebookImage from '../../assets/garden/dark-theme/notebook.webp'
import darkTodoBoardImage from '../../assets/garden/dark-theme/to-do-board.webp'
import darkTreeImage from '../../assets/garden/dark-theme/tree.webp'
import darkWateringCanImage from '../../assets/garden/dark-theme/watering-can.webp'
import darkCloudImage from '../../assets/garden/dark-theme/cloud.webp'
import ThemeToggle from '../../components/ThemeToggle/ThemeToggle'
import SceneElement, { type SceneTheme } from '../../components/scene/SceneElement/SceneElement'
import type { SceneElementDefinition } from '../../components/scene/types'
import { useTheme } from '../../context/theme'
import './Home.css'

const THEME_BEHAVIOR: Record<SceneTheme, { restrictedElementIds: Set<string> }> = {
  light: {
    restrictedElementIds: new Set<string>(),
  },
  dark: {
    restrictedElementIds: new Set(['tree', 'watering-can', 'notebook']),
  },
}

const cloudClipPath =
  'polygon(1% 60%, 7% 38%, 22% 32%, 33% 12%, 57% 0%, 76% 18%, 84% 34%, 98% 41%, 100% 62%, 86% 72%, 74% 88%, 52% 96%, 30% 89%, 17% 79%, 5% 74%)'

const createCloudElement = (
  id: string,
  placementClassName: string,
  tooltipClassName = 'top-full mt-2',
): SceneElementDefinition => ({
  id,
  title: 'Respiraciones guiadas',
  imageAlt: 'Nube del jardin para respiraciones guiadas',
  image: { light: cloudImage, dark: darkCloudImage },
  placementClassName,
  imageClassName: 'w-full',
  hotspotClassName: 'left-[2%] top-[5%] h-[88%] w-[96%]',
  clipPath: cloudClipPath,
  tooltipClassName,
  to: '/guided-breathing',
})

const cloudElements: SceneElementDefinition[] = [
  createCloudElement('cloud-top-left', 'left-[8%] top-[2.5%] z-10 w-[12%] md:left-[8%] md:top-[1.2%] md:w-[11%]'),
  createCloudElement('cloud-upper-left', 'left-[25%] top-[7%] z-10 w-[18%] md:left-[22%] md:top-[7.2%] md:w-[16.5%]'),
  createCloudElement('cloud-center', 'left-[47%] top-[8%] z-10 w-[23%] md:left-[46.8%] md:top-[5.2%] md:w-[25%]'),
  createCloudElement('cloud-right', 'left-[82%] top-[11%] z-10 w-[10%] md:left-[82.6%] md:top-[8.8%] md:w-[9.5%]'),
  createCloudElement('cloud-bottom-left', 'hidden md:block md:left-[-3.2%] md:top-[30%] md:z-10 md:w-[8.5%]', 'top-full mt-1'),
  createCloudElement('cloud-bottom-right', 'hidden md:block md:left-[88.2%] md:top-[39.5%] md:z-10 md:w-[12.5%]', 'top-full mt-1'),
]

const gardenElements: SceneElementDefinition[] = [
  {
    id: 'tree',
    title: 'Minijuegos',
    imageAlt: 'Arbol con hamaca en el jardin',
    image: { light: treeImage, dark: darkTreeImage },
    placementClassName: 'left-[8%] top-[24%] z-20 w-[25%] md:left-[2.5%] md:top-[18%] md:w-[29%]',
    imageClassName: 'w-full',
    hotspotClassName: 'left-[5%] top-[3%] h-[94%] w-[88%]',
    clipPath: 'polygon(8% 23%, 22% 8%, 53% 2%, 84% 10%, 98% 35%, 91% 55%, 79% 59%, 73% 97%, 34% 99%, 26% 64%, 5% 54%)',
    tooltipClassName: 'top-[6%]',
    to: '/minigames',
  },
  {
    id: 'house',
    title: 'Perfil',
    imageAlt: 'Casa con forma de hongo en el jardin',
    image: { light: houseImage, dark: darkHouseImage },
    placementClassName: 'left-[39%] top-[35%] z-30 w-[21%] md:left-[38.2%] md:top-[28.2%] md:w-[24.8%]',
    imageClassName: 'w-full',
    hotspotClassName: 'left-[6%] top-[1%] h-[92%] w-[88%]',
    clipPath: 'polygon(8% 40%, 26% 10%, 52% 1%, 79% 8%, 95% 33%, 93% 79%, 73% 94%, 24% 96%, 6% 80%)',
    tooltipClassName: 'bottom-full mb-2',
    to: '/profile',
  },
  {
    id: 'todo-board',
    title: 'Pendientes',
    imageAlt: 'Cartel de pendientes en el jardin',
    image: { light: todoBoardImage, dark: darkTodoBoardImage },
    placementClassName: 'left-[72%] top-[52%] z-20 w-[11.5%] md:left-[66.2%] md:top-[44%] md:w-[10.5%]',
    imageClassName: 'w-full',
    hotspotClassName: 'left-[7%] top-[1%] h-[98%] w-[86%]',
    clipPath: 'polygon(12% 5%, 84% 0%, 97% 13%, 98% 99%, 7% 99%, 1% 15%)',
    tooltipClassName: 'bottom-full mb-2',
    to: '/pending',
  },
  {
    id: 'watering-can',
    title: 'Retos',
    imageAlt: 'Regadera y maceta en el jardin',
    image: { light: wateringCanImage, dark: darkWateringCanImage },
    placementClassName: 'left-[28%] top-[74%] z-30 w-[17%] md:left-[26%] md:top-[70.4%] md:w-[11.5%]',
    imageClassName: 'w-full',
    hotspotClassName: 'left-[2%] top-[4%] h-[92%] w-[96%]',
    clipPath: 'polygon(3% 56%, 18% 16%, 47% 12%, 68% 2%, 96% 26%, 94% 95%, 56% 97%, 34% 82%, 8% 83%)',
    tooltipClassName: 'bottom-full mb-1',
    to: '/challenges',
  },
  {
    id: 'notebook',
    title: 'Diario',
    imageAlt: 'Banco con cuaderno en el jardin',
    image: { light: notebookImage, dark: darkNotebookImage },
    placementClassName: 'left-[68%] top-[78%] z-30 w-[20%] md:left-[73%] md:top-[70.8%] md:w-[14.5%]',
    imageClassName: 'w-full',
    hotspotClassName: 'left-[2%] top-[8%] h-[84%] w-[96%]',
    clipPath: 'polygon(9% 31%, 93% 9%, 99% 44%, 85% 95%, 18% 96%, 1% 56%)',
    tooltipClassName: 'bottom-full mb-2',
    to: '/diary',
  },
]

const sceneElements = [...cloudElements, ...gardenElements]

export default function Home() {
  const { theme: sceneTheme } = useTheme()

  useEffect(() => {
    const sources = new Set<string>([dayBackgroundImage, nightBackgroundImage])
    for (const element of sceneElements) {
      sources.add(element.image.light)
      if (element.image.dark) {
        sources.add(element.image.dark)
      }
    }

    sources.forEach(src => {
      const image = new Image()
      image.src = src
    })
  }, [])

  const currentThemeBehavior = THEME_BEHAVIOR[sceneTheme]
  const renderedSceneElements = sceneElements.map(element => {
    const isRestrictedForTheme = currentThemeBehavior.restrictedElementIds.has(element.id)
    if (!isRestrictedForTheme) return element

    return {
      ...element,
      interactive: false,
    }
  })

  return (
    <main className={`garden-page ${sceneTheme === 'dark' ? 'garden-page--dark' : ''}`}>
      <section className="garden-scene">
        <div className="garden-scene__theme-toggle">
          <ThemeToggle />
        </div>

        <img
          src={dayBackgroundImage}
          alt="Fondo del jardin"
          className={`garden-scene__background garden-scene__background--light pointer-events-none select-none ${sceneTheme === 'light' ? 'garden-scene__background--active' : ''}`}
        />
        <img
          src={nightBackgroundImage}
          alt=""
          aria-hidden="true"
          className={`garden-scene__background garden-scene__background--dark pointer-events-none select-none ${sceneTheme === 'dark' ? 'garden-scene__background--active' : ''}`}
        />

        <div className="absolute inset-0">
          {renderedSceneElements.map(element => (
            <SceneElement key={element.id} theme={sceneTheme} {...element} />
          ))}
        </div>
      </section>
    </main>
  )
}
