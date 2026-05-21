package com.example.browser.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.browser.data.*
import com.example.browser.utils.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("auryx_browser_prefs", Context.MODE_PRIVATE)
    private val database = AppDatabase.getDatabase(application)
    private val repository = DatabaseRepository(database)

    // UI Configuration & Language State
    private val _appLanguage = MutableStateFlow(AppLanguage.EN)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _currentTheme = MutableStateFlow(AuryxTheme.COSMIC)
    val currentTheme: StateFlow<AuryxTheme> = _currentTheme.asStateFlow()

    private val _performanceMode = MutableStateFlow("Balanced") // "Balanced", "Boost", "EXtreme"
    val performanceMode: StateFlow<String> = _performanceMode.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(false)
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // Browsing State
    val bookmarks: StateFlow<List<Bookmark>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryItem>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedPasswords: StateFlow<List<SavedPassword>> = repository.allPasswords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadItem>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tabs: StateFlow<List<TabItem>> = repository.allTabs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Navigation State
    private val _currentUrl = MutableStateFlow("https://google.com")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _searchEngine = MutableStateFlow("Google") // "Google", "DuckDuckGo", "Bing", "Ecosia", "AuryxSafe"
    val searchEngine: StateFlow<String> = _searchEngine.asStateFlow()

    // Privacy Mode (Hidden Tor-Tunnel/Incognito state)
    private val _isAdvancedPrivacyActive = MutableStateFlow(false)
    val isAdvancedPrivacyActive: StateFlow<Boolean> = _isAdvancedPrivacyActive.asStateFlow()

    // Ad blocker counters
    private val _adsBlockedSession = MutableStateFlow(0)
    val adsBlockedSession: StateFlow<Int> = _adsBlockedSession.asStateFlow()

    private val _isAdBlockerActive = MutableStateFlow(true)
    val isAdBlockerActive: StateFlow<Boolean> = _isAdBlockerActive.asStateFlow()

    // Cross-Device Sync State
    private val _syncAccount = MutableStateFlow<String?>(null)
    val syncAccount: StateFlow<String?> = _syncAccount.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncedDevicesCount = MutableStateFlow(3)
    val syncedDevicesCount: StateFlow<Int> = _syncedDevicesCount.asStateFlow()

    // Active Simulated Download Tracker Jobs
    private val downloadJobs = mutableMapOf<Int, Job>()

    init {
        // Load preferences
        val savedLang = sharedPrefs.getString("app_lang", "en") ?: "en"
        _appLanguage.value = AppLanguage.values().firstOrNull { it.code == savedLang } ?: AppLanguage.EN

        val savedTheme = sharedPrefs.getString("app_theme", "COSMIC") ?: "COSMIC"
        _currentTheme.value = AuryxTheme.values().firstOrNull { it.name == savedTheme } ?: AuryxTheme.COSMIC

        _performanceMode.value = sharedPrefs.getString("perf_mode", "Balanced") ?: "Balanced"
        _onboardingCompleted.value = sharedPrefs.getBoolean("onboarding_done", false)
        _isAdBlockerActive.value = sharedPrefs.getBoolean("ad_blocker_active", true)
        _searchEngine.value = sharedPrefs.getString("search_engine", "DuckDuckGo") ?: "DuckDuckGo"
        _syncAccount.value = sharedPrefs.getString("sync_account", null)

        // Seed initial homepage tabs if empty
        viewModelScope.launch {
            repository.allTabs.first().let { currentTabs ->
                if (currentTabs.isEmpty()) {
                    repository.insertTab(TabItem(title = "Google Search", url = "https://google.com", isActive = true))
                    repository.insertTab(TabItem(title = "DuckDuckGo", url = "https://duckduckgo.com", isActive = false))
                }
            }
        }
    }

    // Language / Theme Setters
    fun setLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
        sharedPrefs.edit().putString("app_lang", lang.code).apply()
    }

    fun setTheme(theme: AuryxTheme) {
        _currentTheme.value = theme
        sharedPrefs.edit().putString("app_theme", theme.name).apply()
    }

    fun setPerformanceMode(mode: String) {
        _performanceMode.value = mode
        sharedPrefs.edit().putString("perf_mode", mode).apply()
    }

    fun completeOnboarding() {
        _onboardingCompleted.value = true
        sharedPrefs.edit().putBoolean("onboarding_done", true).apply()
    }

    fun toggleAdBlocker() {
        val next = !_isAdBlockerActive.value
        _isAdBlockerActive.value = next
        sharedPrefs.edit().putBoolean("ad_blocker_active", next).apply()
    }

    fun setSearchEngine(engine: String) {
        _searchEngine.value = engine
        sharedPrefs.edit().putString("search_engine", engine).apply()
    }

    // Navigation and History
    fun navigateTo(url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            if (cleanUrl.contains(".") && !cleanUrl.contains(" ")) {
                cleanUrl = "https://$cleanUrl"
            } else {
                // Perform Search Engine integration
                cleanUrl = when (_searchEngine.value) {
                    "Google" -> "https://google.com/search?q=$cleanUrl"
                    "Bing" -> "https://bing.com/search?q=$cleanUrl"
                    "Ecosia" -> "https://ecosia.org/search?q=$cleanUrl"
                    "AuryxSafe" -> "https://duckduckgo.com/?q=$cleanUrl&t=auryx"
                    else -> "https://duckduckgo.com/?q=$cleanUrl"
                }
            }
        }
        _currentUrl.value = cleanUrl

        // Block ads simulation
        if (_isAdBlockerActive.value) {
            _adsBlockedSession.value += Random.nextInt(2, 6)
        }

        // Write History only if NOT in incognito privacy mode
        if (!_isAdvancedPrivacyActive.value) {
            viewModelScope.launch {
                val title = cleanUrl.removePrefix("https://").removePrefix("www.").substringBefore("/")
                repository.insertHistory(HistoryItem(title = title, url = cleanUrl))
            }
        }
    }

    fun toggleAdvancedPrivacy() {
        _isAdvancedPrivacyActive.value = !_isAdvancedPrivacyActive.value
    }

    // Tabs Manager
    fun addTab(title: String = "New Page", url: String = "https://google.com") {
        viewModelScope.launch {
            // Un-active other tabs
            tabs.value.forEach {
                if (it.isActive) repository.updateTab(it.copy(isActive = false))
            }
            repository.insertTab(TabItem(title = title, url = url, isActive = true))
            _currentUrl.value = url
        }
    }

    fun selectTab(tab: TabItem) {
        viewModelScope.launch {
            tabs.value.forEach {
                val nextActive = it.id == tab.id
                if (nextActive != it.isActive) {
                    repository.updateTab(it.copy(isActive = nextActive))
                }
            }
            _currentUrl.value = tab.url
        }
    }

    fun closeTab(tab: TabItem) {
        viewModelScope.launch {
            repository.deleteTab(tab)
            // If active tab closed, pick another
            if (tab.isActive) {
                val remaining = tabs.value.filter { it.id != tab.id }
                if (remaining.isNotEmpty()) {
                    selectTab(remaining.first())
                } else {
                    addTab("Homepage", "https://google.com")
                }
            }
        }
    }

    // Bookmarks Manager
    fun toggleBookmarkCurrent() {
        viewModelScope.launch {
            val url = _currentUrl.value
            val existing = bookmarks.value.firstOrNull { it.url == url }
            if (existing != null) {
                repository.deleteBookmark(existing)
            } else {
                val title = url.removePrefix("https://").removePrefix("www.").substringBefore("/")
                repository.insertBookmark(Bookmark(title = title, url = url))
            }
        }
    }

    fun addBookmark(title: String, url: String) {
        viewModelScope.launch {
            repository.insertBookmark(Bookmark(title = title, url = url))
        }
    }

    fun removeBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmark)
        }
    }

    fun removeHistoryItem(item: HistoryItem) {
        viewModelScope.launch {
            repository.deleteHistory(item)
        }
    }

    // Password Vault Manager
    fun addPassword(site: String, user: String, pass: String) {
        viewModelScope.launch {
            repository.insertPassword(SavedPassword(siteName = site, username = user, password = pass))
        }
    }

    fun removePassword(password: SavedPassword) {
        viewModelScope.launch {
            repository.deletePassword(password)
        }
    }

    // Synchronization System
    fun setSyncAccount(email: String?) {
        _syncAccount.value = email
        if (email != null) {
            sharedPrefs.edit().putString("sync_account", email).apply()
            // Run a rapid animated Sync state sync-up
            triggerDeviceSync()
        } else {
            sharedPrefs.edit().remove("sync_account").apply()
        }
    }

    fun triggerDeviceSync() {
        if (_syncAccount.value == null) return
        _isSyncing.value = true
        viewModelScope.launch {
            delay(2000) // Beautiful account handshake animation
            _isSyncing.value = false
            _syncedDevicesCount.value = Random.nextInt(3, 6)
        }
    }

    // Clear Cache and Temp Data Action
    fun purgeTemporaryData(onCleared: () -> Unit) {
        viewModelScope.launch {
            // Delete history and tabs
            repository.clearHistory()
            repository.clearTabs()
            // Reset blockers count
            _adsBlockedSession.value = 0
            // Re-seed simple starter tab
            repository.insertTab(TabItem(title = "Google Search", url = "https://google.com", isActive = true))
            _currentUrl.value = "https://google.com"
            _isAdvancedPrivacyActive.value = false
            
            delay(1000) // Delay representation for visuals
            onCleared()
        }
    }

    // Download Engine Simulator (High speed optimized)
    fun requestDownload(fileName: String, url: String) {
        viewModelScope.launch {
            val entity = DownloadItem(
                fileName = fileName,
                url = url,
                progress = 0,
                status = "PENDING",
                totalBytes = Random.nextLong(10_000_000, 150_000_000), // ~10MB to 150MB
                downloadedBytes = 0
            )
            repository.insertDownload(entity)
            
            // Refetch it to get generated ID
            delay(100)
            val insertedItem = repository.allDownloads.first().firstOrNull { it.fileName == fileName }
            if (insertedItem != null) {
                runSimulatedDownloading(insertedItem)
            }
        }
    }

    fun pauseDownload(item: DownloadItem) {
        downloadJobs[item.id]?.cancel()
        downloadJobs.remove(item.id)
        viewModelScope.launch {
            repository.updateDownload(item.copy(status = "PAUSED"))
        }
    }

    fun resumeDownload(item: DownloadItem) {
        runSimulatedDownloading(item)
    }

    fun deleteDownloadItem(item: DownloadItem) {
        downloadJobs[item.id]?.cancel()
        downloadJobs.remove(item.id)
        viewModelScope.launch {
            repository.deleteDownload(item)
        }
    }

    private fun runSimulatedDownloading(item: DownloadItem) {
        val job = viewModelScope.launch {
            var currentBytes = item.downloadedBytes
            val totalBytes = item.totalBytes
            var progress = item.progress

            repository.updateDownload(item.copy(status = "DOWNLOADING"))

            val modeMult = when (_performanceMode.value) {
                "Boost" -> 1.8
                "EXtreme" -> 3.2
                else -> 1.0
            }

            while (progress < 100) {
                delay(400)
                // Random dynamic speed calculation
                val speed = (Random.nextDouble(12.0, 32.0) * modeMult)
                val increment = (speed * 1_024_000 * 0.4).toLong()

                currentBytes = (currentBytes + increment).coerceAtMost(totalBytes)
                progress = ((currentBytes.toDouble() / totalBytes.toDouble()) * 100).toInt()

                val updated = item.copy(
                    progress = progress,
                    status = if (progress >= 100) "COMPLETED" else "DOWNLOADING",
                    downloadedBytes = currentBytes,
                    speedMbps = if (progress >= 100) 0.0 else speed
                )
                repository.updateDownload(updated)
            }
        }
        downloadJobs[item.id] = job
    }

    override fun onCleared() {
        super.onCleared()
        downloadJobs.values.forEach { it.cancel() }
        downloadJobs.clear()
    }
}
