import { BadgeResponse } from '../../api/badges'
import { LockClosedIcon } from '@heroicons/react/24/solid'

interface BadgeCardProps {
    badge: BadgeResponse
    unlocked: boolean
    obtainedAt: string | null
}

export default function BadgeCard({ badge, unlocked }: BadgeCardProps) {
    return (
         <div className="flex flex-col items-center gap-2">
            <div className={`relative flex h-[80px] w-[80px] items-center justify-center rounded-[20px] transition-all duration-300
                ${unlocked
                    ? 'bg-white shadow-[0_6px_16px_rgba(136,105,172,0.3)] ring-1 ring-[#8869AC]/20'
                    : 'border-2 border-dashed border-[#8869AC]/40 bg-[#fdf5e8]'
                }`}
            >
                <img
                    src={badge.imageUrl ?? ''}
                    alt={badge.name}
                    className={`h-14 w-14 object-contain transition-all duration-300 ${
                        unlocked ? 'opacity-100 drop-shadow-sm' : 'grayscale opacity-20'
                    }`}
                />
                {!unlocked && (
                    <div data-testid="lock-icon" className="absolute bottom-1.5 right-1.5">
                        <LockClosedIcon className="h-3.5 w-3.5 text-[#8869AC]" />
                    </div>
                )}
            </div>
            <p className={`text-center text-[11px] font-semibold leading-tight ${
                unlocked ? 'text-[#6A4F8C]' : 'text-[#B0A0C0]'
            }`}>
                {badge.name}
            </p>
        </div>
    )
}