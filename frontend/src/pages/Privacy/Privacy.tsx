import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, AppWindow, Globe, Mail, Lock, Eye, Database, Shield } from 'lucide-react'

type TabType = 'general' | 'extension'

interface PolicySection {
  title: string
  icon: any
  content: React.ReactNode
  highlight?: boolean
}

interface PolicyDocument {
  id: TabType
  title: string
  subtitle: string
  lastUpdated: string
  badge?: string
  intro: React.ReactNode
  sections: PolicySection[]
}

const GENERAL_POLICY: PolicyDocument = {
  id: 'general' as const,
  title: 'Política de privacidad general',
  subtitle: 'Huly TPI',
  lastUpdated: '13 de junio de 2026',
  badge: undefined,
  intro: (
    <>
      Bienvenido al ecosistema de <strong>Huly</strong>. Valoramos enormemente tu privacidad y bienestar. 
      Esta política describe cómo procesamos y protegemos tus datos cuando usas nuestra plataforma web y jardín virtual.
    </>
  ),
  sections: [
    {
      title: '1. Información que recopilamos',
      icon: Database,
      content: (
        <>
          <p className="leading-relaxed">
            Al interactuar con la plataforma web de Huly, recopilamos los datos estrictamente necesarios para tu experiencia de usuario:
          </p>
          <ul className="list-disc pl-6 mt-3 space-y-2">
            <li><strong>Perfil de usuario:</strong> nombre, dirección de correo electrónico y contraseña (encriptada) para permitirte iniciar sesión de manera segura.</li>
            <li><strong>Registro de emociones y diario:</strong> textos y tags de emociones que decides registrar de forma voluntaria en tu diario personal.</li>
            <li><strong>Configuración del jardín:</strong> tus preferencias en minijuegos, sonidos de respiración, estado del jardín virtual y compras virtuales realizadas con monedas del juego.</li>
          </ul>
        </>
      ),
    },
    {
      title: '2. Seguridad de tus datos personales',
      icon: Lock,
      content: (
        <p className="leading-relaxed">
          Los datos correspondientes a tu diario emocional y tus estadísticas son de carácter estrictamente privado. Toda transmisión de información se realiza a través de protocolos seguros (HTTPS con cifrado SSL) y los datos se almacenan en servidores seguros con controles de acceso restringidos.
        </p>
      ),
    },
    {
      title: '3. No comercialización y control',
      icon: Eye,
      content: (
        <p className="leading-relaxed">
          <strong>No compartimos ni vendemos tus datos a anunciantes ni a terceros.</strong> La información recopilada se utiliza de forma exclusiva para brindarte reportes sobre tus niveles de bienestar e integrar las distintas funciones de Huly (como las pausas digitales y el chatbot).
        </p>
      ),
    },
    {
      title: '4. Contacto y derechos',
      icon: Mail,
      content: (
        <p className="leading-relaxed">
          Puedes solicitar la eliminación total de tu cuenta y todos tus datos asociados en cualquier momento contactando a nuestro equipo.
        </p>
      ),
    },
  ],
}

const EXTENSION_POLICY: PolicyDocument = {
  id: 'extension' as const,
  title: 'Política de privacidad: Huly Pausa Digital (anti-scroll)',
  subtitle: 'Chrome Web Store',
  lastUpdated: '13 de junio de 2026',
  badge: 'Extensión de navegador',
  intro: (
    <>
      Esta sección detalla de forma transparente cómo se recopilan, almacenan y procesan los datos a través de nuestra extensión oficial de Google Chrome, diseñada para ayudarte a controlar y monitorear el scroll excesivo y fomentar pausas saludables.
    </>
  ),
  sections: [
    {
      title: '1. Permisos del navegador y su uso',
      icon: Globe,
      content: (
        <div className="space-y-4">
          <p>
            Para funcionar, la extensión solicita permisos específicos. A continuación detallamos exactamente por qué son necesarios:
          </p>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-2">
            <div className="p-4 rounded-xl border border-gray-100 dark:border-gray-800/40 bg-gray-50/50 dark:bg-gray-900/40">
              <span className="font-mono text-xs font-bold text-[#8869AC] dark:text-[#A78BFA]">storage</span>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Guarda tu configuración personalizada de pausas y métricas acumuladas localmente en tu navegador.</p>
            </div>
            <div className="p-4 rounded-xl border border-gray-100 dark:border-gray-800/40 bg-gray-50/50 dark:bg-gray-900/40">
              <span className="font-mono text-xs font-bold text-[#8869AC] dark:text-[#A78BFA]">tabs</span>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Detecta qué pestaña estás visitando para pausar o iniciar los contadores de tiempo activo únicamente en sitios web relevantes.</p>
            </div>
            <div className="p-4 rounded-xl border border-gray-100 dark:border-gray-800/40 bg-gray-50/50 dark:bg-gray-900/40">
              <span className="font-mono text-xs font-bold text-[#8869AC] dark:text-[#A78BFA]">alarms</span>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Programa las alarmas internas que disparan el recordatorio visual para tomar un descanso.</p>
            </div>
            <div className="p-4 rounded-xl border border-gray-100 dark:border-gray-800/40 bg-gray-50/50 dark:bg-gray-900/40">
              <span className="font-mono text-xs font-bold text-[#8869AC] dark:text-[#A78BFA]">host_permissions (&lt;all_urls&gt;)</span>
              <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">Permite inyectar de forma segura la interfaz de aviso de descanso (el modal de pausa) en cualquier pestaña activa cuando se cumple el tiempo configurado.</p>
            </div>
          </div>
        </div>
      ),
    },
    {
      title: '2. Recopilación selectiva (sin datos personales por defecto)',
      icon: Shield,
      content: (
        <ul className="list-disc pl-6 space-y-2">
          <li><strong>Solo dominios base:</strong> la extensión monitorea el scroll acumulado y tiempo activo <strong>únicamente a nivel de dominio base</strong> (ej. `youtube.com`). <strong>Nunca guardamos ni transmitimos la URL completa, ni parámetros de búsqueda, ni contenido sensible de tus páginas.</strong></li>
          <li><strong>Almacenamiento local primero:</strong> todos estos contadores se guardan en la memoria local de tu dispositivo de manera encriptada a través del almacenamiento local del navegador.</li>
        </ul>
      ),
    },
    {
      title: '3. Compartir datos: solo si lo permites (consentimiento explícito)',
      icon: Lock,
      highlight: true,
      content: (
        <p className="text-sm leading-relaxed text-[#4A5568] dark:text-gray-300">
          Por defecto, la opción de sincronización remota (`dataSharingConsent`) está <strong>desactivada</strong>. Tus datos de uso de la extensión no saldrán de tu computadora. 
          <br /><br />
          Si decides loguearte en la extensión con tu cuenta de Huly y activas la opción de compartir estadísticas, la extensión enviará de forma segura mediante HTTPS un sumario de tus dominios monitoreados al backend de Huly para que los veas en tus gráficos de bienestar digital. Puedes revocar este permiso en cualquier momento desde los ajustes de la extensión.
        </p>
      ),
    },
    {
      title: '4. Contacto y consultas del editor',
      icon: Mail,
      content: (
        <p className="leading-relaxed">
          Si eres revisor de la Chrome Web Store o usuario de la extensión y tienes alguna duda de seguridad, puedes ponerte en contacto con nosotros escribiéndonos al correo verificado del editor provisto en los metadatos de la tienda de aplicaciones.
        </p>
      ),
    },
  ],
}

export default function Privacy() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState<TabType>('general')

  const activeDoc = activeTab === 'general' ? GENERAL_POLICY : EXTENSION_POLICY

  return (
    <div className="h-full w-full overflow-hidden flex flex-col bg-[#EDF2ED] dark:bg-[#09111f] text-[#4A5568] dark:text-gray-300 font-sans transition-colors duration-200">
      <header className="sticky top-4 lg:top-5 z-10 mx-4 mt-4 lg:mx-5 lg:mt-5 shrink-0 rounded-2xl bg-white dark:bg-[#172033] shadow-sm border border-gray-100 dark:border-gray-800/40 transition-colors duration-200">
        <div className="flex h-[72px] items-center gap-4 px-4 lg:px-5 justify-between w-full">
          <div className="flex items-center gap-3">
            <button
              onClick={() => navigate(-1)}
              className="p-2 text-[#A0AEC0] dark:text-gray-500 hover:bg-[#D1CAEF]/30 dark:hover:bg-[#2A233C]/50 hover:text-[#8869AC] dark:hover:text-[#A78BFA] rounded-xl transition-all"
              aria-label="Volver"
            >
              <ChevronLeft className="w-6 h-6" />
            </button>
            <div>
              <h1 className="text-xl sm:text-2xl font-extrabold leading-tight tracking-tight text-[#8869AC] dark:text-[#A78BFA] flex items-center gap-2">
                Centro de privacidad
              </h1>
              <p className="hidden sm:block text-[10px] font-bold uppercase tracking-[0.14em] text-[#A0AEC0] dark:text-gray-500">
                Políticas de bienestar emocional - Huly
              </p>
            </div>
          </div>
          
          <div className="flex items-center gap-2">
            <span className="text-[11px] font-bold uppercase tracking-wider text-[#A0AEC0] dark:text-gray-500">Versión 1.0.0</span>
          </div>
        </div>
      </header>

      <div className="flex-1 min-h-0 mx-4 mt-6 mb-6 lg:mx-5 lg:mb-8 flex flex-col lg:flex-row gap-6 overflow-hidden">
        <aside className="w-full lg:w-[304px] shrink-0 flex flex-col bg-white dark:bg-[#172033] rounded-2xl p-4 shadow-sm border border-gray-100 dark:border-gray-800/40 h-fit lg:h-full justify-between transition-colors duration-200">
          <div className="w-full">
            <div className="hidden lg:block px-2 py-3 mb-2">
              <h2 className="text-xs font-bold uppercase tracking-widest text-[#A0AEC0] dark:text-gray-500">Seleccionar documento</h2>
              <p className="text-[11px] text-gray-400 dark:text-gray-400 mt-1">Navega por las políticas de privacidad de nuestro ecosistema.</p>
            </div>
            
            <nav className="flex flex-row lg:flex-col gap-2 w-full">
              <button
                onClick={() => setActiveTab('general')}
                className={`flex flex-1 lg:w-full items-center justify-center lg:justify-start gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all ${
                  activeTab === 'general'
                    ? 'bg-[#8869AC] dark:bg-[#A78BFA] text-white dark:text-[#09111f] shadow-sm'
                    : 'text-[#4A5568] dark:text-gray-300 hover:bg-[#D1CAEF]/30 dark:hover:bg-[#2A233C]/50 hover:text-[#8869AC] dark:hover:text-[#A78BFA]'
                }`}
              >
                <AppWindow className={`w-[18px] h-[18px] shrink-0 ${activeTab === 'general' ? 'text-white dark:text-[#09111f]' : 'text-[#A0AEC0] dark:text-gray-500'}`} />
                <span className="text-xs sm:text-sm">Aplicación web</span>
              </button>
              
              <button
                onClick={() => setActiveTab('extension')}
                className={`flex flex-1 lg:w-full items-center justify-center lg:justify-start gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all ${
                  activeTab === 'extension'
                    ? 'bg-[#8869AC] dark:bg-[#A78BFA] text-white dark:text-[#09111f] shadow-sm'
                    : 'text-[#4A5568] dark:text-gray-300 hover:bg-[#D1CAEF]/30 dark:hover:bg-[#2A233C]/50 hover:text-[#8869AC] dark:hover:text-[#A78BFA]'
                }`}
              >
                <Globe className={`w-[18px] h-[18px] shrink-0 ${activeTab === 'extension' ? 'text-white dark:text-[#09111f]' : 'text-[#A0AEC0] dark:text-gray-500'}`} />
                <span className="text-xs sm:text-sm">Extensión Chrome</span>
              </button>
            </nav>
          </div>

          <div className="hidden lg:block mt-4 pt-4 border-t border-gray-100 dark:border-gray-800/40 text-[11px] text-[#A0AEC0] dark:text-gray-500 px-2 text-center lg:text-left">
            Huly está comprometido con la transparencia y el software open source.
          </div>
        </aside>

        <main className="flex-1 bg-white dark:bg-[#172033] rounded-2xl p-6 sm:p-10 shadow-sm border border-gray-100 dark:border-gray-800/40 overflow-y-auto min-h-0 transition-colors duration-200">
          <article className="space-y-8 animate-fadeIn">
            <div className="border-b border-gray-100 dark:border-gray-800/40 pb-5">
              {activeDoc.badge && (
                <span className="bg-[#D1CAEF]/40 dark:bg-[#2A233C]/50 text-[#8869AC] dark:text-[#A78BFA] text-[10px] font-extrabold px-3 py-1 rounded-full uppercase tracking-wider">
                  {activeDoc.badge}
                </span>
              )}
              <h2 className="text-2xl sm:text-3xl font-black text-gray-800 dark:text-gray-100 mt-3 leading-tight">
                {activeDoc.title}
              </h2>
              <div className="flex items-center gap-2 mt-2">
                <span className="text-xs text-[#A0AEC0] dark:text-gray-500">Última actualización: {activeDoc.lastUpdated}</span>
                <span className="h-1.5 w-1.5 rounded-full bg-[#8869AC] dark:bg-[#A78BFA]" />
                <span className="text-xs text-[#8869AC] dark:text-[#A78BFA] font-bold">{activeDoc.subtitle}</span>
              </div>
            </div>

            <div className="text-base leading-relaxed text-[#4A5568] dark:text-gray-300">
              {activeDoc.intro}
            </div>

            <div className="space-y-6 pt-2">
              {activeDoc.sections.map((sec, index) => {
                const Icon = sec.icon
                return (
                  <section
                    key={index}
                    className={
                      sec.highlight
                        ? 'bg-[#D1CAEF]/20 dark:bg-[#2A233C]/20 border border-[#D1CAEF]/40 dark:border-[#2A233C]/40 rounded-xl p-5'
                        : undefined
                    }
                  >
                    <h3 className="text-lg font-bold flex items-center gap-2 mb-3 text-[#8869AC] dark:text-[#A78BFA]">
                      <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-[#D1CAEF]/30 dark:bg-[#2A233C]/50 text-[#8869AC] dark:text-[#A78BFA]">
                        <Icon className="w-4 h-4" />
                      </div>
                      {sec.title}
                    </h3>
                    <div className="text-[#4A5568] dark:text-gray-300">
                      {sec.content}
                    </div>
                  </section>
                )
              })}
            </div>
          </article>
        </main>
      </div>
    </div>
  )
}
