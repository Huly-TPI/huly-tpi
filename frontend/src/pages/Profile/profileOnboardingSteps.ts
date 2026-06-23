import type { HomeOnboardingStep } from '../../components/Onboarding/HomeOnboarding/types'

export const profileOnboardingSteps: HomeOnboardingStep[] = [
  {
    id: 'window',
    title: 'Ventana',
    description: 'Volver al jardín',
    icon: 'V',
    elementIds: ['window'],
    cardClassName:
      'left-[1rem] right-[1rem] bottom-[6.75rem] md:left-[40.5%] md:right-auto md:top-[56%] md:bottom-auto',
  },
  {
    id: 'mirror',
    title: 'Espejo',
    description: 'Editar información del perfil',
    icon: 'E',
    elementIds: ['mirror'],
    cardClassName:
      'left-[1rem] right-[1rem] top-[1.5rem] md:left-[28%] md:right-auto md:top-[45%]',
  },
  {
    id: 'chest',
    title: 'Baúl',
    description: 'Cambio de contraseña',
    icon: 'B',
    elementIds: ['chest'],
    cardClassName:
      'left-[1rem] right-[1rem] top-[1.5rem] md:left-[40.5%] md:right-auto md:top-[35%]',
  },
  {
    id: 'clock',
    title: 'Reloj',
    description: 'Configuración de la extensión anti-scroll',
    icon: 'R',
    elementIds: ['clock'],
    cardClassName:
      'left-[1rem] right-[1rem] bottom-[6.75rem] md:left-auto md:right-[29%] md:top-[34%] md:bottom-auto',
  },
  {
    id: 'music',
    title: 'Música',
    description: 'Sonidos ambientales y volúmenes',
    icon: 'M',
    elementIds: ['music'],
    cardClassName:
      'left-[1rem] right-[1rem] top-[1.5rem] md:left-auto md:right-[22%] md:top-[34%]',
  },
]
