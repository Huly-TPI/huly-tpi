import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App'
import { ThemeProvider } from './context/theme'
import { AudioSettingsProvider } from './context/audioSettings'
import { setupAntiScrollBridge } from './integrations/antiScrollBridge'

setupAntiScrollBridge()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider>
      <BrowserRouter>
        <AudioSettingsProvider>
          <App />
        </AudioSettingsProvider>
      </BrowserRouter>
    </ThemeProvider>
  </StrictMode>,
)
