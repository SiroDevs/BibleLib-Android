package com.biblelib.core.casting.server.webclient

/**
 * UI-chrome script for [com.biblelib.core.casting.server.WebClientPage]: theme toggle,
 * pinch/keyboard font zoom, and the info dialog. Independent of [CastingWebScript] — it
 * never touches the live reading state, only presentation preferences.
 */
object CastingWebControls {

    val js: String = """
        (function () {
          var root = document.documentElement;
          var toggleBtn = document.getElementById('theme-toggle');
          var stored = localStorage.getItem('biblelib-cast-theme');

          function setTheme(theme) {
            root.setAttribute('data-theme', theme);
            localStorage.setItem('biblelib-cast-theme', theme);
            toggleBtn.setAttribute(
              'aria-label',
              theme === 'dark' ? 'Switch to light theme' : 'Switch to dark theme'
            );
          }

          toggleBtn.addEventListener('click', function () {
            setTheme(root.getAttribute('data-theme') === 'light' ? 'dark' : 'light');
          });

          setTheme(stored === 'light' ? 'light' : 'dark');
        })();

        (function () {
          var root = document.documentElement;
          var stage = document.getElementById('slide-stage');
          var MIN_SCALE = 0.6;
          var MAX_SCALE = 2.2;
          var STEP = 0.1;
          var scale = parseFloat(localStorage.getItem('biblelib-cast-font-scale')) || 1;

          function applyScale(next) {
            scale = Math.min(MAX_SCALE, Math.max(MIN_SCALE, next));
            root.style.setProperty('--font-scale', scale.toFixed(2));
            localStorage.setItem('biblelib-cast-font-scale', scale);
          }

          function distance(touches) {
            var dx = touches[0].clientX - touches[1].clientX;
            var dy = touches[0].clientY - touches[1].clientY;
            return Math.sqrt(dx * dx + dy * dy);
          }

          var pinchStartDistance = null;
          var pinchStartScale = 1;

          stage.addEventListener('touchstart', function (e) {
            if (e.touches.length === 2) {
              pinchStartDistance = distance(e.touches);
              pinchStartScale = scale;
            }
          }, { passive: true });

          stage.addEventListener('touchmove', function (e) {
            if (e.touches.length === 2 && pinchStartDistance) {
              applyScale(pinchStartScale * (distance(e.touches) / pinchStartDistance));
              e.preventDefault();
            }
          }, { passive: false });

          stage.addEventListener('touchend', function (e) {
            if (e.touches.length < 2) pinchStartDistance = null;
          });

          document.addEventListener('keydown', function (e) {
            if (e.key === '+' || e.key === '=') applyScale(scale + STEP);
            else if (e.key === '-' || e.key === '_') applyScale(scale - STEP);
          });

          applyScale(scale);
        })();

        (function () {
          var infoBtn = document.getElementById('info-btn');
          var overlay = document.getElementById('info-overlay');
          var closeBtn = document.getElementById('info-close');

          infoBtn.addEventListener('click', function () { overlay.classList.add('open'); });
          closeBtn.addEventListener('click', function () { overlay.classList.remove('open'); });
          overlay.addEventListener('click', function (e) {
            if (e.target === overlay) overlay.classList.remove('open');
          });
          document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape') overlay.classList.remove('open');
          });
        })();
    """.trimIndent()
}
