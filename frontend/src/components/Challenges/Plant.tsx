import wateringCanImg from '../../assets/challenges/watering-can.png'
import flowerpotBaseImg from '../../assets/challenges/plant-stages/flowerpot-base.png'
import flowerpotTopImg from '../../assets/challenges/plant-stages/flowerpot-top.png'
import plant0 from '../../assets/challenges/plant-stages/plant-0.png'
import plant1 from '../../assets/challenges/plant-stages/plant-1.png'
import plant2 from '../../assets/challenges/plant-stages/plant-2.png'
import plant3 from '../../assets/challenges/plant-stages/plant-3.png'
import plant4 from '../../assets/challenges/plant-stages/plant-4.png'
import plant5 from '../../assets/challenges/plant-stages/plant-5.png'

const PLANT_IMAGES = [plant0, plant1, plant2, plant3, plant4, plant5] as const

export interface PlantProps {
  stage: 0 | 1 | 2 | 3 | 4 | 5
  isWatering: boolean
}

export default function Plant({ stage, isWatering }: PlantProps) {
  return (
    <div className={`plant relative w-[220px] h-[260px] flex-shrink-0 z-[1]${isWatering ? ' plant--watering' : ''}`}>
      {isWatering && (
        <img
          src={wateringCanImg}
          alt=""
          aria-hidden="true"
          className="plant__watering-can absolute top-[-150px] left-[-95px] z-[5] drop-shadow-[0_3px_6px_rgba(0,0,0,0.18)]"
        />
      )}
      <img
        src={flowerpotBaseImg}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 w-full h-full object-contain [object-position:center_bottom] z-[1] pointer-events-none"
      />
      <img
        key={stage}
        src={PLANT_IMAGES[stage]}
        alt={`Planta etapa ${stage + 1}`}
        className="plant__image absolute inset-0 w-full h-full object-contain [object-position:center_bottom] [transform-origin:center_bottom] z-[2]"
      />
      <div className="absolute inset-0 pointer-events-none overflow-hidden z-[3]" aria-hidden="true">
        <div className="plant__drop" />
        <div className="plant__drop" />
        <div className="plant__drop" />
        <div className="plant__drop" />
        <div className="plant__drop" />
      </div>
      <img
        src={flowerpotTopImg}
        alt=""
        aria-hidden="true"
        className="absolute inset-0 w-full h-full object-contain [object-position:center_bottom] z-[4] pointer-events-none"
      />
    </div>
  )
}
