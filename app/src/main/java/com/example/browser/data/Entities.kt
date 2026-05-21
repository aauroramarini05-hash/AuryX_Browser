package com.example.browser.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tabs")
data class TabItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val url: String,
    val isActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "passwords")
data class SavedPassword(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val siteName: String,
    val username: String,
    val password: String, // Stored safely offline
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val url: String,
    val progress: Int = 0, // 0 to 100
    val status: String, // "PENDING", "DOWNLOADING", "PAUSED", "COMPLETED", "FAILED"
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val speedMbps: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
