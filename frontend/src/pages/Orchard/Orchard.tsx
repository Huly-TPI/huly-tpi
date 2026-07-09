import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useUserPlants } from '../../hooks/useUserPlants'
import { type UserPlantSummaryResponse } from '../../api/userGoals'
import OrchardPlantSlot, { VISUAL_W, VISUAL_H } from '../../components/Orchard/OrchardPlantSlot'
import PlantGoalsModal from '../../components/Orchard/PlantGoalsModal'
import BackButton from '../../components/Buttons/BackButton/BackButton'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'
import dayBackground from '../../assets/shared/day-background.webp'
import nightBackground from '../../assets/shared/dark-background.webp'
import plotImg from '../../assets/challenges/plot.png'
import mobilePlotImg from '../../assets/challenges/mobile/plot.png'
import { useTheme } from '../../context/theme'
import { InlineError } from '../../components/feedback/InlineError'

const TOTAL_SLOTS = 12
const HISTORY_SLOTS = TOTAL_SLOTS - 1
const MOBILE_TOTAL_SLOTS = 9
const MOBILE_HISTORY_SLOTS = MOBILE_TOTAL_SLOTS - 1
const SLOT_WIDTH = VISUAL_W
const GAP = 12


const M_LEFT   = '5%'
const M_RIGHT  = '5%'

// Measured from mobile/plot.png (920x1350): top edge of each shelf plank sits at
// y = 209, 475, 780, 1083. Mobile shows 3 rows on the bottom 3 tiers, expressed as
// a % distance from the bottom of the image so a row's pots rest flush on the plank.
// POT_VISUAL_BOTTOM_INSET_PCT compensates for transparent padding at the bottom of the
// plant sprite's own bounding box (measured ~2.5% of image height) so the pot's actual
// drawn base lands on the plank instead of floating above it.
const POT_VISUAL_BOTTOM_INSET_PCT = 2.5
const MOBILE_TIER_BOTTOM_PCT = [
  ((1350 - 475) / 1350) * 100 - POT_VISUAL_BOTTOM_INSET_PCT,
  ((1350 - 780) / 1350) * 100 - POT_VISUAL_BOTTOM_INSET_PCT,
  ((1350 - 1083) / 1350) * 100 - POT_VISUAL_BOTTOM_INSET_PCT,
]

// Measured from challenges/plot.png (1821x864): top edge of each shelf plank sits at
// y = 299, 497.
const DESKTOP_TIER_BOTTOM_PCT = [
  ((864 - 299) / 864) * 100 - POT_VISUAL_BOTTOM_INSET_PCT,
  ((864 - 497) / 864) * 100 - POT_VISUAL_BOTTOM_INSET_PCT,
]

function EmptyGardenSlot({ isMobile }: { isMobile?: boolean }) {
  return (
    <div aria-hidden="true" className="flex flex-col items-center" style={{ width: SLOT_WIDTH }}>
      {!isMobile && <div className="mb-1" style={{ minHeight: 32 }} />}
      <div style={{ width: VISUAL_W, height: VISUAL_H }} />
    </div>
  )
}

interface StatCardProps {
  label: string
  count: number
  isDark: boolean
  showBubble?: boolean
}

function StatCard({ label, count, isDark, showBubble }: StatCardProps) {
  const bubbleColor = isDark ? 'rgba(255,255,255,0.1)' : 'rgba(255,255,255,0.7)'

  return (
    <div className="flex flex-col items-center">
      <div
        className={`rounded-2xl shadow-md text-center ${
          showBubble ? 'px-7 py-3' : 'px-5 py-2'
        } ${isDark ? 'bg-white/10 backdrop-blur-sm' : 'bg-white/70 backdrop-blur-sm'}`}
      >
        <p className={`mb-0.5 ${showBubble ? 'text-sm' : 'text-xs'} ${isDark ? 'text-white/70' : 'text-bosque/70'}`}>
          {label}
        </p>
        <p className={`font-extrabold leading-none ${showBubble ? 'text-3xl' : 'text-2xl'} ${isDark ? 'text-white' : 'text-bosque'}`}>
          {count}
        </p>
      </div>
      {showBubble && (
        <div style={{
          width: 0,
          height: 0,
          borderLeft: '10px solid transparent',
          borderRight: '10px solid transparent',
          borderTop: `10px solid ${bubbleColor}`,
        }} />
      )}
    </div>
  )
}

function MiniStatPill({ label, count, isDark }: Omit<StatCardProps, 'showBubble'>) {
  return (
    <div
      className={`flex items-center gap-1.5 rounded-full border-[2px] border-transparent px-3 py-2 text-xs shadow-sm backdrop-blur-sm ${
        isDark
          ? 'bg-[rgba(23,32,51,0.92)] text-violeta-claro shadow-[0_8px_22px_rgba(2,6,23,0.32)]'
          : 'bg-[var(--surface-tertiary)] text-violeta'
      }`}
    >
      <span className="font-medium opacity-70">{label}</span>
      <span className="font-extrabold">{count}</span>
    </div>
  )
}

export default function Orchard() {
  const { theme } = useTheme()
  const isDark = theme === 'dark'
  const navigate = useNavigate()
  const { plants, loading, error } = useUserPlants()
  const [selectedPlant, setSelectedPlant] = useState<UserPlantSummaryResponse | null>(null)
  const [scrollPage, setScrollPage] = useState(0)
  const [hoveredPlantId, setHoveredPlantId] = useState<number | null>(null)
  const [isMobile, setIsMobile] = useState(() => window.innerWidth < 768)

  useEffect(() => {
    const handler = () => setIsMobile(window.innerWidth < 768)
    window.addEventListener('resize', handler)
    return () => window.removeEventListener('resize', handler)
  }, [])

  const growingPlant = plants.find(p => p.status === 'GROWING') ?? null
  const completedPlants = plants
    .filter(p => p.status === 'COMPLETED')
    .sort((a, b) =>
      new Date(b.completedAt ?? 0).getTime() - new Date(a.completedAt ?? 0).getTime()
    )

  const handleCardClick = (plant: UserPlantSummaryResponse) => {
    if (plant.status === 'GROWING') {
      navigate('/challenges')
    } else {
      setSelectedPlant(plant)
    }
  }

  const historySlots = isMobile ? MOBILE_HISTORY_SLOTS : HISTORY_SLOTS
  const totalScrollPages = Math.max(1, Math.ceil(completedPlants.length / historySlots))
  const safePage = Math.min(scrollPage, totalScrollPages - 1)
  const showLeftArrow = safePage > 0
  const showRightArrow = safePage < totalScrollPages - 1

  const currentPageCompleted = Array.from({ length: historySlots }, (_, i) =>
    completedPlants[safePage * historySlots + i] ?? null
  )

  const gridItems: (UserPlantSummaryResponse | null)[] = [growingPlant, ...currentPageCompleted]

  const arrowClass = (visible: boolean) =>
    `${isMobile ? 'text-5xl' : 'text-8xl'} font-thin leading-none select-none transition-opacity duration-150 pb-4 ${
      visible
        ? 'opacity-100 cursor-pointer text-[#2f4f3a] hover:text-[#1f382a]'
        : 'opacity-0 pointer-events-none'
    }`

  return (
    <div className="relative h-full flex flex-col overflow-hidden">
      <ThemeBackground
        lightSrc={dayBackground}
        darkSrc={nightBackground}
        lightAlt="Fondo vivero"
        darkAlt="Fondo nocturno vivero"
      />
      <BackButton to="/challenges" />

      <div className="relative z-20 hidden md:flex justify-center gap-4 pt-6 px-4 flex-shrink-0">
        <StatCard label="Creciendo" count={loading ? 0 : (growingPlant ? 1 : 0)} isDark={isDark} />
        <StatCard label="Completadas" count={loading ? 0 : completedPlants.length} isDark={isDark} />
      </div>

      <div className="fixed top-20 right-6 z-50 flex md:hidden gap-2">
        <MiniStatPill label="Creciendo" count={loading ? 0 : (growingPlant ? 1 : 0)} isDark={isDark} />
        <MiniStatPill label="Completadas" count={loading ? 0 : completedPlants.length} isDark={isDark} />
      </div>

      {isMobile && !loading && !error && plants.length > 0 && (
        <div
          className="fixed top-36 z-40 flex items-center justify-between md:hidden"
          style={{ left: M_LEFT, right: M_RIGHT }}
        >
          <button
            onClick={() => setScrollPage(p => Math.max(0, p - 1))}
            aria-label="Ver plantas más recientes"
            className={arrowClass(showLeftArrow)}
          >
            ‹
          </button>
          <button
            onClick={() => setScrollPage(p => Math.min(totalScrollPages - 1, p + 1))}
            aria-label="Ver plantas más antiguas"
            className={arrowClass(showRightArrow)}
          >
            ›
          </button>
        </div>
      )}

      {loading && (
        <div className="relative z-10 flex-1 flex items-center justify-center">
          <p className={`text-sm italic ${isDark ? 'text-white/60' : 'text-bosque/60'}`}>
            Cargando tu vivero…
          </p>
        </div>
      )}

      {error && !loading && (
        <div className="relative z-10 flex-1 flex items-center justify-center px-4">
          <InlineError message={error} className="max-w-md w-full" />
        </div>
      )}

      {!loading && !error && plants.length === 0 && (
        <div className="relative z-10 flex-1 flex flex-col items-center justify-center gap-3">
          <p className={`text-sm italic ${isDark ? 'text-white/70' : 'text-bosque/70'}`}>
            Tu vivero está vacío todavía.
          </p>
          <button
            onClick={() => navigate('/challenges')}
            className="text-sm text-bosque underline underline-offset-2 bg-transparent border-0 cursor-pointer hover:opacity-70 transition-opacity"
          >
            Ir a Retos para empezar
          </button>
        </div>
      )}

      {!loading && !error && plants.length > 0 && (
        <div className="relative flex-1 min-h-0 flex flex-col items-center justify-end overflow-y-auto md:overflow-visible">

          <div
            className="relative flex-shrink-0"
            style={isMobile
              ? { width: '100%', marginBottom: 65 }
              : { width: '85%', maxWidth: 1100, minWidth: 640, marginBottom: -25 }
            }
          >
            <div
              className="relative"
              style={{ transform: isMobile ? 'translateY(6%)' : 'translateY(3%)' }}
            >
              <img
                src={isMobile ? mobilePlotImg : plotImg}
                alt=""
                aria-hidden="true"
                className="block w-full"
              />

              {isMobile ? (
                <>
                  {[0, 1, 2].map(row => (
                    <div
                      key={row}
                      style={{
                        position: 'absolute',
                        left: '50%',
                        transform: 'translateX(-50%)',
                        bottom: `${MOBILE_TIER_BOTTOM_PCT[row]}%`,
                        zIndex: 20,
                        display: 'flex',
                      }}
                    >
                      {gridItems.slice(row * 3, row * 3 + 3).map((plant, i) => (
                        <div key={plant ? plant.id : `empty-${row}-${i}`}>
                          {plant ? (
                            <OrchardPlantSlot
                              plant={plant}
                              onClick={() => handleCardClick(plant)}
                              isMobile
                            />
                          ) : (
                            <EmptyGardenSlot isMobile />
                          )}
                        </div>
                      ))}
                    </div>
                  ))}
                </>
              ) : (
                <div className="absolute inset-0" style={{ zIndex: 20 }}>
                  {[0, 1].map(row => (
                    <div
                      key={row}
                      style={{
                        position: 'absolute',
                        left: '50%',
                        transform: 'translateX(-50%)',
                        bottom: `${DESKTOP_TIER_BOTTOM_PCT[row]}%`,
                        display: 'flex',
                        gap: `${GAP}px`,
                      }}
                    >
                      {gridItems
                        .map((plant, i) => ({ plant, i }))
                        .filter(({ i }) => i % 2 === row)
                        .map(({ plant, i }) => plant ? (
                          <OrchardPlantSlot
                            key={plant.id}
                            plant={plant}
                            onClick={() => handleCardClick(plant)}
                            hideBadge={
                              plant.status === 'GROWING' &&
                              hoveredPlantId !== null &&
                              hoveredPlantId !== plant.id
                            }
                            onHoverStart={() => setHoveredPlantId(plant.id)}
                            onHoverEnd={() => setHoveredPlantId(null)}
                          />
                        ) : (
                          <EmptyGardenSlot key={`empty-${i}`} />
                        ))}
                    </div>
                  ))}

                  <button
                    onClick={() => setScrollPage(p => Math.max(0, p - 1))}
                    aria-label="Ver plantas más recientes"
                    className={arrowClass(showLeftArrow)}
                    style={{ position: 'absolute', left: 0, top: '50%', transform: 'translateY(-50%)' }}
                  >
                    ‹
                  </button>
                  <button
                    onClick={() => setScrollPage(p => Math.min(totalScrollPages - 1, p + 1))}
                    aria-label="Ver plantas más antiguas"
                    className={arrowClass(showRightArrow)}
                    style={{ position: 'absolute', right: 0, top: '50%', transform: 'translateY(-50%)' }}
                  >
                    ›
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {selectedPlant && (
        <PlantGoalsModal
          plant={selectedPlant}
          onClose={() => setSelectedPlant(null)}
        />
      )}
    </div>
  )
}
