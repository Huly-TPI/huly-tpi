import { api } from './client';
import { BreathingTechnique } from '../components/BreathingGuide';

export function getBreathingTechniques(): Promise<BreathingTechnique[]> {
  return api.get('/breathing/techniques');
}