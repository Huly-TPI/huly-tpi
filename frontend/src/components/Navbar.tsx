import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth, hasSessionFlag } from '../context/auth'
import { useTheme } from '../context/theme'
import logo from '../assets/brand/monocromatico-menta-logo.png'
import ThemeToggle from './ThemeToggle/ThemeToggle'
import BadgeModal from './Badges/BadgeModal'

const NAV_LINKS = [
  { to: '/', label: 'Jardín' },
  { to: '/pending', label: 'Pendientes' },
  { to: '/minigames', label: 'Minijuegos' },
  { to: '/diary', label: 'Diario' },
  { to: '/challenges', label: 'Regar planta' },
] as const

export default function Navbar() {
  const { isAuthenticated, user, loading } = useAuth()
  const { theme } = useTheme()
  const [menuOpen, setMenuOpen] = useState(false)
  const isDark = theme === 'dark'
  const [badgesOpen, setBadgesOpen] = useState(false)

  const navRef = useRef<HTMLElement>(null)
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (navRef.current && !navRef.current.contains(e.target as Node)) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const closeMenu = () => setMenuOpen(false)

  return (
    <nav
      ref={navRef}
      className={`relative z-[300] shrink-0 shadow-md ${isDark ? 'bg-[#375847]' : 'bg-bosque'}`}
    >
      {/* Barra principal */}
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 md:px-8">
        {/* Logo desde assets */}
        <Link to="/" onClick={closeMenu} className="flex items-center">
          <img
            src={logo}
            alt="Huly logo"
            className="h-8 w-auto object-contain"
          />
        </Link>

        {/* Links — visible solo en desktop */}
        <div className="hidden items-center gap-6 md:flex">
          {NAV_LINKS.map(link => (
            <Link
              key={link.to}
              to={link.to}
              className="text-sm font-medium text-white transition-colors hover:text-violeta-claro md:text-base"
            >
              {link.label}
            </Link>
          ))}
        </div>

        <div className="flex items-center gap-3">
          {loading && hasSessionFlag() ? (
            <div className="h-9 w-24 animate-pulse rounded-full bg-white/10" aria-hidden="true" />
          ) : isAuthenticated && user ? (
            <UserMenu name={user.name} />
          ) : (
            <AuthButtons />
          )}
          <div className="hidden md:block">
            <ThemeToggle compact />
          </div>

          <button
            type="button"
            className="flex items-center justify-center rounded-lg p-2 text-white transition-colors hover:bg-white/10 md:hidden"
            aria-label={menuOpen ? 'Cerrar menú' : 'Abrir menú'}
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen(prev => !prev)}
          >
            {menuOpen ? <XIcon className="size-6" /> : <HamburgerIcon className="size-6" />}
          </button>
        </div>
      </div>

      {menuOpen && (
        <div className={`border-t border-white/10 px-4 pb-4 md:hidden ${isDark ? 'bg-[#375847]' : 'bg-bosque'}`}>
          <ul className="flex flex-col gap-1 pt-2">
            {NAV_LINKS.map(link => (
              <li key={link.to}>
                <Link
                  to={link.to}
                  onClick={closeMenu}
                  className="block rounded-lg px-3 py-2.5 text-base font-medium text-white transition-colors hover:bg-white/10 hover:text-violeta-claro"
                >
                  {link.label}
                </Link>
              </li>
            ))}
          </ul>

          <div className="-mx-4 mt-3 flex items-center justify-between border-t border-white/10 px-7 pt-3 text-base font-medium text-white">
            <span>Tema</span>
            <ThemeToggle compact />
          </div>

          {loading && hasSessionFlag() ? (
            <div className="mx-3 my-2 h-10 w-3/4 animate-pulse rounded-lg bg-white/10" aria-hidden="true" />
          ) : isAuthenticated ? (
            <div className="-mx-4 mt-3 flex items-center justify-between border-t border-white/10 px-7 pt-3 text-base font-medium text-white">
              <span>Mis estampitas</span>
              <button
                type="button"
                onClick={() => { setBadgesOpen(true); closeMenu() }}
                aria-label="Abrir insignias"
                className="flex h-11 w-11 items-center justify-center transition hover:scale-105"
              >
                <img src="/badges/badge_launcher.webp" alt="" className="h-11 w-11 object-contain" />
              </button>
            </div>
          ) : (
            <div className="mt-3 flex flex-col gap-2 border-t border-white/10 pt-3">
              <Link
                to="/login"
                onClick={closeMenu}
                className="rounded-full border border-white/40 px-4 py-2.5 text-center text-base font-semibold text-white transition-colors hover:bg-white/10"
              >
                Iniciar sesión
              </Link>
              <Link
                to="/register"
                onClick={closeMenu}
                className="rounded-full bg-white px-4 py-2.5 text-center text-base font-semibold text-bosque transition-all hover:brightness-95"
              >
                Registrarse
              </Link>
            </div>
          )}
        </div>
      )}
      <BadgeModal isOpen={badgesOpen} onClose={() => setBadgesOpen(false)} />
    </nav>
  )
}

// function AuthSlotPlaceholder() {
//   return (
//     <div
//       className="hidden h-9 w-36 animate-pulse rounded-full bg-white/10 md:block"
//       aria-hidden="true"
//     />
//   )
// }

function UserMenu({ name }: { name: string }) {
  const [open, setOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()
  const { logout } = useAuth()

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const handleLogout = async () => {
    setOpen(false)
    await logout()
    navigate('/login')
  }

  return (
    <div ref={menuRef} className="relative">
      <button
        type="button"
        onClick={() => setOpen(prev => !prev)}
        aria-haspopup="menu"
        aria-expanded={open}
        className="flex items-center gap-2 rounded-full bg-white px-4 py-2 text-sm font-semibold text-bosque shadow-sm transition-all hover:brightness-95 active:translate-y-px"
      >
        <UserIcon className="size-4" />
        <span className="max-w-[8rem] truncate">{name}</span>
      </button>

      {open && (
        <div
          role="menu"
          className="absolute right-0 mt-2 w-44 overflow-hidden rounded-xl border border-black/5 bg-white py-1 shadow-lg"
        >
          <Link
            to="/profile"
            role="menuitem"
            className="block px-4 py-2 text-sm text-gray-700 transition-colors hover:bg-gray-100"
            onClick={() => setOpen(false)}
          >
            Mi perfil
          </Link>
          <Link
            to="/privacy"
            role="menuitem"
            className="block px-4 py-2 text-sm text-gray-700 transition-colors hover:bg-gray-100"
            onClick={() => setOpen(false)}
          >
            Centro de privacidad
          </Link>
          <button
            type="button"
            role="menuitem"
            onClick={handleLogout}
            className="block w-full px-4 py-2 text-left text-sm text-red-600 transition-colors hover:bg-red-50"
          >
            Cerrar sesión
          </button>
        </div>
      )}
    </div>
  )
}

function AuthButtons() {
  return (
    <div className="hidden items-center gap-2 md:flex md:gap-3">
      <Link
        to="/login"
        className="rounded-full px-4 py-2 text-sm font-semibold text-white transition-colors hover:text-violeta-claro"
      >
        Iniciar sesión
      </Link>
      <Link
        to="/register"
        className="rounded-full bg-white px-4 py-2 text-sm font-semibold text-bosque shadow-sm transition-all hover:brightness-95 active:translate-y-px"
      >
        Registrarse
      </Link>
    </div>
  )
}

function UserIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  )
}

function HamburgerIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <line x1="4" y1="6" x2="20" y2="6" />
      <line x1="4" y1="12" x2="20" y2="12" />
      <line x1="4" y1="18" x2="20" y2="18" />
    </svg>
  )
}

function XIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor"
      strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      <line x1="18" y1="6" x2="6" y2="18" />
      <line x1="6" y1="6" x2="18" y2="18" />
    </svg>
  )
}
