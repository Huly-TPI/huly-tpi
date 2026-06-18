import BackButton from '../../components/Buttons/BackButton/BackButton'
import { ZenSandCanvas } from '../../components/SandZenGarden'
import {useTheme} from '../../context/theme'

import lightBackground from "../../assets/zen-sand/light-theme/background/zen-sand-minigame.webp"
import darkBackground from "../../assets/zen-sand/dark-theme/background/zen-sand-minigame.webp"

function SandZenGarden() {

  const {theme} = useTheme()
  const isDark  = theme === 'dark';

  const currentBackground = isDark ? darkBackground : lightBackground;

  return (
    <main className="relative min-h-full w-full overflow-x-hidden">
      <div 
      className='absolute inset-0 bg-cover bg-upper bg-no-repeat'
      style={{ backgroundImage: `url(${currentBackground})` }}
      />

      <div className='relative z-10 w-full min-h-full'>
      <BackButton to="/minigames" />
      <ZenSandCanvas/>
      </div>
    </main>
  )
}

export default SandZenGarden
