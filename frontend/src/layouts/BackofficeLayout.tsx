import { useState } from 'react'
import { Navigate, Outlet, useNavigate } from 'react-router-dom'
import { logout } from '../api/auth'
import { clearToken } from '../api/client'
import Sidebar from '../components/backoffice/Sidebar'
import Header from '../components/backoffice/Header'
import { useAuth } from '../context/auth'

export default function BackofficeLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const navigate = useNavigate()
  const { user, loading, isAuthenticated } = useAuth()

  if (loading) {
    return (
      <div className="flex h-screen items-center justify-center bg-[#EDF2ED] dark:bg-[#09111f]">
        <span className="text-sm text-gray-500">Cargando...</span>
      </div>
    )
  }

  if (!isAuthenticated || !user || user.role !== 'ADMIN') {
    return <Navigate to="/backoffice/login" replace />
  }

  const handleLogout = async () => {
    try {
      await logout()
    } finally {
      clearToken()
      localStorage.removeItem('role')
      navigate('/backoffice/login')
    }
  }

  const userInitial = user.name.charAt(0).toUpperCase()

  return (
    <div className="flex h-screen overflow-hidden bg-[#EDF2ED] dark:bg-[#09111f] font-sans transition-colors duration-200">
      <Sidebar
        isOpen={sidebarOpen}
        onClose={() => setSidebarOpen(false)}
        onLogout={handleLogout}
      />

      <div className="flex flex-1 min-w-0 flex-col h-screen overflow-hidden bg-[#EDF2ED] dark:bg-[#09111f] relative transition-colors duration-200">
        <Header onOpenSidebar={() => setSidebarOpen(true)} userInitial={userInitial} />

        <div className="pointer-events-none absolute left-0 right-0 z-10 h-10 lg:h-12 bg-gradient-to-b from-[#EDF2ED] dark:from-[#09111f] to-transparent top-[72px] lg:top-[92px]" />

        <main className="flex-1 min-h-0 overflow-y-auto p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

