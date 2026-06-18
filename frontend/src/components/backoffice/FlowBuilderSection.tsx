import { SectionCard, CardHeader } from './SectionCard'
import Button from '../Buttons/Button/Button'

const FLOW_NODE_STYLES: Record<string, { bg: string; border: string; labelColor: string }> = {
  amber:  { bg: 'bg-amber-50 dark:bg-amber-950/20',  border: 'border-amber-200 dark:border-amber-900/30',  labelColor: 'text-amber-500 dark:text-amber-400' },
  green:  { bg: 'bg-green-50 dark:bg-green-950/20',  border: 'border-green-200 dark:border-green-900/30',  labelColor: 'text-green-500 dark:text-green-400' },
  violet: { bg: 'bg-violet-50 dark:bg-violet-950/20', border: 'border-violet-200 dark:border-violet-900/30', labelColor: 'text-violeta dark:text-violeta-claro'   },
}

interface FlowNodeProps {
  icon: string
  label: string
  typeLabel: string
  color: string
  type: 'trigger' | 'action' | 'decision'
}

function FlowNode({ icon, label, typeLabel, color }: FlowNodeProps) {
  const s = FLOW_NODE_STYLES[color]
  return (
    <div className={`flex flex-col items-center rounded-2xl border px-3 py-3 min-w-[90px] max-w-[110px] ${s.bg} ${s.border}`}>
      <p className={`text-[9px] font-bold uppercase tracking-wider ${s.labelColor}`}>{typeLabel}</p>
      <div className="my-2 flex h-9 w-9 items-center justify-center rounded-full bg-white dark:bg-[#09111f] shadow-sm dark:shadow-none dark:border dark:border-gray-800 text-xl font-bold">
        {icon}
      </div>
      <p className="text-center text-[10px] font-semibold text-gray-600 dark:text-gray-300 leading-tight">{label}</p>
    </div>
  )
}

export function FlowBuilderSection() {
  return (
    <SectionCard>
      <CardHeader
        title="Constructor de flujos terapéuticos"
        action={
          <Button variant="secondary" size="sm">
            + Nuevo nodo
          </Button>
        }
      />
      <div className="overflow-x-auto rounded-xl border border-dashed border-gray-200 dark:border-gray-800 bg-gray-50/50 dark:bg-[#09111f]/50 p-4">
        <div className="flex min-w-[320px] items-center gap-2">
          <FlowNode type="trigger"  icon="⚡" label="Ansiedad detectada" typeLabel="TRIGGER"    color="amber"  />
          <div className="flex-1 border-t-2 border-dashed border-gray-200 dark:border-gray-800 min-w-[24px]" />
          <FlowNode type="action"   icon="🌱" label="Mensaje empático"   typeLabel="ACCIÓN AI"  color="green"  />
          <div className="flex-1 border-t-2 border-dashed border-gray-200 dark:border-gray-800 min-w-[24px]" />
          <FlowNode type="decision" icon="?"  label="¿Desea actividad?"  typeLabel="DECISIÓN"   color="violet" />
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          {['{{user_name}}', '{{last_mood}}', '{{time_of_day}}'].map(v => (
            <span key={v} className="rounded-full bg-white dark:bg-[#172033] border border-gray-200 dark:border-gray-800 px-2.5 py-1 text-[10px] font-mono text-gray-500 dark:text-gray-400 shadow-sm dark:shadow-none">
              {v}
            </span>
          ))}
          <Button variant="tertiary" size="sm">
            + Añadir variable
          </Button>
        </div>
      </div>
    </SectionCard>
  )
}
