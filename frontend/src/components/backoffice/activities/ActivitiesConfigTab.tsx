import { Edit2, CirclePlay } from 'lucide-react'
import { SectionCard, CardHeader } from '../SectionCard'
import { Table, Column } from '../Table'
import InfoPopup from '../InfoPopup'
import { AdminActivityConfig } from '../../../api/adminActivities'

interface ActivitiesConfigTabProps {
  configs: AdminActivityConfig[]
  activityIcons: Record<string, React.ComponentType<any>>
  activityThemes: Record<string, { bg: string; text: string; bar: string; bullet: string }>
  handleEditClick: (config: AdminActivityConfig) => void
}

export default function ActivitiesConfigTab({
  configs,
  activityIcons,
  activityThemes,
  handleEditClick,
}: ActivitiesConfigTabProps) {

  const formatVal = (val: number) => {
    return val >= 0 ? `+${val.toFixed(2)}` : val.toFixed(2)
  }

  const formatDelta = (val: number) => {
    return val >= 0 ? `+${val.toFixed(2)}` : `${val.toFixed(2)}`
  }

  const tableColumns: Column<AdminActivityConfig>[] = [
    {
      header: 'Actividad',
      render: (config) => {
        const Icon = activityIcons[config.type] || CirclePlay
        const theme = activityThemes[config.type] || { bg: 'bg-gray-100 text-gray-500' }
        return (
          <div className="flex items-center gap-3">
            <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl ${theme.bg}`}>
              <Icon className="h-5 w-5" />
            </div>
            <div>
              <p className="font-bold text-gray-800 dark:text-gray-100">{config.title}</p>
              <p className="text-[10px] text-gray-400 uppercase font-bold tracking-wider">{config.type}</p>
            </div>
          </div>
        )
      },
    },
    {
      header: (
        <div className="flex items-center justify-center">
          <span>Rango VAD (V - A - D)</span>
          <InfoPopup title="¿Qué es el Rango Emocional VAD?">
            <p className="text-gray-700 dark:text-gray-300 font-semibold mb-1">
              Mide el estado de ánimo del usuario en tres aspectos para decidir cuándo recomendar esta actividad:
            </p>
            <ul className="space-y-2 pl-3 list-disc text-gray-650 dark:text-gray-400">
              <li>
                <strong>V (Humor / Valencia):</strong> Qué tan bien se siente. 
                <span className="block pl-3 text-gray-500">Valores positivos = Alegre, contento, en paz.</span>
                <span className="block pl-3 text-gray-500">Valores negativos = Triste, angustiado, frustrado.</span>
              </li>
              <li>
                <strong>A (Energía / Estrés):</strong> Nivel de agitación física o mental. 
                <span className="block pl-3 text-gray-500">Valores altos = Nervioso, estresado, con mucha energía o enojado.</span>
                <span className="block pl-3 text-gray-500">Valores bajos = Apagado, muy cansado, relajado o con desgano.</span>
              </li>
              <li>
                <strong>D (Sensación de Control):</strong> Qué tan dueño de la situación se siente. 
                <span className="block pl-3 text-gray-500">Valores altos = Seguro, con confianza.</span>
                <span className="block pl-3 text-gray-500">Valores bajos = Abrumado, desprotegido, sin control.</span>
              </li>
            </ul>
            <p className="text-[10px] text-gray-400 dark:text-gray-500 italic mt-1 border-t border-gray-100 dark:border-gray-800 pt-1.5 font-normal">
              * El chatbot sugerirá esta actividad cuando el estado emocional detectado en los mensajes del usuario coincida con los rangos configurados.
            </p>
          </InfoPopup>
        </div>
      ),
      className: 'text-center',
      render: (config) => (
        <div className="flex flex-col items-center gap-0.5 font-mono text-[11px] text-gray-600 dark:text-gray-400 whitespace-nowrap">
          <p>
            <span className="text-violeta dark:text-violeta-claro font-bold mr-1">V</span>
            {formatVal(config.valenceMin)} &nbsp; {formatVal(config.valenceMax)}
          </p>
          <p>
            <span className="text-blue-400 font-bold mr-1">A</span>
            {formatVal(config.arousalMin)} &nbsp; {formatVal(config.arousalMax)}
          </p>
          <p>
            <span className="text-teal-400 font-bold mr-1">D</span>
            {formatVal(config.dominanceMin)} &nbsp; {formatVal(config.dominanceMax)}
          </p>
        </div>
      ),
    },
    {
      header: (
        <div className="flex items-center justify-center">
          <span>Efectos Esperados</span>
          <InfoPopup title="¿Qué son los Efectos Esperados?">
            <p className="text-gray-700 dark:text-gray-300 font-semibold mb-1">
              Es el cambio que buscamos provocar en el estado de ánimo de la persona tras realizar la actividad:
            </p>
            <ul className="space-y-2 pl-3 list-disc text-gray-650 dark:text-gray-400">
              <li>
                <strong>ΔV (Cambio en Humor):</strong> Cuánto mejora su ánimo. Un valor positivo (+0.25) significa que el usuario terminará sintiéndose más contento o aliviado.
              </li>
              <li>
                <strong>ΔA (Cambio en Energía/Estrés):</strong> Cómo impacta en su nivel de agitación:
                <div className="pl-3 mt-1 text-[11px] font-normal leading-normal">
                  <span className="text-blue-500 font-bold block">• Calmante (Valores bajo cero):</span> Ideal para bajar revoluciones, calmar la ansiedad, el estrés o el enojo (ej: Respiración).
                  <span className="text-orange-500 font-bold block">• Activante (Valores sobre cero):</span> Ideal para recargar pilas, despertar la mente y combatir el desgano o aburrimiento (ej: Retos).
                </div>
              </li>
              <li>
                <strong>ΔD (Cambio en Control):</strong> Cuánto le ayuda a recuperar la autoconfianza y sentirse a cargo de sus emociones.
              </li>
            </ul>
          </InfoPopup>
        </div>
      ),
      className: 'text-center',
      render: (config) => (
        <div className="flex flex-col items-center gap-0.5 font-mono text-[11px] text-gray-600 dark:text-gray-400 whitespace-nowrap">
          <p>
            <span className="text-violeta dark:text-violeta-claro font-bold mr-1">ΔV</span>
            {formatDelta(config.effectValence)}
          </p>
          <p className={config.effectArousal <= 0 ? 'text-blue-400' : 'text-orange-400'}>
            <span className="font-bold mr-1">ΔA</span>
            {formatDelta(config.effectArousal)}
          </p>
          <p>
            <span className="text-teal-400 font-bold mr-1">ΔD</span>
            {formatDelta(config.effectDominance)}
          </p>
        </div>
      ),
    },
    {
      header: 'Palabras Clave',
      render: (config) => (
        <span className="text-xs text-gray-500 truncate max-w-[150px] md:max-w-[180px] block" title={config.goalKeywords}>
          {config.goalKeywords || <span className="italic text-gray-300">Ninguna</span>}
        </span>
      ),
    },
    {
      header: 'Ruta',
      render: (config) => (
        <span className="font-mono text-xs text-violeta dark:text-violeta-claro">
          {config.routePath}
        </span>
      ),
    },
    {
      header: 'Acciones',
      className: 'text-center',
      render: (config) => (
        <div className="flex justify-center">
          <button
            onClick={() => handleEditClick(config)}
            className="inline-flex h-8 w-8 items-center justify-center rounded-lg hover:bg-[#D1CAEF]/30 dark:hover:bg-[#D1CAEF]/10 text-gray-400 dark:text-gray-555 hover:text-violeta dark:hover:text-violeta-claro transition duration-150"
            aria-label={`Editar configuración de ${config.title}`}
          >
            <Edit2 className="h-5 w-5" strokeWidth={2} />
          </button>
        </div>
      ),
    },
  ]

  return (
    <SectionCard className="bg-white dark:bg-[#172033] md:flex-1 md:min-h-0 flex flex-col">
      <CardHeader title="Configuración de Rangos VAD y Parámetros" />
      <Table
        data={configs}
        columns={tableColumns}
        keyExtractor={(item) => item.id}
      />
    </SectionCard>
  )
}
