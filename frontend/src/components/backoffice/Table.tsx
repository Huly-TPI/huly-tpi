import { ReactNode } from 'react'

export interface Column<T> {
  header: ReactNode
  render: (item: T) => ReactNode
  className?: string
}

interface TableProps<T> {
  data: T[]
  columns: Column<T>[]
  keyExtractor: (item: T) => string | number
  className?: string
}

export function Table<T>({ data, columns, keyExtractor, className = '' }: TableProps<T>) {
  return (
    <div className={className}>
      {/* Vista Mobile: Lista de Tarjetas */}
      <div className="block md:hidden space-y-4">
        {data.map((item) => (
          <div
            key={keyExtractor(item)}
            className="bg-gray-55/40 dark:bg-[#09111f]/30 border border-gray-100 dark:border-gray-800/40 rounded-xl p-4 shadow-sm flex flex-col gap-3"
          >
            {columns.map((col, idx) => (
              <div key={idx} className="flex justify-between items-start text-xs gap-3">
                <span className="font-bold text-gray-400 dark:text-gray-500 uppercase tracking-wider shrink-0 mt-0.5">
                  {col.header}
                </span>
                <div className={`text-gray-700 dark:text-gray-200 font-medium text-right ${col.className || ''}`}>
                  {col.render(item)}
                </div>
              </div>
            ))}
          </div>
        ))}
      </div>

      {/* Vista Desktop: Tabla Estándar */}
      <div className="hidden md:block overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-gray-100 dark:border-gray-800 text-xs lg:text-[13px] font-bold uppercase tracking-wider text-gray-400 dark:text-gray-500">
              {columns.map((col, idx) => (
                <th key={idx} className={`pb-3 pr-4 ${col.className || ''}`}>
                  {col.header}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-50 dark:divide-gray-800/40 text-sm lg:text-[16px] text-gray-600 dark:text-gray-300">
            {data.map((item) => (
              <tr key={keyExtractor(item)} className="hover:bg-gray-55/50 dark:hover:bg-gray-800/35">
                {columns.map((col, idx) => (
                  <td key={idx} className={`py-3.5 pr-4 ${col.className || ''}`}>
                    {col.render(item)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
