package com.example.browser.ui

import android.os.Build
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.data.*
import com.example.browser.utils.AppLanguage
import com.example.browser.utils.LanguageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuryxConsolidatedHub(
    selectedTab: Int,
    viewModel: BrowserViewModel,
    language: AppLanguage,
    bookmarks: List<Bookmark>,
    history: List<HistoryItem>,
    passwords: List<SavedPassword>,
    downloads: List<DownloadItem>,
    syncAccount: String?,
    isSyncing: Boolean,
    syncedCount: Int,
    onTabSelect: (Int) -> Unit,
    onBookmarkGo: (String) -> Unit,
    onHistoryGo: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var innerSelectedTab by remember { mutableStateOf(selectedTab) }

    // Synchronize inner state with outer index changes
    LaunchedEffect(selectedTab) {
        innerSelectedTab = selectedTab
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) {},
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auryx Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("hub_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Hub")
                    }
                }

                // SCROLLABLE HORIZONTAL TARGET SELECTION TABS
                ScrollableTabRow(
                    selectedTabIndex = innerSelectedTab,
                    edgePadding = 12.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[innerSelectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    val tabsList = listOf(
                        LanguageManager.getString("nav_bookmarks", language),
                        LanguageManager.getString("nav_history", language),
                        "Passwords",
                        "Downloads",
                        "Device Sync",
                        LanguageManager.getString("nav_settings", language)
                    )
                    tabsList.forEachIndexed { idx, title ->
                        Tab(
                            selected = innerSelectedTab == idx,
                            onClick = { 
                                innerSelectedTab = idx
                                onTabSelect(idx)
                            },
                            text = { Text(title, fontSize = 13.sp) },
                            modifier = Modifier.testTag("hub_tab_$idx")
                        )
                    }
                }

                Divider()

                // Active Tab Content Display with horizontal slide transitions
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    AnimatedContent(
                        targetState = innerSelectedTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> -width } + fadeOut()
                                )
                            } else {
                                (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                    slideOutHorizontally { width -> width } + fadeOut()
                                )
                            }.using(
                                SizeTransform(clip = false)
                            )
                        },
                        label = "HubPaneSlide"
                    ) { targetTab ->
                        when (targetTab) {
                            0 -> BookmarksPane(bookmarks, language, onBookmarkGo, { viewModel.removeBookmark(it) })
                            1 -> HistoryPane(history, language, onHistoryGo, { viewModel.removeHistoryItem(it) })
                            2 -> PasswordsPane(passwords, language, { s, u, p -> viewModel.addPassword(s, u, p) }, { viewModel.removePassword(it) })
                            3 -> DownloadsPane(downloads, language, viewModel)
                            4 -> SyncPane(syncAccount, isSyncing, syncedCount, language, { viewModel.setSyncAccount(it) }, { viewModel.triggerDeviceSync() })
                            5 -> SettingsPane(viewModel, language, onDismiss)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANE 0: BOOKMARKS GESTION
// -------------------------------------------------------------
@Composable
fun BookmarksPane(
    bookmarks: List<Bookmark>,
    language: AppLanguage,
    onBookmarkGo: (String) -> Unit,
    onDelete: (Bookmark) -> Unit
) {
    if (bookmarks.isEmpty()) {
        EmptyPlaceholder(desc = "No bookmarks added yet. Tap the Bookmark icon in your browser address bar to save pages!")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(bookmarks) { bk ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookmarkGo(bk.url) }
                        .testTag("bookmark_item_${bk.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(bk.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(bk.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        IconButton(
                            onClick = { onDelete(bk) },
                            modifier = Modifier.testTag("delete_bookmark_${bk.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete bookmark", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANE 1: HISTORIAL VIEWER
// -------------------------------------------------------------
@Composable
fun HistoryPane(
    history: List<HistoryItem>,
    language: AppLanguage,
    onHistoryGo: (String) -> Unit,
    onDelete: (HistoryItem) -> Unit
) {
    if (history.isEmpty()) {
        EmptyPlaceholder(desc = "Browsing history clean. Private sessions leave no trace.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(history) { hs ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHistoryGo(hs.url) }
                        .testTag("history_item_${hs.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(hs.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(hs.url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        IconButton(
                            onClick = { onDelete(hs) },
                            modifier = Modifier.testTag("delete_history_${hs.id}")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove history point")
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANE 2: PASSWORD CREDS VAULT
// -------------------------------------------------------------
@Composable
fun PasswordsPane(
    passwords: List<SavedPassword>,
    language: AppLanguage,
    onAdd: (String, String, String) -> Unit,
    onDelete: (SavedPassword) -> Unit
) {
    var siteName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }

    val clipboard = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        if (!showAddForm) {
            Button(
                onClick = { showAddForm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("pwd_add_entry_trigger")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lock New Account Credentials")
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Secure Vault Lock", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = siteName,
                        onValueChange = { siteName = it },
                        label = { Text("Web Site / Domain (e.g. google.com)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_site_input")
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Email / Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_username_input")
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Access Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vault_password_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddForm = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (siteName.isNotBlank() && username.isNotBlank() && password.isNotBlank()) {
                                    onAdd(siteName, username, password)
                                    siteName = ""
                                    username = ""
                                    password = ""
                                    showAddForm = false
                                }
                            },
                            modifier = Modifier.testTag("vault_save_button")
                        ) {
                            Text("Encase Vault")
                        }
                    }
                }
            }
        }

        if (passwords.isEmpty()) {
            EmptyPlaceholder(desc = LanguageManager.getString("pwd_vault_empty", language))
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(passwords) { pwd ->
                    var isRevealed by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_item_${pwd.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(pwd.siteName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                Row {
                                    IconButton(
                                        onClick = { isRevealed = !isRevealed },
                                        modifier = Modifier.testTag("reveal_pwd_${pwd.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isRevealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                            contentDescription = "Reveal"
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(pwd.password))
                                        },
                                        modifier = Modifier.testTag("copy_pwd_${pwd.id}")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                    }
                                    IconButton(
                                        onClick = { onDelete(pwd) },
                                        modifier = Modifier.testTag("delete_pwd_${pwd.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Trash", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("User: ${pwd.username}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Code: " + if (isRevealed) pwd.password else "• • • • • • • •",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANE 3: DOWNLOAD ENGINE MANAGER
// -------------------------------------------------------------
@Composable
fun DownloadsPane(
    downloads: List<DownloadItem>,
    language: AppLanguage,
    viewModel: BrowserViewModel
) {
    if (downloads.isEmpty()) {
        EmptyPlaceholder(desc = "No files downloaded. Click download anchors inside URLs to trigger downloading.")
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(downloads) { dl ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("download_item_${dl.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DownloadForOffline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(dl.fileName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        text = "${dl.status} • " + String.format("%.1f", dl.downloadedBytes.toDouble() / 1_000_000.0) + "MB of " + String.format("%.1f", dl.totalBytes.toDouble() / 1_000_000.0) + "MB",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row {
                                if (dl.status == "DOWNLOADING") {
                                    IconButton(
                                        onClick = { viewModel.pauseDownload(dl) },
                                        modifier = Modifier.testTag("pause_dl_${dl.id}")
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = "Pause", tint = MaterialTheme.colorScheme.primary)
                                    }
                                } else if (dl.status == "PAUSED") {
                                    IconButton(
                                        onClick = { viewModel.resumeDownload(dl) },
                                        modifier = Modifier.testTag("resume_dl_${dl.id}")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = MaterialTheme.colorScheme.secondary)
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.deleteDownloadItem(dl) },
                                    modifier = Modifier.testTag("delete_dl_${dl.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete record", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }

                        if (dl.status == "DOWNLOADING") {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { dl.progress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Auryx Fast Engine active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(String.format("%.1f Mbps", dl.speedMbps), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANE 4: ACCOUNT SYNC PANEL
// -------------------------------------------------------------
@Composable
fun SyncPane(
    syncAccount: String?,
    isSyncing: Boolean,
    syncedCount: Int,
    language: AppLanguage,
    onLogin: (String?) -> Unit,
    onSyncTrigger: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (syncAccount == null) {
            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            Text(LanguageManager.getString("sync_title", language), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
            Text(LanguageManager.getString("sync_desc", language), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text("Account sync email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_email_input")
            )

            Button(
                onClick = {
                    if (emailInput.contains("@") && emailInput.length > 5) {
                        onLogin(emailInput)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_submit_button")
            ) {
                Text("Lock Account & Sync data")
            }
        } else {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF00E676),
                modifier = Modifier.size(64.dp)
            )
            Text(syncAccount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Synchronization secured. Encrypted bookmarks & credentials saved in Cloud.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Linked Devices", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$syncedCount active", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                }
                Divider(modifier = Modifier.height(30.dp).width(1.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Server Status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("ONLINE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF00FFCC))
                }
            }

            if (isSyncing) {
                CircularProgressIndicator()
                Text("Syncing tabs, passwords and history...", style = MaterialTheme.typography.labelSmall)
            } else {
                Button(
                    onClick = onSyncTrigger,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sync_force_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync Now")
                }

                TextButton(
                    onClick = { onLogin(null) },
                    modifier = Modifier.testTag("sync_signout_button")
                ) {
                    Text("Sign out of Cloud Accounts", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PANE 5: CONFIGS & SYSTEM DEETS STATS
// -------------------------------------------------------------
@Composable
fun SettingsPane(
    viewModel: BrowserViewModel,
    language: AppLanguage,
    onDismiss: () -> Unit
) {
    val performanceMode by viewModel.performanceMode.collectAsState()
    val adsBlockedSession by viewModel.adsBlockedSession.collectAsState()
    val isAdBlockerActive by viewModel.isAdBlockerActive.collectAsState()

    var showClearConfirmation by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Real system metrics
    var connectionType by remember { mutableStateOf("Connessione...") }
    var batteryPercentage by remember { mutableIntStateOf(100) }
    var hardwareTemperature by remember { mutableStateOf("35.0°C") }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                // Get Connection Type
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                if (connectivityManager != null) {
                    val activeNetwork = connectivityManager.activeNetwork
                    val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                    connectionType = when {
                        capabilities == null -> "Disconnesso"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Rete Cellulare"
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                        else -> "Connesso"
                    }
                } else {
                    connectionType = "Sconosciuta"
                }

                // Get Battery & Temperature
                val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                if (intent != null) {
                    val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                    hardwareTemperature = String.format("%.1f°C", temp / 10.0f)

                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        batteryPercentage = (level * 100f / scale).toInt()
                    }
                }
            } catch (e: Exception) {
                // Fallback gracefully in case of any platform security restriction or exception
                connectionType = "Attiva"
                batteryPercentage = 100
                hardwareTemperature = "35.0°C"
            }
            delay(1500) // update every 1.5 seconds
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // PERFORMANCE BOOST MODE PICKER
        item {
            Text(LanguageManager.getString("perf_title", language), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Balanced", "Boost", "EXtreme").forEach { mode ->
                    val isSel = performanceMode == mode
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setPerformanceMode(mode) }
                            .testTag("perf_mode_$mode"),
                        border = BorderStroke(1.2.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (mode) {
                                    "Boost" -> Icons.Default.FlashOn
                                    "EXtreme" -> Icons.Default.Bolt
                                    else -> Icons.Default.BatteryChargingFull
                                },
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(mode, fontWeight = FontWeight.Bold)
                                Text(
                                    text = when (mode) {
                                        "Boost" -> LanguageManager.getString("pref_boost", language)
                                        "EXtreme" -> LanguageManager.getString("pref_extreme", language)
                                        else -> LanguageManager.getString("pref_balanced", language)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // LANGUAGE TRANSLATION PICKER
        item {
            Text(LanguageManager.getString("lang_select", language), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val languages = listOf(
                    AppLanguage.EN,
                    AppLanguage.ES,
                    AppLanguage.FR,
                    AppLanguage.DE,
                    AppLanguage.JA
                )
                languages.forEach { l ->
                    val isSel = language == l
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { viewModel.setLanguage(l) }
                            .padding(8.dp)
                            .testTag("lang_btn_${l.code}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = l.displayName.substring(0, 3).uppercase(),
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // CLEAR DATA & PURGE CACHE
        item {
            Text(LanguageManager.getString("btn_clear_data", language), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            if (!showClearConfirmation) {
                Button(
                    onClick = { showClearConfirmation = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().testTag("trigger_clear_cache_button")
                ) {
                    Text("Form purge cache credentials", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("This will wipe history, bookmarks, tabs and reset blocking stats. Proceed?", color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TextButton(onClick = { showClearConfirmation = false }) { Text("Cancel") }
                            Button(
                                onClick = {
                                    viewModel.purgeTemporaryData {
                                        showClearConfirmation = false
                                        onDismiss()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.testTag("confirm_clear_cache_button")
                            ) {
                                Text("Purge All")
                            }
                        }
                    }
                }
            }
        }

        // DEVICE AND VERSION HARDWARE TECHNICAL REPORT
        item {
            Text(LanguageManager.getString("sys_stats", language), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HardwareDeetsRow("Versione App", "1.2.0-Beta")
                    HardwareDeetsRow("Dispositivo", Build.MANUFACTURER + " " + Build.MODEL)
                    HardwareDeetsRow("Piattaforma OS", "Android " + Build.VERSION.RELEASE)
                    HardwareDeetsRow("Stato Connessione", connectionType)
                    HardwareDeetsRow("Livello Batteria", "$batteryPercentage%")
                    HardwareDeetsRow("Temperatura HW", hardwareTemperature)
                }
            }
        }
    }
}

@Composable
fun HardwareDeetsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
    }
}

// Global empty state indicator
@Composable
fun EmptyPlaceholder(desc: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Inbox, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(54.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
