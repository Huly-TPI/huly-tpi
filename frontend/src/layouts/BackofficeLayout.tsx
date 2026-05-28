import { useState } from 'react'
import { NavLink, Outlet } from 'react-router-dom'
import colorLogo from '../assets/brand/color-logo.webp'

// ─── Design tokens (from Figma) ──────────────────────────────────────────────
// Sidebar bg:       #FFFFFF
// Icon inactive:    #A0AEC0
// Text inactive:    #4A5568
// Active bg:        #8869AC  (violeta)
// Logo area bg:     #D1CAEF  (violeta-claro)
// Hover overlay:    rgba(255,255,255,0.2)
// Content bg:       #EDF2ED

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
    to: '/backoffice/profesionales',
    label: 'Profesionales',
    end: false,
    icon: (
      <svg className="w-[18px] h-[18px] shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
        <path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a4 4 0 00-3-3.87M9 20H4v-2a4 4 0 013-3.87m9-4a4 4 0 11-8 0 4 4 0 018 0zm6 2a3 3 0 11-6 0 3 3 0 016 0zM3 19a3 3 0 116 0" />
      </svg>
    ),
  },
]

export default function BackofficeLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  return (
    <div className="flex h-screen overflow-hidden bg-[#EDF2ED] font-sans">

      {/* Mobile overlay */}
      {sidebarOpen && (
        <div
          className="fixed inset-0 z-20 bg-black/40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      {/* ── Sidebar ─────────────────────────────────────────────────────── */}
      {/* Mobile: fixed overlay | Desktop: static left column, full height */}
      <aside
        className={`
          fixed inset-y-0 left-0 z-30 flex h-full w-[304px] flex-col bg-white
          transition-transform duration-300 ease-in-out
          lg:relative lg:translate-x-0 lg:shadow-none
          ${sidebarOpen ? 'translate-x-0 shadow-2xl' : '-translate-x-full'}
        `}
      >
        {/* Logo area */}
        <div className="flex items-center gap-3 px-4 py-6">
          <div className="flex h-[52px] w-[52px] shrink-0 items-center justify-center overflow-hidden rounded-2xl bg-[#D1CAEF]">
            <img src={colorLogo} alt="Huly" className="h-9 w-9 object-contain" />
          </div>
          <div className="min-w-0">
            <p className="text-[15px] font-extrabold leading-tight text-[#8869AC]">huly</p>
            <p className="mt-0.5 text-[9px] font-bold uppercase tracking-[0.12em] text-[#A0AEC0]">
              Bienestar emocional
            </p>
          </div>
        </div>

        {/* Divider */}
        <div className="mx-4 h-px bg-gray-100" />

        {/* Nav */}
        <nav className="flex-1 overflow-y-auto px-4 py-4">
          <ul className="space-y-1">
            {NAV_ITEMS.map(item => (
              <li key={item.to}>
                <NavLink
                  to={item.to}
                  end={item.end}
                  onClick={() => setSidebarOpen(false)}
                  className={({ isActive }) =>
                    `flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all ${
                      isActive
                        ? 'bg-[#8869AC] text-white shadow-sm'
                        : 'text-[#4A5568] hover:bg-[#D1CAEF]/30 hover:text-[#8869AC]'
                    }`
                  }
                >
                  {({ isActive }) => (
                    <>
                      <span className={isActive ? 'text-white' : 'text-[#A0AEC0]'}>
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
      </aside>

      {/* ── Main column ─────────────────────────────────────────────────── */}
      <div className="flex flex-1 min-w-0 flex-col overflow-hidden bg-[#EDF2ED]">

        {/* ── Top bar card (Figma: W:1137 H:128, rounded, shadow, padding 16) */}
        <header className="mx-4 mt-4 shrink-0 rounded-2xl bg-white shadow-sm">
          <div className="flex h-[96px] items-center gap-4 px-6">
            {/* Hamburger — mobile only */}
            <button
              className="rounded-xl p-2 text-[#A0AEC0] hover:bg-gray-100 transition-colors lg:hidden"
              onClick={() => setSidebarOpen(true)}
              aria-label="Abrir menú"
            >
              <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>

            {/* Title */}
            <h1 className="flex-1 text-[28px] font-extrabold leading-tight tracking-tight text-[#8869AC] md:text-[32px]">
              Huly — Backoffice
            </h1>

            {/* Bell with red badge */}
            <button className="relative rounded-full p-2.5 text-[#A0AEC0] hover:bg-gray-50 transition-colors">
              <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6 6 0 10-12 0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
              </svg>
              <span className="absolute right-2 top-2 h-2 w-2 rounded-full bg-[#EF4444] ring-2 ring-white" />
            </button>

            {/* Avatar */}
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#D1CAEF] text-[#8869AC] text-sm font-extrabold shadow-sm ring-2 ring-white">
              A
            </div>
          </div>
        </header>

        {/* Scrollable page content */}
        <main className="flex-1 overflow-y-auto p-4 lg:p-5">
          <Outlet />
        </main>

      </div>
    </div>
  )
}
