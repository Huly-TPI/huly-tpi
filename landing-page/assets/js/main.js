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

  function showModalError(modalId, message) {
    const modal = document.getElementById(modalId);
    if (!modal) return;
    let errorEl = modal.querySelector('.modal-error');
    if (!errorEl) {
      errorEl = document.createElement('p');
      errorEl.className = 'modal-error';
      errorEl.style.cssText = 'color:#e55;font-size:.85rem;margin:.5rem 0 0;text-align:center';
      modal.querySelector('.modal-submit')?.insertAdjacentElement('afterend', errorEl);
    }
    errorEl.textContent = message;
  }

  function clearModalError(modalId) {
    const errorEl = document.getElementById(modalId)?.querySelector('.modal-error');
    if (errorEl) errorEl.textContent = '';
  }

  function showModalSuccess(modalId) {
    const modal = document.getElementById(modalId)?.querySelector('.modal');
    if (!modal) return;
    modal.innerHTML = `
      <span class="modal-icon"><img src="./assets/images/garden/envelope.png" alt="Envelope" width="56" height="56"/></span>
      <h3>¡Ya estás en la lista!</h3>
      <p class="modal-sub">Te avisamos cuando estés listo para entrar. Sin spam, prometido.</p>
    `;
  }

  document.getElementById('submit-explore')?.addEventListener('click', () => {
    const email = document.getElementById('exp-email')?.value.trim();
    if (!email) return;
    // TODO: POST to /api/early-access with email before redirecting
    window.open('https://huly-tpi-frontend.onrender.com/', '_blank');
    closeModal('modal-explore');
  });

});
