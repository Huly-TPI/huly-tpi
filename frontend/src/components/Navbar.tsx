import { Link } from 'react-router-dom'

export default function Navbar() {
  const links = [
    { to: '/', label: 'Inicio' },
    { to: '/diary', label: 'Diario emocional' },
    { to: '/minigames', label: 'Minijuegos' },
  ]

  return (
    <nav className="bg-bosque px-8 py-4 shadow-md">
      <div className="max-w-6xl mx-auto flex items-center justify-between">
        <Link to="/" className="text-white text-xl font-bold hover:text-violeta-claro transition-colors">
          Huly
        </Link>
        <div className="flex gap-6">
          {links.map(link => (
            <Link
              key={link.to}
              to={link.to}
              className="text-white hover:text-violeta-claro transition-colors font-medium"
            >
              {link.label}
            </Link>
          ))}
        </div>
      </div>
    </nav>
  )
}