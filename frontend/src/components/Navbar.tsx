import { useState, useRef, useEffect } from 'react'
import { Menu, X, User, Sparkles, ShieldCheck, LogOut } from 'lucide-react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth, hasSessionFlag } from '../context/auth'
import { useSubscriptionModal } from '../context/subscriptionModal'
import logo from '../assets/brand/monocromatico-menta-logo.png'
import navbarBg from '../assets/navbbar.png'
import ThemeToggle from './ThemeToggle/ThemeToggle'
import BadgeModal from './Badges/BadgeModal'
import { useMembership } from '../hooks/shop/useMembership'
import SubscriptionModal from './SubscriptionModal/SubscriptionModal'
import budIcon from '../assets/suscription/budIcon.webp'
import flowerpotIcon from '../assets/suscription/flowerpotIcon.webp'
import crownIcon from '../assets/suscription/crownIcon.webp'

const NAV_LINKS = [
  { to: '/', label: 'Jardín' },
  { to: '/pending', label: 'Pendientes' },
  { to: '/minigames', label: 'Minijuegos' },
  { to: '/diary', label: 'Diario' },
  { to: '/challenges', label: 'Regar planta' },
] as const

// El after: genera la barra blanca animada debajo del link activo
function getDesktopLinkClass(isActive: boolean): string {
  const base =
    "text-sm font-medium md:text-base transition-colors after:content-[''] after:block after:h-0.5 after:rounded-full after:bg-white after:transition-all after:duration-200"
  return isActive
    ? `${base} text-white after:w-full`
    : `${base} text-white/75 hover:text-white after:w-0`
}

function getMobileLinkClass(isActive: boolean): string {
  return `block rounded-lg px-3 py-2.5 text-base font-medium transition-colors ${
    isActive ? 'bg-white/15 text-white' : 'text-white hover:bg-white/10'
  }`
}

export default function Navbar() {
  const { isAuthenticated, user, loading } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)
  const [badgesOpen, setBadgesOpen] = useState(false)
  const navRef = useRef<HTMLElement>(null)

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (navRef.current && !navRef.current.contains(e.target as Node)) {
        setMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  const closeMenu = () => setMenuOpen(false)

  return (
    <nav ref={navRef} className="relative z-[300] shrink-0 shadow-md">
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-x-0 top-0 h-16 bg-center"
        style={{ backgroundImage: `url(${navbarBg})` }}
      />

      {menuOpen && (
        <div
          aria-hidden="true"
          className="pointer-events-none absolute inset-x-0 bottom-0 top-16 bg-[#5B834F] md:hidden"
        />
      )}

      {/* Barra principal */}
      <div className="relative mx-auto flex h-16 max-w-6xl items-center justify-between px-4 md:px-8">
        <Link to="/" onClick={closeMenu} className="flex items-center">
          <img src={logo} alt="Huly logo" className="h-8 w-auto object-contain" />
        </Link>

        {/* Links desktop */}
        <div className="hidden items-center gap-6 md:flex">
          {NAV_LINKS.map(link => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === '/'}
              className={({ isActive }) => getDesktopLinkClass(isActive)}
            >
              {link.label}
            </NavLink>
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
            {menuOpen ? <X className="size-6" strokeWidth={2} /> : <Menu className="size-6" strokeWidth={2} />}
          </button>
        </div>
      </div>

      {/* Menú mobile */}
      {menuOpen && (
        <div className="relative border-t border-white/10 px-4 pb-4 md:hidden">
          <ul className="flex flex-col gap-1 pt-2">
            {NAV_LINKS.map(link => (
              <li key={link.to}>
                <NavLink
                  to={link.to}
                  end={link.to === '/'}
                  onClick={closeMenu}
                  className={({ isActive }) => getMobileLinkClass(isActive)}
                >
                  {link.label}
                </NavLink>
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
                onClick={() => {
                  setBadgesOpen(true)
                  closeMenu()
                }}
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

interface UserMenuProps {
  name: string
}

function UserMenu({ name }: UserMenuProps) {
  const [open, setOpen] = useState(false)
  const { subscriptionOpen, openSubscriptionModal, closeSubscriptionModal } = useSubscriptionModal()
  const menuRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()
  const { logout } = useAuth()
  const { membership, refresh: refreshMembership } = useMembership()

  const subscriptionIcon =
    !membership?.active
      ? budIcon
      : membership.planCode === 'PREMIUM'
        ? crownIcon
        : flowerpotIcon

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
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
        <img src={subscriptionIcon} alt="" aria-hidden="true" className="size-5 object-contain" />
        <span className="max-w-[8rem] truncate">{name}</span>
      </button>

      <SubscriptionModal
        isOpen={subscriptionOpen}
        onClose={closeSubscriptionModal}
        onRefreshMembership={refreshMembership}
      />

      {open && (
        <div
          role="menu"
          className="absolute right-0 mt-2 w-52 overflow-hidden rounded-2xl border border-[#5a8a50]/20 bg-[#f9f5ef] py-1.5 shadow-[0_8px_24px_rgba(76,124,100,0.18)]"
        >
          <Link
            to="/profile"
            role="menuitem"
            className="flex items-center gap-2.5 px-4 py-2.5 text-sm font-medium text-[#3d5c3a] transition-colors hover:bg-[#4C7C64]/10"
            onClick={() => setOpen(false)}
          >
            <User className="size-4 shrink-0 text-[#4C7C64]" strokeWidth={2} />
            Mi perfil
          </Link>
          <button
            type="button"
            role="menuitem"
            onClick={() => {
              setOpen(false)
              openSubscriptionModal()
            }}
            className="flex w-full items-center gap-2.5 px-4 py-2.5 text-left text-sm font-medium text-[#3d5c3a] transition-colors hover:bg-[#4C7C64]/10"
          >
            <Sparkles className="size-4 shrink-0 text-[#4C7C64]" strokeWidth={2} />
            Suscripciones
          </button>
          <Link
            to="/privacy"
            role="menuitem"
            className="flex items-center gap-2.5 px-4 py-2.5 text-sm font-medium text-[#3d5c3a] transition-colors hover:bg-[#4C7C64]/10"
            onClick={() => setOpen(false)}
          >
            <ShieldCheck className="size-4 shrink-0 text-[#4C7C64]" strokeWidth={2} />
            Centro de privacidad
          </Link>

          <div className="mx-3 my-1 border-t border-[#5a8a50]/20" />

          <button
            type="button"
            role="menuitem"
            onClick={handleLogout}
            className="flex w-full items-center gap-2.5 px-4 py-2.5 text-left text-sm font-medium text-red-500 transition-colors hover:bg-red-50"
          >
            <LogOut className="size-4 shrink-0" strokeWidth={2} />
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