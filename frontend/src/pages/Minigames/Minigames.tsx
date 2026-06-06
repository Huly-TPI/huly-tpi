import { useEffect } from 'react'
import backgroundImage from '../../assets/minigames/light-theme/background/Background-minigames.webp'
import mobileBackgroundImage from '../../assets/minigames/light-theme/background/mobile/background-minigames-mobile.webp'

import fishImage from '../../assets/minigames/light-theme/fish.webp'
import easelImage from '../../assets/minigames/light-theme/paddle.webp'
import stonesImage from '../../assets/minigames/light-theme/rocks.webp'
import sandImage from '../../assets/minigames/light-theme/sand.webp'
import cloudImage from '../../assets/garden/light-theme/cloud.webp'

import SceneElement from '../../components/Scene/SceneElement/SceneElement'
import type { SceneElementDefinition } from '../../components/Scene/types'
import './Minigames.css'
import BackButton from '../../components/Buttons/BackButton/BackButton'

const FULL_WIDTH = 'w-full'
const DEFAULT_HOTSPOT = 'left-[2%] top-[4%] h-[92%] w-[96%]'
const CLOUD_HOTSPOT = 'left-[2%] top-[5%] h-[88%] w-[96%]'
const RECT_CLIP_PATH = 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)'
const CLOUD_CLIP_PATH =
    'polygon(1% 60%, 7% 38%, 22% 32%, 33% 12%, 57% 0%, 76% 18%, 84% 34%, 98% 41%, 100% 62%, 86% 72%, 74% 88%, 52% 96%, 30% 89%, 17% 79%, 5% 74%)'

const createCloudElement = (
    id: string,
    placementClassName: string,
): SceneElementDefinition => ({
    id,
    title: 'Nubes que pasan',
    imageAlt: 'Nubes que pasan',
    image: { light: cloudImage },
    placementClassName,
    imageClassName: FULL_WIDTH,
    hotspotClassName: CLOUD_HOTSPOT,
    clipPath: CLOUD_CLIP_PATH,
    tooltipClassName: 'top-full mt-2',
    to: '/clouds',
})

const cloudElements: SceneElementDefinition[] = [
    createCloudElement('cloud-left', 'left-[8%] top-[16%] z-10 w-[24%] md:left-[18%] md:top-[12%] md:w-[11%]'),
    createCloudElement('cloud-center', 'left-[38%] top-[24%] z-10 w-[28%] md:left-[45%] md:top-[7%] md:w-[15%]'),
    createCloudElement('cloud-right', 'left-[64%] top-[18%] z-10 w-[22%] md:left-[76%] md:top-[14%] md:w-[11%]'),
]

const minigameElements: SceneElementDefinition[] = [
    {
        id: 'bubbles',
        title: 'Burbujas',
        imageAlt: 'Pez en el lago minijuego de burbujas',
        image: { light: fishImage },
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
        image: { light: stonesImage },
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
        image: { light: easelImage },
        placementClassName: 'left-[43%] top-[47%] z-20 w-[34%] md:left-[53%] md:top-[38%] md:w-[11%]',
        imageClassName: FULL_WIDTH,
        hotspotClassName: DEFAULT_HOTSPOT,
        clipPath: RECT_CLIP_PATH,
        tooltipClassName: 'bottom-full mb-2',
        to: '/mandalas',
    },
    {
        id: 'free-draw',
        title: 'Arena zen',
        imageAlt: 'Tablero de arena para dibujar libremente',
        image: { light: sandImage },
        placementClassName: 'left-[68%] top-[67%] z-30 w-[34%] md:left-[70%] md:top-[62%] md:w-[15%]',
        imageClassName: FULL_WIDTH,
        hotspotClassName: DEFAULT_HOTSPOT,
        clipPath: RECT_CLIP_PATH,
        tooltipClassName: 'bottom-full mb-2',
        to: '/free-draw',
    },
]

const sceneElements = [...cloudElements, ...minigameElements]

export default function Minigames() {
    useEffect(() => {
        const sources = new Set<string>([backgroundImage, mobileBackgroundImage])
        for (const element of sceneElements) {
            sources.add(element.image.light)
        }
        sources.forEach(src => {
            const img = new Image()
            img.src = src
        })
    }, [])

    return (
        <main className="minigames-page">
            <section className="minigames-scene">
                <BackButton to="/" />
                <img
                    src={backgroundImage}
                    alt="Fondo del jardin de minijuegos"
                    className="minigames-scene__background minigames-scene__background--desktop pointer-events-none select-none"
                />
                <img
                    src={mobileBackgroundImage}
                    alt="Fondo del jardin de minijuegos para celular"
                    className="minigames-scene__background minigames-scene__background--mobile pointer-events-none select-none"
                />

                <div className="absolute inset-0">
                    {sceneElements.map(element => (
                        <SceneElement key={element.id} theme="light" {...element} />
                    ))}
                </div>
            </section>
        </main>
    )
}