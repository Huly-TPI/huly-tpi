import Plant from '../Challenges/Plant'
import { type UserPlantSummaryResponse } from '../../api/userGoals'

function getPlantStage(progress: number, required: number): 0 | 1 | 2 | 3 | 4 | 5 {
  if (progress === 0) return 0
  return Math.min(Math.ceil((progress / required) * 5), 5) as 0 | 1 | 2 | 3 | 4 | 5
}

interface OrchardPlantSlotProps {
  plant: UserPlantSummaryResponse
  onClick: () => void
  hideBadge?: boolean
  onHoverStart?: () => void
  onHoverEnd?: () => void
  isMobile?: boolean
}

const SCALE = 0.55
const PLANT_W = 220
const PLANT_H = 260
export const VISUAL_W = Math.round(PLANT_W * SCALE)
export const VISUAL_H = Math.round(PLANT_H * SCALE)

export default function OrchardPlantSlot({ plant, onClick, hideBadge, onHoverStart, onHoverEnd, isMobile }: OrchardPlantSlotProps) {
  const isGrowing = plant.status === 'GROWING'
  const stage = isGrowing ? getPlantStage(plant.completedGoalsCount, plant.requiredGoals) : 5

  return (
    <button
      onClick={onClick}
      onMouseEnter={onHoverStart}
      onMouseLeave={onHoverEnd}
      className="group flex flex-col items-center cursor-pointer bg-transparent border-0 p-0 gap-0 transition-all duration-150"
      style={{ position: 'relative' }}
      aria-label={
        isGrowing
          ? 'Planta en curso — ir a retos'
          : `Planta #${plant.plantNumber} — ver retos completados`
      }
    >
      {isGrowing && isMobile && (
        <div
          className={`flex flex-col items-center transition-opacity duration-150 ${hideBadge ? 'opacity-0' : 'opacity-100'}`}
          style={{ position: 'absolute', bottom: '100%', left: '50%', transform: 'translateX(-50%)', whiteSpace: 'nowrap', zIndex: 10 }}
        >
          <span className="text-m font-bold px-3 py-1 rounded-full shadow-sm leading-none" style={{ background: '#f5e6c8', color: '#7a5c2e', border: '1px solid #d4b483' }}>
            En curso
          </span>
          <div style={{ width: 0, height: 0, borderLeft: '7px solid transparent', borderRight: '7px solid transparent', borderTop: '7px solid #f5e6c8' }} />
        </div>
      )}

      {!isMobile && (
        <div className="flex flex-col items-center mb-1" style={{ minHeight: 32 }}>
          {isGrowing ? (
            <div className={`flex flex-col items-center transition-opacity duration-150 ${hideBadge ? 'opacity-0' : 'opacity-100'}`}>
              <span className="text-m font-bold px-3 py-1 rounded-full whitespace-nowrap shadow-sm leading-none" style={{ background: '#f5e6c8', color: '#7a5c2e', border: '1px solid #d4b483' }}>
                En curso
              </span>
              <div style={{ width: 0, height: 0, borderLeft: '7px solid transparent', borderRight: '7px solid transparent', borderTop: '7px solid #f5e6c8' }} />
            </div>
          ) : (
            <div className="flex flex-col items-center opacity-0 group-hover:opacity-100 transition-opacity duration-150">
              <span className="text-m font-bold px-3 py-1 rounded-full whitespace-nowrap shadow-sm leading-none" style={{ background: '#f5e6c8', color: '#7a5c2e', border: '1px solid #d4b483' }}>
                Planta #{plant.plantNumber}
              </span>
              <div style={{ width: 0, height: 0, borderLeft: '7px solid transparent', borderRight: '7px solid transparent', borderTop: '7px solid #f5e6c8' }} />
            </div>
          )}
        </div>
      )}


      <div className="transition-[filter] duration-200 group-hover:drop-shadow-[0_6px_18px_rgba(0,0,0,0.4)]">
        <div style={{ width: VISUAL_W, height: VISUAL_H, overflow: 'hidden', position: 'relative' }}>
          <div
            style={{
              width: PLANT_W,
              height: PLANT_H,
              transform: `scale(${SCALE})`,
              transformOrigin: 'top left',
              position: 'absolute',
              top: 0,
              left: 0,
            }}
          >
            <Plant stage={stage} isWatering={false} plantType={plant.plantNumber} />
          </div>
        </div>
      </div>

    </button>
  )
}
