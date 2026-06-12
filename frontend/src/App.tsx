import { Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/auth'
import AppLayout from './layouts/AppLayout'
import BackofficeLayout from './layouts/BackofficeLayout'
import BackofficeLogin from './pages/Backoffice/BackofficeLogin'
import ChatbotPage from './pages/Backoffice/ChatbotPage'
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

const App = () => {
  return (

    <AuthProvider>
      <Routes>
        <Route path="/backoffice" element={<BackofficeLayout />}>
          <Route index element={<ChatbotPage />} />
          <Route path="login" element={<BackofficeLogin />} />
          <Route path="chatbot" element={<ChatbotPage />} />
        </Route>

        <Route element={<AppLayout />}>
          <Route path="/" element={<Home />} />
          <Route path="/minigames" element={<Minigames />} />
          <Route path="/diary" element={<Diary />} />
          <Route path="/onboarding" element={<Onboarding />} />
          <Route path="/challenges" element={<Challenges />} />
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
    </AuthProvider>
  )
}

export default App