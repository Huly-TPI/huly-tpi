import { BubblesActivity as BubblesActivityComponent } from '../../components/Bubbles'
import BackButton from '../../components/Buttons/BackButton/BackButton'

const BubblesActivity = () => {
  return( 
      <div className="h-full w-full overflow-hidden">
      <BackButton to="/minigames" />
      <BubblesActivityComponent />
    </div>
  )
}

export default BubblesActivity