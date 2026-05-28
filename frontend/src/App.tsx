import { Routes, Route } from 'react-router-dom'
import AppLayout from './layouts/AppLayout'
import BackofficeLayout from './layouts/BackofficeLayout'
import Home from './pages/Home'
import NotFound from './pages/NotFound'
import Diary from './pages/Diary'
import ChatTest from './pages/ChatTest'
import CloudsActivity from './pages/CloudsActivity/CloudsActivity'
import ChatbotPage from './pages/Backoffice/ChatbotPage'


const App = () => {
  return (
    <Routes>
      <Route path="/chat-test" element={<ChatTest />} />
      <Route path="/backoffice" element={<BackofficeLayout />}>
        <Route index element={<ChatbotPage />} />
        <Route path="chatbot" element={<ChatbotPage />} />
      </Route>
      <Route element={<AppLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/diary" element={<Diary />} />
        <Route path="/clouds" element={<CloudsActivity />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}

export default App