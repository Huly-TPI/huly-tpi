import { SectionCard, CardHeader } from './SectionCard'

const FLOW_NODE_STYLES: Record<string, { bg: string; border: string; labelColor: string }> = {
  amber:  { bg: 'bg-amber-50',  border: 'border-amber-200',  labelColor: 'text-amber-500' },
  green:  { bg: 'bg-green-50',  border: 'border-green-200',  labelColor: 'text-green-500' },
  violet: { bg: 'bg-violet-50', border: 'border-violet-200', labelColor: 'text-violeta'   },
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
      <div className="my-2 flex h-9 w-9 items-center justify-center rounded-full bg-white shadow-sm text-xl font-bold">
        {icon}
      </div>
      <p className="text-center text-[10px] font-semibold text-gray-600 leading-tight">{label}</p>
    </div>
  )
}

export function FlowBuilderSection() {
  return (
    <SectionCard>
      <CardHeader
        title="Constructor de flujos terapéuticos"
        action={
          <button className="flex items-center gap-1 rounded-xl border border-violeta-claro bg-violeta-claro/40 px-3 py-1.5 text-xs font-semibold text-violeta hover:bg-violeta hover:text-white transition-colors">
            + Nuevo nodo
          </button>
        }
      />
      <div className="overflow-x-auto rounded-xl border border-dashed border-gray-200 bg-gray-50/50 p-4">
        <div className="flex min-w-[320px] items-center gap-2">
          <FlowNode type="trigger"  icon="⚡" label="Ansiedad detectada" typeLabel="TRIGGER"    color="amber"  />
          <div className="flex-1 border-t-2 border-dashed border-gray-200 min-w-[24px]" />
          <FlowNode type="action"   icon="🌱" label="Mensaje empático"   typeLabel="ACCIÓN AI"  color="green"  />
          <div className="flex-1 border-t-2 border-dashed border-gray-200 min-w-[24px]" />
          <FlowNode type="decision" icon="?"  label="¿Desea actividad?"  typeLabel="DECISIÓN"   color="violet" />
        </div>
        <div className="mt-4 flex flex-wrap gap-2">
          {['{{user_name}}', '{{last_mood}}', '{{time_of_day}}'].map(v => (
            <span key={v} className="rounded-full bg-white border border-gray-200 px-2.5 py-1 text-[10px] font-mono text-gray-500 shadow-sm">
              {v}
            </span>
          ))}
          <button className="rounded-full border border-dashed border-violeta-claro px-2.5 py-1 text-[10px] font-semibold text-violeta hover:bg-violeta-claro/40 transition-colors">
            + Añadir variable
          </button>
        </div>
      </div>
    </SectionCard>
  )
}
