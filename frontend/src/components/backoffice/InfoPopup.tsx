import { useState, useRef, useEffect, ReactNode } from 'react'
import { Info } from 'lucide-react'

interface InfoPopupProps {
  title: string
  children: ReactNode
}

export default function InfoPopup({ title, children }: InfoPopupProps) {
  const [isOpen, setIsOpen] = useState(false)
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (ref.current && !ref.current.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  return (
    <div className="relative inline-block ml-1.5 align-middle" ref={ref}>
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="text-gray-400 hover:text-violeta transition-colors focus:outline-none flex items-center justify-center p-0.5 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full"
        type="button"
        title="Click para ver información"
      >
        <Info className="h-3.5 w-3.5" />
      </button>

      {isOpen && (
        <div className="absolute top-full left-1/2 -translate-x-1/2 mt-2 w-80 rounded-2xl bg-white dark:bg-gray-900 p-4 shadow-2xl border border-gray-200 dark:border-gray-850 z-50 flex flex-col gap-2.5 text-xs leading-relaxed text-left text-gray-700 dark:text-gray-200 font-normal normal-case tracking-normal">
          <div className="flex items-center justify-between font-bold text-gray-850 dark:text-white border-b border-gray-100 dark:border-gray-800 pb-1.5">
            <span className="flex items-center gap-1.5 text-violeta dark:text-violeta-claro">
              <Info className="h-3.5 w-3.5" /> {title}
            </span>
            <button 
              onClick={() => setIsOpen(false)} 
              className="text-gray-400 hover:text-gray-600 dark:hover:text-white focus:outline-none"
              type="button"
            >
              ✕
            </button>
          </div>
          <div className="flex flex-col gap-2 font-normal text-gray-600 dark:text-gray-300">
            {children}
          </div>
        </div>
      )}
    </div>
  )
}
