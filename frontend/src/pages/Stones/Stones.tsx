import BackButton from '../../components/Buttons/BackButton/BackButton'
import { StonesLake } from '../../components/StonesLake'
import { ActivityType } from '../../api/activities'
import { useActivitySessionTracker } from '../../hooks/useActivitySessionTracker'
import './Stones.css'

export default function Stones() {
  const { markConditionMet, saveSession } = useActivitySessionTracker(ActivityType.STONES, {
    autoStart: true,
    minDurationSeconds: 10,
  })

  return (
    <main className="stones-page">
      <BackButton to="/minigames" onBeforeNavigate={saveSession} />
      <StonesLake onStoneThrown={markConditionMet} />
      <p className="stones-page__hint">Hacé clic en el agua para lanzar piedras</p>
    </main>
  )
}
