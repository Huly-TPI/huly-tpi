import { SectionCard, CardHeader } from './SectionCard'
import { WellbeingChart } from './WellbeingChart'

// TODO: replace with real wellbeing historical data endpoint
const WELLBEING_POINTS = [62, 58, 71, 68, 75, 65, 72]
const WELLBEING_LABELS = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom']

export function WellbeingSection() {
  return (
    <SectionCard>
      <CardHeader title="Evolución del bienestar general" />
      <WellbeingChart points={WELLBEING_POINTS} labels={WELLBEING_LABELS} />
    </SectionCard>
  )
}
