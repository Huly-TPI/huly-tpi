import React from 'react';
import { usePopupSettings, PREDEFINED_SITES } from '../../hooks/usePopupSettings';
import './Popup.css';

const Popup: React.FC = () => {
  const {
    settings,
    mins,
    secs,
    newDomain,
    setNewDomain,
    errorMsg,
    accumulated,
    handleToggle,
    handleMinChange,
    handleSecChange,
    isPredefinedEnabled,
    handleTogglePredefined,
    getCustomDomains,
    handleAddCustomDomain,
    handleRemoveCustomDomain,
  } = usePopupSettings();

  if (!settings) return <div className="huly-popup-loading">Cargando...</div>;

  return (
    <div className="huly-popup">
      <header className="huly-popup-header">
        <img src="/color-logo.webp" alt="Huly" className="huly-logo" />
        <h1 className="huly-popup-title">Pausa digital</h1>
      </header>
      
      <main className="huly-popup-content">
        <div className="huly-setting-item">
          <span className="huly-setting-label">Activar pausa</span>
          <label className="huly-switch">
            <input 
              type="checkbox" 
              checked={settings.enabled} 
              onChange={handleToggle} 
            />
            <span className="huly-slider-round"></span>
          </label>
        </div>

        <div className="huly-setting-item vertical">
          <span className="huly-setting-label">Intervalo de pausa</span>
          <div className="huly-time-input-group">
            <div className="huly-input-wrapper">
              <input 
                type="number" 
                min="0" 
                value={mins} 
                onChange={handleMinChange}
                className="huly-input-small"
              />
              <span className="huly-input-label">min</span>
            </div>
            <span className="huly-separator">:</span>
            <div className="huly-input-wrapper">
              <input 
                type="number" 
                min="0" 
                max="59" 
                value={secs} 
                onChange={handleSecChange}
                className="huly-input-small"
              />
              <span className="huly-input-label">seg</span>
            </div>
          </div>
        </div>

        <div className="huly-setting-item vertical">
          <span className="huly-setting-label">Tiempo de scroll acumulado</span>
          <div className="huly-scroll-progress-bar-container">
            <div 
              className="huly-scroll-progress-bar-fill" 
              style={{ width: `${Math.min(100, (accumulated / settings.pauseIntervalSeconds) * 100)}%` }} 
            />
          </div>
          <span className="huly-scroll-progress-text">
            {Math.floor(accumulated / 60)}m {accumulated % 60}s scrolleados de {Math.floor(settings.pauseIntervalSeconds / 60)}m {settings.pauseIntervalSeconds % 60}s
          </span>
        </div>

        <div className="huly-divider" />

        <div className="huly-section-title">Sitios web a monitorear</div>

        <div className="huly-predefined-grid">
          {PREDEFINED_SITES.map((site) => {
            const enabled = isPredefinedEnabled(site.domains);
            const mainDomain = site.domains[0];
            return (
              <div 
                key={site.name} 
                className={`huly-predefined-item ${enabled ? 'active' : ''}`}
                onClick={() => handleTogglePredefined(site.domains)}
              >
                <img 
                  src={`https://www.google.com/s2/favicons?domain=${mainDomain}&sz=32`} 
                  alt={site.name}
                  className="huly-site-icon" 
                  onError={(e) => {
                    (e.target as HTMLImageElement).src = 'https://www.google.com/s2/favicons?domain=huly.io&sz=32';
                  }}
                />
                <span className="huly-site-name">{site.name}</span>
                <span className={`huly-site-status-dot ${enabled ? 'active' : ''}`} />
              </div>
            );
          })}
        </div>

        <div className="huly-divider" />

        <div className="huly-section-title">Sitios personalizados</div>
        <form onSubmit={handleAddCustomDomain} className="huly-add-domain-form">
          <input 
            type="text" 
            placeholder="ej: reddit.com" 
            value={newDomain} 
            onChange={(e) => {
              setNewDomain(e.target.value);
            }}
            className="huly-input-text"
          />
          <button type="submit" className="huly-btn-add">+</button>
        </form>
        {errorMsg && <div className="huly-error-message">{errorMsg}</div>}

        <div className="huly-custom-domains-list">
          {getCustomDomains().map((domain) => (
            <div key={domain} className="huly-custom-domain-item">
              <img 
                src={`https://www.google.com/s2/favicons?domain=${domain}&sz=32`} 
                alt={domain} 
                className="huly-site-icon-small"
                onError={(e) => {
                  (e.target as HTMLImageElement).src = 'https://www.google.com/s2/favicons?domain=huly.io&sz=32';
                }}
              />
              <span className="huly-custom-domain-name">{domain}</span>
              <button 
                type="button" 
                onClick={() => handleRemoveCustomDomain(domain)} 
                className="huly-btn-delete"
              >
                &times;
              </button>
            </div>
          ))}
        </div>
      </main>

      <footer className="huly-popup-footer">
        <p>Tu bienestar es lo primero</p>
      </footer>
    </div>
  );
};

export default Popup;
