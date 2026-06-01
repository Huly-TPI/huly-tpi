import Button from '../Buttons/Button/Button'
import plant5 from '../../assets/challenges/plant-stages/plant-5.png'

export interface HarvestModalProps {
  plantNumber: number
  onCreateNew: () => void
}

export default function HarvestModal({ plantNumber, onCreateNew }: HarvestModalProps) {
  return (
    <div className="fixed inset-0 bg-black/[0.42] flex items-center justify-center z-[1000] p-4 backdrop-blur-[3px]" role="dialog" aria-modal="true">
      <div className="harvest-modal bg-white rounded-3xl p-[2rem_1.75rem_1.75rem] w-full max-w-[320px] text-center shadow-[0_24px_64px_rgba(0,0,0,0.28)]">
        <img
          src={plant5}
          alt="Planta cosechada"
          className="harvest-modal__plant w-[130px] h-[170px] object-contain drop-shadow-[0_8px_18px_rgba(100,153,89,0.38)]"
        />
        <h2 className="text-[1.25rem] font-extrabold text-[#2D3748] mt-[0.9rem] mb-[0.35rem] leading-[1.3]">
          ¡Cosechaste la Planta #{plantNumber}!
        </h2>
        <p className="text-[0.85rem] text-bosque m-0 mb-6 italic">
          Tu jardín sigue creciendo. ¡Seguí así!
        </p>
        <Button variant="primary" size="sm" fullWidth onClick={onCreateNew}>
          + Crear nuevo reto
        </Button>
      </div>
    </div>
  )
}
