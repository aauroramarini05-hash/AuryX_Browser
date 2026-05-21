package com.example.browser.data

import kotlinx.coroutines.flow.Flow

class DatabaseRepository(private val db: AppDatabase) {

    // Bookmarks
    val allBookmarks: Flow<List<Bookmark>> = db.bookmarkDao().getAllBookmarks()
    suspend fun insertBookmark(bookmark: Bookmark) = db.bookmarkDao().insertBookmark(bookmark)
    suspend fun deleteBookmark(bookmark: Bookmark) = db.bookmarkDao().deleteBookmark(bookmark)
    suspend fun clearBookmarks() = db.bookmarkDao().deleteAllBookmarks()

    // History
    val allHistory: Flow<List<HistoryItem>> = db.historyDao().getAllHistory()
    suspend fun insertHistory(historyItem: HistoryItem) = db.historyDao().insertHistory(historyItem)
    suspend fun deleteHistory(historyItem: HistoryItem) = db.historyDao().deleteHistory(historyItem)
    suspend fun clearHistory() = db.historyDao().deleteAllHistory()

    // Tabs
    val allTabs: Flow<List<TabItem>> = db.tabDao().getAllTabs()
    suspend fun insertTab(tabItem: TabItem) = db.tabDao().insertTab(tabItem)
    suspend fun updateTab(tabItem: TabItem) = db.tabDao().updateTab(tabItem)
    suspend fun deleteTab(tabItem: TabItem) = db.tabDao().deleteTab(tabItem)
    suspend fun clearTabs() = db.tabDao().deleteAllTabs()

    // Passwords
    val allPasswords: Flow<List<SavedPassword>> = db.passwordDao().getAllPasswords()
    suspend fun insertPassword(password: SavedPassword) = db.passwordDao().insertPassword(password)
    suspend fun deletePassword(password: SavedPassword) = db.passwordDao().deletePassword(password)
    suspend fun clearPasswords() = db.passwordDao().deleteAllPasswords()

    // Downloads
    val allDownloads: Flow<List<DownloadItem>> = db.downloadDao().getAllDownloads()
    suspend fun insertDownload(download: DownloadItem) = db.downloadDao().insertDownload(download)
    suspend fun updateDownload(download: DownloadItem) = db.downloadDao().updateDownload(download)
    suspend fun deleteDownload(download: DownloadItem) = db.downloadDao().deleteDownload(download)
    suspend fun clearDownloads() = db.downloadDao().deleteAllDownloads()
}
