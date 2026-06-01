import { useCallback, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { EmotionalCloudsActivity } from '../../components/EmotionalClouds'
import { api } from '../../api/client'
import Button from '../../components/Buttons/Button/Button'

interface CloudRecommendationResponse {
    activity_type: string
    action_id: string
    title: string
    description: string
    redirect_url: string
}

const ACTIVITY_LABELS: Record<string, string> = {
    diary: 'al diario',
    clouds: 'a las nubes',
    breathing: 'a respiración guiada',
    bubbles: 'a las burbujas',
}

const CloudsActivity = () => {
    const navigate = useNavigate()
    const [recommendation, setRecommendation] = useState<CloudRecommendationResponse | null>(null)
    const [loading, setLoading] = useState(false)

    const handleThoughtAdded = useCallback((thought: string) => {
        api.post('/clouds/thought', { thought }).catch(() => {})
    }, [])

    const handleFinish = useCallback(async (thoughts: string[]) => {
        if (thoughts.length === 0) return
        setLoading(true)
        try {
            const rec = await api.post<CloudRecommendationResponse>('/clouds/recommendation', { thoughts })
            setRecommendation(rec)
        } catch (error) {
            console.error('Error al obtener recomendación:', error)
        } finally {
            setLoading(false)
        }
    }, [])

    const handleNavigate = useCallback(() => {
        if (recommendation) {
            navigate(recommendation.redirect_url)
            setRecommendation(null)
        }
    }, [recommendation, navigate])

    return (
        <div className="h-full max-h-full w-full overflow-hidden flex flex-col select-none relative">

            <main className="w-full flex-1 min-h-0 flex flex-col relative z-10 overflow-hidden">
                <EmotionalCloudsActivity
                    onThoughtAdded={handleThoughtAdded}
                    onFinish={handleFinish}
                    maxClouds={8}
                />
            </main>

            {loading && (
                <div className="absolute inset-0 bg-white/60 flex items-center justify-center z-20">
                    <p className="text-[#8869AC] text-lg font-semibold">Analizando tus nubes...</p>
                </div>
            )}

            {recommendation && !loading && (
                <div className="absolute inset-0 bg-white/70 backdrop-blur-sm flex items-center justify-center z-20">
                    <div className="bg-white rounded-2xl shadow-xl p-8 max-w-sm mx-4 text-center">
                        <h2 className="text-2xl font-bold text-[#8869AC] mb-3">
                            {recommendation.title}
                        </h2>
                        <p className="text-gray-500 mb-6 leading-relaxed">
                            {recommendation.description}
                        </p>
                        <Button variant="primary" fullWidth onClick={handleNavigate}>
                            Ir {ACTIVITY_LABELS[recommendation.activity_type] ?? 'a la actividad'}
                        </Button>
                        <Button
                            variant="secondary"
                            fullWidth
                            onClick={() => setRecommendation(null)}
                            className="mt-3"
                        >
                            Quedarme aquí
                        </Button>
                    </div>
                </div>
            )}
        </div>
    )
}

export default CloudsActivity
