import Button from '../Buttons/Button/Button'
import { useAudioSettings } from '../../context/audioSettings'

interface AudioSettingsModalProps {
  onClose: () => void
}

const formatVolume = (volume: number) => `${Math.round(volume * 100)}%`

function VolumeControl({
  title,
  description,
  value,
  onChange,
  label,
}: {
  title: string
  description: string
  value: number
  onChange: (volume: number) => void
  label: string
}) {
  return (
    <div className="rounded-xl bg-[var(--surface-secondary)] p-3 text-left">
      <div className="mb-3 flex items-start justify-between gap-4">
        <div>
          <div className="text-sm font-semibold text-[var(--text-primary)]">{title}</div>
          <div className="mt-0.5 text-xs text-[var(--text-secondary)]">{description}</div>
        </div>
        <div className="shrink-0 text-xs font-semibold text-[var(--text-secondary)]">{formatVolume(value)}</div>
      </div>
      <input
        type="range"
        min="0"
        max="1"
        step="0.05"
        value={value}
        onChange={event => onChange(Number(event.target.value))}
        className="h-2 w-full cursor-pointer accent-[#8869AC]"
        aria-label={label}
      />
    </div>
  )
}

export default function AudioSettingsModal({ onClose }: AudioSettingsModalProps) {
  const {
    interfaceVolume,
    ambientVolume,
    minigameVolume,
    setInterfaceVolume,
    setAmbientVolume,
    setMinigameVolume,
  } = useAudioSettings()

  return (
    <div className="fixed inset-0 z-[400] flex items-center justify-center overflow-y-auto bg-[var(--overlay-strong)] px-4 backdrop-blur-sm">
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="audio-settings-title"
        className="relative my-8 w-full max-w-md rounded-2xl bg-[var(--surface-primary)] p-8 text-center text-[var(--text-primary)] shadow-xl"
      >
        <button
          type="button"
          onClick={onClose}
          className="absolute right-4 top-4 text-xl font-bold text-[var(--text-secondary)] transition duration-150 hover:text-[var(--text-primary)]"
          aria-label="Cerrar modal"
        >
          &times;
        </button>

        <h2 id="audio-settings-title" className="mb-3 text-2xl font-bold text-[#8869AC] dark:text-violeta-claro">
          Ambiente sonoro
        </h2>
        <p className="mb-6 text-left text-sm leading-relaxed text-[var(--text-secondary)]">
          Ajusta como suena Huly mientras navegas. Lleva una barra a 0% para silenciar esa categoria.
        </p>

        <div className="mb-6 space-y-4 border-y border-[var(--border-soft)] py-4">
          <VolumeControl
            title="Interfaz"
            description="Feedback suave al tocar botones y objetos."
            value={interfaceVolume}
            onChange={setInterfaceVolume}
            label="Volumen de interfaz"
          />
          <VolumeControl
            title="Fondo"
            description="Musica ambiental suave para navegar por Huly."
            value={ambientVolume}
            onChange={setAmbientVolume}
            label="Volumen de fondo"
          />
          <VolumeControl
            title="Minijuegos"
            description="Sonidos de actividades como la arena zen."
            value={minigameVolume}
            onChange={setMinigameVolume}
            label="Volumen de minijuegos"
          />
        </div>

        <Button variant="primary" fullWidth onClick={onClose}>
          Entendido
        </Button>
      </div>
    </div>
  )
}
