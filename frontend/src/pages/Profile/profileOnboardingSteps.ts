import type { HomeOnboardingStep } from '../../components/Onboarding/HomeOnboarding/types'

export const profileOnboardingSteps: HomeOnboardingStep[] = [
  {
    id: 'window',
    title: 'Ventana',
    description: 'Volvés al jardín cuando quieras salir de tu habitación y seguir recorriendo las distintas actividades.',
    icon: 'V',
    elementIds: ['window'],
    cardClassName:
      'left-[1rem] right-[1rem] bottom-[6.75rem] md:left-[40.5%] md:right-auto md:top-[56%] md:bottom-auto',
  },
  {
    id: 'mirror',
    title: 'Espejo',
    description: 'Configurá tu información básica para mantener tu perfil actualizado.',
    icon: 'E',
    elementIds: ['mirror'],
    cardClassName:
      'left-[1rem] right-[1rem] top-[1.5rem] md:left-[28%] md:right-auto md:top-[45%]',
  },
  {
    id: 'chest',
    title: 'Baúl',
    description: 'Administrás tu cambio de contraseña y las claves delicadas que protegen tu cuenta.',
    icon: 'B',
    elementIds: ['chest'],
    cardClassName:
      'left-[1rem] right-[1rem] top-[1.5rem] md:left-[40.5%] md:right-auto md:top-[35%]',
  },
  {
    id: 'clock',
    title: 'Reloj',
    description: 'Configurá la extensión para pausas digitales y elegí cuándo querés recibir avisos.',
    icon: 'R',
    elementIds: ['clock'],
    cardClassName:
      'left-[1rem] right-[1rem] bottom-[6.75rem] md:left-auto md:right-[29%] md:top-[34%] md:bottom-auto',
  },
  {
    id: 'music',
    title: 'Música',
    description: 'Ajustás el volumen del ambiente para que la experiencia suene cómoda para vos.',
    icon: 'M',
    elementIds: ['music'],
    cardClassName:
      'left-[1rem] right-[1rem] top-[1.5rem] md:left-auto md:right-[22%] md:top-[34%]',
  },
]
