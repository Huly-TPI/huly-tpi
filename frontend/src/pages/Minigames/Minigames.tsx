import { useEffect } from 'react'
import darkBackgroundImage from '../../assets/minigames/dark-theme/background/Background-minigames.webp'
import darkMobileBackgroundImage from '../../assets/minigames/dark-theme/background/mobile/background-minigames-mobile.webp'
import backgroundImage from '../../assets/minigames/light-theme/background/background-minigames.webp'
import mobileBackgroundImage from '../../assets/minigames/light-theme/background/mobile/background-minigames-mobile.webp'

import cloudImage from '../../assets/garden/light-theme/Cloud.webp'
import darkCloudImage from '../../assets/garden/dark-theme/Cloud.webp'
import darkLanternImage from '../../assets/lanterns/dark-theme/Lantern-Dark.webp'
import lanternImage from '../../assets/lanterns/ligth-theme/Lantern-Ligth.webp'
import darkFishImage from '../../assets/minigames/dark-theme/fish.webp'
import darkEaselImage from '../../assets/minigames/dark-theme/paddle.webp'
import darkStonesImage from '../../assets/minigames/dark-theme/rocks.webp'
import darkSandImage from '../../assets/minigames/dark-theme/sand.webp'
import fishImage from '../../assets/minigames/light-theme/fish.webp'
import easelImage from '../../assets/minigames/light-theme/paddle.webp'
import stonesImage from '../../assets/minigames/light-theme/rocks.webp'
import sandImage from '../../assets/minigames/light-theme/sand.webp'

import BackButton from '../../components/Buttons/BackButton/BackButton'
import SceneElement from '../../components/Scene/SceneElement/SceneElement'
import type { SceneElementDefinition } from '../../components/Scene/types'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'
import { useTheme } from '../../context/theme'
import './Minigames.css'

const FULL_WIDTH = 'w-full'
const DEFAULT_HOTSPOT = 'left-[2%] top-[4%] h-[92%] w-[96%]'
const RECT_CLIP_PATH = 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)'

interface DecorativeElement {
    id: string
    imageAlt: string
    image: { light: string; dark: string }
    placementClassName: string
    imageClassName: string
}

const cloudElements: DecorativeElement[] = [
    {
        id: 'cloud-upper-left',
        imageAlt: 'Nube decorativa',
        image: { light: cloudImage, dark: darkCloudImage },
        placementClassName: 'left-[12%] top-[4%] z-[5] w-[24%] md:left-[14%] md:top-[3%] md:w-[15%]',
        imageClassName: FULL_WIDTH,
    },
    {
        id: 'cloud-center',
        imageAlt: 'Nube decorativa',
        image: { light: cloudImage, dark: darkCloudImage },
        placementClassName: 'left-[44%] top-[2%] z-[5] w-[28%] md:left-[45%] md:top-[0%] md:w-[18%]',
        imageClassName: FULL_WIDTH,
    },
    {
        id: 'cloud-right',
        imageAlt: 'Nube decorativa',
        image: { light: cloudImage, dark: darkCloudImage },
        placementClassName: 'left-[74%] top-[5%] z-[5] w-[20%] md:left-[75%] md:top-[4%] md:w-[13%]',
        imageClassName: FULL_WIDTH,
    },
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
        'left-[21%] top-[9%] z-10 w-[22%] md:left-[22%] md:top-[9%] md:w-[13.2%]',
    ),
    createLanternElement(
        'lantern-center',
        'left-[47%] top-[1%] z-10 w-[24%] md:left-[48%] md:top-[1%] md:w-[16%]',
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
    {
        id: 'stones',
        title: 'Piedras del lago',
        imageAlt: 'Piedras a la orilla del lago',
        image: { light: stonesImage, dark: darkStonesImage },
        placementClassName: 'left-[38%] top-[74%] z-30 w-[28%] md:left-[58%] md:top-[80%] md:w-[9%]',
        imageClassName: FULL_WIDTH,
        hotspotClassName: DEFAULT_HOTSPOT,
        clipPath: RECT_CLIP_PATH,
        tooltipClassName: 'bottom-full mb-2',
        to: '/stones',
    },
    {
        id: 'mandalas',
        title: 'Colorear mandalas',
        imageAlt: 'Atril para pintar mandalas',
        image: { light: easelImage, dark: darkEaselImage },
        placementClassName: 'left-[43%] top-[47%] z-20 w-[34%] md:left-[53%] md:top-[38%] md:w-[11%]',
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

const interactiveElements = [...lanternElements, ...minigameElements]
const allImageSources: string[] = [
    ...cloudElements.flatMap(e => [e.image.light, e.image.dark]),
    ...interactiveElements.flatMap(e => {
        const sources = [e.image.light]
        if (e.image.dark) sources.push(e.image.dark)
        return sources
    }),
]

export default function Minigames() {
    const { theme } = useTheme()
    const isDark = theme === 'dark'

    useEffect(() => {
        const sources = new Set<string>([
            backgroundImage,
            mobileBackgroundImage,
            darkBackgroundImage,
            darkMobileBackgroundImage,
            ...allImageSources,
        ])
        sources.forEach(src => {
            const img = new Image()
            img.src = src
        })
    }, [])

    return (
        <main className={`minigames-page ${isDark ? 'minigames-page--dark' : ''}`}>
            <section className="minigames-scene">
                <BackButton to="/" />
                <ThemeBackground
                    lightSrc={backgroundImage}
                    lightMobileSrc={mobileBackgroundImage}
                    darkSrc={darkBackgroundImage}
                    darkMobileSrc={darkMobileBackgroundImage}
                    lightAlt="Fondo de minijuegos"
                    darkAlt="Fondo nocturno de minijuegos"
                />

                <div className="absolute inset-0 z-10">
                    {cloudElements.map(cloud => (
                        <div
                            key={cloud.id}
                            className={`absolute ${cloud.placementClassName}`}
                            aria-hidden="true"
                        >
                            <img
                                src={isDark ? cloud.image.dark : cloud.image.light}
                                alt={cloud.imageAlt}
                                className={cloud.imageClassName}
                                draggable={false}
                            />
                        </div>
                    ))}

                    {interactiveElements.map(element => (
                        <SceneElement key={element.id} theme={theme} {...element} />
                    ))}
                </div>
            </section>
        </main>
    )
}