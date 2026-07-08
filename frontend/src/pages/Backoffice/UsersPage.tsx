import { useState, useEffect, FormEvent } from 'react'
import { useUsers } from '../../hooks/backoffice/useUsers'
import { UserResponse } from '../../api/admin'
import { SectionCard } from '../../components/backoffice/SectionCard'
import { Table, Column } from '../../components/backoffice/Table'
import { Search, Eye, Check, X } from 'lucide-react'
import { mapEmotionToSpanish } from '../../types/ai'
import { ErrorMessage } from '../../components/backoffice/ErrorMessage'
import seedIcon from '../../assets/rewards/seed.webp'
import PageHeader from '../../components/backoffice/PageHeader'


export default function UsersPage() {
  const {
    search,
    setSearch,
    loading,
    error,
    filteredUsers,
    navigate,
  } = useUsers()

  const [inputValue, setInputValue] = useState(search)

  useEffect(() => {
    setInputValue(search)
  }, [search])

  const handleSearchSubmit = (e: FormEvent) => {
    e.preventDefault()
    setSearch(inputValue)
  }

  const userColumns: Column<UserResponse>[] = [
    {
      header: 'Usuario',
      render: (u) => (
        <div>
          <div className="font-semibold text-gray-800 dark:text-gray-200 text-sm lg:text-[16px]">{u.name || 'sin nombre'}</div>
          <div className="text-xs lg:text-[13px] text-gray-400 dark:text-gray-550">{u.email}</div>
        </div>
      ),
    },
    {
      header: 'Emoción predominante',
      className: 'text-center',
      render: (u) => (
        <span className="font-semibold text-xs lg:text-[13px] text-gray-750 dark:text-gray-300">
          {mapEmotionToSpanish(u.dominantEmotion ?? null)}
        </span>
      ),
    },
    {
      header: 'Plan',
      className: 'text-center',
      render: (u) => (
        <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs lg:text-[13px] font-bold leading-5 ${
          u.plan && u.plan !== 'Gratuito' && u.plan !== 'FREE'
            ? 'bg-violeta-claro/30 dark:bg-[#2A233C] text-violeta dark:text-violeta-claro'
            : 'bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-gray-300'
        }`}>
          {u.plan || 'Gratuito'}
        </span>
      ),
    },
    {
      header: 'Semillas',
      className: 'text-center',
      render: (u) => (
        <span className="inline-flex items-center gap-1 font-bold text-sm lg:text-[16px] text-amber-500 dark:text-amber-400">
          <img src={seedIcon} alt="" aria-hidden="true" className="h-4 w-4 object-contain shrink-0" />
          {u.coins ?? 0}
        </span>
      ),
    },
    {
      header: 'Antiscroll',
      className: 'text-center',
      render: (u) => (
        <span className={`inline-flex items-center font-bold ${u.antiScrollEnabled ? 'text-green-600 dark:text-green-400' : 'text-red-500 dark:text-red-400'}`}>
          {u.antiScrollEnabled ? (
            <Check className="h-4 w-4 stroke-[3]" />
          ) : (
            <X className="h-4 w-4 stroke-[3]" />
          )}
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
          className="inline-flex h-8 w-8 items-center justify-center rounded-lg hover:bg-[#D1CAEF]/30 dark:hover:bg-[#D1CAEF]/10 text-gray-400 dark:text-gray-555 hover:text-violeta dark:hover:text-violeta-claro transition duration-150"
          aria-label={`Ver detalles de ${u.name}`}
        >
          <Eye className="h-5 w-5" strokeWidth={2} />
        </button>
      ),
    },
  ]

  return (
    <div className="flex flex-col gap-6 animate-fadeIn h-[calc(100vh-160px)] min-h-0">
      <PageHeader 
        title="Usuarios" 
        subtitle="Administra los usuarios registrados en el sistema y visualiza sus consentimientos y configuraciones de extensión."
      />

      <SectionCard className="bg-white dark:bg-[#172033] flex-1 min-h-0 flex flex-col">
        <div className="flex flex-col gap-4 flex-1 min-h-0">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between shrink-0">
            <h2 className="text-lg lg:text-[22px] font-bold text-gray-700 dark:text-gray-200">Listado de usuarios</h2>
            <form onSubmit={handleSearchSubmit} className="relative w-full sm:w-64">
              <input
                type="text"
                placeholder="Buscar por nombre o email..."
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                className="w-full rounded-xl border border-gray-200 dark:border-gray-800 bg-gray-55 dark:bg-[#09111f] py-2 pl-4 pr-10 text-sm lg:text-[15px] text-gray-700 dark:text-gray-200 outline-none transition duration-155 focus:border-violeta dark:focus:border-violeta-claro focus:bg-white dark:focus:bg-[#172033]"
              />
              <button
                type="submit"
                className="absolute right-3 top-2.5 text-gray-400 dark:text-gray-555 hover:text-violeta dark:hover:text-violeta-claro transition duration-150"
                aria-label="Buscar"
              >
                <Search className="h-4 w-4" strokeWidth={2} />
              </button>
            </form>
          </div>

          {loading ? (
            <div className="py-8 text-center text-sm text-gray-400 dark:text-gray-500 shrink-0">Cargando usuarios...</div>
          ) : error ? (
            <div className="py-8 flex justify-center shrink-0">
              <ErrorMessage message={error} />
            </div>

          ) : filteredUsers.length === 0 ? (
            <div className="py-8 text-center text-sm text-gray-400 dark:text-gray-555 shrink-0">No se encontraron usuarios</div>
          ) : (
            <Table 
              data={filteredUsers} 
              columns={userColumns} 
              keyExtractor={(u) => u.id} 
            />
          )}
        </div>
      </SectionCard>
    </div>
  )
}
