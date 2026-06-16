const API_BASE_URL = 'https://huly-tpi.onrender.com'; // Actualizar con la URL del backend en prod

document.addEventListener('DOMContentLoaded', () => {

  const revealObserver = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (!entry.isIntersecting) return;
      entry.target.classList.add('visible');
      revealObserver.unobserve(entry.target);
    });
  }, { threshold: 0.12 });

  document.querySelectorAll('.reveal').forEach((el) => revealObserver.observe(el));

  /**
   * @param {string} id
   */
  function openModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.add('open');
    document.body.style.overflow = 'hidden';

    const firstInput = modal.querySelector('input');
    if (firstInput) setTimeout(() => firstInput.focus(), 350);
  }

  /**
   * @param {string} id
   */
  function closeModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.remove('open');
    document.body.style.overflow = '';
  }

  document.addEventListener('click', (e) => {
    const trigger = e.target.closest('[data-modal]');
    if (trigger) {
      e.preventDefault();
      openModal(`modal-${trigger.dataset.modal}`);
      return;
    }

    const closeBtn = e.target.closest('[data-close]');
    if (closeBtn) {
      closeModal(closeBtn.dataset.close);
      return;
    }

    if (e.target.classList.contains('modal-overlay') && e.target.classList.contains('open')) {
      closeModal(e.target.id);
    }
  });

  document.addEventListener('keydown', (e) => {
    if (e.key !== 'Escape') return;
    document.querySelectorAll('.modal-overlay.open').forEach((m) => closeModal(m.id));
  });

  document.getElementById('submit-register')?.addEventListener('click', async () => {
    const nicknameInput = document.getElementById('reg-username');
    const emailInput    = document.getElementById('reg-email');
    const submitBtn     = document.getElementById('submit-register');
    const nickname      = nicknameInput?.value.trim();
    const email         = emailInput?.value.trim();

    if (!nickname || !email) return;

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      showModalError('modal-register', 'Ingresá un email válido.');
      return;
    }

    const originalLabel = submitBtn.textContent;
    submitBtn.disabled = true;
    submitBtn.textContent = 'Enviando...';
    clearModalError('modal-register');

    try {
      const res = await fetch(`${API_BASE_URL}/api/leads`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, nickname, sourceAction: 'LANDING' }),
      });

      if (res.status === 201) {
        showModalSuccess('modal-register');
        return;
      }

      if (res.status === 409) {
        showModalError('modal-register', 'Este email ya está registrado.');
      } else if (res.status === 400) {
        showModalError('modal-register', 'Revisá los datos ingresados.');
      } else {
        showModalError('modal-register', 'Algo salió mal. Intentá de nuevo.');
      }
    } catch {
      showModalError('modal-register', 'No pudimos conectarnos. Intentá de nuevo.');
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = originalLabel;
    }
  });

  // Video controls
  const video      = document.getElementById('hero-video');
  const btnPlay    = document.getElementById('vc-play');
  const btnMute    = document.getElementById('vc-mute');
  const btnSpeed   = document.getElementById('vc-speed');
  const speedMenu  = document.getElementById('vc-speed-menu');

  const ICON_PLAY  = '<svg viewBox="0 0 12 12" width="12" height="12" fill="currentColor"><polygon points="2,1 11,6 2,11"/></svg>';
  const ICON_PAUSE = '<svg viewBox="0 0 12 12" width="12" height="12" fill="currentColor"><rect x="2" y="1" width="3" height="10" rx="1"/><rect x="7" y="1" width="3" height="10" rx="1"/></svg>';
  const ICON_SOUND = '<svg viewBox="0 0 16 14" width="15" height="13" fill="currentColor"><path d="M0 4.5v5h3.5l4.5 4V.5L3.5 4.5H0zm12 2.5c0-1.4-.8-2.6-2-3.2v6.4c1.2-.6 2-1.8 2-3.2z"/><path d="M10 .8v1.5c2 .8 3.5 2.8 3.5 4.7s-1.5 3.9-3.5 4.7v1.5c2.8-.9 5-3.5 5-6.2S12.8 1.7 10 .8z"/></svg>';
  const ICON_MUTED = '<svg viewBox="0 0 16 14" width="15" height="13" fill="currentColor"><path d="M0 4.5v5h3.5l4.5 4V.5L3.5 4.5H0z"/><line x1="10.5" y1="3.5" x2="15.5" y2="10.5" stroke="currentColor" stroke-width="1.6"/><line x1="15.5" y1="3.5" x2="10.5" y2="10.5" stroke="currentColor" stroke-width="1.6"/></svg>';

  if (video && btnPlay && btnMute) {
    const syncPlay = () => {
      btnPlay.innerHTML = video.paused ? ICON_PLAY : ICON_PAUSE;
      btnPlay.setAttribute('aria-label', video.paused ? 'Reproducir' : 'Pausar');
    };
    const syncMute = () => {
      btnMute.innerHTML = video.muted ? ICON_MUTED : ICON_SOUND;
      btnMute.setAttribute('aria-label', video.muted ? 'Activar sonido' : 'Silenciar');
    };

    syncPlay();
    syncMute();
    video.addEventListener('play',  syncPlay);
    video.addEventListener('pause', syncPlay);

    btnPlay.addEventListener('click', () => { video.paused ? video.play() : video.pause(); });
    btnMute.addEventListener('click', () => { video.muted = !video.muted; syncMute(); });

    btnSpeed.addEventListener('click', (e) => {
      e.stopPropagation();
      speedMenu.classList.toggle('open');
    });

    speedMenu.querySelectorAll('button').forEach((btn) => {
      btn.addEventListener('click', () => {
        video.playbackRate = parseFloat(btn.dataset.speed);
        btnSpeed.textContent = btn.textContent;
        speedMenu.querySelectorAll('button').forEach((b) => b.classList.remove('active'));
        btn.classList.add('active');
        speedMenu.classList.remove('open');
      });
    });

    document.addEventListener('click', () => speedMenu.classList.remove('open'));
  }

  document.getElementById('submit-explore')?.addEventListener('click', () => {
    const email = document.getElementById('exp-email')?.value.trim();
    if (!email) return;
    // TODO: POST to /api/early-access with email before redirecting
    window.open('https://huly-tpi-frontend.onrender.com/', '_blank');
    closeModal('modal-explore');
  });

});
