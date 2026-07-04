import { Navigate } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { useAuth } from '../../context/auth'
import { useTheme } from '../../context/theme'
import { completeProfileTutorial } from '../../api/onboarding'
import HomeOnboarding from '../../components/Onboarding/HomeOnboarding/HomeOnboarding'
import SceneElement from '../../components/Scene/SceneElement/SceneElement'
import AntiScrollConsentModal from '../../components/AntiScrollConsentModal'
import AudioSettingsModal from '../../components/Profile/AudioSettingsModal'
import ChangePasswordModal from '../../components/Profile/ChangePasswordModal'
import type { SceneElementDefinition } from '../../components/Scene/types'
import { useSceneOnboarding } from '../../hooks/useSceneOnboarding'
import chestImage from '../../assets/profile/light-theme/chest.webp'
import clockImage from '../../assets/profile/light-theme/clock.webp'
import mirrorImage from '../../assets/profile/light-theme/mirror.webp'
import musicImage from '../../assets/profile/light-theme/music.webp'
import windowImage from '../../assets/profile/light-theme/window.webp'
import notificationImage from '../../assets/profile/light-theme/notification.webp'
import { usePushNotifications } from '../../hooks/usePushNotifications'
import { profileOnboardingSteps } from './profileOnboardingSteps'
import NotificationSettingsModal from '../../components/Profile/NotificationSettingsModal'
import './Profile.css'

const FULL_WIDTH = 'w-full'
const DEFAULT_HOTSPOT = 'left-[2%] top-[4%] h-[92%] w-[96%]'
const RECT_CLIP_PATH = 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)'

const profileElements: SceneElementDefinition[] = [
  {
    id: 'mirror',
    title: 'Espejo',
    imageAlt: 'Espejo del perfil',
    image: { light: mirrorImage },
    placementClassName: 'left-[3%] bottom-[3.5%] z-[3] w-[38%] md:left-[11.5%] md:bottom-[9%] md:w-[20%]',
    imageClassName: FULL_WIDTH,
    hotspotClassName: DEFAULT_HOTSPOT,
    clipPath: RECT_CLIP_PATH,
    tooltipClassName: 'bottom-full mb-2',
  },
  {
    id: 'window',
    title: 'Volver al jardin',
    imageAlt: 'Ventana hacia el jardin',
    image: { light: windowImage },
    placementClassName: 'left-[23%] top-[14%] z-[2] w-[50%] md:left-[42%] md:top-[16.5%] md:w-[20.8%]',
    imageClassName: FULL_WIDTH,
    hotspotClassName: DEFAULT_HOTSPOT,
    clipPath: RECT_CLIP_PATH,
    tooltipClassName: 'bottom-full mb-2',
    to: '/',
  },
  {
    id: 'chest',
    title: 'Baúl',
    imageAlt: 'Baúl del perfil',
    image: { light: chestImage },
    placementClassName: 'left-[37%] bottom-[0.1%] z-[4] w-[33%] md:left-[45.2%] md:bottom-[14%] md:w-[13.6%]',
    imageClassName: FULL_WIDTH,
    hotspotClassName: DEFAULT_HOTSPOT,
    clipPath: RECT_CLIP_PATH,
    tooltipClassName: 'bottom-full mb-2',
  },
  {
    id: 'clock',
    title: 'Reloj',
    imageAlt: 'Reloj del perfil',
    image: { light: clockImage },
    placementClassName: 'left-[52.5%] top-[42.5%] z-[5] w-[23%] md:left-[70%] md:top-auto md:bottom-[40%] md:w-[7.5%]',
    imageClassName: FULL_WIDTH,
    hotspotClassName: DEFAULT_HOTSPOT,
    clipPath: RECT_CLIP_PATH,
    tooltipClassName: 'bottom-full mb-2',
  },
  {
    id: 'music',
    title: 'Musica',
    imageAlt: 'Equipo de musica del perfil',
    image: { light: musicImage },
    placementClassName: 'right-[3%] top-[42%] z-[6] w-[28%] md:right-[12%] md:top-auto md:bottom-[37%] md:w-[10%]',
    imageClassName: FULL_WIDTH,
    hotspotClassName: DEFAULT_HOTSPOT,
    clipPath: RECT_CLIP_PATH,
    tooltipClassName: 'bottom-full mb-2',
  },
]

const selectProfileTutorialCompleted = (currentUser: { profileOnboardingTutorialCompleted?: boolean }) =>
  currentUser.profileOnboardingTutorialCompleted

function getFirstName(name: string): string {
  return name.trim().split(/\s+/)[0] || 'Usuario'
}

export default function Profile() {
  const { user, loading } = useAuth()
  const { theme } = useTheme()
  const { isSubscribed, isLoading: pushLoading, isSupported, subscribe, unsubscribe, notificationHour, updateHour } = usePushNotifications()
  const [showAntiScrollModal, setShowAntiScrollModal] = useState(false)
  const [showAudioSettingsModal, setShowAudioSettingsModal] = useState(false)
  const [showChangePasswordModal, setShowChangePasswordModal] = useState(false)
  const [showNotifModal, setShowNotifModal] = useState(false)
  const {
    onboardingMode,
    onboardingStepIndex,
    shouldRenderOnboarding,
    startOnboarding,
    advanceOnboarding,
  } = useSceneOnboarding({
    totalSteps: profileOnboardingSteps.length,
    completedSelector: selectProfileTutorialCompleted,
    completeTutorial: completeProfileTutorial,
  })

  useEffect(() => {
    if (onboardingMode !== 'hidden') {
      document.body.setAttribute('data-home-onboarding-active', 'true')
      window.dispatchEvent(new CustomEvent('home-onboarding-visibility-change'))
      return () => {
        document.body.removeAttribute('data-home-onboarding-active')
        window.dispatchEvent(new CustomEvent('home-onboarding-visibility-change'))
      }
    }

    document.body.removeAttribute('data-home-onboarding-active')
    window.dispatchEvent(new CustomEvent('home-onboarding-visibility-change'))
    return undefined
  }, [onboardingMode])


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

  const renderedElements = profileElements.map(element => {
    const baseElement = shouldRenderOnboarding
      ? {
        ...element,
        interactive: false,
      }
      : element

    if (element.id === 'chest') {
      return {
        ...baseElement,
        onClick: (e: React.MouseEvent<HTMLButtonElement | HTMLAnchorElement>) => {
          e.preventDefault()
          setShowChangePasswordModal(true)
        },
      }
    }
    if (element.id === 'clock') {
      return {
        ...baseElement,
        onClick: (e: React.MouseEvent<HTMLButtonElement | HTMLAnchorElement>) => {
          e.preventDefault()
          setShowAntiScrollModal(true)
        },
      }
    }
    if (element.id === 'music') {
      return {
        ...baseElement,
        onClick: (e: React.MouseEvent<HTMLButtonElement | HTMLAnchorElement>) => {
          e.preventDefault()
          setShowAudioSettingsModal(true)
        },
      }
    }
    return baseElement
  })

  return (
    <main className="profile-page" aria-label="Perfil de usuario">
      <div className="profile-scene-scroll" aria-label="Habitacion de perfil">
        <section className="profile-scene">
          {renderedElements.map(element => (
            <SceneElement key={element.id} theme={theme} {...element} />
          ))}

          <div className="profile-welcome" aria-label={`Bienvenido ${getFirstName(user.name)}`}>
            <span>Bienvenido</span>
            <strong>{getFirstName(user.name)}</strong>
          </div>
          {onboardingMode !== 'hidden' ? (
            <HomeOnboarding
              mode={onboardingMode}
              theme={theme}
              sceneElements={renderedElements}
              steps={profileOnboardingSteps}
              currentStepIndex={onboardingStepIndex}
              onStart={startOnboarding}
              onAdvance={advanceOnboarding}
              intro={{
                showBrand: false,
                title: 'Tu habitación personal',
                subtitle: 'Vamos a recorrer los objetos de tu perfil y para qué sirve cada rincón',
              }}
            />
          ) : null}
        </section>
      </div>
      {isSupported && (
        <div className="scene-element fixed top-20 right-6 z-40 w-20">
          <img
            src={notificationImage}
            alt="Recordatorios"
            className="scene-element__image scene-element__shadow w-full select-none"
          />
          <div
            aria-hidden="true"
            className="scene-element__tooltip pointer-events-none absolute right-0 top-full z-[130] mt-5 w-max max-w-[12rem] rounded-2xl border border-white/50 bg-[#fffaf0]/95 px-3 py-2 text-center shadow-lg backdrop-blur-sm"
          >
            <p className="text-sm font-semibold text-[#5e4030]">
              {isSubscribed ? 'Recordatorios activados' : 'Activar recordatorios'}
            </p>
          </div>
          <button
            type="button"
            aria-label={isSubscribed ? 'Desactivar recordatorios diarios' : 'Activar recordatorios diarios'}
            onClick={() => setShowNotifModal(true)}
            disabled={pushLoading}
            className="scene-element__hotspot scene-element__hotspot--interactive absolute inset-0 cursor-pointer rounded-full"
          />
        </div>
      )}

      {showAntiScrollModal && (
        <AntiScrollConsentModal onClose={() => setShowAntiScrollModal(false)} />
      )}
      ...
      {showAntiScrollModal && (
        <AntiScrollConsentModal onClose={() => setShowAntiScrollModal(false)} />
      )}
      {showAudioSettingsModal && (
        <AudioSettingsModal onClose={() => setShowAudioSettingsModal(false)} />
      )}
      {showChangePasswordModal && (
        <ChangePasswordModal onClose={() => setShowChangePasswordModal(false)} />
      )}
      {showNotifModal && (
        <NotificationSettingsModal
          isSubscribed={isSubscribed}
          isLoading={pushLoading}
          notificationHour={notificationHour}
          onToggle={isSubscribed ? unsubscribe : subscribe}
          onHourChange={updateHour}
          onClose={() => setShowNotifModal(false)}
        />
      )}
    </main>

  )
}
