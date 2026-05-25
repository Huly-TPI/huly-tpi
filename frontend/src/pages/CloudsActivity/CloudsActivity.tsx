import { useCallback } from 'react'
import { EmotionalCloudsActivity } from '../../components/EmotionalClouds';

const CloudsActivity = () => {

    const handleThoughtSubmit = useCallback(async (text: string) => {
        try {
            console.log('Enviando pensamiento al jardín de Huly:', text)
        } catch (error) {
            console.error('Error al conectar con el servidor de Huly:', error)
        }
    }, [])

    return (
        <div className="h-full max-h-full w-full overflow-hidden flex flex-col select-none relative">

            <main className="w-full flex-1 min-h-0 flex flex-col relative z-10 overflow-hidden">
                <EmotionalCloudsActivity
                    onThoughtSubmit={handleThoughtSubmit}
                    maxClouds={8}
                />
            </main>

        </div>
    )
}

export default CloudsActivity