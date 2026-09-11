package com.biblelib.core.casting.server.webclient

/**
 * Stylesheet for [com.biblelib.core.casting.server.WebClientPage].
 *
 * Colors are CSS variables mirroring the app's Material palette
 * (see core/design_system/theme/Color.kt) so the presented screen matches
 * BibleLib's own dark and light themes rather than a generic look.
 */
object CastingWebStyles {

    val css: String = """
        :root {
          color-scheme: dark;
          --bg: #181210;
          --fg: #ece0da;
          --muted: #a08d83;
          --accent: #ffb690;
          --accent-strong: #e1550f;
          --card-bg: #241c19;
          --indicator-bg: #2a211d;
          --indicator-fg: #a08d83;
          --indicator-active-bg: #ece0da;
          --indicator-active-fg: #181210;
          --overlay-bg: rgba(24, 18, 16, .92);
          --live-dot: #ef4444;
          --live-dot-on: #22c55e;
          --font-scale: 1;
        }
        html[data-theme="light"] {
          color-scheme: light;
          --bg: #fffbff;
          --fg: #211a15;
          --muted: #75655c;
          --accent: #e1550f;
          --accent-strong: #e1550f;
          --card-bg: #f4ded2;
          --indicator-bg: #f0e2d8;
          --indicator-fg: #75655c;
          --indicator-active-bg: #211a15;
          --indicator-active-fg: #fffbff;
          --overlay-bg: rgba(255, 251, 255, .92);
        }
        * { box-sizing: border-box; }
        html, body {
          margin: 0;
          height: 100%;
          background: var(--bg);
          color: var(--fg);
          font-family: -apple-system, Roboto, "Segoe UI", Helvetica, Arial, sans-serif;
          overflow: hidden;
          transition: background .2s, color .2s;
        }
        #app {
          position: fixed;
          inset: 0;
          display: flex;
          flex-direction: column;
        }
        #toolbar {
          position: absolute;
          top: 12px;
          right: 14px;
          display: flex;
          align-items: center;
          gap: 10px;
          z-index: 20;
        }
        #status-dot {
          width: 10px;
          height: 10px;
          border-radius: 50%;
          background: var(--live-dot);
          transition: background .25s;
        }
        #status-dot.connected { background: var(--live-dot-on); }
        .icon-btn {
          width: 34px;
          height: 34px;
          border-radius: 50%;
          border: none;
          background: var(--card-bg);
          color: var(--fg);
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          opacity: .85;
        }
        .icon-btn:hover { opacity: 1; }
        .icon-btn .icon-sun { display: none; }
        html[data-theme="light"] .icon-btn .icon-moon { display: none; }
        html[data-theme="light"] .icon-btn .icon-sun { display: block; }
        #header {
          display: none;
          flex-direction: column;
          align-items: center;
          text-align: center;
          gap: 4px;
          padding: 28px 64px 0;
        }
        #title-text {
          font-size: clamp(20px, 3.4vw, 30px);
          font-weight: 700;
          color: var(--fg);
          line-height: 1.25;
          max-width: 100%;
        }
        #book-text {
          font-size: 13px;
          font-weight: 600;
          letter-spacing: .06em;
          text-transform: uppercase;
          color: var(--muted);
        }
        #book-text:empty, #secondary-text:empty { display: none; }
        #secondary-text { font-size: 12px; font-weight: 500; color: var(--muted); }
        #idle-screen {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-direction: column;
          gap: 24px;
          text-align: center;
          padding: 24px;
        }
        #idle-logo {
          width: clamp(140px, 22vw, 220px);
          height: auto;
          border-radius: 18px;
          box-shadow: 0 12px 40px rgba(0, 0, 0, .45);
          animation: idle-breathe 2.6s ease-in-out infinite;
        }
        @keyframes idle-breathe {
          0%, 100% { transform: scale(1); opacity: .92; }
          50% { transform: scale(1.04); opacity: 1; }
        }
        #idle-brand { font-size: 22px; font-weight: 700; color: var(--fg); margin: 0; }
        #idle-screen h1 { font-size: 15px; font-weight: 500; color: var(--muted); margin: 0; }
        #slide-stage {
          flex: 1;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 32px 32px 96px;
          position: relative;
          touch-action: pan-y;
        }
        .verse {
          display: none;
          color: var(--fg);
          font-size: calc(clamp(22px, 5vw, 52px) * var(--font-scale));
          line-height: 1.35;
          text-align: center;
          font-weight: 600;
          white-space: pre-wrap;
          max-width: 1000px;
        }
        .verse.active { display: block; animation: fade .25s ease-out; }
        @keyframes fade {
          from { opacity: 0; transform: translateY(6px); }
          to { opacity: 1; transform: translateY(0); }
        }
        #indicators {
          position: absolute;
          bottom: 28px;
          left: 0;
          right: 0;
          display: flex;
          justify-content: center;
          gap: 8px;
          flex-wrap: wrap;
          padding: 0 16px;
        }
        .indicator {
          min-width: 22px;
          height: 22px;
          padding: 0 6px;
          border-radius: 11px;
          background: var(--indicator-bg);
          color: var(--indicator-fg);
          font-size: 11px;
          font-weight: 700;
          display: flex;
          align-items: center;
          justify-content: center;
        }
        .indicator.active { background: var(--indicator-active-bg); color: var(--indicator-active-fg); }
        #app-footer {
          position: absolute;
          bottom: 6px;
          left: 0;
          right: 0;
          text-align: center;
          font-size: 11px;
          font-weight: 500;
          letter-spacing: .04em;
          color: var(--muted);
          opacity: .55;
          pointer-events: none;
          z-index: 5;
        }
        #info-overlay {
          position: fixed;
          inset: 0;
          display: none;
          align-items: center;
          justify-content: center;
          background: var(--overlay-bg);
          z-index: 30;
          padding: 24px;
        }
        #info-overlay.open { display: flex; }
        #info-dialog {
          background: var(--card-bg);
          color: var(--fg);
          border-radius: 16px;
          max-width: 440px;
          width: 100%;
          padding: 24px;
          box-shadow: 0 20px 60px rgba(0, 0, 0, .35);
        }
        #info-dialog h2 { margin: 0 0 12px; font-size: 18px; }
        #info-dialog p { margin: 0 0 10px; font-size: 14px; line-height: 1.5; color: var(--muted); }
        #info-dialog button {
          margin-top: 8px;
          width: 100%;
          padding: 10px;
          border: none;
          border-radius: 10px;
          background: var(--accent-strong);
          color: #fff;
          font-weight: 700;
          cursor: pointer;
        }
    """.trimIndent()
}
