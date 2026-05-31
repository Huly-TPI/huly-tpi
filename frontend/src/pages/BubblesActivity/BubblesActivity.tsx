import { useNavigate } from 'react-router-dom'
import { BubblesActivity as BubblesActivityComponent } from '../../components/Bubbles'

const BubblesActivity = () => {
  const navigate = useNavigate()
  return( 
      <div className="h-full w-full overflow-hidden">
      <BubblesActivityComponent onBack={() => navigate(-1)} />
    </div>
  )
}

export default BubblesActivity