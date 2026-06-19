import wateringCanImg from '../../assets/challenges/watering-can.png'
import { getPlantAssets } from '../../config/plantImages'

export interface PlantProps {
  stage: 0 | 1 | 2 | 3 | 4 | 5
  isWatering: boolean
  plantType?: number
}

export default function Plant({ stage, isWatering, plantType = 1 }: PlantProps) {
  const { stages: images, flowerpotBase: flowerpotBaseImg, flowerpotTop: flowerpotTopImg } = getPlantAssets(plantType)

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
        key={`${plantType}-${stage}`}
        src={images[stage]}
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
