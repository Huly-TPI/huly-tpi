import React from 'react';
import browser from 'webextension-polyfill';
import { Sprout, Bell } from 'lucide-react';

interface PauseModalProps {
  onClose: () => void;
  onRedirect: () => void;
}

const PauseModal: React.FC<PauseModalProps> = ({ onClose, onRedirect }) => {
  const logoUrl = browser.runtime.getURL('color-logo.webp');
  
  return (
    <div className="huly-modal-overlay">
      <div className="huly-modal-content">
        <img src={logoUrl} alt="Huly" className="huly-modal-logo" />
        <h2 className="huly-modal-title">¿Qué tal una pausa?</h2>
        <p className="huly-modal-text">
          Has estado navegando un buen rato. ¿Por qué no te tomas un momento para reconectar en tu jardín?
        </p>
        <div className="huly-modal-actions">
          <button className="huly-btn huly-btn-primary" onClick={onRedirect}>
            <Sprout size={18} style={{ marginRight: '8px' }} />
            Ir al jardín
          </button>
          <button className="huly-btn huly-btn-secondary" onClick={onClose}>
            <Bell size={18} style={{ marginRight: '8px' }} />
            Recordarme más tarde
          </button>
        </div>
      </div>
    </div>
  );
};

export default PauseModal;
