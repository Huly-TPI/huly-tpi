import { NavLink } from 'react-router-dom'
import { LogOut, X, Leaf } from 'lucide-react'
import colorLogo from '../../assets/brand/color-logo.webp'
import ThemeToggle from '../ThemeToggle/ThemeToggle'

interface SidebarProps {
  isOpen: boolean
  onClose: () => void
  onLogout: () => void
}

const NAV_ITEMS = [
  {
    to: '/backoffice',
    label: 'Dashboard',
    end: true,
    icon: (
      <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 6a7.5 7.5 0 107.5 7.5h-7.5V6z" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M13.5 10.5H21A7.5 7.5 0 0013.5 3v7.5z" />
      </svg>
    ),
  },
  {
    to: '/backoffice/chatbot',
    label: 'Chatbot',
    end: false,
    icon: (
      <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-3 3-3-3z" />
      </svg>
    ),
  },
  {
    to: '/backoffice/respiraciones',
    label: 'Respiraciones',
    end: false,
    icon: (
      <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M3 8c3 0 3 3 6 3s3-3 6-3 3 3 6 3" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M3 13c3 0 3 3 6 3s3-3 6-3 3 3 6 3" />
      </svg>
    ),
  },
  {
    to: '/backoffice/antiscroll',
    label: 'Antiscroll',
    end: false,
    icon: (
      <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <rect x="5" y="2" width="14" height="20" rx="3" />
        <line x1="9" y1="7" x2="15" y2="7" />
        <line x1="9" y1="11" x2="13" y2="11" />
      </svg>
    ),
  },
  {
    to: '/backoffice/actividades',
    label: 'Actividades',
    end: false,
    icon: (
      <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
        <path strokeLinecap="round" strokeLinejoin="round" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
  },
  {
    to: '/backoffice/usuarios',
    label: 'Usuarios',
    end: false,
    icon: (
      <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
    ),
  },
]

export default function Sidebar({ isOpen, onClose, onLogout }: SidebarProps) {
  return (
    <>
      {isOpen && (
        <div
          className="fixed inset-0 z-20 bg-black/40 lg:hidden"
          onClick={onClose}
        />
      )}

      <aside
        className={`
          fixed top-0 bottom-0 left-0 z-30 flex w-[304px] flex-col bg-white dark:bg-[#172033] rounded-r-[32px]
          transition-transform duration-300 ease-in-out dark:border-r dark:border-gray-800/40
          lg:relative lg:top-auto lg:bottom-auto lg:translate-x-0 lg:my-4 lg:rounded-r-[32px] lg:shadow-none
          ${isOpen ? 'translate-x-0 shadow-2xl' : '-translate-x-full'}
        `}
      >
        <div className="flex items-center justify-between px-4 py-6">
          <div className="flex items-center gap-3">
            <div className="flex h-[52px] w-[52px] shrink-0 items-center justify-center overflow-hidden rounded-2xl bg-[#D1CAEF] dark:bg-[#2A233C]">
              <Leaf className="h-8 w-8 text-violeta dark:text-violeta-claro fill-violeta dark:fill-violeta-claro" strokeWidth={1.8} />
            </div>
            <div className="min-w-0">
              <img src={colorLogo} alt="Huly" className="h-9 w-auto object-contain object-left" />
              <p className="mt-0.5 text-[11px] font-bold uppercase tracking-[0.14em] text-[#A0AEC0] dark:text-gray-500">
                Bienestar emocional
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="rounded-xl p-2 text-[#A0AEC0] dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors lg:hidden"
            aria-label="Cerrar menú"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="mx-4 h-px bg-gray-100 dark:bg-gray-800" />

        <nav className="flex-1 overflow-y-auto px-4 py-4">
          <ul className="space-y-1">
            {NAV_ITEMS.map(item => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  end={item.end}
                  onClick={onClose}
                  className={({ isActive }) =>
                    `flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all group ${isActive
                      ? 'bg-violeta text-white shadow-sm'
                      : 'text-[#4A5568] dark:text-gray-300 hover:bg-[#D1CAEF]/30 dark:hover:bg-[#D1CAEF]/10 hover:text-violeta dark:hover:text-violeta-claro'
                    }`
                  }
                >
                  {({ isActive }) => (
                    <>
                      <span className={isActive ? 'text-white' : 'text-[#A0AEC0] dark:text-gray-500 group-hover:text-violeta dark:group-hover:text-violeta-claro transition-colors'}>
                        {item.icon}
                      </span>
                      {item.label}
                    </>
                  )}
                </NavLink>
              </li>
            ))}
          </ul>
        </nav>

        <div className="shrink-0 px-4 pb-5 pt-2">
          <div className="lg:hidden flex items-center justify-between px-3 py-2.5 mb-3 text-sm font-semibold text-gray-500 dark:text-gray-400 border border-gray-100 dark:border-gray-800/60 rounded-xl bg-gray-55/50 dark:bg-[#09111f]/40">
            <span>Tema</span>
            <ThemeToggle compact />
          </div>
          <div className="mx-0 mb-3 h-px bg-gray-100 dark:bg-gray-800" />
          <button
            onClick={onLogout}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-anaranjado/10 dark:bg-anaranjado/20 px-4 py-3 text-sm font-semibold text-anaranjado dark:text-orange-400 transition-all hover:bg-anaranjado/20 dark:hover:bg-anaranjado/30 active:scale-[0.98]"
          >
            <LogOut className="h-4 w-4" strokeWidth={2} />
            <span>Cerrar sesión</span>
          </button>
        </div>
      </aside>
    </>
  )
}
