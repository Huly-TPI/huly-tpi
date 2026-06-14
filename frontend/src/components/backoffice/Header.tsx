import ThemeToggle from '../ThemeToggle/ThemeToggle'

interface HeaderProps {
  onOpenSidebar: () => void
  userInitial?: string
}

export default function Header({ onOpenSidebar, userInitial }: HeaderProps) {
  return (
    <header className="sticky top-0 lg:top-5 z-20 w-full mt-0 ml-0 mr-0 rounded-none lg:mt-5 lg:ml-5 lg:rounded-l-[32px] lg:rounded-r-none shrink-0 bg-white dark:bg-[#172033] shadow-sm dark:shadow-none dark:border-b dark:border-gray-800/40 transition-colors duration-200">
      <div className="flex h-[72px] items-center gap-4 px-4 lg:px-5">
        <button
          className="rounded-xl p-2 text-[#A0AEC0] dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors lg:hidden"
          onClick={onOpenSidebar}
          aria-label="Abrir menú"
        >
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        <h1 className="flex-1 text-[28px] font-extrabold leading-tight tracking-tight text-[#8869AC] dark:text-[#A78BFA] md:text-[32px]">
          Huly - Backoffice
        </h1>

        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#D1CAEF] dark:bg-[#2A233C] text-[#8869AC] dark:text-[#A78BFA] text-sm font-extrabold shadow-sm ring-2 ring-white dark:ring-[#172033] mr-3">
          {userInitial}
        </div>

        <div className="flex items-center mr-4 lg:mr-6">
          <ThemeToggle compact />
        </div>
      </div>
    </header>
  )
}
