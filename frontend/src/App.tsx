import { Routes, Route } from 'react-router-dom'
import AppLayout from './layouts/AppLayout'
import Home from './pages/Home'
import NotFound from './pages/NotFound'
import Diary from './pages/Diary'
import ChatTest from './pages/ChatTest'

const App = () => {
  return (
    <Routes>
      <Route path="/chat-test" element={<ChatTest />} />
      <Route element={<AppLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/diary" element={<Diary />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}

export default App