import { useEffect } from 'react'
import darkBackgroundImage from '../../assets/minigames/dark-theme/background/Background-minigames.webp'
import darkMobileBackgroundImage from '../../assets/minigames/dark-theme/background/mobile/background-minigames-mobile.webp'
import backgroundImage from '../../assets/minigames/light-theme/background/Background-minigames.webp'
import mobileBackgroundImage from '../../assets/minigames/light-theme/background/mobile/background-minigames-mobile.webp'

import darkCloudImage from '../../assets/garden/dark-theme/cloud.webp'
import darkTreeImage from '../../assets/garden/dark-theme/tree.webp'
import cloudImage from '../../assets/garden/light-theme/cloud.webp'
import treeImage from '../../assets/garden/light-theme/tree.webp'
import darkLanternImage from '../../assets/lanterns/dark-theme/lantern-dark.webp'
import lanternImage from '../../assets/lanterns/ligth-theme/Lantern-Ligth.webp'
import darkFishImage from '../../assets/minigames/dark-theme/fish.webp'
import darkEaselImage from '../../assets/minigames/dark-theme/paddle.webp'
// import darkStonesImage from '../../assets/minigames/dark-theme/rocks.webp'
import darkSandImage from '../../assets/minigames/dark-theme/sand.webp'
import fishImage from '../../assets/minigames/light-theme/fish.webp'
import easelImage from '../../assets/minigames/light-theme/paddle.webp'
// import stonesImage from '../../assets/minigames/light-theme/rocks.webp'
import sandImage from '../../assets/minigames/light-theme/sand.webp'

import SceneElement from '../../components/Scene/SceneElement/SceneElement'
import { resolveEquippedImages } from '../../components/Scene/cosmeticAssets'
import type { SceneElementDefinition } from '../../components/Scene/types'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'
import { useTheme } from '../../context/theme'
import { useInventory } from '../../hooks/store/useInventory'
import './Minigames.css'

const FULL_WIDTH = 'w-full'
const DEFAULT_HOTSPOT = 'left-[2%] top-[4%] h-[92%] w-[96%]'
const RECT_CLIP_PATH = 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)'

interface DecorativeCloud {
    id: string
    placementClassName: string
}

const cloudElements: DecorativeCloud[] = [
    { id: 'cloud-left',   placementClassName: 'left-[8%]  top-[16%] z-[5] w-[24%] md:left-[18%] md:top-[12%] md:w-[11%]' },
    { id: 'cloud-center', placementClassName: 'left-[38%] top-[24%] z-[5] w-[28%] md:left-[45%] md:top-[7%]  md:w-[15%]' },
    { id: 'cloud-right',  placementClassName: 'left-[64%] top-[18%] z-[5] w-[22%] md:left-[76%] md:top-[14%] md:w-[11%]' },
]

const createLanternElement = (
    id: string,
    placementClassName: string,
    tooltipClassName = 'top-full mt-2',
): SceneElementDefinition => ({
    id,
    title: 'Farolitos que vuelan',
    imageAlt: 'Farol del jardín para soltar pensamientos',
    image: { light: lanternImage, dark: darkLanternImage },
    placementClassName,
    imageClassName: FULL_WIDTH,
    hotspotClassName: DEFAULT_HOTSPOT,
    clipPath: RECT_CLIP_PATH,
    tooltipClassName,
    to: '/lanterns',
})

const lanternElements: SceneElementDefinition[] = [
    createLanternElement(
        'lantern-top-left',
        'left-[7%] top-[7%] z-10 w-[10%] md:left-[8%] md:top-[6%] md:w-[8.8%]',
    ),
    createLanternElement(
        'lantern-upper-left',
        'left-[25%] top-[9%] z-10 w-[22%] md:left-[29%] md:top-[9%] md:w-[11%]',
    ),
    createLanternElement(
        'lantern-center',
        'left-[47%] top-[1%] z-10 w-[24%] md:left-[56%] md:top-[1%] md:w-[12%]',
    ),
    createLanternElement(
        'lantern-right',
        'left-[82%] top-[9%] z-10 w-[10%] md:left-[82.6%] md:top-[9%] md:w-[7.5%]',
    ),
]

const minigameElements: SceneElementDefinition[] = [
    {
        id: 'bubbles',
        title: 'Burbujas',
        imageAlt: 'Pez en el lago minijuego de burbujas',
        image: { light: fishImage, dark: darkFishImage },
        placementClassName: 'left-[1%] top-[61%] z-20 w-[36%] md:left-[19%] md:top-[64%] md:w-[19%]',
        imageClassName: FULL_WIDTH,
        hotspotClassName: DEFAULT_HOTSPOT,
        clipPath: RECT_CLIP_PATH,
        tooltipClassName: 'bottom-full mb-2',
        to: '/bubbles',
    },
    // {
    //     id: 'stones',
    //     title: 'Piedras del lago',
    //     imageAlt: 'Piedras a la orilla del lago',
    //     image: { light: stonesImage, dark: darkStonesImage },
    //     placementClassName: 'left-[38%] top-[74%] z-30 w-[28%] md:left-[58%] md:top-[80%] md:w-[9%]',
    //     imageClassName: FULL_WIDTH,
    //     hotspotClassName: DEFAULT_HOTSPOT,
    //     clipPath: RECT_CLIP_PATH,
    //     tooltipClassName: 'bottom-full mb-2',
    //     to: '/stones',
    // },
    {
        id: 'mandalas',
        title: 'Colorear mandalas',
        imageAlt: 'Atril para pintar mandalas',
        image: { light: easelImage, dark: darkEaselImage },
        placementClassName: 'left-[33%] top-[47%] z-20 w-[34%] md:left-[53%] md:top-[38%] md:w-[11%]',
        imageClassName: FULL_WIDTH,
        hotspotClassName: DEFAULT_HOTSPOT,
        clipPath: RECT_CLIP_PATH,
        tooltipClassName: 'bottom-full mb-2',
        to: '/mandalas',
    },
    {
        id: 'zen-sand-garden',
        title: 'Arena zen',
        imageAlt: 'Tablero de arena para dibujar libremente',
        image: { light: sandImage, dark: darkSandImage },
        placementClassName: 'left-[68%] top-[67%] z-30 w-[34%] md:left-[70%] md:top-[62%] md:w-[15%]',
        imageClassName: FULL_WIDTH,
        hotspotClassName: DEFAULT_HOTSPOT,
        clipPath: RECT_CLIP_PATH,
        tooltipClassName: 'bottom-full mb-2',
        to: '/zen-sand-garden',
    },
]

export default function Minigames() {
    const { theme } = useTheme()
    const { inventory, loading: inventoryLoading } = useInventory()
    const equippedByCategory = resolveEquippedImages(inventory)
    const equippedTreeImage = equippedByCategory.TREE
    const homeReturnElement: SceneElementDefinition = {
        id: 'home-return-tree',
        title: 'Volver al jardin',
        imageAlt: 'Fragmento del arbol del jardin',
        image: equippedTreeImage ?? { light: treeImage, dark: darkTreeImage },
        placementClassName: 'minigames-home-return',
        imageClassName: 'minigames-home-return__image',
        hotspotClassName: 'minigames-home-return__hotspot',
        clipPath: 'polygon(8% 14%, 24% 2%, 70% 2%, 92% 10%, 100% 24%, 100% 100%, 18% 100%, 8% 64%)',
        tooltipClassName: 'left-[24%] top-[14%] -translate-x-1/2',
        to: '/',
    }
    const sceneElements = [...lanternElements, ...minigameElements, homeReturnElement]
    const isDark = theme === 'dark'

    useEffect(() => {
        const sources = new Set<string>([
            backgroundImage, mobileBackgroundImage, darkBackgroundImage, darkMobileBackgroundImage,
            cloudImage, darkCloudImage,
        ])
        for (const element of sceneElements) {
            sources.add(element.image.light)
            if (element.image.dark) sources.add(element.image.dark)
        }
        sources.forEach(src => {
            const img = new Image()
            img.src = src
        })
    }, [])

    return (
        <main className={`minigames-page ${theme === 'dark' ? 'minigames-page--dark' : ''}`}>
            <section className="minigames-scene">
                <ThemeBackground
                    lightSrc={backgroundImage}
                    lightMobileSrc={mobileBackgroundImage}
                    darkSrc={darkBackgroundImage}
                    darkMobileSrc={darkMobileBackgroundImage}
                    lightAlt="Fondo de minijuegos"
                    darkAlt="Fondo nocturno de minijuegos"
                />

                {cloudElements.map(cloud => (
                    <div
                        key={cloud.id}
                        aria-hidden="true"
                        className={`absolute pointer-events-none select-none ${cloud.placementClassName}`}
                    >
                        <img src={isDark ? darkCloudImage : cloudImage} alt="" className="w-full" />
                    </div>
                ))}

                <div className={`absolute inset-0 z-10 transition-opacity duration-300 ${inventoryLoading ? 'opacity-0' : 'opacity-100'}`}>
                    {sceneElements.map(element => (
                        <SceneElement key={element.id} theme={theme} {...element} />
                    ))}
                </div>
            </section>
        </main>
    )
}
