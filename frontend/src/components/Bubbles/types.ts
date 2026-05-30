export interface Bubble {
    id: string
    color: string
    size: number
    driftX: number
    driftY: number
    animationDuration: number
    animationDelay: number
}

export interface EmotionalBubblesActivityProps{
    onBack?: () => void
}