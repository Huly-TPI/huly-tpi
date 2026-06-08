import { useEffect, useState } from 'react'
import { BreathingGuide, type BreathingTechnique } from '../../components/BreathingGuide'
import { getBreathingTechniques } from '../../api/breathing'
import './Breathing.css'
import lightBreathingBackground from '../../assets/breathing/light-theme/background/breathing-background.webp'
import darkBreathingBackground from '../../assets/breathing/dark-theme/background/breathing-background.webp'
import ThemeBackground from '../../components/ThemeBackground/ThemeBackground'
import cloudV1 from '../../assets/breathing/cloud-v1.webp'
import cloudV2 from '../../assets/breathing/cloud-v2.webp'
import cloudHuly from '../../assets/breathing/cloud-huly.webp'
import cloudHulyInhalando from '../../assets/breathing/cloud-huly-inhalando.webp'
import cloudHulyExhalando from '../../assets/breathing/cloud-huly-exhalando.webp'


export default function Breathing() {
  const [techniques, setTechniques] = useState<BreathingTechnique[]>([])
  
  useEffect(() => {
    getBreathingTechniques().then(setTechniques).catch(console.error)
  }, [])

  return ( 
    <div className="relative flex items-center justify-center h-full overflow-hidden">
        <ThemeBackground
          lightSrc={lightBreathingBackground}
          darkSrc={darkBreathingBackground}
          lightAlt="Fondo de respiración guiada"
          darkAlt="Fondo nocturno de respiración guiada"
        />
        <img src={cloudV1} alt="" className="cloud cloud-1" />
        <img src={cloudV2} alt="" className="cloud cloud-2" />
        <img src={cloudV1} alt="" className="cloud cloud-3" />
        <img src={cloudV2} alt="" className="cloud cloud-4" />
        
        <div className="relative z-10">
         <BreathingGuide
          techniques={techniques}
          hulyNormal={cloudHuly}
          hulyInhalando={cloudHulyInhalando}
          hulyExhalando={cloudHulyExhalando}
        />
      </div>
    </div>
  )
}