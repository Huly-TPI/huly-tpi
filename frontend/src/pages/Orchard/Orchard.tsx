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
import './Orchard.css'

const TOTAL_SLOTS = 12
const HISTORY_SLOTS = TOTAL_SLOTS - 1
const SLOT_WIDTH = VISUAL_W
const GAP = 12


const M_TOP    = '5%'
const M_BOTTOM = '5%'
const M_LEFT   = '5%'
const M_RIGHT  = '5%'

function EmptyGardenSlot() {
  return (
    <div aria-hidden="true" className="flex flex-col items-center" style={{ width: SLOT_WIDTH }}>
      <div className="mb-1" style={{ minHeight: 32 }} />
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

  const totalScrollPages = Math.max(1, Math.ceil(completedPlants.length / HISTORY_SLOTS))
  const safePage = Math.min(scrollPage, totalScrollPages - 1)
  const showLeftArrow = safePage > 0
  const showRightArrow = safePage < totalScrollPages - 1

  const currentPageCompleted = Array.from({ length: HISTORY_SLOTS }, (_, i) =>
    completedPlants[safePage * HISTORY_SLOTS + i] ?? null
  )

  const gridItems: (UserPlantSummaryResponse | null)[] = [growingPlant, ...currentPageCompleted]

  const gardenGridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateRows: 'repeat(2, auto)',
    gridAutoFlow: 'column',
    gridAutoColumns: `${SLOT_WIDTH}px`,
    columnGap: `${GAP}px`,
    rowGap: 0,
  }

  const arrowClass = (visible: boolean) =>
    `${isMobile ? 'text-5xl' : 'text-8xl'} font-thin leading-none select-none transition-opacity duration-150 pb-4 ${
      visible
        ? 'opacity-100 cursor-pointer text-[#f5ead8]/90 hover:text-[#f5ead8]'
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

      <div className="relative z-20 flex justify-center gap-4 pt-16 md:pt-6 px-4 flex-shrink-0">
        <StatCard label="En curso" count={loading ? 0 : (growingPlant ? 1 : 0)} isDark={isDark} />
        <StatCard label="Completadas" count={loading ? 0 : completedPlants.length} isDark={isDark} />
      </div>

      {loading && (
        <div className="relative z-10 flex-1 flex items-center justify-center">
          <p className={`text-sm italic ${isDark ? 'text-white/60' : 'text-bosque/60'}`}>
            Cargando tu vivero…
          </p>
        </div>
      )}

      {error && !loading && (
        <div className="relative z-10 flex-1 flex items-center justify-center">
          <p className="text-sm text-red-600 bg-red-50 rounded-xl px-4 py-3">{error}</p>
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
        <div className="relative flex-1 flex flex-col items-center justify-end">

          <div
            className="relative flex-shrink-0"
            style={isMobile
              ? { width: '100%', marginBottom: 65 }
              : { width: '85%', maxWidth: 1100, minWidth: 640, marginBottom: -25 }
            }
          >
            <img
              src={isMobile ? mobilePlotImg : plotImg}
              alt=""
              aria-hidden="true"
              className="block w-full"
              style={{ transform: isMobile ? 'translateY(70px)' : 'translateY(30px)' }}
            />

            {isMobile ? (
              <>
                <button
                  onClick={() => setScrollPage(p => Math.max(0, p - 1))}
                  aria-label="Ver plantas más recientes"
                  className={arrowClass(showLeftArrow)}
                  style={{ position: 'absolute', left: 0, top: '50%', transform: 'translateY(-50%)', zIndex: 30 }}
                >
                  ‹
                </button>
                <button
                  onClick={() => setScrollPage(p => Math.min(totalScrollPages - 1, p + 1))}
                  aria-label="Ver plantas más antiguas"
                  className={arrowClass(showRightArrow)}
                  style={{ position: 'absolute', right: 0, top: '50%', transform: 'translateY(-50%)', zIndex: 30 }}
                >
                  ›
                </button>

                <div
                  style={{
                    position: 'absolute',
                    top: M_TOP,
                    bottom: M_BOTTOM,
                    left: M_LEFT,
                    right: M_RIGHT,
                    zIndex: 20,
                    display: 'grid',
                    gridTemplateColumns: 'repeat(3, 1fr)',
                    gridTemplateRows: 'repeat(4, 1fr)',
                    columnGap: `${GAP}px`,
                  }}
                >
                  {gridItems.map((plant, i) => (
                    <div
                      key={plant ? plant.id : `empty-${i}`}
                      style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'center' }}
                    >
                      {plant ? (
                        <OrchardPlantSlot
                          plant={plant}
                          onClick={() => handleCardClick(plant)}
                          isMobile
                        />
                      ) : (
                        <EmptyGardenSlot />
                      )}
                    </div>
                  ))}
                </div>
              </>
            ) : (
              <div className="absolute inset-0 flex items-end justify-center pb-[15%]" style={{ zIndex: 20 }}>
                <div className="flex items-end gap-3">
                  <button
                    onClick={() => setScrollPage(p => Math.max(0, p - 1))}
                    aria-label="Ver plantas más recientes"
                    className={arrowClass(showLeftArrow)}
                  >
                    ‹
                  </button>

                  <div style={gardenGridStyle}>
                    {gridItems.map((plant, i) => {
                      const rowStyle = i % 2 === 1
                        ? { marginTop: -50, position: 'relative' as const, zIndex: 1 }
                        : {}
                      return plant ? (
                        <div key={plant.id} style={rowStyle}>
                          <OrchardPlantSlot
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
                        </div>
                      ) : (
                        <div key={`empty-${i}`} style={rowStyle}>
                          <EmptyGardenSlot />
                        </div>
                      )
                    })}
                  </div>

                  <button
                    onClick={() => setScrollPage(p => Math.min(totalScrollPages - 1, p + 1))}
                    aria-label="Ver plantas más antiguas"
                    className={arrowClass(showRightArrow)}
                  >
                    ›
                  </button>
                </div>
              </div>
            )}
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
