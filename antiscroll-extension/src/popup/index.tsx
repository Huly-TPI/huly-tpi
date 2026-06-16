import React from 'react';
import ReactDOM from 'react-dom/client';
import Popup from '../pages/Popup/Popup';

const rootElement = document.getElementById('root');

if (rootElement) {
  try {
    const root = ReactDOM.createRoot(rootElement);
    root.render(
      <React.StrictMode>
        <Popup />
      </React.StrictMode>
    );
  } catch (error) {
    rootElement.innerHTML = `<pre style="color: red; padding: 20px;">Error al iniciar React: ${error}</pre>`;
  }
} else {
  console.error("No se encontró el elemento #root");
}
