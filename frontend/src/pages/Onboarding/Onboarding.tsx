import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useEmotionalOnboarding } from '../../hooks/useEmotionalOnboarding'
import EmotionalOnboarding from '../../components/Onboarding/EmotionalOnboarding/EmotionalOnboarding'
import { useAuth } from '../../context/auth'
import { useBadgeToast } from '../../context/BadgeToast'
import { badgesApi } from '../../api/badges'

export default function Onboarding() {
    const navigate = useNavigate()
    const { user, loading, refreshUser } = useAuth()
    const { showBadgeToast } = useBadgeToast()

    useEffect(() => {
        if (loading) return
        if (user?.onBoardingCompleted === true) {
            navigate('/')
        }
    }, [loading, navigate, user])

    const { step, options, isLoading, selectOption } = useEmotionalOnboarding(async () => {
        await refreshUser()
          const myBadges = await badgesApi.getMyBadges()
        const primerPaso = myBadges.find(ub => ub.badge.code === 'PRIMER_PASO')
        if (primerPaso) {
            showBadgeToast(primerPaso.badge)
        }
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
