import Button from './Buttons/Button/Button'
import colorLogo from '../assets/brand/color-logo.webp'

interface DiaryConsentModalProps {
  onAccept: () => void
  onReject: () => void
}

export default function DiaryConsentModal({ onAccept, onReject }: DiaryConsentModalProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm px-4">
      <div className="bg-white rounded-2xl shadow-xl p-8 max-w-sm w-full text-center">
        <img src={colorLogo} alt="Huly" className="h-8 object-contain mx-auto mb-4" />
        <h2 className="text-xl font-bold text-[#8869AC] mb-3">
          Una pregunta antes de empezar
        </h2>
        <p className="text-gray-500 text-sm leading-relaxed mb-6">
          Lo que escribís en tu diario puede ayudarnos a personalizar las recomendaciones y respuestas que te damos.
          Si preferís mantenerlo privado, igual podemos usar tu estado de ánimo para acompañarte.
        </p>
        <div className="flex flex-col gap-3">
          <Button variant="primary" fullWidth onClick={onAccept}>
            Sí, pueden usarlo
          </Button>
          <Button variant="secondary" fullWidth onClick={onReject}>
            Prefiero mantenerlo privado
          </Button>
        </div>
      </div>
    </div>
  )
}
