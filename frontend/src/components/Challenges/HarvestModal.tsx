import Button from '../Buttons/Button/Button'
import Plant from './Plant'
import stumpShortImg from '../../assets/challenges/stump-short.png'

const SCALE = 0.65
const PLANT_W = 220
const PLANT_H = 260
const VISUAL_W = Math.round(PLANT_W * SCALE)
const VISUAL_H = Math.round(PLANT_H * SCALE)

export interface HarvestModalProps {
  plantNumber: number
  onCreateNew: () => void
  onGoToOrchard: () => void
}

export default function HarvestModal({ plantNumber, onCreateNew, onGoToOrchard }: HarvestModalProps) {
  return (
    <div className="fixed inset-0 bg-black/[0.42] flex items-center justify-center z-[1000] p-4 backdrop-blur-[3px]" role="dialog" aria-modal="true">
      <div className="harvest-modal bg-white rounded-3xl p-[2rem_1.75rem_1.75rem] w-full max-w-[320px] text-center shadow-[0_24px_64px_rgba(0,0,0,0.28)]">

        <div className="flex flex-col items-center">
          <div style={{ width: VISUAL_W, height: VISUAL_H, overflow: 'hidden', position: 'relative' }}>
            <div style={{
              width: PLANT_W,
              height: PLANT_H,
              transform: `scale(${SCALE})`,
              transformOrigin: 'top left',
              position: 'absolute',
              top: 0,
              left: 0,
            }}>
              <Plant stage={5} isWatering={false} plantType={plantNumber} />
            </div>
          </div>
          <img
            src={stumpShortImg}
            alt=""
            aria-hidden="true"
            className="object-contain"
            style={{ width: 88, marginTop: -22 }}
          />
        </div>

        <h2 className="text-[1.25rem] font-extrabold text-[#2D3748] mt-[0.9rem] mb-[0.35rem] leading-[1.3]">
          ¡Cosechaste la Planta #{plantNumber}!
        </h2>
        <p className="text-[0.85rem] text-bosque m-0 mb-6 italic">
          Tu jardín sigue creciendo. ¡Seguí así!
        </p>

        <div className="flex flex-col gap-2">
          <Button variant="success" size="sm" fullWidth onClick={onGoToOrchard}>
            Ver Vivero
          </Button>
          <Button variant="successSecondary" size="sm" fullWidth onClick={onCreateNew}>
            + Crear nuevo reto
          </Button>
        </div>
      </div>
    </div>
  )
}
