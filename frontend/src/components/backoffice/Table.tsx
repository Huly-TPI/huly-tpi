import { ReactNode } from 'react'

export interface Column<T> {
  header: string
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
    <div className={`overflow-x-auto ${className}`}>
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="border-b border-gray-100 text-xs font-bold uppercase tracking-wider text-gray-400">
            {columns.map((col, idx) => (
              <th key={idx} className={`pb-3 pr-4 ${col.className || ''}`}>
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-50 text-sm text-gray-600">
          {data.map((item) => (
            <tr key={keyExtractor(item)} className="hover:bg-gray-50/50">
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
  )
}
