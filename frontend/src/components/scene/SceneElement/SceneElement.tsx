import { Link } from 'react-router-dom'
import type { SceneElementDefinition } from '../types'
import './SceneElement.css'

export type SceneTheme = 'light' | 'dark'

type SceneElementProps = SceneElementDefinition & {
  interactive?: boolean
  theme?: SceneTheme
}

export default function SceneElement({
  title,
  imageAlt,
  image,
  imageClassName,
  placementClassName,
  hotspotClassName,
  clipPath,
  interactive = true,
  tooltipClassName = '',
  theme = 'light',
  to,
}: SceneElementProps) {
  const sharedClassName = [
    'absolute',
    'block',
    'rounded-full',
    'transition duration-200',
    'focus-visible:outline-none',
    'focus-visible:ring-4',
    'focus-visible:ring-white/90',
    'focus-visible:ring-offset-2',
    'focus-visible:ring-offset-[#7da261]',
  ].join(' ')
  const hotspotInteractionClassName = interactive
    ? 'scene-element__hotspot--interactive cursor-pointer'
    : 'scene-element__hotspot--inactive cursor-default pointer-events-none'

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

      {to && interactive ? (
        <Link
          to={to}
          aria-label={title}
          className={`scene-element__hotspot ${hotspotInteractionClassName} ${sharedClassName} ${hotspotClassName}`}
          style={{ clipPath }}
        />
      ) : (
        <button
          type="button"
          aria-label={title}
          disabled={!interactive}
          className={`scene-element__hotspot ${hotspotInteractionClassName} ${sharedClassName} ${hotspotClassName}`}
          style={{ clipPath }}
        />
      )}
    </div>
  )
}
