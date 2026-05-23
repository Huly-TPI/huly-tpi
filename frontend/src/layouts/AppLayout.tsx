import { Outlet } from 'react-router-dom'
import Navbar from '../components/Navbar'

export default function AppLayout() {
  return (
    <div className="flex h-dvh min-h-dvh flex-col overflow-hidden">
      <Navbar />
      <main className="h-[calc(100dvh-4rem)] min-h-0 flex-1 overflow-hidden">
        <Outlet />
      </main>
    </div>
  )
}
