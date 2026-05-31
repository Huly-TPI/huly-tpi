import hulyCloudsImage from '../../assets/tutorial/huly-clouds.webp'
import hulyHouseImage from '../../assets/tutorial/huly-house.webp'
import hulyNotebookImage from '../../assets/tutorial/huly-notebook.webp'
import hulyTodoImage from '../../assets/tutorial/huly-to-do.webp'
import hulyTreeImage from '../../assets/tutorial/huly-tree.webp'
import hulyWateringCanImage from '../../assets/tutorial/huly-watering-can.webp'
import type { HomeOnboardingStep } from '../../components/Onboarding/HomeOnboarding/types'

export function createHomeOnboardingSteps(cloudElementIds: string[]): HomeOnboardingStep[] {
  return [
    {
      id: 'house',
      title: 'Casa',
      description: 'Acá vas a encontrar tu perfil, configuración y tu espacio personal',
      icon: 'C',
      elementIds: ['house'],
      cardClassName:
        'left-[1rem] right-[1rem] top-[1.6rem] md:left-[56%] md:right-auto md:top-[8.5%] min-[1400px]:left-[52%]',
      mascot: {
        imageSrc: hulyHouseImage,
        anchorElementId: 'house',
        placementClassName:
          'left-[-26%] top-[39.5%] z-[225] w-[30%] md:left-[-22%] md:top-[38.5%] md:w-[28%] min-[1400px]:left-[-24%] min-[1400px]:top-[38.5%] min-[1400px]:w-[28%]',
        imageClassName: 'w-full',
      },
    },
    {
      id: 'clouds',
      title: 'Respiraciones guiadas',
      description: 'Tomá unos minutos para frenar, respirar y bajar un cambio',
      icon: 'N',
      elementIds: cloudElementIds,
      cardClassName:
        'left-[1rem] right-[1rem] bottom-[6.75rem] md:left-auto md:right-[16%] md:bottom-auto md:top-[28%] min-[1400px]:right-[16.5%]',
      mascot: {
        imageSrc: hulyCloudsImage,
        placementClassName:
          'left-[49.5%] top-[53.5%] z-[225] w-[13%] md:left-[50.8%] md:top-[61%] md:w-[6.9%] min-[1400px]:left-[51.2%] min-[1400px]:top-[60.2%]',
        imageClassName: 'w-full',
      },
    },
    {
      id: 'todo-board',
      title: 'Pendientes',
      description: 'Liberá tu mente de lo que tenés que hacer y ordená tus pendientes',
      icon: 'P',
      elementIds: ['todo-board'],
      cardClassName:
        'left-[1rem] right-[1rem] top-[1.3rem] md:left-auto md:right-[13%] md:top-[33%] min-[1400px]:top-[31%]',
      mascot: {
        imageSrc: hulyTodoImage,
        anchorElementId: 'todo-board',
        placementClassName:
          'right-[53%] top-[14%] z-[225] w-[100%] md:right-[55%] md:top-[16%] md:w-[82%] min-[1400px]:right-[53%] min-[1400px]:top-[14%] min-[1400px]:w-[82%]',
        imageClassName: 'w-full',
      },
    },
    {
      id: 'tree',
      title: 'Minijuegos',
      description: 'Descansá la cabeza, distraete un rato y volvé con más aire',
      icon: 'M',
      elementIds: ['tree'],
      cardClassName:
        'left-[1rem] right-[1rem] top-[1.6rem] md:left-[19%] md:right-auto md:top-[18%] min-[1400px]:left-[19.5%]',
      mascot: {
        imageSrc: hulyTreeImage,
        anchorElementId: 'tree',
        placementClassName:
          'left-[52%] top-[50.5%] z-[225] w-[25%] md:left-[57%] md:top-[54.5%] md:w-[21%] min-[1400px]:left-[56%] min-[1400px]:top-[53.5%] min-[1400px]:w-[21%]',
        imageClassName: 'w-full',
      },
    },
    {
      id: 'notebook',
      title: 'Diario emocional',
      description: 'Escribí, reflexioná y registrá cómo te sentiste en tu día',
      icon: 'D',
      elementIds: ['notebook'],
      cardClassName:
        'left-[1rem] right-[1rem] top-[1.6rem] md:left-auto md:right-[14%] md:top-[58%] min-[1400px]:top-[54%]',
      mascot: {
        imageSrc: hulyNotebookImage,
        anchorElementId: 'notebook',
        placementClassName:
          'right-[38%] top-[18%] z-[225] w-[96%] md:right-[64%] md:top-[14%] md:w-[84%] min-[1400px]:right-[64%] min-[1400px]:top-[-8%] min-[1400px]:w-[92%]',
        imageClassName: 'w-full',
      },
    },
    {
      id: 'watering-can',
      title: 'Regar planta',
      description: 'Avanzá con pequeños retos y acompañá el crecimiento de tu planta',
      icon: 'R',
      elementIds: ['watering-can'],
      cardClassName:
        'left-[1rem] right-[1rem] top-[1.6rem] md:left-[25%] md:right-auto md:top-[60%] md:bottom-auto min-[1400px]:top-[56%]',
      mascot: {
        imageSrc: hulyWateringCanImage,
        anchorElementId: 'watering-can',
        placementClassName:
          'left-[88%] top-[-14%] z-[225] w-[84%] md:left-[102%] md:top-[2%] md:w-[76%] min-[1400px]:left-[102%] min-[1400px]:top-[2%] min-[1400px]:w-[76%]',
        imageClassName: 'w-full',
      },
    },
  ]
}
