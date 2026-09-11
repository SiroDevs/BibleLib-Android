package com.biblelib.core.data.repos

import android.content.Context
import androidx.core.content.edit
import com.biblelib.core.common.entity.DonationMethod
import dagger.hilt.android.qualifiers.ApplicationContext
import com.biblelib.core.common.utils.PrefConstants
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefsRepo @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(PrefConstants.PREFERENCE_FILE, Context.MODE_PRIVATE)

    var installDate: Long
        get() = prefs.getLong(PrefConstants.INSTALL_DATE, 0L)
        set(v) = prefs.edit { putLong(PrefConstants.INSTALL_DATE, v) }

    var selectedBibles: String
        get() = prefs.getString(PrefConstants.SELECTED_BIBLES, "") ?: ""
        set(v) = prefs.edit { putString(PrefConstants.SELECTED_BIBLES, v) }

    var primaryBible: String
        get() = prefs.getString(PrefConstants.PRIMARY_BIBLE, "") ?: ""
        set(v) = prefs.edit { putString(PrefConstants.PRIMARY_BIBLE, v) }

    var isDataSelected: Boolean
        get() = prefs.getBoolean(PrefConstants.IS_DATA_SELECTED, false)
        set(v) = prefs.edit { putBoolean(PrefConstants.IS_DATA_SELECTED, v) }

    var isPrimaryLoaded: Boolean
        get() = prefs.getBoolean(PrefConstants.IS_PRIMARY_LOADED, false)
        set(v) = prefs.edit { putBoolean(PrefConstants.IS_PRIMARY_LOADED, v) }

    var selectAfresh: Boolean
        get() = prefs.getBoolean(PrefConstants.SELECT_A_FRESH, false)
        set(v) = prefs.edit { putBoolean(PrefConstants.SELECT_A_FRESH, v) }

    var hasSeenBiblesManagementTip: Boolean
        get() = prefs.getBoolean(PrefConstants.HAS_SEEN_BIBLES_MANAGEMENT_TIP, false)
        set(v) = prefs.edit { putBoolean(PrefConstants.HAS_SEEN_BIBLES_MANAGEMENT_TIP, v) }

    var lastBible: String
        get() = prefs.getString(PrefConstants.LAST_BIBLE, "") ?: ""
        set(v) = prefs.edit { putString(PrefConstants.LAST_BIBLE, v) }

    var lastBibleAbbr: String
        get() = prefs.getString(PrefConstants.LAST_BIBLE_ABBR, "") ?: ""
        set(v) = prefs.edit { putString(PrefConstants.LAST_BIBLE_ABBR, v) }

    var lastBookId: String
        get() = prefs.getString(PrefConstants.LAST_BOOK_ID, "") ?: ""
        set(v) = prefs.edit { putString(PrefConstants.LAST_BOOK_ID, v) }

    var lastChapterId: String
        get() = prefs.getString(PrefConstants.LAST_CHAPTER_ID, "") ?: ""
        set(v) = prefs.edit { putString(PrefConstants.LAST_CHAPTER_ID, v) }

    var lastVerseId: String
        get() = prefs.getString(PrefConstants.LAST_VERSE_ID, "") ?: ""
        set(v) = prefs.edit { putString(PrefConstants.LAST_VERSE_ID, v) }

    var readerFontFamily: String
        get() = prefs.getString(PrefConstants.READER_FONT_FAMILY, "default") ?: "default"
        set(v) = prefs.edit { putString(PrefConstants.READER_FONT_FAMILY, v) }

    var readerBackground: String
        get() = prefs.getString(PrefConstants.READER_BACKGROUND, "default") ?: "default"
        set(v) = prefs.edit { putString(PrefConstants.READER_BACKGROUND, v) }

    var appThemeMode: ThemeMode
        get() = ThemeMode.valueOf(
            prefs.getString(PrefConstants.THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        )
        set(v) = prefs.edit { putString(PrefConstants.THEME_MODE, v.name) }

    var fontSizeSp: Float
        get() = prefs.getFloat(PrefConstants.FONT_SIZE_SP, 18f)
        set(v) = prefs.edit { putFloat(PrefConstants.FONT_SIZE_SP, v) }

    var lastSyncedAt: Long
        get() = prefs.getLong(PrefConstants.LAST_SYNCED_AT, 0L)
        set(v) = prefs.edit { putLong(PrefConstants.LAST_SYNCED_AT, v) }

    var multiBibleReaderEnabled: Boolean
        get() = prefs.getBoolean(PrefConstants.MULTI_BIBLE_ENABLED, true)
        set(v) = prefs.edit { putBoolean(PrefConstants.MULTI_BIBLE_ENABLED, v) }

    var secondaryBibles: String
        get() = prefs.getString(PrefConstants.SECONDARY_BIBLES, "") ?: ""
        set(v) = prefs.edit { putString(PrefConstants.SECONDARY_BIBLES, v) }

    fun getSecondaryBibleList(): List<String> =
        secondaryBibles.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun setSecondaryBibleList(list: List<String>) {
        secondaryBibles = list.joinToString(",")
    }

    fun getSelectedBibleList(): List<String> =
        selectedBibles.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    var donationDoneAt: Long
        get() = prefs.getLong(PrefConstants.DONATION_DONE_AT, 0L)
        set(value) = prefs.edit { putLong(PrefConstants.DONATION_DONE_AT, value) }

    var donationMethod: DonationMethod
        get() = runCatching {
            DonationMethod.valueOf(
                prefs.getString(PrefConstants.DONATION_METHOD, DonationMethod.DIY.name)
                    ?: DonationMethod.DIY.name
            )
        }.getOrDefault(DonationMethod.DIY)
        set(value) = prefs.edit { putString(PrefConstants.DONATION_METHOD, value.name) }

    var donationRemindNextOpen: Boolean
        get() = prefs.getBoolean(PrefConstants.DONATION_REMIND_NEXT_OPEN, false)
        set(value) = prefs.edit { putBoolean(PrefConstants.DONATION_REMIND_NEXT_OPEN, value) }

    fun shouldShowDonation(): Boolean {
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L
        if (installDate == 0L || now - installDate < oneDayMs) return false

        val donated = donationDoneAt
        if (donated == 0L) return true

        val monthsUntilNextPrompt = when (donationMethod) {
            DonationMethod.DIY -> 3
            DonationMethod.PAYSTACK -> 3
            DonationMethod.CRYPTO -> 5
        }
        val nextPromptAt = Calendar.getInstance().apply {
            timeInMillis = donated
            add(Calendar.MONTH, monthsUntilNextPrompt)
        }.timeInMillis

        return now >= nextPromptAt
    }

    fun recordDonation(method: DonationMethod = DonationMethod.DIY) {
        donationDoneAt = System.currentTimeMillis()
        donationMethod = method
        donationRemindNextOpen = false
    }

    fun needsDailySync(): Boolean {
        val last = lastSyncedAt
        if (last == 0L) return false
        return System.currentTimeMillis() - last >= 24 * 60 * 60 * 1000L
    }

    fun resetAppData() {
        isDataSelected = false
        isPrimaryLoaded = false
        selectAfresh = false
        selectedBibles = ""
        primaryBible = ""
        lastBibleAbbr = ""
        lastBookId = ""
        lastChapterId = ""
        lastVerseId = ""
        secondaryBibles = ""
        readerFontFamily = "default"
        readerBackground = "default"
        appThemeMode = ThemeMode.SYSTEM
        fontSizeSp = 18f
        multiBibleReaderEnabled = true
    }

    companion object {
        const val DEFAULT_SECONDARY_BIBLES = 2
        const val MAX_SECONDARY_BIBLES = 5
    }
}
