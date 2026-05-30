import type { HomeOnboardingStep } from '../../components/Onboarding/HomeOnboarding/types'

export function createHomeOnboardingSteps(cloudElementIds: string[]): HomeOnboardingStep[] {
  return [
    {
      id: 'house',
      title: 'Casa',
      description: 'Aca vas a encontrar tu perfil, configuracion y tu espacio personal.',
      icon: 'C',
      elementIds: ['house'],
      cardClassName:
        'left-[1rem] top-[5.5rem] md:left-[56%] md:top-[8.5%] min-[1400px]:left-[52%]',
    },
    {
      id: 'clouds',
      title: 'Respiraciones guiadas',
      description: 'Tomate unos minutos para frenar, respirar y bajar un cambio.',
      icon: 'N',
      elementIds: cloudElementIds,
      cardClassName:
        'right-[1rem] top-[6rem] md:right-[16%] md:top-[28%] min-[1400px]:right-[16.5%]',
    },
    {
      id: 'todo-board',
      title: 'Pendientes',
      description: 'Libera tu mente de lo que tenes que hacer y ordena tus pendientes.',
      icon: 'P',
      elementIds: ['todo-board'],
      cardClassName:
        'right-[1rem] top-[20rem] md:right-[13%] md:top-[33%] min-[1400px]:top-[31%]',
    },
    {
      id: 'tree',
      title: 'Minijuegos',
      description: 'Descansa la cabeza, distraete un rato y volve con mas aire.',
      icon: 'M',
      elementIds: ['tree'],
      cardClassName:
        'left-[1rem] top-[10rem] md:left-[19%] md:top-[18%] min-[1400px]:left-[19.5%]',
    },
    {
      id: 'notebook',
      title: 'Diario emocional',
      description: 'Escribi, reflexiona y registra como te sentiste en tu dia.',
      icon: 'D',
      elementIds: ['notebook'],
      cardClassName:
        'right-[1rem] top-[19rem] md:right-[14%] md:top-[58%] min-[1400px]:top-[54%]',
    },
    {
      id: 'watering-can',
      title: 'Regar planta',
      description: 'Avanza con pequenos retos y acompana el crecimiento de tu planta.',
      icon: 'R',
      elementIds: ['watering-can'],
      cardClassName:
        'left-[1rem] bottom-[8.25rem] md:left-[25%] md:top-[60%] md:bottom-auto min-[1400px]:top-[56%]',
    },
  ]
}
