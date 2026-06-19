import { useState } from 'react'
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
import stumpShortImg from '../../assets/challenges/stump-short.png'
import { useTheme } from '../../context/theme'
import './Orchard.css'

// Garden layout constants
const COLS = 6
const ROWS = 2
const TOTAL_SLOTS = COLS * ROWS       // 12
const HISTORY_SLOTS = TOTAL_SLOTS - 1  // 11 slots for completed plants
const SLOT_WIDTH = VISUAL_W            // 121px per slot
const GAP = 12

function EmptyGardenSlot() {
  return (
    <div aria-hidden="true" className="flex flex-col items-center" style={{ width: SLOT_WIDTH }}>
      {/* Badge spacer */}
      <div className="mb-1" style={{ minHeight: 32 }} />
      {/* Plant spacer */}
      <div style={{ width: VISUAL_W, height: VISUAL_H }} />
      {/* Faded stump */}
      <img
        src={stumpShortImg}
        alt=""
        className="object-contain"
        style={{ width: 88, marginTop: -22, opacity: 0.25 }}
      />
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

  // Slot 0 = ACT (always top-left), slots 1–11 = current page of completed plants
  const currentPageCompleted = Array.from({ length: HISTORY_SLOTS }, (_, i) =>
    completedPlants[safePage * HISTORY_SLOTS + i] ?? null
  )

  const gridItems: (UserPlantSummaryResponse | null)[] = [growingPlant, ...currentPageCompleted]

  const gardenGridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateRows: `repeat(${ROWS}, auto)`,
    gridAutoFlow: 'column',
    gridAutoColumns: `${SLOT_WIDTH}px`,
    columnGap: `${GAP}px`,
    rowGap: 0,
  }

  const arrowClass = (visible: boolean) =>
    `text-5xl font-thin leading-none select-none transition-opacity duration-150 pb-4 ${
      visible
        ? 'opacity-100 cursor-pointer text-bosque hover:text-bosque/60'
        : 'opacity-0 pointer-events-none'
    }`

  return (
    <div className="relative h-full flex flex-col overflow-hidden">
      <ThemeBackground
        lightSrc={dayBackground}
        darkSrc={nightBackground}
        lightAlt="Fondo huerta"
        darkAlt="Fondo nocturno huerta"
      />
      <BackButton to="/challenges" />

      {/* Stats cards */}
      <div className="relative z-20 flex justify-center gap-4 pt-6 px-4 flex-shrink-0">
        <StatCard label="En curso" count={loading ? 0 : (growingPlant ? 1 : 0)} isDark={isDark} />
        <StatCard label="Completadas" count={loading ? 0 : completedPlants.length} isDark={isDark} />
      </div>

      {/* Loading / error / empty states */}
      {loading && (
        <div className="relative z-10 flex-1 flex items-center justify-center">
          <p className={`text-sm italic ${isDark ? 'text-white/60' : 'text-bosque/60'}`}>
            Cargando tu huerta…
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
            Tu huerta está vacía todavía.
          </p>
          <button
            onClick={() => navigate('/challenges')}
            className="text-sm text-bosque underline underline-offset-2 bg-transparent border-0 cursor-pointer hover:opacity-70 transition-opacity"
          >
            Ir a Retos para empezar
          </button>
        </div>
      )}

      {/* Garden: plants + plot */}
      {!loading && !error && plants.length > 0 && (
        <div className="relative flex-1 flex flex-col items-center justify-end">

          {/* plot.png es el fondo del contenedor; la grilla se posiciona dentro de él */}
          <div
            className="relative flex-shrink-0"
            style={{ width: '85%', maxWidth: 1100, minWidth: 640, marginBottom: -25 }}
          >
            {/* plot.png — define el tamaño del contenedor, actúa como fondo */}
            <img
              src={plotImg}
              alt=""
              aria-hidden="true"
              className="block w-full"
              style={{ transform: 'translateY(30px)' }}
            />

            {/* Grilla: cubre el contenedor, alineada al fondo del plot */}
            <div className="absolute inset-0 flex items-end justify-center pb-2" style={{ zIndex: 20 }}>
              <div className="flex items-end gap-3">
                {/* Left arrow */}
                <button
                  onClick={() => setScrollPage(p => Math.max(0, p - 1))}
                  aria-label="Ver plantas más recientes"
                  className={arrowClass(showLeftArrow)}
                >
                  ‹
                </button>

                {/* 6×2 CSS grid, column-major fill */}
                <div style={gardenGridStyle}>
                  {gridItems.map((plant, i) => {
                    const rowStyle = i % 2 === 1
                      ? { marginTop: -80, position: 'relative' as const, zIndex: 1 }
                      : {}
                    return plant ? (
                      <div key={plant.id} style={rowStyle}>
                        <OrchardPlantSlot plant={plant} onClick={() => handleCardClick(plant)} />
                      </div>
                    ) : (
                      <div key={`empty-${i}`} style={rowStyle}>
                        <EmptyGardenSlot />
                      </div>
                    )
                  })}
                </div>

                {/* Right arrow */}
                <button
                  onClick={() => setScrollPage(p => Math.min(totalScrollPages - 1, p + 1))}
                  aria-label="Ver plantas más antiguas"
                  className={arrowClass(showRightArrow)}
                >
                  ›
                </button>
              </div>
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
