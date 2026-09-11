package com.biblelib.core.casting.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CastingRepo @Inject constructor() {

    private val _readingState = MutableStateFlow<CastingState>(CastingState.Idle)
    val readingState: StateFlow<CastingState> = _readingState.asStateFlow()

    /** What the cast device actually renders. Equal to [readingState] unless frozen. */
    private val _broadcastState = MutableStateFlow<CastingState>(CastingState.Idle)
    val broadcastState: StateFlow<CastingState> = _broadcastState.asStateFlow()

    private val _isFrozen = MutableStateFlow(false)
    val isFrozen: StateFlow<Boolean> = _isFrozen.asStateFlow()

    private val _serverStatus = MutableStateFlow<ServerStatus>(ServerStatus.Stopped)
    val serverStatus: StateFlow<ServerStatus> = _serverStatus.asStateFlow()

    private val _connectedClients = MutableStateFlow(0)
    val connectedClients: StateFlow<Int> = _connectedClients.asStateFlow()

    private val _hotspotStatus = MutableStateFlow<HotspotStatus>(HotspotStatus.Stopped)
    val hotspotStatus: StateFlow<HotspotStatus> = _hotspotStatus.asStateFlow()

    /** Called whenever a chapter is loaded (or replaced) in the reader. */
    fun publishReading(
        bibleName: String,
        bookName: String,
        chapterRef: String,
        verses: List<String>,
        indicators: List<String>,
        currentIndex: Int = 0,
        multiBibleEnabled: Boolean = false,
        secondaryBibleNames: List<String> = emptyList(),
    ) {
        if (verses.isEmpty()) {
            publishIdle()
            return
        }
        _readingState.value = CastingState.Reading(
            bibleName = bibleName,
            bookName = bookName,
            chapterRef = chapterRef,
            verses = verses,
            indicators = indicators,
            currentIndex = currentIndex.coerceIn(0, verses.size - 1),
            multiBibleEnabled = multiBibleEnabled,
            secondaryBibleNames = secondaryBibleNames,
        )
        pushToBroadcast()
    }

    /** Called as the reader scrolls to a new verse within the current chapter. */
    fun updateIndex(index: Int) {
        val current = _readingState.value
        if (current is CastingState.Reading && current.verses.isNotEmpty()) {
            val safeIndex = index.coerceIn(0, current.verses.size - 1)
            if (safeIndex != current.currentIndex) {
                _readingState.value = current.copy(currentIndex = safeIndex)
            }
        }
        pushToBroadcast()
    }

    /** Called when the reader screen is closed — falls back to the waiting page. */
    fun publishIdle() {
        _readingState.value = CastingState.Idle
        pushToBroadcast()
    }

    /**
     * Freezes (or resumes) what's sent to the cast device. While frozen, the app can
     * keep navigating freely — the last broadcast frame stays on screen until resumed,
     * at which point the device immediately catches up to the live state.
     */
    fun setFrozen(frozen: Boolean) {
        _isFrozen.value = frozen
        if (!frozen) pushToBroadcast()
    }

    fun toggleFrozen() {
        setFrozen(!_isFrozen.value)
    }

    private fun pushToBroadcast() {
        if (!_isFrozen.value) {
            _broadcastState.value = _readingState.value
        }
    }

    fun setServerStatus(status: ServerStatus) {
        _serverStatus.value = status
    }

    fun setHotspotStatus(status: HotspotStatus) {
        _hotspotStatus.value = status
    }

    fun onClientConnected() {
        _connectedClients.update { it + 1 }
    }

    fun onClientDisconnected() {
        _connectedClients.update { (it - 1).coerceAtLeast(0) }
    }
}
