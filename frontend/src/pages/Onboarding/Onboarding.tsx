import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useEmotionalOnboarding } from '../../hooks/useEmotionalOnboarding'
import EmotionalOnboarding from '../../components/Onboarding/EmotionalOnboarding/EmotionalOnboarding'
import { useAuth } from '../../context/auth'

export default function Onboarding() {
    const navigate = useNavigate()
    const { user, loading, refreshUser } = useAuth()

    useEffect(() => {
        if (loading) return
        if (user?.onBoardingCompleted === true) {
            navigate('/')
        }
    }, [loading, navigate, user])

    const { step, options, isLoading, selectOption } = useEmotionalOnboarding(async () => {
        await refreshUser()
        navigate('/')
    })

    if (loading || user?.onBoardingCompleted === true) {
        return null
    }

    return (<EmotionalOnboarding 
    step={step}
     options={options} 
     isLoading={isLoading} 
     onSelectOption={selectOption} />
    )
}
