import { Link } from 'react-router-dom'

export default function Navbar() {
  const links = [
    { to: '/', label: 'Inicio' },
    { to: '/diary', label: 'Diario emocional' },
    { to: '/minigames', label: 'Minijuegos' },
  ]

  return (
    <nav className="h-16 shrink-0 bg-bosque px-4 shadow-md md:px-8">
      <div className="mx-auto flex h-full max-w-6xl items-center justify-between">
        <Link to="/" className="text-lg font-bold text-white transition-colors hover:text-violeta-claro md:text-xl">
          Huly
        </Link>
        <div className="flex gap-4 md:gap-6">
          {links.map(link => (
            <Link
              key={link.to}
              to={link.to}
              className="text-sm font-medium text-white transition-colors hover:text-violeta-claro md:text-base"
            >
              {link.label}
            </Link>
          ))}
        </div>
      </div>
    </nav>
  )
}
