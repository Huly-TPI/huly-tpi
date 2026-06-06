import { Outlet, useLocation } from 'react-router-dom'
import { AuthGateProvider } from '../context/authGate'
import Navbar from '../components/Navbar'
import ChatbotLauncher from '../components/Chatbot/ChatbotLauncher'
import './AppLayout.css'

export default function AppLayout() {
  const location = useLocation()
  return (
    <AuthGateProvider>
      <div className="flex h-dvh min-h-dvh flex-col">
        <Navbar />
        <main className="h-[calc(100dvh-4rem)] min-h-0 flex-1 overflow-auto">
         <div key={location.key} className="page-enter h-full">
            <Outlet />
          </div>
        </main>
        <ChatbotLauncher />
      </div>
    </AuthGateProvider>
  )
}