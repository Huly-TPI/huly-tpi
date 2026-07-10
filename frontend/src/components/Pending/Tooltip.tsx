import { useRef, useState, type PropsWithChildren, type ReactNode } from 'react'
import { createPortal } from 'react-dom'

export interface TooltipProps {
  label: ReactNode
  wrap?: boolean
  align?: 'center' | 'start'
}

export default function Tooltip({ label, wrap = false, align = 'center', children }: PropsWithChildren<TooltipProps>) {
  const triggerRef = useRef<HTMLSpanElement>(null)
  const [coords, setCoords] = useState<{ top: number; left: number } | null>(null)
  const isStart = align === 'start'

  const show = () => {
    const rect = triggerRef.current?.getBoundingClientRect()
    if (!rect) return
    setCoords({ top: rect.top - 8, left: isStart ? rect.left : rect.left + rect.width / 2 })
  }

  const hide = () => setCoords(null)

  return (
    <span
      ref={triggerRef}
      className="relative inline-flex"
      onMouseEnter={show}
      onMouseLeave={hide}
      onFocus={show}
      onBlur={hide}
    >
      {children}
      {coords &&
        createPortal(
          <span
            role="tooltip"
            className={`pointer-events-none fixed z-[9999] -translate-y-full rounded-md bg-[#3b2510] px-2 py-1 text-[0.75rem] font-medium text-[#fff8e7] shadow-[0_4px_10px_rgba(0,0,0,0.25)] ${
              isStart ? '' : '-translate-x-1/2'
            } ${wrap ? 'w-28 whitespace-normal text-center leading-tight' : 'whitespace-nowrap'}`}
            style={{ top: coords.top, left: coords.left }}
          >
            {label}
          </span>,
          document.body,
        )}
    </span>
  )
}
