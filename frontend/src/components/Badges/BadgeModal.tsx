import { useBadges } from '../../hooks/useBadges'
import BadgeCard from './BadgeCard'
import { X } from 'lucide-react'

interface BadgeModalProps {
    isOpen: boolean
    onClose: () => void
}

export default function BadgeModal({ isOpen, onClose }: BadgeModalProps) {
    const { badges, loading } = useBadges()
    if (!isOpen) return null

    const earned = badges.filter(b => b.unlocked).length
    const progress = badges.length ? (earned / badges.length) * 100 : 0

    return (
        <div className="fixed inset-0 z-[400] flex items-center justify-center overflow-y-auto bg-[var(--overlay-strong)] p-4 backdrop-blur-sm">
            <div
                className="relative flex w-full max-w-sm overflow-hidden rounded-2xl shadow-2xl sm:max-w-lg md:max-w-xl"
                style={{ boxShadow: '0 32px 64px rgba(0,0,0,0.4), 0 0 0 1px rgba(136,105,172,0.2)' }}
            >
                <div className="flex w-6 flex-shrink-0 flex-col items-center justify-start gap-[13px] bg-gradient-to-b from-[#5C3F7A] to-[#8869AC] py-5 sm:w-9">
                    {[...Array(9)].map((_, i) => (
                        <div key={i} className="h-3 w-3 rounded-full border-2 border-white/40 bg-white/15 shadow-inner sm:h-4 sm:w-4" />
                    ))}
                </div>

                <div className="flex flex-1 flex-col">
                    <div className="bg-gradient-to-r from-[#7A5A9E] to-[#9B7BBF] px-4 py-4 text-white sm:px-6 sm:py-5">
                        <div className="flex items-start justify-between">
                            <div>
                                <p className="text-[11px] font-medium uppercase tracking-[0.18em] opacity-60 lg:text-[12px]">Mi álbum de estampitas</p>
                                <h2 className="font-nunito text-2xl font-black leading-tight sm:text-3xl">Mis estampitas</h2>
                                <div className="mt-2 flex items-center gap-3 sm:mt-3">
                                    <div className="h-2.5 w-32 overflow-hidden rounded-full bg-white/20 shadow-inner sm:h-3 sm:w-44">
                                        <div
                                            className="h-full rounded-full bg-gradient-to-r from-yellow-300 to-white/90 transition-all duration-700"
                                            style={{ width: `${progress}%` }}
                                        />
                                    </div>
                                    <span className="text-xs font-semibold opacity-80">{earned}/{badges.length}</span>
                                </div>
                            </div>
                            <button
                                onClick={onClose}
                                aria-label="Cerrar"
                                className="rounded-full p-1.5 transition hover:bg-white/20"
                            >
                                <X className="h-5 w-5" strokeWidth={2} />
                            </button>
                        </div>
                    </div>
                    <div
                        className="h-4 bg-[#8869AC]"
                        style={{
                            backgroundImage: 'radial-gradient(circle at 10px 100%, #fdf8f0 9px, transparent 10px)',
                            backgroundSize: '20px 18px',
                            backgroundRepeat: 'repeat-x',
                            backgroundPosition: 'bottom',
                        }}
                    />
                    <div
                        className="flex-1 px-4 py-4 sm:px-6 sm:py-5"
                        style={{
                            background: '#fdf8f0',
                            backgroundImage: `repeating-linear-gradient(
                                0deg,
                                transparent,
                                transparent 35px,
                                #e2d4f028 35px,
                                #e2d4f028 36px
                            )`,
                        }}
                    >
                        {loading ? (
                            <p className="py-6 text-center text-sm text-[#9B7BBF]">Cargando estampitas...</p>
                        ) : (
                            <div className="grid grid-cols-3 gap-3 sm:grid-cols-4 sm:gap-5">
                                {badges.map(({ badge, unlocked, obtainedAt }) => (
                                    <BadgeCard
                                        key={badge.code}
                                        badge={badge}
                                        unlocked={unlocked}
                                        obtainedAt={obtainedAt}
                                    />
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    )
}
