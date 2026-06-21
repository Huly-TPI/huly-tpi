import BackButton from '../../components/Buttons/BackButton/BackButton'
import { ZenSandCanvas } from '../../components/SandZenGarden'
import { ActivityType } from '../../api/activities'
import {useTheme} from '../../context/theme'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'

import lightBackground from "../../assets/zen-sand/light-theme/background/zen-sand-minigame.webp"
import darkBackground from "../../assets/zen-sand/dark-theme/background/zen-sand-minigame.webp"

function SandZenGarden() {

  const {theme} = useTheme()
  const isDark  = theme === 'dark';
  const { markConditionMet, saveSession } = useActivitySessionTracker(ActivityType.ARENA_ZEN, {
    autoStart: true,
    minDurationSeconds: 10,
  })

  const currentBackground = isDark ? darkBackground : lightBackground;

  return (
    <main className="relative min-h-full w-full overflow-x-hidden">
      <div 
      className='absolute inset-0 bg-cover bg-upper bg-no-repeat'
      style={{ backgroundImage: `url(${currentBackground})` }}
      />

      <div className='relative z-10 w-full min-h-full'>
      <BackButton to="/minigames" onBeforeNavigate={() => saveSession()} />
      <ZenSandCanvas onDraw={markConditionMet} />
      </div>
    </main>
  )
}

export default SandZenGarden
