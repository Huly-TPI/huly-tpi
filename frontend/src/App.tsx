import { Routes, Route } from 'react-router-dom'
import AppLayout from './layouts/AppLayout'
import Home from './pages/Home'
import NotFound from './pages/NotFound'
import Diary from './pages/Diary'
import CloudsActivity from './pages/CloudsActivity/CloudsActivity'

const App = () => {
  return (
    <Routes>
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