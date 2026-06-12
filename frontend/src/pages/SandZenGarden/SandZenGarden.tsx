import BackButton from '../../components/Buttons/BackButton/BackButton'
import { ZenSandCanvas } from '../../components/SandZenGarden'

function SandZenGarden() {
  return (
    <main className="min-h-full w-full overflow-x-hidden bg-[radial-gradient(circle_at_top,_rgba(211,204,235,0.3),_transparent_38%),var(--app-bg)]">
      <BackButton to="/minigames" />
      <ZenSandCanvas />
    </main>
  )
}

export default SandZenGarden
