import { useUsers } from '../../hooks/backoffice/useUsers'
import { UserResponse } from '../../api/admin'
import { SectionCard } from '../../components/backoffice/SectionCard'
import { Table, Column } from '../../components/backoffice/Table'
import { Search, Eye } from 'lucide-react'

export default function UsersPage() {
  const {
    search,
    setSearch,
    loading,
    error,
    filteredUsers,
    navigate,
  } = useUsers()

  const userColumns: Column<UserResponse>[] = [
    {
      header: 'Usuario',
      render: (u) => (
        <div>
          <div className="font-semibold text-gray-800 dark:text-gray-200 text-sm">{u.name || 'sin nombre'}</div>
          <div className="text-xs text-gray-400 dark:text-gray-500">{u.email}</div>
        </div>
      ),
    },
    {
      header: 'Antiscroll',
      className: 'text-center',
      render: (u) => (
        <span
          className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold leading-5 ${
            u.antiScrollEnabled
              ? 'bg-green-100 dark:bg-green-950/30 text-green-800 dark:text-green-400'
              : 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-300'
          }`}
        >
          {u.antiScrollEnabled ? 'activo' : 'inactivo'}
        </span>
      ),
    },
    {
      header: 'Estadísticas',
      className: 'text-center',
      render: (u) => (
        <span
          className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-bold leading-5 ${
            u.dataSharingConsent
              ? 'bg-blue-100 dark:bg-blue-955/35 text-blue-800 dark:text-blue-400'
              : 'bg-red-100 dark:bg-red-955/35 text-red-800 dark:text-red-400'
          }`}
        >
          {u.dataSharingConsent ? 'aceptado' : 'denegado'}
        </span>
      ),
    },
    {
      header: 'Acciones',
      className: 'text-center',
      render: (u) => (
        <button
          onClick={() => {
            navigate(`/backoffice/usuarios/${u.id}`)
          }}
          className="inline-flex h-8 w-8 items-center justify-center rounded-lg hover:bg-[#D1CAEF]/30 dark:hover:bg-[#D1CAEF]/10 text-gray-400 dark:text-gray-550 hover:text-violeta dark:hover:text-violeta-claro transition duration-150"
          aria-label={`Ver detalles de ${u.name}`}
        >
          <Eye className="h-5 w-5" strokeWidth={1.8} />
        </button>
      ),
    },
  ]

  return (
    <div className="flex flex-col gap-4 animate-fadeIn">
      <div className="flex flex-col gap-0.5">
        <h1 className="text-[30px] font-extrabold leading-tight text-violeta dark:text-violeta-claro">Usuarios</h1>
        <p className="text-[16px] text-[#A0AEC0] dark:text-gray-400">
          Administra los usuarios registrados en el sistema y visualiza sus consentimientos y configuraciones de extensión.
        </p>
      </div>

      <SectionCard className="bg-white dark:bg-[#172033]">
        <div className="flex flex-col gap-4">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <h2 className="text-lg font-bold text-gray-700 dark:text-gray-200">Listado de usuarios</h2>
            <div className="relative w-full sm:w-64">
              <input
                type="text"
                placeholder="Buscar por nombre o email..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-gray-800 bg-gray-55 dark:bg-[#09111f] py-2 pl-4 pr-10 text-sm text-gray-700 dark:text-gray-200 outline-none transition duration-150 focus:border-violeta dark:focus:border-violeta-claro focus:bg-white dark:focus:bg-[#172033]"
              />
              <div className="absolute right-3 top-2.5 text-gray-400 dark:text-gray-500">
                <Search className="h-4 w-4" strokeWidth={1.8} />
              </div>
            </div>
          </div>

          {loading ? (
            <div className="py-8 text-center text-sm text-gray-400 dark:text-gray-500">Cargando usuarios...</div>
          ) : error ? (
            <div className="py-8 text-center text-sm text-red-500">{error}</div>
          ) : filteredUsers.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-400 dark:text-gray-500">No se encontraron usuarios</div>
          ) : (
            <Table data={filteredUsers} columns={userColumns} keyExtractor={(u) => u.id} />
          )}
        </div>
      </SectionCard>
    </div>
  )
}
