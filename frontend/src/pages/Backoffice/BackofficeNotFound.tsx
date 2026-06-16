import { Link } from 'react-router-dom'
import { AlertOctagon } from 'lucide-react'

export default function BackofficeNotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-center p-6 animate-fadeIn">
      <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-red-50 dark:bg-red-950/20 text-red-500 mb-6 shadow-sm">
        <AlertOctagon className="h-10 w-10" strokeWidth={1.8} />
      </div>
      
      <h2 className="text-3xl font-extrabold text-gray-800 dark:text-gray-100 tracking-tight mb-2">
        Página no encontrada
      </h2>
      
      <p className="text-gray-400 dark:text-gray-500 text-sm max-w-md mb-8">
        La sección del backoffice a la que intentas acceder no existe o no tienes los permisos necesarios para verla.
      </p>

      <Link
        to="/backoffice"
        className="inline-flex items-center justify-center rounded-xl bg-[#8869AC] px-6 py-3 text-sm font-semibold text-white shadow-sm hover:bg-[#8869AC]/95 hover:translate-y-[-1px] transition-all duration-150"
      >
        Volver al Dashboard
      </Link>
    </div>
  )
}
