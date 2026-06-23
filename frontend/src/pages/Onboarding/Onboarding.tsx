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

    
    
  const { step, step1Options, pillOptions, advance, selectOption, skip, submitting } =
    useEmotionalOnboarding(async () => {
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

    return ( <EmotionalOnboarding
        step={step}
        step1Options={step1Options}
        pillOptions={pillOptions}
        onAdvance={advance}
        onSelectOption={selectOption}
        onSkip={skip}
        submitting={submitting}
    />
    )
}
