import { useEffect } from 'react'
import { BadgeResponse } from '../../api/badges'
import { XMarkIcon } from '@heroicons/react/24/solid'

interface BadgeUnlockToastProps {
  badge: BadgeResponse | null
  onDismiss: () => void
}

export default function BadgeUnloackToast({ badge, onDismiss }: BadgeUnlockToastProps) {
  useEffect(() => {
    if (!badge) return
    const timer = setTimeout(onDismiss, 10000)
    return () => clearTimeout(timer)
  }, [badge, onDismiss])

  if (!badge) return null

  return (
     <div
            role="status"
            className="fixed bottom-24 left-5 z-50 flex items-center gap-4 rounded-2xl bg-gradient-to-r from-[#8869AC] to-[#A07CC5] 
            px-5 py-4 shadow-xl text-white"
        >
            <img
                src={badge.imageUrl ?? ''}
                alt={badge.name}
                className="h-16 w-16 rounded-full object-cover ring-2 ring-white/40"
            />
            <div>
                <p className="text-sm opacity-80">✨ ¡Nueva estampita!</p>
                <p className="font-nunito text-lg font-bold">{badge.name}</p>
            </div>
            <button
                aria-label="Cerrar"
                onClick={onDismiss}
                className="ml-1 rounded-full p-1.5 hover:bg-white/20 transition"
            >
                <XMarkIcon className="h-5 w-5" />
            </button>
        </div>
  )
}