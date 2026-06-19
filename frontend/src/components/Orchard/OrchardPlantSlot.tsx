import Plant from '../Challenges/Plant'
import stumpShortImg from '../../assets/challenges/stump-short.png'
import { type UserPlantSummaryResponse } from '../../api/userGoals'

function getPlantStage(progress: number, required: number): 0 | 1 | 2 | 3 | 4 | 5 {
  if (progress === 0) return 0
  return Math.min(Math.ceil((progress / required) * 5), 5) as 0 | 1 | 2 | 3 | 4 | 5
}

interface OrchardPlantSlotProps {
  plant: UserPlantSummaryResponse
  onClick: () => void
}

const SCALE = 0.55
const PLANT_W = 220
const PLANT_H = 260
export const VISUAL_W = Math.round(PLANT_W * SCALE)  // 121
export const VISUAL_H = Math.round(PLANT_H * SCALE)  // 143

export default function OrchardPlantSlot({ plant, onClick }: OrchardPlantSlotProps) {
  const isGrowing = plant.status === 'GROWING'
  const stage = isGrowing ? getPlantStage(plant.completedGoalsCount, plant.requiredGoals) : 5

  return (
    <button
      onClick={onClick}
      className="group flex flex-col items-center cursor-pointer bg-transparent border-0 p-0 gap-0 transition-all duration-150"
      aria-label={
        isGrowing
          ? 'Planta en curso — ir a retos'
          : `Planta #${plant.plantNumber} — ver retos completados`
      }
    >
      {/* Badge row — always reserves height for vertical alignment */}
      <div className="flex flex-col items-center mb-1" style={{ minHeight: 32 }}>
        {isGrowing ? (
          <>
            <span className="text-m font-bold px-3 py-1 rounded-full whitespace-nowrap shadow-sm leading-none" style={{ background: '#f5e6c8', color: '#7a5c2e', border: '1px solid #d4b483' }}>
              En curso
            </span>
            <div style={{ width: 0, height: 0, borderLeft: '7px solid transparent', borderRight: '7px solid transparent', borderTop: '7px solid #f5e6c8' }} />
          </>
        ) : (
          <div className="flex flex-col items-center opacity-0 group-hover:opacity-100 transition-opacity duration-150">
            <span className="text-m font-bold px-3 py-1 rounded-full whitespace-nowrap shadow-sm leading-none" style={{ background: '#f5e6c8', color: '#7a5c2e', border: '1px solid #d4b483' }}>
              Planta #{plant.plantNumber}
            </span>
            <div style={{ width: 0, height: 0, borderLeft: '7px solid transparent', borderRight: '7px solid transparent', borderTop: '7px solid #f5e6c8' }} />
          </div>
        )}
      </div>

      {/* Shadow wrapper — hover effect traces the plant pixels (outside overflow:hidden) */}
      <div className="transition-[filter] duration-200 group-hover:drop-shadow-[0_6px_18px_rgba(0,0,0,0.4)]">
        {/* Clip wrapper: collapses Plant's DOM size to its visual size */}
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

      {/* Stump base — grounds the flowerpot visually */}
      <img
        src={stumpShortImg}
        alt=""
        aria-hidden="true"
        className="object-contain"
        style={{ width: 88, marginTop: -22 }}
      />
    </button>
  )
}
