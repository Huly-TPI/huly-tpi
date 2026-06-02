import './EmotionalOnboarding.css'
import registerBackground from '../../../assets/register/background.webp'
import cardFrame from '../../../assets/register/cardFrame.webp'
type Props = {
    step: 1 | 2 | 3
    options: string[]
    isLoading: boolean
    onSelectOption: (option: string) => void
}

const TITLES: Record<1 | 2 | 3, string> = {
  1: '¿Qué querés lograr?',
  2: '¿Qué aspecto te llama más?',
  3: '¿Cómo querés empezar?',
}

const SUBTITLES: Record<1 | 2 | 3, string> = {
  1: 'Elegí lo que más se acerca a lo que buscás hoy.',
  2: 'Basándonos en tu elección, ¿qué te llama más la atención?',
  3: 'Casi listo. Elegí cómo dar el primer paso.',
}

export default function EmotionalOnboarding({ step, options, isLoading, onSelectOption }: Props) {
  return (
    <div
      className="relative h-[calc(100vh-4rem)] flex items-center justify-center overflow-hidden"
      style={{
        backgroundImage: `url(${registerBackground})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center',
      }}
    >
      <div className="absolute inset-0 backdrop-blur-md" />

      <div
        className="relative z-10 w-full max-w-md mx-4 px-10 py-12 md:px-16 md:py-14"
        style={{
          backgroundImage: `url(${cardFrame})`,
          backgroundSize: '100% 100%',
          backgroundRepeat: 'no-repeat',
        }}
      >
        <div className="flex justify-center gap-2 mb-5" aria-hidden="true">
          {[1, 2, 3].map(s => (
            <div
              key={s}
              className={`h-2 rounded-full transition-all duration-200 ${
                s === step ? 'w-5 bg-[#4C7C64]' : 'w-2 bg-[#ACCCA4]'
              }`}
            />
          ))}
        </div>

        <h2 className="mb-2 text-center text-2xl font-bold text-[#4C7C64]">
          {TITLES[step]}
        </h2>
        <p className="mb-6 text-center text-sm italic text-[#8c7b66]">
          {SUBTITLES[step]}
        </p>

        {isLoading ? (
          <div
            role="status"
            className="flex justify-center items-center py-8 text-[#4C7C64] font-semibold text-sm"
          >
            Cargando opciones...
          </div>
        ) : (
          <div className="grid grid-cols-2 gap-3">
            {options.map(option => (
            <button
                  key={option}
                  onClick={() => onSelectOption(option)}
                  disabled={isLoading}
                  className="emotional-onboarding-option"
                >
                  {option}
                </button>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}