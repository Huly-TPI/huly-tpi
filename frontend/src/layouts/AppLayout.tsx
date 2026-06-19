import { Outlet, useLocation } from 'react-router-dom'
import { AuthGateProvider } from '../context/authGate'
import Navbar from '../components/Navbar'
import ChatbotLauncher from '../components/Chatbot/ChatbotLauncher'
import BadgeLauncher from '../components/Badges/BadgeLauncher'
import { BadgeToastProvider } from '../context/BadgeToast'

export default function AppLayout() {
  const location = useLocation()
  const isPrivacyPage = location.pathname === '/privacy'

  return (
    <AuthGateProvider>
        <BadgeToastProvider>
      <div className="flex h-dvh min-h-dvh flex-col bg-[var(--app-bg)] text-[var(--app-fg)]">
        <Navbar />
        <main className="h-[calc(100dvh-4rem)] min-h-0 flex-1 overflow-auto">
          <Outlet/>
        </main>
        {!isPrivacyPage && <ChatbotLauncher />}
        {!isPrivacyPage && <BadgeLauncher />}
      </div>
      </BadgeToastProvider>
    </AuthGateProvider>
  )
}
