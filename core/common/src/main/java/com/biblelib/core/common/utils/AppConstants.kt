package com.biblelib.core.common.utils

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object ApiConstants {
    const val PAYSTACK_BASE_URL = "https://api.paystack.co/"
    const val PAYSTACK_INITIALIZE = "transaction/initialize"
    const val PAYSTACK_CALLBACK_URL = "https://songlive.vercel.app/donation/callback"
    const val DONOR_EMAIL = "anonymous_donor@biblelib.app"

    const val BIBLELIB_BASE = "https://biblive.vercel.app/"
}

object PayConstants {
    const val MPESA_NUMBER = "+254115586529"
    const val PAYBILL_NUMBER = "880100"
    const val ACCOUNT_NUMBER = "9530000018"
    const val CRYPTO_PROFILE = "sirodevs.base.eth"
    const val ETH_ADDRESS = "0x1C9A68b346D956e9Eff027C34DB671817e365fbb"
    const val SOL_ADDRESS = "5qW65Qu8hMLdsjNPxvpFGuv6EAJosGA5YDo6ePSnva1A"
    const val PAYPAL_ADDRESS = "sirodevske@gmail.com"
}

object PrefConstants {
    const val PREFERENCE_FILE = "biblelib_pref"
    const val SELECTED_BIBLES = "selectedBibles"
    const val PRIMARY_BIBLE = "primaryBible"
    const val IS_DATA_SELECTED = "dataSelected"
    const val IS_PRIMARY_LOADED = "primaryLoaded"
    const val SELECT_A_FRESH = "selectAfresh"
    const val INSTALL_DATE = "install_date"
    const val THEME_MODE = "themeMode"
    const val HAS_SEEN_BIBLES_MANAGEMENT_TIP = "hasSeenBiblesManagementTip"

    const val DONATION_DONE_AT = "donation_done_at"
    const val DONATION_METHOD = "donation_method"
    const val DONATION_REMIND_NEXT_OPEN = "donation_remind_next"
    const val LAST_SYNCED_AT = "last_synced_at"

    const val LAST_BOOK_ID = "lastBookId"
    const val LAST_CHAPTER_ID = "lastChapterId"
    const val LAST_BIBLE = "lastBible"
    const val LAST_BIBLE_ABBR = "lastBibleAbbr"
    const val FONT_SIZE_SP = "fontSizeSp"
    const val MULTI_BIBLE_ENABLED = "multiBibleReaderEnabled"
    const val SECONDARY_BIBLES = "secondaryBibles"
    const val LAST_VERSE_ID = "lastVerseId"
    const val READER_FONT_FAMILY = "readerFontFamily"
    const val READER_BACKGROUND = "readerBackground"
}

object Routes {
    const val SELECTION = "selection"
    const val READER =
        "reader?bibleAbbr={bibleAbbr}&bookId={bookId}&chapterId={chapterId}" +
                "&verseId={verseId}&searchQuery={searchQuery}"
    const val SEARCH = "search"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val HOW_IT_WORKS = "how_it_works"
    const val HELP = "help"
    const val DONATION = "donation"
    const val BOOKMARKS_NOTES = "bookmarks_notes"
    const val BIBLES = "bibles"
    const val APPEARANCE_SETTINGS = "appearance_settings"
    const val READING_SETTINGS = "reading_settings"
    const val DATA_SETTINGS = "data_settings"
    const val CASTING = "casting"

    fun reader(
        bibleAbbr: String = "",
        bookId: String = "",
        chapterId: String = "",
        verseId: String = "",
        searchQuery: String = "",
    ): String {
        val encodedVerseId = URLEncoder.encode(verseId, StandardCharsets.UTF_8.toString())
        val encodedQuery = URLEncoder.encode(searchQuery, StandardCharsets.UTF_8.toString())
        return "reader?bibleAbbr=$bibleAbbr&bookId=$bookId&chapterId=$chapterId" +
                "&verseId=$encodedVerseId&searchQuery=$encodedQuery"
    }

    const val NOTES =
        "notes?bibleAbbr={bibleAbbr}&verseId={verseId}&bookId={bookId}&chapterId={chapterId}&title={title}&verseText={verseText}"

    fun notes(
        bibleAbbr: String,
        verseId: String,
        bookId: String,
        chapterId: String,
        title: String,
        verseText: String,
    ): String {
        val encodedVerseId = URLEncoder.encode(verseId, StandardCharsets.UTF_8.toString())
        val encodedBookId = URLEncoder.encode(bookId, StandardCharsets.UTF_8.toString())
        val encodedChapterId = URLEncoder.encode(chapterId, StandardCharsets.UTF_8.toString())
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        val encodedVerseText = URLEncoder.encode(verseText, StandardCharsets.UTF_8.toString())
        return "notes?bibleAbbr=$bibleAbbr&verseId=$encodedVerseId&bookId=$encodedBookId" +
                "&chapterId=$encodedChapterId&title=$encodedTitle&verseText=$encodedVerseText"
    }

    const val PAYMENT_WEBVIEW = "payment_webview/{redirectUrl}"

    fun paymentWebView(redirectUrl: String): String {
        val encoded = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8.toString())
        return "payment_webview/$encoded"
    }

    const val SCRIPTURE_OPENER = "scripture_opener?bibleAbbr={bibleAbbr}&bibleName={bibleName}"

    fun scriptureOpener(bibleAbbr: String, bibleName: String): String {
        val encodedName = URLEncoder.encode(bibleName, StandardCharsets.UTF_8.toString())
        return "scripture_opener?bibleAbbr=$bibleAbbr&bibleName=$encodedName"
    }

    const val SCRIPTURE_LISTS = "scripture_lists"

    const val SCRIPTURE_LIST_DETAIL = "scripture_list_detail/{listId}"

    fun scriptureListDetail(listId: Long) = "scripture_list_detail/$listId"

    fun decodeRedirectUrl(encoded: String): String =
        URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())

    fun decode(encoded: String): String =
        URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
}

object AppFonts {
    const val DEFAULT_FONT_SP = 18f
    const val MIN_FONT_SP = 12f
    const val MAX_FONT_SP = 32f
}
