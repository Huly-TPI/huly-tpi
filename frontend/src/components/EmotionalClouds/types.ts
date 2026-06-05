export interface Cloud {
    id: string
    text: string
    top?: string
    duration?: string
}

export interface EmotionalCloudsProps {
    onThoughtAdded?: (thought: string) => void
    onFinish?: (thoughts: string[]) => void
    maxClouds?: number
}