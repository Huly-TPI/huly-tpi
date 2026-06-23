import { Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/auth'
import { ToastProvider } from './context/toast'
import AppLayout from './layouts/AppLayout'
import BackofficeLayout from './layouts/BackofficeLayout'
import BackofficeLogin from './pages/Backoffice/BackofficeLogin'
import ChatbotPage from './pages/Backoffice/ChatbotPage'
import AntiScrollPage from './pages/Backoffice/AntiScrollPage'
import DashboardPage from './pages/Backoffice/DashboardPage'
import UsersPage from './pages/Backoffice/UsersPage'
import UserDetailPage from './pages/Backoffice/UserDetailPage'
import BackofficeNotFound from './pages/Backoffice/BackofficeNotFound'
import Breathing from './pages/Breathing/Breathing'
import BubblesActivity from './pages/BubblesActivity/BubblesActivity'
import Challenges from './pages/Challenges/Challenges'
import CloudsActivity from './pages/CloudsActivity/CloudsActivity'
import Diary from './pages/Diary/Diary.tsx'
import Home from './pages/Home'
import Login from './pages/Login/Login'
import Minigames from './pages/Minigames/Minigames'
import NotFound from './pages/NotFound'
import Onboarding from './pages/Onboarding/Onboarding'
import Register from './pages/Register/Register'
import SandZenGarden from './pages/SandZenGarden/SandZenGarden.tsx'
import Shop from './pages/Shop/Shop'
import Profile from './pages/Profile/Profile'
import Orchard from './pages/Orchard/Orchard.tsx'
import Privacy from './pages/Privacy/Privacy'
import Unsubscribe from './pages/Unsubscribe/Unsubscribe'

const App = () => {
  return (

    <AuthProvider>
      <ToastProvider>
        <Routes>
        <Route path="/backoffice/login" element={<BackofficeLogin />} />
        <Route path="/unsubscribe" element={<Unsubscribe />} />
        <Route path="/backoffice" element={<BackofficeLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="chatbot" element={<ChatbotPage />} />
          <Route path="antiscroll" element={<AntiScrollPage />} />
          <Route path="usuarios" element={<UsersPage />} />
          <Route path="usuarios/:id" element={<UserDetailPage />} />
          <Route path="*" element={<BackofficeNotFound />} />
        </Route>


        <Route element={<AppLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/privacy" element={<Privacy />} />
          <Route path="/minigames" element={<Minigames />} />
          <Route path="/diary" element={<Diary />} />
          <Route path="/onboarding" element={<Onboarding />} />
          <Route path="/challenges" element={<Challenges />} />
          <Route path="/orchard" element={<Orchard />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/guided-breathing" element={<Breathing />} />
          <Route path="/clouds" element={<CloudsActivity />} />
          <Route path="/bubbles" element={<BubblesActivity />} />
          <Route path="/zen-sand-garden" element={<SandZenGarden />} />
          <Route path="/shop" element={<Shop />} />
          <Route path="*" element={<NotFound />} />
        </Route>
        </Routes>
      </ToastProvider>
    </AuthProvider>
  )
}

export default App
