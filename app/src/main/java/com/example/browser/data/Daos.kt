package com.example.browser.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(historyItem: HistoryItem)

    @Delete
    suspend fun deleteHistory(historyItem: HistoryItem)

    @Query("DELETE FROM history")
    suspend fun deleteAllHistory()
}

@Dao
interface TabDao {
    @Query("SELECT * FROM tabs ORDER BY timestamp ASC")
    fun getAllTabs(): Flow<List<TabItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tabItem: TabItem)

    @Update
    suspend fun updateTab(tabItem: TabItem)

    @Delete
    suspend fun deleteTab(tabItem: TabItem)

    @Query("DELETE FROM tabs")
    suspend fun deleteAllTabs()
}

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY timestamp DESC")
    fun getAllPasswords(): Flow<List<SavedPassword>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: SavedPassword)

    @Delete
    suspend fun deletePassword(password: SavedPassword)

    @Query("DELETE FROM passwords")
    suspend fun deleteAllPasswords()
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY timestamp DESC")
    fun getAllDownloads(): Flow<List<DownloadItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadItem)

    @Update
    suspend fun updateDownload(download: DownloadItem)

    @Delete
    suspend fun deleteDownload(download: DownloadItem)

    @Query("DELETE FROM downloads")
    suspend fun deleteAllDownloads()
}
