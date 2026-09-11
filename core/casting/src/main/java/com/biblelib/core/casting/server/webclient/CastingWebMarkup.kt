package com.biblelib.core.casting.server.webclient

/** Body markup for [com.biblelib.core.casting.server.WebClientPage]. */
object CastingWebMarkup {

    val body: String = """
        <div id="app">
          <div id="toolbar">
            <div id="status-dot"></div>
            <button id="theme-toggle" class="icon-btn" aria-label="Toggle theme">
              <svg class="icon-moon" width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z"/>
              </svg>
              <svg class="icon-sun" width="16" height="16" viewBox="0 0 24 24" fill="none"
                   stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="4"/>
                <path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/>
              </svg>
            </button>
            <button id="info-btn" class="icon-btn" aria-label="How this works">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <path d="M12 16v-5"/>
                <circle cx="12" cy="8" r="0.5" fill="currentColor"/>
              </svg>
            </button>
          </div>

          <div id="header">
            <div id="title-text"></div>
            <div id="book-text"></div>
            <div id="secondary-text"></div>
          </div>

          <div id="idle-screen">
            <img id="idle-logo" src="/logo.png" alt="BibleLib" />
            <div id="idle-brand">BibleLib</div>
            <h1>Waiting for a reading&hellip;</h1>
          </div>

          <div id="slide-stage" style="display:none">
            <div id="verses"></div>
            <div id="indicators"></div>
          </div>

          <div id="app-footer">BibleLib</div>
        </div>

        <div id="info-overlay">
          <div id="info-dialog">
            <h2>How casting works</h2>
            <p>This screen mirrors whatever passage is open on the BibleLib phone, live,
               over your local Wi-Fi or hotspot — no internet is used.</p>
            <p>The presenter can freeze this screen on the current verse from their phone
               while they keep navigating privately.</p>
            <p>Pinch with two fingers, or press the <strong>+</strong> / <strong>-</strong> keys,
               to resize the verse text. Use the moon/sun button to switch themes.</p>
            <button id="info-close">Got it</button>
          </div>
        </div>
    """.trimIndent()
}
