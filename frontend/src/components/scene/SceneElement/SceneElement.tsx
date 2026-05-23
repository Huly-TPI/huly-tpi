import { Link } from 'react-router-dom'
import type { SceneElementImage } from '../types'
import './SceneElement.css'

export type SceneTheme = 'light' | 'dark'

interface SceneElementProps {
  title: string
  imageAlt: string
  image: SceneElementImage
  imageClassName: string
  placementClassName: string
  hotspotClassName: string
  clipPath: string
  tooltipClassName?: string
  theme?: SceneTheme
  to?: string
}

export default function SceneElement({
  title,
  imageAlt,
  image,
  imageClassName,
  placementClassName,
  hotspotClassName,
  clipPath,
  tooltipClassName = '',
  theme = 'light',
  to,
}: SceneElementProps) {
  const sharedClassName = [
    'absolute',
    'block',
    'rounded-full',
    'cursor-pointer',
    'transition duration-200',
    'focus-visible:outline-none',
    'focus-visible:ring-4',
    'focus-visible:ring-white/90',
    'focus-visible:ring-offset-2',
    'focus-visible:ring-offset-[#7da261]',
  ].join(' ')

  const imageSrc = theme === 'dark' && image.dark ? image.dark : image.light

  return (
    <div className={`group scene-element absolute hover:z-[120] focus-within:z-[120] ${placementClassName}`}>
      <img
        src={imageSrc}
        alt={imageAlt}
        className={`scene-element__image scene-element__shadow pointer-events-none select-none transition duration-200 ${imageClassName}`}
      />

      <div
        aria-hidden="true"
        className={`scene-element__tooltip pointer-events-none absolute left-1/2 z-[130] w-max max-w-[12rem] -translate-x-1/2 rounded-2xl border border-white/50 bg-[#fffaf0]/95 px-3 py-2 text-center shadow-lg backdrop-blur-sm ${tooltipClassName}`}
      >
        <p className="text-sm font-semibold text-[#5e4030]">{title}</p>
      </div>

      {to ? (
        <Link
          to={to}
          aria-label={title}
          className={`scene-element__hotspot ${sharedClassName} ${hotspotClassName}`}
          style={{ clipPath }}
        />
      ) : (
        <button
          type="button"
          aria-label={title}
          className={`scene-element__hotspot ${sharedClassName} ${hotspotClassName}`}
          style={{ clipPath }}
        />
      )}
    </div>
  )
}
