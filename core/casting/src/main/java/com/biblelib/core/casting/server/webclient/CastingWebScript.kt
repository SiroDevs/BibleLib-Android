package com.biblelib.core.casting.server.webclient

/**
 * Live-state script for [com.biblelib.core.casting.server.WebClientPage]: connects to
 * `/ws`, reconnects with backoff, and renders whatever [com.biblelib.core.casting.data.CastingState]
 * the server last broadcast (which is held on the last frame while the presenter has frozen it).
 */
object CastingWebScript {

    val js: String = """
        (function () {
          var statusDot = document.getElementById('status-dot');
          var header = document.getElementById('header');
          var titleText = document.getElementById('title-text');
          var bookText = document.getElementById('book-text');
          var secondaryText = document.getElementById('secondary-text');
          var idleScreen = document.getElementById('idle-screen');
          var slideStage = document.getElementById('slide-stage');
          var versesEl = document.getElementById('verses');
          var indicatorsEl = document.getElementById('indicators');

          var lastSignature = null;
          var socket = null;
          var retryDelay = 1000;

          function escapeHtml(str) {
            return str
              .replace(/&/g, '&amp;')
              .replace(/</g, '&lt;')
              .replace(/>/g, '&gt;');
          }

          function showIdle() {
            header.style.display = 'none';
            idleScreen.style.display = 'flex';
            slideStage.style.display = 'none';
            lastSignature = null;
          }

          function showReading(state) {
            header.style.display = 'flex';
            idleScreen.style.display = 'none';
            slideStage.style.display = 'flex';

            titleText.textContent = state.chapterRef || '';
            bookText.textContent = state.bibleName || '';
            secondaryText.textContent = (state.multiBibleEnabled && state.secondaryBibleNames && state.secondaryBibleNames.length)
              ? 'Also reading: ' + state.secondaryBibleNames.join(', ')
              : '';

            var signature = state.chapterRef + '|' + state.verses.join('\u0001');
            if (signature !== lastSignature) {
              lastSignature = signature;
              versesEl.innerHTML = state.verses
                .map(function (verse, i) {
                  return '<div class="verse" data-i="' + i + '">' +
                    escapeHtml(verse).replace(/\n/g, '<br>') +
                    '</div>';
                })
                .join('');
              indicatorsEl.innerHTML = (state.indicators || []).map(function (label, i) {
                return '<div class="indicator" data-i="' + i + '">' + escapeHtml(label) + '</div>';
              }).join('');
            }

            var verseNodes = versesEl.children;
            for (var i = 0; i < verseNodes.length; i++) {
              verseNodes[i].classList.toggle('active', i === state.currentIndex);
            }
            var indicatorNodes = indicatorsEl.children;
            for (var j = 0; j < indicatorNodes.length; j++) {
              indicatorNodes[j].classList.toggle('active', j === state.currentIndex);
            }
          }

          function render(state) {
            if (!state || state.type === 'idle') {
              showIdle();
            } else if (state.type === 'reading') {
              showReading(state);
            }
          }

          function connect() {
            var protocol = location.protocol === 'https:' ? 'wss://' : 'ws://';
            socket = new WebSocket(protocol + location.host + '/ws');

            socket.onopen = function () {
              statusDot.classList.add('connected');
              retryDelay = 1000;
            };

            socket.onmessage = function (event) {
              try {
                render(JSON.parse(event.data));
              } catch (e) {
                // ignore malformed frames
              }
            };

            socket.onclose = scheduleReconnect;
            socket.onerror = scheduleReconnect;
          }

          function scheduleReconnect() {
            statusDot.classList.remove('connected');
            if (socket) {
              socket.onclose = null;
              socket.onerror = null;
            }
            setTimeout(connect, retryDelay);
            retryDelay = Math.min(retryDelay * 1.5, 8000);
          }

          showIdle();
          connect();
        })();
    """.trimIndent()
}
