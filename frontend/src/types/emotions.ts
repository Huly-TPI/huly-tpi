export enum EmotionType {
  JOY = 'JOY',
  SADNESS = 'SADNESS',
  ANGER = 'ANGER',
  FEAR = 'FEAR',
  SURPRISE = 'SURPRISE',
  DISGUST = 'DISGUST',
  ANXIETY = 'ANXIETY',
  STRESS = 'STRESS',
  OVERWHELM = 'OVERWHELM',
  PANIC = 'PANIC',
  HOPELESSNESS = 'HOPELESSNESS',
  EMPTINESS = 'EMPTINESS',
  LONELINESS = 'LONELINESS',
  GRIEF = 'GRIEF',
  SHAME = 'SHAME',
  GUILT = 'GUILT',
  FRUSTRATION = 'FRUSTRATION',
  IRRITABILITY = 'IRRITABILITY',
  CALM = 'CALM',
  LOVE = 'LOVE',
  GRATITUDE = 'GRATITUDE',
  MOTIVATION = 'MOTIVATION',
  EXHAUSTION = 'EXHAUSTION',
  NUMBNESS = 'NUMBNESS',
  CONFUSION = 'CONFUSION',
  NEUTRAL = 'NEUTRAL'
}

export const EMOTION_SPANISH_MAP: Record<EmotionType, string> = {
  [EmotionType.JOY]: 'alegría',
  [EmotionType.SADNESS]: 'tristeza',
  [EmotionType.ANGER]: 'enojo',
  [EmotionType.FEAR]: 'miedo',
  [EmotionType.SURPRISE]: 'sorpresa',
  [EmotionType.DISGUST]: 'disgusto',
  [EmotionType.ANXIETY]: 'ansiedad',
  [EmotionType.STRESS]: 'estrés',
  [EmotionType.OVERWHELM]: 'agobio',
  [EmotionType.PANIC]: 'pánico',
  [EmotionType.HOPELESSNESS]: 'desesperanza',
  [EmotionType.EMPTINESS]: 'vacío',
  [EmotionType.LONELINESS]: 'soledad',
  [EmotionType.GRIEF]: 'duelo',
  [EmotionType.SHAME]: 'vergüenza',
  [EmotionType.GUILT]: 'culpa',
  [EmotionType.FRUSTRATION]: 'frustración',
  [EmotionType.IRRITABILITY]: 'irritabilidad',
  [EmotionType.CALM]: 'calma',
  [EmotionType.LOVE]: 'amor',
  [EmotionType.GRATITUDE]: 'gratitud',
  [EmotionType.MOTIVATION]: 'motivación',
  [EmotionType.EXHAUSTION]: 'agotamiento',
  [EmotionType.NUMBNESS]: 'embotamiento',
  [EmotionType.CONFUSION]: 'confusión',
  [EmotionType.NEUTRAL]: 'neutral'
}

export const mapEmotionToSpanish = (emotion: string | null): string => {
  if (!emotion) return 'Neutral'
  const normalized = emotion.trim().toUpperCase() as EmotionType
  const spanish = EMOTION_SPANISH_MAP[normalized]
  if (spanish) {
    return spanish.charAt(0).toUpperCase() + spanish.slice(1)
  }
  const fallback = emotion.trim().toLowerCase()
  return fallback.charAt(0).toUpperCase() + fallback.slice(1)
}
