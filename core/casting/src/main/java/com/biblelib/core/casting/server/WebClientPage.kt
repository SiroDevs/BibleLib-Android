package com.biblelib.core.casting.server

import com.biblelib.core.casting.server.webclient.CastingWebControls
import com.biblelib.core.casting.server.webclient.CastingWebMarkup
import com.biblelib.core.casting.server.webclient.CastingWebScript
import com.biblelib.core.casting.server.webclient.CastingWebStyles

/**
 * The page served to cast devices at `/`. Assembled from small, focused pieces
 * (styles, markup, and two independent scripts) so no single file grows unwieldy —
 * see the `webclient` package for each part.
 */
object WebClientPage {

    val html: String = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>BibleLib - Your Bible on the Go</title>
        <style>
        ${CastingWebStyles.css}
        </style>
        </head>
        <body>
        ${CastingWebMarkup.body}
        <script>
        ${CastingWebScript.js}
        </script>
        <script>
        ${CastingWebControls.js}
        </script>
        </body>
        </html>
    """.trimIndent()
}
