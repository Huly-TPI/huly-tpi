import BackButton from '../../components/Buttons/BackButton/BackButton'
import { StonesLake } from '../../components/StonesLake'
import './Stones.css'

export default function Stones() {
  return (
    <main className="stones-page">
      <BackButton to="/minigames" />
      <StonesLake />
      <p className="stones-page__hint">Hacé clic en el agua para lanzar piedras</p>
    </main>
  )
}
