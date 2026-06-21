import { Menu } from 'lucide-react'
import ThemeToggle from '../ThemeToggle/ThemeToggle'

interface HeaderProps {
  onOpenSidebar: () => void
  userInitial?: string
}

export default function Header({ onOpenSidebar, userInitial }: HeaderProps) {
  return (
    <header className="relative z-20 mt-0 ml-0 mr-0 w-full shrink-0 rounded-none bg-white shadow-sm transition-colors duration-200 dark:border-b dark:border-gray-800/40 dark:bg-[#172033] dark:shadow-none lg:mt-5 lg:ml-5 lg:w-[calc(100%-1.25rem)] lg:rounded-l-[32px] lg:rounded-r-none">
      <div className="flex h-[72px] items-center gap-4 px-4 lg:px-5">
        <button
          className="rounded-xl p-2 text-[#A0AEC0] transition-colors hover:bg-gray-100 dark:text-gray-500 dark:hover:bg-gray-800 lg:hidden"
          onClick={onOpenSidebar}
          aria-label="Abrir menú"
        >
          <Menu className="h-5 w-5" strokeWidth={2} />
        </button>

        <h1 className="flex-1 text-[28px] font-extrabold leading-tight tracking-tight text-violeta dark:text-violeta-claro md:text-[32px]">
          Huly - Backoffice
        </h1>

        <div className="mr-3 flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#D1CAEF] text-sm font-extrabold text-violeta shadow-sm ring-2 ring-white dark:bg-[#2A233C] dark:text-violeta-claro dark:ring-[#172033]">
          {userInitial}
        </div>

        <div className="mr-4 hidden items-center lg:mr-6 lg:flex">
          <ThemeToggle compact />
        </div>
      </div>
    </header>
  )
}
