import { useNavigate } from 'react-router-dom'
import { useEmotionalOnboarding } from '../../hooks/useEmotionalOnboarding'
import EmotionalOnboarding from '../../components/onboarding/EmotionalOnboarding/EmotionalOnboarding'

export default function Onboarding() {
    const navigate = useNavigate()
    const { step, options, isLoading, selectOption } = useEmotionalOnboarding(() =>
        { navigate('/')})

    return (<EmotionalOnboarding 
    step={step}
     options={options} 
     isLoading={isLoading} 
     onSelectOption={selectOption} />
    )
}