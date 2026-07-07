import { NavLink } from 'react-router-dom'
import {
  Bot,
  CirclePlay,
  LayoutDashboard,
  Leaf,
  LogOut,
  Smartphone,
  User,
  Wind,
  X,
  ShoppingBag,
} from 'lucide-react'
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
    label: 'Panel de control',
    end: true,
    icon: <LayoutDashboard className="h-[18px] w-[18px] shrink-0" strokeWidth={2} />,
  },
  {
    to: '/backoffice/chatbot',
    label: 'Chatbot',
    end: false,
    icon: <Bot className="h-[18px] w-[18px] shrink-0" strokeWidth={2} />,
  },
  {
    to: '/backoffice/respiraciones',
    label: 'Respiraciones',
    end: false,
    icon: <Wind className="h-[18px] w-[18px] shrink-0" strokeWidth={2} />,
  },
  {
    to: '/backoffice/antiscroll',
    label: 'Antiscroll',
    end: false,
    icon: <Smartphone className="h-[18px] w-[18px] shrink-0" strokeWidth={2} />,
  },
  {
    to: '/backoffice/actividades',
    label: 'Actividades',
    end: false,
    icon: <CirclePlay className="h-[18px] w-[18px] shrink-0" strokeWidth={2} />,
  },
  {
    to: '/backoffice/usuarios',
    label: 'Usuarios',
    end: false,
    icon: <User className="h-[18px] w-[18px] shrink-0" strokeWidth={2} />,
  },
  {
    to: '/backoffice/productos',
    label: 'Productos',
    end: false,
    icon: <ShoppingBag className="h-[18px] w-[18px] shrink-0" strokeWidth={2} />,
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
              <Leaf className="h-8 w-8 text-violeta dark:text-violeta-claro" strokeWidth={2} />
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
            className="rounded-xl p-2 text-[#A0AEC0] transition-colors hover:bg-gray-100 dark:text-gray-500 dark:hover:bg-gray-800 lg:hidden"
            aria-label="Cerrar menú"
          >
            <X className="h-5 w-5" />
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
                    `group flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all ${isActive
                      ? 'bg-violeta text-white shadow-sm'
                      : 'text-[#4A5568] hover:bg-[#D1CAEF]/30 hover:text-violeta dark:text-gray-300 dark:hover:bg-[#D1CAEF]/10 dark:hover:text-violeta-claro'
                    }`
                  }
                >
                  {({ isActive }) => (
                    <>
                      <span className={isActive ? 'text-white' : 'text-[#A0AEC0] transition-colors group-hover:text-violeta dark:text-gray-500 dark:group-hover:text-violeta-claro'}>
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
          <div className="mb-3 flex items-center justify-between rounded-xl border border-gray-100 bg-gray-55/50 px-3 py-2.5 text-sm font-semibold text-gray-500 dark:border-gray-800/60 dark:bg-[#09111f]/40 dark:text-gray-400 lg:hidden">
            <span>Tema</span>
            <ThemeToggle compact />
          </div>
          <div className="mx-0 mb-3 h-px bg-gray-100 dark:bg-gray-800" />
          <button
            onClick={onLogout}
            className="flex w-full items-center justify-center gap-2 rounded-xl bg-anaranjado/10 px-4 py-3 text-sm font-semibold text-anaranjado transition-all hover:bg-anaranjado/20 active:scale-[0.98] dark:bg-anaranjado/20 dark:text-orange-400 dark:hover:bg-anaranjado/30"
          >
            <LogOut className="h-4 w-4" strokeWidth={2} />
            <span>Cerrar sesión</span>
          </button>
        </div>
      </aside>
    </>
  )
}
