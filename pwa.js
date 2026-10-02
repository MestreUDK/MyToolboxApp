(() => {
  'use strict';

  let deferredInstallPrompt = null;
  let reloadingForUpdate = false;

  const isStandalone = () =>
    window.matchMedia('(display-mode: standalone)').matches ||
    window.navigator.standalone === true;

  async function registerServiceWorker() {
    if (!('serviceWorker' in navigator)) return null;
    try {
      const registration = await navigator.serviceWorker.register('./sw.js', { scope: './' });

      registration.addEventListener('updatefound', () => {
        const worker = registration.installing;
        if (!worker) return;
        worker.addEventListener('statechange', () => {
          if (worker.state === 'installed' && navigator.serviceWorker.controller) {
            showUpdateToast();
          }
        });
      });

      navigator.serviceWorker.addEventListener('controllerchange', () => {
        if (reloadingForUpdate) return;
        reloadingForUpdate = true;
        location.reload();
      });

      return registration;
    } catch (error) {
      console.warn('Service Worker não pôde ser registrado:', error);
      return null;
    }
  }

  function showUpdateToast() {
    if (document.getElementById('toolbox-update-toast')) return;
    const box = document.createElement('div');
    box.id = 'toolbox-update-toast';
    box.innerHTML = '<span>Nova versão disponível.</span><button type="button">Atualizar</button>';
    Object.assign(box.style, {
      position: 'fixed', left: '12px', right: '12px', bottom: '18px', zIndex: '2147483647',
      maxWidth: '520px', margin: '0 auto', display: 'flex', alignItems: 'center',
      justifyContent: 'space-between', gap: '12px', padding: '12px 14px', borderRadius: '14px',
      background: '#252238', color: '#fff', boxShadow: '0 12px 30px rgba(0,0,0,.28)',
      fontFamily: 'system-ui,-apple-system,Segoe UI,sans-serif', fontSize: '14px'
    });
    const btn = box.querySelector('button');
    Object.assign(btn.style, {
      border: '0', borderRadius: '10px', padding: '9px 12px', background: '#8d74d6',
      color: '#fff', fontWeight: '700', cursor: 'pointer'
    });
    btn.addEventListener('click', async () => {
      const reg = await navigator.serviceWorker.getRegistration('./');
      if (reg?.waiting) reg.waiting.postMessage({ type: 'SKIP_WAITING' });
      else location.reload();
    });
    document.body.appendChild(box);
  }

  function addHomeButton() {
    const page = location.pathname.split('/').pop() || 'index.html';
    if (page === 'index.html' || page === '') return;
    if (document.getElementById('toolbox-home')) return;
    const a = document.createElement('a');
    a.id = 'toolbox-home';
    a.href = './';
    a.textContent = '⌂';
    a.setAttribute('aria-label', 'Voltar para a Anime Toolbox');
    a.title = 'Início';
    Object.assign(a.style, {
      position: 'fixed', right: '16px', bottom: '16px', zIndex: '2147483646',
      width: '50px', height: '50px', borderRadius: '16px', display: 'grid', placeItems: 'center',
      textDecoration: 'none', color: '#fff', background: '#4f378b', fontSize: '25px',
      boxShadow: '0 8px 24px rgba(0,0,0,.28)', border: '1px solid rgba(255,255,255,.2)'
    });
    document.body.appendChild(a);
  }

  function configureInstallButton() {
    const btn = document.getElementById('installPwa');
    if (!btn || isStandalone()) return;

    window.addEventListener('beforeinstallprompt', (event) => {
      event.preventDefault();
      deferredInstallPrompt = event;
      btn.hidden = false;
    });

    btn.addEventListener('click', async () => {
      if (!deferredInstallPrompt) return;
      deferredInstallPrompt.prompt();
      await deferredInstallPrompt.userChoice;
      deferredInstallPrompt = null;
      btn.hidden = true;
    });

    window.addEventListener('appinstalled', () => {
      deferredInstallPrompt = null;
      btn.hidden = true;
    });
  }

  function configureConnectionStatus() {
    const el = document.getElementById('connectionStatus');
    if (!el) return;
    const render = () => {
      const online = navigator.onLine;
      el.classList.toggle('offline', !online);
      const label = el.querySelector('span:last-child');
      if (label) label.textContent = `${online ? 'Online' : 'Offline'} • PWA v1.1.0`;
    };
    addEventListener('online', render);
    addEventListener('offline', render);
    render();
  }

  async function notify(title, options = {}) {
    if (!('Notification' in window)) return false;
    let permission = Notification.permission;
    if (permission === 'default') permission = await Notification.requestPermission();
    if (permission !== 'granted') return false;

    if ('serviceWorker' in navigator) {
      const registration = await navigator.serviceWorker.ready;
      await registration.showNotification(title, {
        icon: './icons/icon-192.png',
        badge: './icons/icon-192.png',
        ...options
      });
      return true;
    }

    new Notification(title, options);
    return true;
  }

  window.ToolboxPWA = { notify, isStandalone };

  document.addEventListener('DOMContentLoaded', () => {
    addHomeButton();
    configureInstallButton();
    configureConnectionStatus();
    registerServiceWorker();
  });
})();
