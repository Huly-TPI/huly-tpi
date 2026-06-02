import { Routes, Route } from 'react-router-dom'
import { AuthProvider } from './context/auth'
import AppLayout from './layouts/AppLayout'
import BackofficeLayout from './layouts/BackofficeLayout'
import Home from './pages/Home'
import NotFound from './pages/NotFound'
import Diary from './pages/Diary'
import Breathing from './pages/Breathing/Breathing'
import CloudsActivity from './pages/CloudsActivity/CloudsActivity'
import BubblesActivity from './pages/BubblesActivity/BubblesActivity'
import Register from './pages/Register/Register'
import ChatbotPage from './pages/Backoffice/ChatbotPage'
import BackofficeLogin from './pages/Backoffice/BackofficeLogin'
import Login from './pages/Login/Login'
import Challenges from './pages/Challenges/Challenges'
import Minigames from './pages/Minigames/Minigames'

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
          <Route path="/challenges" element={<Challenges />} />
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/guided-breathing" element={<Breathing />} />
          <Route path="/clouds" element={<CloudsActivity />} />
          <Route path="/bubbles" element={<BubblesActivity />} />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </AuthProvider>
  )
}

export default App