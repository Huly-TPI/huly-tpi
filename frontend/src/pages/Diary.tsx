import PageHeader from '../components/PageHeader'
import { getToday } from '../utils/date'

export default function Diary() {
  return (
    <div className="max-w-4xl mx-auto p-8">
      <PageHeader title="Diario emocional" subtitle="Escribí libremente lo que sentís" />
      <p className="text-gray-500 italic">Hoy es {getToday()}</p>
    </div>
  )
}