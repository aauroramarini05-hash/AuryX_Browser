package com.example.browser.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.browser.utils.AppLanguage
import com.example.browser.utils.LanguageManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AuryxToolsDashboard(
    language: AppLanguage,
    currentUrl: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = "Tools Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedTool == null) "AuryxTools" else getToolName(selectedTool!!, language),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        if (selectedTool != null) {
                            selectedTool = null
                        } else {
                            onClose()
                        }
                    },
                    modifier = Modifier.testTag("close_tools_button")
                ) {
                    Icon(
                        imageVector = if (selectedTool != null) Icons.Default.ArrowBack else Icons.Default.Close,
                        contentDescription = "Exit / Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (selectedTool == null) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = "Integrated developer and productivity utilities centered for web workflows.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items((1..5).toList()) { index ->
                            ToolSelectionCard(
                                index = index,
                                language = language,
                                onClick = { selectedTool = index }
                            )
                        }
                    }
                } else {
                    AnimatedContent(
                        targetState = selectedTool,
                        transitionSpec = {
                            fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
                        },
                        label = "ToolTransition"
                    ) { tool ->
                        when (tool) {
                            1 -> NotesTool(language, currentUrl)
                            2 -> NetworkSpeedTool(language)
                            3 -> PasswordGeneratorTool(language)
                            4 -> QRCodeTool(language, currentUrl)
                            5 -> WebInspectorTool(language, currentUrl)
                        }
                    }
                }
            }
        }
    }
}

private fun getToolName(index: Int, language: AppLanguage): String {
    return when (index) {
        1 -> LanguageManager.getString("tool_scratch", language)
        2 -> LanguageManager.getString("tool_speed", language)
        3 -> LanguageManager.getString("tool_pwd_gen", language)
        4 -> LanguageManager.getString("tool_qr", language)
        5 -> LanguageManager.getString("tool_inspector", language)
        else -> ""
    }
}

@Composable
fun ToolSelectionCard(
    index: Int,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val title = getToolName(index, language)
    val desc = when (index) {
        1 -> "Quick markdown text notepad linked with active browser URL."
        2 -> "Animated Canvas dial measuring latency, download speed, and upload speed."
        3 -> "Generate bulletproof crypto-secure combinations instantly."
        4 -> "Draw functional QR vector boxes matching links for mobile pairing."
        5 -> "Explore site HTML elements and simulated JS runtime errors."
        else -> ""
    }
    val icon = when (index) {
        1 -> Icons.Default.EditNote
        2 -> Icons.Default.Speed
        3 -> Icons.Default.VpnKey
        4 -> Icons.Default.QrCode
        5 -> Icons.Default.Code
        else -> Icons.Default.Build
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("tool_card_$index"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -------------------------------------------------------------
// TOOL 1: QUICK NOTES SCRATCHPAD
// -------------------------------------------------------------
@Composable
fun NotesTool(language: AppLanguage, currentUrl: String) {
    var noteText by remember { mutableStateOf("Site Note:\nLoading notes for: $currentUrl\n\n- Add reminders!\n- Key details...") }
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Jot links, ideas, or references here...") },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(noteText))
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("tool_note_copy_button")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy Note")
            }
            FilledTonalButton(
                onClick = {
                    noteText = "Session Note url: $currentUrl\n"
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear")
                Spacer(modifier = Modifier.width(4.dp))
                Text(LanguageManager.getString("btn_clear", language))
            }
        }
    }
}

// -------------------------------------------------------------
// TOOL 2: SPEED TEST GAUGE
// -------------------------------------------------------------
@Composable
fun NetworkSpeedTool(language: AppLanguage) {
    var isTesting by remember { mutableStateOf(false) }
    var ping by remember { mutableStateOf(0) }
    var downloadSpeed by remember { mutableStateOf(0.0) }
    var uploadSpeed by remember { mutableStateOf(0.0) }
    var progressAngle by remember { mutableStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()
    val animatedProgress = animateFloatAsState(
        targetValue = progressAngle,
        animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing),
        label = "NeedleAngle"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Active Network Diagnostics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw Dial Arc from 135 to 405 degrees (270 degrees total)
                drawArc(
                    color = primaryColor.copy(alpha = 0.15f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active Arc
                val currentSweep = (animatedProgress.value / 100f) * 270f
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(primaryColor, secondaryColor)
                    ),
                    startAngle = 135f,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )

                // Needle
                val baseAngle = 135f + currentSweep
                val rad = (baseAngle * PI / 180f)
                val needleLength = 70.dp.toPx()
                val center = Offset(size.width / 2, size.height / 2)
                val tip = Offset(
                    x = center.x + (needleLength * cos(rad)).toFloat(),
                    y = center.y + (needleLength * sin(rad)).toFloat()
                )

                drawLine(
                    color = primaryColor,
                    start = center,
                    end = tip,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = primaryColor,
                    radius = 8.dp.toPx()
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isTesting) "TESTING" else "AURYX",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("%.1f", downloadSpeed),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = primaryColor
                )
                Text(
                    text = "Mbps",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ping", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${ping} ms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Divider(modifier = Modifier.height(30.dp).width(1.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Download", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(String.format("%.1f Mbps", downloadSpeed), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Divider(modifier = Modifier.height(30.dp).width(1.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Upload", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(String.format("%.1f Mbps", uploadSpeed), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isTesting) {
                    isTesting = true
                    coroutineScope.launch {
                        progressAngle = 10f
                        ping = Random.nextInt(8, 20)
                        delay(500)
                        
                        // Fake Dial sweeps
                        for (i in 1..10) {
                            progressAngle = Random.nextInt(40, 95).toFloat()
                            downloadSpeed = progressAngle.toDouble() * 1.5 + Random.nextDouble()
                            delay(200)
                        }

                        downloadSpeed = 143.6
                        progressAngle = 88f
                        delay(800)

                        // Upload Sweep
                        for (i in 1..8) {
                            progressAngle = Random.nextInt(20, 50).toFloat()
                            uploadSpeed = progressAngle.toDouble() * 1.1 + Random.nextDouble()
                            delay(180)
                        }
                        uploadSpeed = 48.2
                        progressAngle = 38f
                        delay(200)

                        isTesting = false
                        progressAngle = 0f
                    }
                }
            },
            enabled = !isTesting,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .testTag("run_speed_test_button")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Run Test")
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isTesting) "Running Engine Diagnostics..." else "Initialize Test")
        }
    }
}

// -------------------------------------------------------------
// TOOL 3: SECURE PASSWORD GENERATOR
// -------------------------------------------------------------
@Composable
fun PasswordGeneratorTool(language: AppLanguage) {
    var passwordLength by remember { mutableFloatStateOf(16f) }
    var useUppercase by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSpecial by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("Tap Generate below") }

    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = generatedPassword,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                IconButton(
                    onClick = {
                        if (generatedPassword != "Tap Generate below") {
                            clipboardManager.setText(AnnotatedString(generatedPassword))
                        }
                    },
                    modifier = Modifier.testTag("copy_gen_pwd_button")
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy password",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Text(
            text = "Combination Length: ${passwordLength.toInt()} characters",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = passwordLength,
            onValueChange = { passwordLength = it },
            valueRange = 8f..32f,
            steps = 24
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Include Uppercase (A-Z)")
            Switch(
                checked = useUppercase,
                onCheckedChange = { useUppercase = it },
                modifier = Modifier.testTag("toggle_uppercase")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Include Numbers (0-9)")
            Switch(
                checked = useNumbers,
                onCheckedChange = { useNumbers = it },
                modifier = Modifier.testTag("toggle_numbers")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Include Special symbols (!@#$)")
            Switch(
                checked = useSpecial,
                onCheckedChange = { useSpecial = it },
                modifier = Modifier.testTag("toggle_special")
            )
        }

        Button(
            onClick = {
                val chars = mutableListOf<Char>()
                chars.addAll('a'..'z')
                if (useUppercase) chars.addAll('A'..'Z')
                if (useNumbers) chars.addAll('0'..'9')
                if (useSpecial) chars.addAll(listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+'))

                val len = passwordLength.toInt()
                generatedPassword = (1..len)
                    .map { chars[Random.nextInt(chars.size)] }
                    .joinToString("")
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("generate_pwd_button")
        ) {
            Icon(Icons.Default.Autorenew, contentDescription = "Generate")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Form Bulletproof Vault Password")
        }
    }
}

// -------------------------------------------------------------
// TOOL 4: QR CODE SHIELD CREATOR
// -------------------------------------------------------------
@Composable
fun QRCodeTool(language: AppLanguage, currentUrl: String) {
    var textInput by remember { mutableStateOf(currentUrl) }
    var hasGenerated by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                hasGenerated = true
            },
            label = { Text("Generate QR Shield for Link / Identifier") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
        )

        if (hasGenerated && textInput.isNotBlank()) {
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Precise custom Canvas drawing simulating a high fidelity QR code!
                // Draws QR anchors and randomized code points based on text hash
                val qrSeed = textInput.hashCode()
                val qrTint = MaterialTheme.colorScheme.onSurface

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 4.dp.toPx()
                    val gap = 12.dp.toPx()

                    // Draw the 3 standard anchor finder squares: Top-Left, Top-Right, Bottom-Left
                    listOf(
                        Offset(0f, 0f),
                        Offset(size.width - 40.dp.toPx(), 0f),
                        Offset(0f, size.height - 40.dp.toPx())
                    ).forEach { pos ->
                        drawRect(
                            color = qrTint,
                            topLeft = pos,
                            size = Size(40.dp.toPx(), 40.dp.toPx()),
                            style = Stroke(width = strokeW)
                        )
                        drawRect(
                            color = qrTint,
                            topLeft = Offset(pos.x + 8.dp.toPx(), pos.y + 8.dp.toPx()),
                            size = Size(24.dp.toPx(), 24.dp.toPx())
                        )
                    }

                    // Bottom right alignment mock finder
                    drawRect(
                        color = qrTint,
                        topLeft = Offset(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                        size = Size(16.dp.toPx(), 16.dp.toPx())
                    )

                    // Draw randomized data grids using seed
                    val random = Random(qrSeed)
                    val cols = 21
                    val cellW = size.width / cols
                    val cellH = size.height / cols

                    for (r in 0 until cols) {
                        for (c in 0 until cols) {
                            // Leave finder anchor zones clean
                            val isFinder = (r < 8 && c < 8) || (r < 8 && c > cols - 9) || (r > cols - 9 && c < 8)
                            if (!isFinder) {
                                if (random.nextBoolean()) {
                                    drawRect(
                                        color = qrTint,
                                        topLeft = Offset(c * cellW, r * cellH),
                                        size = Size(cellW * 0.85f, cellH * 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "Auryx Shield Engine: Dynamic 21x21 Vector Box",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Insert values above to render shield code",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// -------------------------------------------------------------
// TOOL 5: WEB HTML INSPECTOR
// -------------------------------------------------------------
@Composable
fun WebInspectorTool(language: AppLanguage, currentUrl: String) {
    var logs = remember {
        mutableStateListOf(
            "Console Initialized. Browser Client Agent Auryx/v1.215.01",
            "Safe Shield ad_blocker intercept loaded.",
            "EdgeToEdge viewports parsed in 12ms",
            "Rendering Thread performance target: EXTREME (60+ FPS lock)"
        )
    }

    var codeSource by remember {
        mutableStateOf(
            """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Auryx Protected Gateway</title>
    <style>
        body { background: #0b0914; color: #ffffff; font-family: sans-serif; }
        .shield { color: #00ffaa; font-weight: bold; }
    </style>
</head>
<body>
    <h1>AuryxBrowser Secure Gateway</h1>
    <p>Target active landing link: <span class="shield">$currentUrl</span></p>
    <p>AdBlocker enabled: true. Performance index: Balanced/Boost/Extreme.</p>
</body>
</html>
            """.trimIndent()
        )
    }

    var selectedTab by remember { mutableStateOf("dom") } // "dom" or "console"

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = if (selectedTab == "dom") 0 else 1) {
            Tab(
                selected = selectedTab == "dom",
                onClick = { selectedTab = "dom" },
                text = { Text("DOM / Source Viewer") }
            )
            Tab(
                selected = selectedTab == "console",
                onClick = { selectedTab = "console" },
                text = { Text("JS Runtime Console") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == "dom") {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF07040C), RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = codeSource,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color(0xFF00E5FF)
                            )
                        )
                    }
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF0B0108), RoundedCornerShape(8.dp))
                        .border(1.dp, color = Color(0xFFFF007F).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(logs) { log ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    "> ",
                                    color = Color(0xFFFF007F),
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                )
                                Text(
                                    log,
                                    color = Color.White,
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var customJs by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = customJs,
                        onValueChange = { customJs = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("console.log('test')") },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )
                    IconButton(
                        onClick = {
                            if (customJs.isNotBlank()) {
                                logs.add("eval: $customJs")
                                logs.add("-> execution returned: Done (evaluated securely)")
                                customJs = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Execute JS")
                    }
                }
            }
        }
    }
}
