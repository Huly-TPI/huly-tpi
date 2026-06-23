import mandala01 from '../../assets/mandalas/mandala-01.svg'
import mandala02 from '../../assets/mandalas/mandala-02.svg'
import mandala03 from '../../assets/mandalas/mandala-03.svg'
import mandala04 from '../../assets/mandalas/mandala-04.svg'
import mandala05 from '../../assets/mandalas/mandala-05.svg'
import type { MandalaCatalogItem } from './mandalaTypes'

export const mandalaCatalog: MandalaCatalogItem[] = [
  {
    id: 'mandala-01',
    title: 'Mandala 01',
    description: 'Trazos circulares para pintar con calma.',
    src: mandala01,
    accessStatus: 'available',
    unlockSource: 'free',
  },
  {
    id: 'mandala-02',
    title: 'Mandala 02',
    description: 'Formas amplias para explorar colores.',
    src: mandala02,
    accessStatus: 'available',
    unlockSource: 'free',
  },
  {
    id: 'mandala-03',
    title: 'Mandala 03',
    description: 'Patron geometrico inspirado en flores.',
    src: mandala03,
    accessStatus: 'available',
    unlockSource: 'free',
  },
  {
    id: 'mandala-04',
    title: 'Mandala 04',
    description: 'Detalle fino para una experiencia concentrada.',
    src: mandala04,
    accessStatus: 'premium',
    unlockSource: 'premiumPlan',
  },
  {
    id: 'mandala-05',
    title: 'Mandala 05',
    description: 'Lineas organicas para pintar por secciones.',
    src: mandala05,
    accessStatus: 'locked',
    unlockSource: 'store',
  },
]
