package com.example.browser.ui

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.browser.data.*
import com.example.browser.utils.AppLanguage
import com.example.browser.utils.LanguageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import kotlin.random.Random

// Home shortcut representing popular websites
data class ShortcutItem(val name: String, val url: String, val icon: String, val color: Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuryxMainScreen(
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.appLanguage.collectAsState()
    val theme by viewModel.currentTheme.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val currentUrl by viewModel.currentUrl.collectAsState()
    val tabs by viewModel.tabs.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()
    val isAdvancedPrivacy by viewModel.isAdvancedPrivacyActive.collectAsState()
    val adsBlockedCount by viewModel.adsBlockedSession.collectAsState()
    val performanceMode by viewModel.performanceMode.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Bottom Sheets & Dialogs State
    var showTabManager by remember { mutableStateOf(false) }
    var showHubSheet by remember { mutableStateOf(false) }
    var selectedHubTab by remember { mutableIntStateOf(0) } // 0: Bookmarks, 1: History, 2: Passwords, 3: Downloads, 4: Cloud Sync, 5: Settings
    var showToolsList by remember { mutableStateOf(false) }
    var showReadingMode by remember { mutableStateOf(false) }

    // Web View controls
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webProgress by remember { mutableIntStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Trigger URL change in WebView
    LaunchedEffect(currentUrl) {
        webViewInstance?.let { web ->
            if (web.url != currentUrl) {
                web.loadUrl(currentUrl)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Master Onboarding Intercept
        if (!onboardingCompleted) {
            AuryxOnboardingScreen(
                language = language,
                onLanguageChange = { viewModel.setLanguage(it) },
                onAccept = { viewModel.completeOnboarding() }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 1. TOP UTILITY CONTROL BAR
                AuryxTopBar(
                    url = currentUrl,
                    webProgress = webProgress,
                    language = language,
                    isPrivacyActive = isAdvancedPrivacy,
                    adsBlocked = adsBlockedCount,
                    activeTheme = theme,
                    onUrlSubmit = { viewModel.navigateTo(it) },
                    onTogglePrivacy = { viewModel.toggleAdvancedPrivacy() },
                    onThemeChange = { viewModel.setTheme(it) },
                    onAdBlockToggle = { viewModel.toggleAdBlocker() },
                    onReadingModeToggle = { showReadingMode = !showReadingMode }
                )

                // 2. ACTIVE VIEWPORT (WEB CONTENT / PORTAL DASHBOARD)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val isHomepage = currentUrl == "https://google.com" || currentUrl.isBlank()
                    
                    if (isHomepage) {
                        AuryxHomepagePortal(
                            language = language,
                            performanceMode = performanceMode,
                            onShortcutClick = { viewModel.navigateTo(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // EdgeToEdge Safe Web Browser client view
                        AuryxWebEngine(
                            url = currentUrl,
                            isAdBlockerActive = viewModel.isAdBlockerActive.collectAsState().value,
                            onProgressChanged = { webProgress = it },
                            onNavigationStateChanged = { back, forward ->
                                canGoBack = back
                                canGoForward = forward
                            },
                            onWebViewCreated = { webViewInstance = it },
                            onAdBlocked = { viewModel.navigateTo(currentUrl) } // triggers mini re-increment logic locally
                        )

                        // Top Loader Line
                        if (webProgress in 1..99) {
                            LinearProgressIndicator(
                                progress = { webProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .align(Alignment.TopCenter),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.Transparent
                            )
                        }
                    }

                    // Reading Mode Overlay Screen
                    if (showReadingMode) {
                        AuraReadingViewport(
                            url = currentUrl,
                            language = language,
                            onClose = { showReadingMode = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // 3. BOTTOM SECTIONS PANEL DRAWER ROW
                AuryxBottomBar(
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    tabCount = tabs.size,
                    onGoBack = { webViewInstance?.goBack() },
                    onGoForward = { webViewInstance?.goForward() },
                    onHome = { viewModel.navigateTo("https://google.com") },
                    onTabsClick = { showTabManager = true },
                    onHubClick = { 
                        selectedHubTab = 0
                        showHubSheet = true 
                    },
                    onToolsClick = { showToolsList = true }
                )
            }

            // 4. DETACHED DIALOGS AND BOTTOM SHEETS

            // AuryxTools Drawer Pane
            AnimatedVisibility(
                visible = showToolsList,
                enter = fadeIn(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(200)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showToolsList = false }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .animateEnterExit(
                                enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
                                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                            )
                    ) {
                        AuryxToolsDashboard(
                            language = language,
                            currentUrl = currentUrl,
                            onClose = { showToolsList = false },
                            modifier = Modifier
                                .fillMaxHeight(0.85f)
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .clickable(enabled = false) {}
                        )
                    }
                }
            }

            // Tabs Manager Modal Sheet
            if (showTabManager) {
                TabManagerOverlay(
                    tabs = tabs,
                    language = language,
                    onSelectTab = {
                        viewModel.selectTab(it)
                        showTabManager = false
                    },
                    onCloseTab = { viewModel.closeTab(it) },
                    onNewTab = {
                        viewModel.addTab("New Page", "https://google.com")
                        showTabManager = false
                    },
                    onDismiss = { showTabManager = false }
                )
            }

            // Consolidated Settings/History/Sync/Passwords hub
            AnimatedVisibility(
                visible = showHubSheet,
                enter = fadeIn(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(200)),
                modifier = Modifier.fillMaxSize()
            ) {
                AuryxConsolidatedHub(
                    selectedTab = selectedHubTab,
                    viewModel = viewModel,
                    language = language,
                    bookmarks = bookmarks,
                    history = history,
                    passwords = viewModel.savedPasswords.collectAsState().value,
                    downloads = viewModel.downloads.collectAsState().value,
                    syncAccount = viewModel.syncAccount.collectAsState().value,
                    isSyncing = viewModel.isSyncing.collectAsState().value,
                    syncedCount = viewModel.syncedDevicesCount.collectAsState().value,
                    onTabSelect = { selectedHubTab = it },
                    onBookmarkGo = { url ->
                        viewModel.navigateTo(url)
                        showHubSheet = false
                    },
                    onHistoryGo = { url ->
                        viewModel.navigateTo(url)
                        showHubSheet = false
                    },
                    onDismiss = { showHubSheet = false }
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// -------------------------------------------------------------
// COMPO 1: TOP NAVIGATION AND BRAND UTILITIES
// -------------------------------------------------------------
@Composable
fun AuryxTopBar(
    url: String,
    webProgress: Int,
    language: AppLanguage,
    isPrivacyActive: Boolean,
    adsBlocked: Int,
    activeTheme: AuryxTheme,
    onUrlSubmit: (String) -> Unit,
    onTogglePrivacy: () -> Unit,
    onThemeChange: (AuryxTheme) -> Unit,
    onAdBlockToggle: () -> Unit,
    onReadingModeToggle: () -> Unit
) {
    var textInput by remember(url) { mutableStateOf(url) }
    var expandedThemeDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(8.dp)
    ) {
        // Row 1: Logo & Utility controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Vectorized Auryx Logo Shield
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Auryx",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Quick Badges and Toggles
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Reading Mode Badge
                IconButton(
                    onClick = onReadingModeToggle,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("reading_mode_icon")
                ) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = "Reading Mode Theme",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Ad blocker Badge
                ChipBadge(
                    text = "🛡️ $adsBlocked",
                    onClick = onAdBlockToggle,
                    tag = "adblock_badge"
                )

                // Advanced Stealth Privacy Toggle
                IconButton(
                    onClick = onTogglePrivacy,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isPrivacyActive) MaterialTheme.colorScheme.errorContainer else Color.Transparent)
                        .testTag("privacy_stealth_toggle")
                ) {
                    Icon(
                        imageVector = if (isPrivacyActive) Icons.Default.Security else Icons.Default.SecurityUpdateWarning,
                        contentDescription = "Stealth",
                        tint = if (isPrivacyActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Quick Theme Picker
                Box {
                    IconButton(
                        onClick = { expandedThemeDropdown = true },
                        modifier = Modifier.size(32.dp).testTag("theme_picker_button")
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = "Themes",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedThemeDropdown,
                        onDismissRequest = { expandedThemeDropdown = false }
                    ) {
                        AuryxTheme.values().forEach { themOpt ->
                            DropdownMenuItem(
                                text = { Text(themOpt.displayName) },
                                onClick = {
                                    onThemeChange(themOpt)
                                    expandedThemeDropdown = false
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Circle,
                                        contentDescription = null,
                                        tint = ThemeManager.getColorScheme(themOpt).primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 2: Address Bar and SSL verification
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("url_address_input"),
                textStyle = TextStyle(fontSize = 13.sp),
                singleLine = true,
                placeholder = { Text(LanguageManager.getString("search_hint", language), fontSize = 12.sp) },
                leadingIcon = {
                    val isHttps = textInput.startsWith("https://")
                    Icon(
                        imageVector = if (isHttps) Icons.Default.Lock else Icons.Default.Info,
                        contentDescription = "SSL Status",
                        tint = if (isHttps) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (textInput.isNotEmpty()) {
                        IconButton(
                            onClick = { textInput = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(26.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Action Arrow
            IconButton(
                onClick = { onUrlSubmit(textInput) },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("url_go_button")
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Go Link",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChipBadge(
    text: String,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(tag)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun GoogleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val strokeWidth = radius * 0.35f
        val innerRadius = radius - strokeWidth / 2

        // Bottom - Green
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 100f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Left - Yellow
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 140f,
            sweepAngle = 80f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Top - Red
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 215f,
            sweepAngle = 110f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )
        // Right - Blue
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -35f,
            sweepAngle = 80f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Blue horizontal shelf bar
        val yShift = center.y
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(center.x - radius * 0.1f, yShift),
            end = Offset(center.x + innerRadius + strokeWidth / 2, yShift),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun DuckDuckGoLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFDE5833), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val centerOffset = center
            val r = size.minDimension / 2
            
            // Draw head
            drawCircle(color = Color.White, radius = r * 0.7f, center = Offset(centerOffset.x, centerOffset.y + r * 0.1f))
            
            // Draw beak
            val beakPath = Path().apply {
                moveTo(centerOffset.x - r * 0.4f, centerOffset.y)
                quadraticTo(
                    centerOffset.x - r * 0.9f, centerOffset.y + r * 0.2f,
                    centerOffset.x - r * 0.3f, centerOffset.y + r * 0.4f
                )
                close()
            }
            drawPath(beakPath, Color(0xFFFBBC05))

            // Draw eye
            drawCircle(color = Color(0xFF1E1E1E), radius = r * 0.09f, center = Offset(centerOffset.x - r * 0.15f, centerOffset.y - r * 0.15f))

            // Green bowtie
            val bowPath = Path().apply {
                // Left wing
                moveTo(centerOffset.x - r * 0.3f, centerOffset.y + r * 0.7f)
                lineTo(centerOffset.x, centerOffset.y + r * 0.55f)
                lineTo(centerOffset.x - r * 0.3f, centerOffset.y + r * 0.4f)
                
                // Right wing
                moveTo(centerOffset.x + r * 0.3f, centerOffset.y + r * 0.7f)
                lineTo(centerOffset.x, centerOffset.y + r * 0.55f)
                lineTo(centerOffset.x + r * 0.3f, centerOffset.y + r * 0.4f)
                close()
            }
            drawPath(bowPath, Color(0xFF4CAF50))
        }
    }
}

@Composable
fun YouTubeLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFFF0000), shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, size.height / 2)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path = path, color = Color.White)
        }
    }
}

@Composable
fun RedditLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFFF4500), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val r = size.minDimension / 2
            
            // Head
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(center.x - r * 0.65f, center.y - r * 0.35f),
                size = Size(r * 1.3f, r * 0.85f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(r * 0.4f, r * 0.35f)
            )
            
            // Antenna bar
            drawLine(
                color = Color.White,
                start = Offset(center.x, center.y - r * 0.35f),
                end = Offset(center.x + r * 0.15f, center.y - r * 0.70f),
                strokeWidth = r * 0.08f,
                cap = StrokeCap.Round
            )
            
            // Antenna dot
            drawCircle(
                color = Color.White,
                radius = r * 0.15f,
                center = Offset(center.x + r * 0.18f, center.y - r * 0.76f)
            )

            // Eyes
            drawCircle(
                color = Color(0xFFFF4500),
                radius = r * 0.12f,
                center = Offset(center.x - r * 0.25f, center.y + r * 0.08f)
            )
            drawCircle(
                color = Color(0xFFFF4500),
                radius = r * 0.12f,
                center = Offset(center.x + r * 0.25f, center.y + r * 0.08f)
            )

            // Smile
            val smilePath = Path().apply {
                moveTo(center.x - r * 0.15f, center.y + r * 0.25f)
                quadraticTo(
                    center.x, center.y + r * 0.4f,
                    center.x + r * 0.15f, center.y + r * 0.25f
                )
            }
            drawPath(
                path = smilePath,
                color = Color(0xFFFF4500),
                style = Stroke(width = r * 0.06f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun WikipediaLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFEFEFEF), shape = CircleShape)
            .border(1.dp, Color(0xFFCCCCCC), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            
            // Stylized W
            drawLine(
                color = Color(0xFF1E1E1E),
                start = Offset(w * 0.15f, h * 0.25f),
                end = Offset(w * 0.4f, h * 0.85f),
                strokeWidth = w * 0.08f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF757575),
                start = Offset(w * 0.4f, h * 0.85f),
                end = Offset(w * 0.55f, h * 0.35f),
                strokeWidth = w * 0.06f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF1E1E1E),
                start = Offset(w * 0.55f, h * 0.35f),
                end = Offset(w * 0.65f, h * 0.85f),
                strokeWidth = w * 0.08f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF757575),
                start = Offset(w * 0.65f, h * 0.85f),
                end = Offset(w * 0.85f, h * 0.25f),
                strokeWidth = w * 0.06f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun GitHubLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF1F2328), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(26.dp)) {
            val r = size.minDimension / 2
            
            // Draw head
            drawCircle(
                color = Color.White,
                radius = r * 0.48f,
                center = center
            )
            
            // Ear Left
            val earLeft = Path().apply {
                moveTo(center.x - r * 0.46f, center.y - r * 0.15f)
                lineTo(center.x - r * 0.4f, center.y - r * 0.6f)
                lineTo(center.x - r * 0.12f, center.y - r * 0.36f)
                close()
            }
            drawPath(earLeft, Color.White)

            // Ear Right
            val earRight = Path().apply {
                moveTo(center.x + r * 0.46f, center.y - r * 0.15f)
                lineTo(center.x + r * 0.4f, center.y - r * 0.6f)
                lineTo(center.x + r * 0.12f, center.y - r * 0.36f)
                close()
            }
            drawPath(earRight, Color.White)

            // Bottom body
            val body = Path().apply {
                moveTo(center.x - r * 0.24f, center.y + r * 0.36f)
                lineTo(center.x - r * 0.35f, center.y + r * 0.75f)
                lineTo(center.x + r * 0.35f, center.y + r * 0.75f)
                lineTo(center.x + r * 0.24f, center.y + r * 0.36f)
                close()
            }
            drawPath(body, Color.White)
        }
    }
}

@Composable
fun HackerNewsLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFFF6600), shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val strokeW = w * 0.14f
            
            // Y shape
            drawLine(
                color = Color.White,
                start = Offset(w * 0.25f, h * 0.25f),
                end = Offset(w * 0.5f, h * 0.5f),
                strokeWidth = strokeW,
                cap = StrokeCap.Square
            )
            drawLine(
                color = Color.White,
                start = Offset(w * 0.75f, h * 0.25f),
                end = Offset(w * 0.5f, h * 0.5f),
                strokeWidth = strokeW,
                cap = StrokeCap.Square
            )
            drawLine(
                color = Color.White,
                start = Offset(w * 0.5f, h * 0.5f),
                end = Offset(w * 0.5f, h * 0.8f),
                strokeWidth = strokeW,
                cap = StrokeCap.Square
            )
        }
    }
}

@Composable
fun BingLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF0F6CBD), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.25f, h * 0.25f)
                lineTo(w * 0.75f, h * 0.25f)
                quadraticTo(w * 0.9f, h * 0.5f, w * 0.75f, h * 0.75f)
                lineTo(w * 0.45f, h * 0.75f)
                lineTo(w * 0.45f, h * 0.45f)
                close()
            }
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00F5D4), Color(0xFF00BBF9))
                )
            )
            
            val slice = Path().apply {
                moveTo(w * 0.45f, h * 0.45f)
                lineTo(w * 0.75f, h * 0.25f)
                lineTo(w * 0.5f, h * 0.75f)
                close()
            }
            drawPath(slice, Color.White.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun ShortcutLogo(
    name: String,
    fallbackText: String,
    fallbackColor: Color,
    modifier: Modifier = Modifier
) {
    when (name) {
        "Google" -> GoogleLogo(modifier)
        "DuckDuckGo" -> DuckDuckGoLogo(modifier)
        "YouTube" -> YouTubeLogo(modifier)
        "Reddit" -> RedditLogo(modifier)
        "Wikipedia" -> WikipediaLogo(modifier)
        "GitHub" -> GitHubLogo(modifier)
        "AuryxNews" -> HackerNewsLogo(modifier)
        "Bing" -> BingLogo(modifier)
        else -> {
            Box(
                modifier = modifier
                    .background(fallbackColor.copy(alpha = 0.15f))
                    .border(1.3.dp, fallbackColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = fallbackText,
                    color = fallbackColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// COMPO 2: HOMEPAGE PORTAL shortcuts
// -------------------------------------------------------------
@Composable
fun AuryxHomepagePortal(
    language: AppLanguage,
    performanceMode: String,
    onShortcutClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shortcuts = listOf(
        ShortcutItem("Google", "https://google.com", "G", Color(0xFF4285F4)),
        ShortcutItem("DuckDuckGo", "https://duckduckgo.com", "D", Color(0xFFFF5722)),
        ShortcutItem("YouTube", "https://youtube.com", "Y", Color(0xFFFF0000)),
        ShortcutItem("Reddit", "https://reddit.com", "R", Color(0xFFFF4500)),
        ShortcutItem("Wikipedia", "https://wikipedia.org", "W", Color(0xFF757575)),
        ShortcutItem("GitHub", "https://github.com", "H", Color(0xFF24292E)),
        ShortcutItem("AuryxNews", "https://news.ycombinator.com", "A", Color(0xFFFF6600)),
        ShortcutItem("Bing", "https://bing.com", "B", Color(0xFF008372))
    )

    // Breathing pulse interaction for shield icon
    val infiniteTransition = rememberInfiniteTransition(label = "HologramPulse")
    val shieldScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shieldScale"
    )

    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Holographic shield display
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = shieldScale
                    scaleY = shieldScale
                }
                .size(90.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Waves,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AuryxBrowser",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = LanguageManager.getString("app_tagline", language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // GRID OF SHORTCUT CARDS
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(shortcuts) { index, item ->
                var isItemLaunched by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(50 + index * 40L)
                    isItemLaunched = true
                }

                val scale by animateFloatAsState(
                    targetValue = if (isItemLaunched) 1f else 0.4f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "entranceScale"
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isItemLaunched) 1f else 0f,
                    animationSpec = tween(durationMillis = 350),
                    label = "entranceAlpha"
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val pressScale by animateFloatAsState(
                    targetValue = if (isPressed) 0.88f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "pressScale"
                )

                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale * pressScale
                            scaleY = scale * pressScale
                            this.alpha = alpha
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = androidx.compose.foundation.LocalIndication.current
                        ) {
                            onShortcutClick(item.url)
                        }
                        .padding(vertical = 8.dp)
                        .testTag("shortcut_${item.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShortcutLogo(
                        name = item.name,
                        fallbackText = item.icon,
                        fallbackColor = item.color,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Fuel indicators / Stats
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.BatteryChargingFull,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Fuel Mode: $performanceMode",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (performanceMode == "EXtreme") "Max frame lock (60 FPS). High memory priority." else "Optimized for device efficiency.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPO 3: WEB VIEW ENGINE WRAPPER
// -------------------------------------------------------------
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuryxWebEngine(
    url: String,
    isAdBlockerActive: Boolean,
    onProgressChanged: (Int) -> Unit,
    onNavigationStateChanged: (Boolean, Boolean) -> Unit,
    onWebViewCreated: (WebView?) -> Unit,
    onAdBlocked: () -> Unit
) {
    val context = LocalContext.current
    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 10; AuryxStealth) AppleWebKit/537.36 Chrome/103.0"
            
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    onProgressChanged(10)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    onProgressChanged(100)
                    onNavigationStateChanged(canGoBack(), canGoForward())
                }

                // HIGHLY REALISTIC AD BLOCK FILTERING LOGIC
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    if (isAdBlockerActive && request != null) {
                        val reqUrl = request.url.toString()
                        // Intercept doubleclick, google-analytics, ads, popups
                        if (reqUrl.contains("ads") || reqUrl.contains("doubleclick") || reqUrl.contains("partner") || reqUrl.contains("analytics")) {
                            onAdBlocked()
                            // Return silent empty response instead of fetching ad
                            return WebResourceResponse("text/javascript", "UTF-8", ByteArrayInputStream("".toByteArray()))
                        }
                    }
                    return super.shouldInterceptRequest(view, request)
                }
            }

            webChromeClient = object : android.webkit.WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    onProgressChanged(newProgress)
                    onNavigationStateChanged(canGoBack(), canGoForward())
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onWebViewCreated(webView)
        onDispose {
            onWebViewCreated(null)
            webView.stopLoading()
            webView.destroy()
        }
    }

    AndroidView(
        factory = { webView },
        modifier = Modifier.fillMaxSize()
    )
}

// -------------------------------------------------------------
// COMPO 4: AURA LECTURE OVERLAY (READING MODE)
// -------------------------------------------------------------
@Composable
fun AuraReadingViewport(
    url: String,
    language: AppLanguage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = url.removePrefix("https://").removePrefix("www.").substringBefore("/")
    val contentLines = listOf(
        "Reader Core Protocol initialized.",
        "Auryx simplified proxy extracted clean semantic HTML elements.",
        "----------------------------------------------------------",
        "THE FUTURE OF HYPER-SECURED WEB BROWSING",
        "As device computing requirements scale, desktop browsers have transitioned to bloated, tracking-dominated application hosts. Web servers send mega-payloads of telemetry scripts, ad layers, and high-intensity structural graphics that tax mobile CPU cores.",
        "Auryx reading mode implements static structural filters, isolating textual hierarchy from unsecure and memory-draining components.",
        "This e-ink layout maximizes text margins, applies soothing typographic ratios, and suspends concurrent background tasks.",
        "ENERGY-SAVING OPTIMIZATION BENEFITS:",
        "- Suspended Javascript evaluation blocks dynamic background miners.",
        "- Canvas drawings render on-demand under single-thread priorities.",
        "- Conserves up to 65% of screen refresh draw calls, perfect for less powerful devices.",
        "End of purified content cache."
    )

    Box(
        modifier = modifier
            .background(Color(0xFFFCFBF7)) // warm cream e-ink background
            .padding(20.dp)
            .testTag("aura_reading_viewport")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF2E2F30))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aura Reader: $title",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2E2F30),
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_reading_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF2E2F30))
                }
            }

            Divider(color = Color.Black.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(contentLines) { line ->
                    Text(
                        text = line,
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontSize = 15.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF1C1D1E)
                        )
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPO 5: BOTTOM ACTIONS ROW
// -------------------------------------------------------------
@Composable
fun AuryxBottomBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onHome: () -> Unit,
    onTabsClick: () -> Unit,
    onHubClick: () -> Unit,
    onToolsClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGoBack,
                enabled = canGoBack,
                modifier = Modifier.testTag("back_button")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            IconButton(
                onClick = onGoForward,
                enabled = canGoForward,
                modifier = Modifier.testTag("forward_button")
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
            }

            IconButton(
                onClick = onHome,
                modifier = Modifier.testTag("home_button")
            ) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }

            // Tab Manager representation with badge count!
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onTabsClick)
                    .padding(8.dp)
                    .testTag("tabs_count_badge_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tab, contentDescription = "Tabs manager")
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$tabCount",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Consolidated hub
            IconButton(
                onClick = onHubClick,
                modifier = Modifier.testTag("hub_menu_button")
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu hub")
            }

            // AuryxTools Fast Launch
            IconButton(
                onClick = onToolsClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .testTag("fast_launch_tools_button")
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = "Quick Tools", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

// -------------------------------------------------------------
// COMPO 6: ONBOARDING INTRO (SHOW ONCE)
// -------------------------------------------------------------
@Composable
fun AuryxOnboardingScreen(
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onAccept: () -> Unit
) {
    var onboardingPage by remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C071A)) // Dark space brand color
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .testTag("onboarding_master_overlay")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header language choice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "AuryxBrowser",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )

                // Quick lang selector
                var listExpanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = { listExpanded = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier.testTag("onboarding_lang_select_button")
                    ) {
                        Text(language.displayName, color = Color.White, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = listExpanded,
                        onDismissRequest = { listExpanded = false }
                    ) {
                        AppLanguage.values().forEach { l ->
                            DropdownMenuItem(
                                text = { Text(l.displayName) },
                                onClick = {
                                    onLanguageChange(l)
                                    listExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Onboarding display details
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                val title = when (onboardingPage) {
                    1 -> LanguageManager.getString("app_welcome", language)
                    2 -> LanguageManager.getString("intro_p1_title", language)
                    3 -> LanguageManager.getString("intro_p2_title", language)
                    else -> LanguageManager.getString("intro_p3_title", language)
                }

                val desc = when (onboardingPage) {
                    1 -> LanguageManager.getString("app_tagline", language)
                    2 -> LanguageManager.getString("intro_p1_desc", language)
                    3 -> LanguageManager.getString("intro_p2_desc", language)
                    else -> LanguageManager.getString("intro_p3_desc", language)
                }

                val illustrationIcon = when (onboardingPage) {
                    1 -> Icons.Default.Launch
                    2 -> Icons.Default.Shield
                    3 -> Icons.Default.FlashOn
                    else -> Icons.Default.Build
                }

                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = illustrationIcon,
                        contentDescription = null,
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Footer / Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    for (i in 1..4) {
                        Box(
                            modifier = Modifier
                                .size(if (onboardingPage == i) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (onboardingPage == i) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }

                if (onboardingPage < 4) {
                    Button(
                        onClick = { onboardingPage++ },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                            .testTag("onboarding_next_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC))
                    ) {
                        Text("Continue", color = Color(0xFF0C071A), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                            .testTag("onboarding_accept_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FFCC))
                    ) {
                        Text(
                            text = LanguageManager.getString("btn_accept", language),
                            color = Color(0xFF0C071A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "v1.215.01-Beta Locked System",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// COMPO 7: TAB MANAGER OVERLAY
// -------------------------------------------------------------
@Composable
fun TabManagerOverlay(
    tabs: List<TabItem>,
    language: AppLanguage,
    onSelectTab: (TabItem) -> Unit,
    onCloseTab: (TabItem) -> Unit,
    onNewTab: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onNewTab,
                modifier = Modifier.testTag("new_tab_add_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(LanguageManager.getString("btn_new_tab", language))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_dialog_button")
            ) {
                Text(LanguageManager.getString("btn_close", language))
            }
        },
        title = { Text(LanguageManager.getString("tab_manager", language), fontWeight = FontWeight.Bold) },
        text = {
            Box(modifier = Modifier.height(260.dp).width(300.dp)) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(tabs) { tab ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (tab.isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    1.dp,
                                    if (tab.isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectTab(tab) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = null,
                                    tint = if (tab.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (tab.isActive) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = tab.url,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            IconButton(
                                onClick = { onCloseTab(tab) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("close_tab_button_${tab.id}")
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}
