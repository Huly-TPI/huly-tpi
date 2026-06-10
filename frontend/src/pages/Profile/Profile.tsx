import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../../context/auth'
import chestImage from '../../assets/profile/light-theme/chest.webp'
import clockImage from '../../assets/profile/light-theme/clock.webp'
import mirrorImage from '../../assets/profile/light-theme/mirror.webp'
import musicImage from '../../assets/profile/light-theme/music.webp'
import windowImage from '../../assets/profile/light-theme/window.webp'
import './Profile.css'

function getFirstName(name: string): string {
  return name.trim().split(/\s+/)[0] || 'Usuario'
}

function handleInactiveObjectClick() {
  return undefined
}

export default function Profile() {
  const { user, loading } = useAuth()

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center bg-fondo">
        <span className="text-sm font-semibold text-bosque">Cargando perfil...</span>
      </div>
    )
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return (
    <main className="profile-page" aria-label="Perfil de usuario">
      <div className="profile-scene-scroll" aria-label="Habitacion de perfil">
        <section className="profile-scene">
          <button
            type="button"
            className="profile-object profile-object--mirror"
            aria-label="Espejo del perfil"
            title="Espejo"
            onClick={handleInactiveObjectClick}
          >
            <img src={mirrorImage} alt="" draggable={false} />
          </button>

          <Link
            to="/"
            className="profile-object profile-object--window"
            aria-label="Volver al jardin"
            title="Volver al jardin"
          >
            <img src={windowImage} alt="" draggable={false} />
          </Link>

          <button
            type="button"
            className="profile-object profile-object--chest"
            aria-label="Cofre"
            title="Cofre"
            onClick={handleInactiveObjectClick}
          >
            <img src={chestImage} alt="" draggable={false} />
          </button>

          <button
            type="button"
            className="profile-object profile-object--clock"
            aria-label="Reloj"
            title="Reloj"
            onClick={handleInactiveObjectClick}
          >
            <img src={clockImage} alt="" draggable={false} />
          </button>

          <button
            type="button"
            className="profile-object profile-object--music"
            aria-label="Musica"
            title="Musica"
            onClick={handleInactiveObjectClick}
          >
            <img src={musicImage} alt="" draggable={false} />
          </button>

          <div className="profile-welcome" aria-label={`Bienvenido ${getFirstName(user.name)}`}>
            <span>Bienvenido</span>
            <strong>{getFirstName(user.name)}</strong>
          </div>
        </section>
      </div>
    </main>
  )
}
