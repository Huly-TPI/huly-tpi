export interface Cloud {
    id: string
    text: string
    top?: string
    duration?: string
}

export interface EmotionalCloudsProps {
    onFinish?: (thoughts: string[]) => void
    maxClouds?: number
}