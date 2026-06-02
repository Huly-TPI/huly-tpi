import { useEffect, useState } from 'react'
import { BreathingGuide, type BreathingTechnique } from '../../components/BreathingGuide'
import { getBreathingTechniques } from '../../api/breathing'
import BackButton from '../../components/Buttons/BackButton/BackButton'
import './Breathing.css'
import dayBackground from '../../assets/garden/light-theme/background/day-background.webp'
import BackButton from '../../components/Buttons/BackButton/BackButton'

export default function Breathing() {
  const [techniques, setTechniques] = useState<BreathingTechnique[]>([])

  useEffect(() => {
    getBreathingTechniques()
      .then(setTechniques)
      .catch(console.error)
  }, [])

  return (
    <div
      className="flex items-center justify-center h-full"
      style={{ backgroundImage: `url(${dayBackground})`, backgroundSize: 'cover', backgroundPosition: 'center' }}
    >
      <BackButton />
      <BreathingGuide techniques={techniques} />
    </div>
  )
}