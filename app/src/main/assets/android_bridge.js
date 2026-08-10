(function () {
  if (!window.Android) return;
  window.__ANIME_TOOLBOX_ANDROID__ = true;

  try {
    Object.defineProperty(navigator, 'share', {
      configurable: true,
      value: function (data) {
        Android.shareText(String((data && data.title) || 'Compartilhar'), String((data && data.text) || ''));
        return Promise.resolve();
      }
    });
  } catch (_) {}

  try {
    Object.defineProperty(navigator, 'clipboard', {
      configurable: true,
      value: {
        writeText: function (text) {
          Android.copyText(String(text == null ? '' : text));
          return Promise.resolve();
        }
      }
    });
  } catch (_) {}

  try {
    function NativeNotification(title, options) {
      Android.showNotification(String(title || 'Anime Toolbox'), String((options && options.body) || ''));
      this.close = function () {};
    }
    Object.defineProperty(NativeNotification, 'permission', { get: function () { return 'granted'; } });
    NativeNotification.requestPermission = function () {
      Android.requestNotificationPermission();
      return Promise.resolve('granted');
    };
    window.Notification = NativeNotification;
  } catch (_) {}

  try {
    var nativeClick = HTMLAnchorElement.prototype.click;
    HTMLAnchorElement.prototype.click = function () {
      var a = this;
      var href = a.href || '';
      var filename = a.download || '';
      if (filename && href.indexOf('blob:') === 0) {
        fetch(href).then(function (r) {
          var mime = r.headers.get('content-type') || 'text/plain;charset=utf-8';
          return r.text().then(function (text) { return { text: text, mime: mime }; });
        }).then(function (data) {
          Android.saveTextFile(filename, data.text, data.mime);
        }).catch(function () {
          nativeClick.call(a);
        });
        return;
      }
      nativeClick.call(a);
    };
  } catch (_) {}
})();