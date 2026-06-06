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

  document.getElementById('submit-register')?.addEventListener('click', () => {
    const username = document.getElementById('reg-username')?.value.trim();
    const email    = document.getElementById('reg-email')?.value.trim();
    if (!username || !email) return;
    // TODO: POST to /api/early-access with username, email
    console.log('Register:', { username, email });
  });

  document.getElementById('submit-explore')?.addEventListener('click', () => {
    const email = document.getElementById('exp-email')?.value.trim();
    if (!email) return;
    // TODO: POST to /api/early-access with email before redirecting
    window.open('https://huly-tpi-frontend.onrender.com/', '_blank');
    closeModal('modal-explore');
  });

});
