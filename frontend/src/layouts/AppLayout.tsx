import { Outlet } from 'react-router-dom'
import { AuthGateProvider } from '../context/authGate'
import Navbar from '../components/Navbar'
import ChatbotLauncher from '../components/Chatbot/ChatbotLauncher'

export default function AppLayout() {
  return (
    <AuthGateProvider>
      <div className="flex h-dvh min-h-dvh flex-col bg-[var(--app-bg)] text-[var(--app-fg)]">
        <Navbar />
        <main className="h-[calc(100dvh-4rem)] min-h-0 flex-1 overflow-auto">
          <Outlet />
        </main>
        <ChatbotLauncher />
      </div>
    </AuthGateProvider>
  )
}
