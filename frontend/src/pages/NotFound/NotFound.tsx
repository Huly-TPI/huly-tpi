import { Link } from 'react-router-dom'
import { useTheme } from '../../context/theme'
import dayBackground from '../../assets/not-found/light-theme/background/background-404.webp'
import nightBackground from '../../assets/not-found/dark-theme/background/background-404-dark.webp'
import dayMobileBackground from '../../assets/not-found/light-theme/background/mobile/background-404-mobile.webp'
import nightMobileBackground from '../../assets/not-found/dark-theme/background/mobile/background-404-dark-mobile.webp'
import './NotFound.css'

export default function NotFound() {
  const { theme } = useTheme()

  return (
    <div className="not-found">
      <img
        src={dayBackground}
        alt=""
        className={`not-found__bg not-found__bg--desktop${theme === 'light' ? ' not-found__bg--active' : ''}`}
      />
      <img
        src={dayMobileBackground}
        alt="Ups... creo que me perdí"
        className={`not-found__bg not-found__bg--mobile${theme === 'light' ? ' not-found__bg--active' : ''}`}
      />
      <img
        src={nightBackground}
        alt=""
        className={`not-found__bg not-found__bg--desktop${theme === 'dark' ? ' not-found__bg--active' : ''}`}
      />
      <img
        src={nightMobileBackground}
        alt=""
        className={`not-found__bg not-found__bg--mobile${theme === 'dark' ? ' not-found__bg--active' : ''}`}
      />
      <Link to="/" className="not-found__button">
        Volver al jardín
      </Link>
    </div>
  )
}
