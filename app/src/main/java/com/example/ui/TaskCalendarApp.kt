@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)
package com.example.ui

import android.widget.Toast
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.ViewGroup
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.text.selection.SelectionContainer
import com.example.data.EmailCampaign
import com.example.data.PlaywrightJob
import com.example.data.SecurityLog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCalendarApp(viewModel: TaskViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Auth0, 2: API Client, 3: Emails, 4: Playwright

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val securityLogs by viewModel.securityLogs.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent, // Managed by gradient background
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "D",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "DabelsTech AuthApp",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "DabelsTech Auth Console", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("nav_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Shield",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable {
                                Toast
                                    .makeText(context, "Logged in as $userName", Toast.LENGTH_SHORT)
                                    .show()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(2).uppercase(),
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = ElegantBottomNavColor,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .testTag("bottom_nav_bar")
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, ElegantBorderColor.copy(alpha = 0.3f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard Hub"
                        )
                    },
                    label = { Text("Dashboard") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 1) Icons.Default.VpnKey else Icons.Outlined.VpnKey,
                            contentDescription = "Auth0 Portal"
                        )
                    },
                    label = { Text("Auth0") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("nav_auth0")
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 2) Icons.Default.Http else Icons.Outlined.Http,
                            contentDescription = "API Client"
                        )
                    },
                    label = { Text("API Client") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("nav_api")
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 3) Icons.Default.Mail else Icons.Outlined.Mail,
                            contentDescription = "Email Automation"
                        )
                    },
                    label = { Text("Emails") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("nav_emails")
                )
                NavigationBarItem(
                    selected = currentTab == 4,
                    onClick = { currentTab = 4 },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == 4) Icons.Default.Terminal else Icons.Outlined.Terminal,
                            contentDescription = "Playwright Jobs"
                        )
                    },
                    label = { Text("Playwright") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        indicatorColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.testTag("nav_playwright")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkSlateBg, Color(0xFF141316))
                    )
                )
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "tab_animation"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> DashboardTabScreen(viewModel = viewModel)
                    1 -> Auth0TabScreen(viewModel = viewModel)
                    2 -> ApiClientTabScreen(viewModel = viewModel)
                    3 -> EmailAutomationTabScreen(viewModel = viewModel)
                    4 -> PlaywrightTabScreen(viewModel = viewModel)
                }
            }
        }
    }
}

// ==========================================
// TAB: OPERATIONS DASHBOARD (LANDING PAGE)
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardTabScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    
    val isEmailConnected by viewModel.isEmailConnected.collectAsStateWithLifecycle()
    val connectedEmailAddress by viewModel.connectedEmailAddress.collectAsStateWithLifecycle()
    val isConnectingEmail by viewModel.isConnectingEmail.collectAsStateWithLifecycle()
    val syncedEmails by viewModel.syncedEmails.collectAsStateWithLifecycle()

    val isGoogleDriveConnected by viewModel.isGoogleDriveConnected.collectAsStateWithLifecycle()
    val driveUserEmail by viewModel.driveUserEmail.collectAsStateWithLifecycle()
    val isConnectingDrive by viewModel.isConnectingDrive.collectAsStateWithLifecycle()
    val driveFiles by viewModel.driveFiles.collectAsStateWithLifecycle()

    val playwrightSummary by viewModel.playwrightSummary.collectAsStateWithLifecycle()
    val isGeneratingSummary by viewModel.isGeneratingSummary.collectAsStateWithLifecycle()
    val playwrightJobs by viewModel.playwrightJobs.collectAsStateWithLifecycle()

    var showEmailConnectDialog by remember { mutableStateOf(false) }
    var showDriveConnectDialog by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("dabelstech@moredesa.com") }
    var driveEmailInput by remember { mutableStateOf("dabelstech@moredesa.com") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Welcome Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "WELCOME BACK,",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DabelsTech Operations Hub",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            letterSpacing = (-0.5).sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Integrate tools, configure Universal Auth0 endpoints, and leverage Gemini AI to summarize and prioritize E2E workloads.",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    )
                }
            }
        }

        // CONNECTED APPLICATIONS Section
        item {
            Text(
                text = "WORKPLACE INTEGRATIONS HUB",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Email Connector Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (isEmailConnected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else ElegantBorderColor,
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isEmailConnected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mail,
                                    contentDescription = "Email Connect",
                                    tint = if (isEmailConnected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emails App",
                                style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            )
                        }

                        if (isEmailConnected) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Connected",
                                    style = TextStyle(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                )
                                Text(
                                    text = connectedEmailAddress,
                                    style = TextStyle(color = TextSecondaryDark, fontSize = 11.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${syncedEmails.size} recent messages synced",
                                    style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp)
                                )
                            }
                            Button(
                                onClick = { viewModel.disconnectEmails() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            ) {
                                Text("Disconnect", style = TextStyle(color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Disconnected",
                                    style = TextStyle(color = TextSecondaryDark, fontSize = 12.sp)
                                )
                                Text(
                                    text = "Connect SMTP/IMAP workspace email",
                                    style = TextStyle(color = TextSecondaryDark.copy(alpha = 0.6f), fontSize = 10.sp),
                                    lineHeight = 14.sp
                                )
                            }
                            Button(
                                onClick = { showEmailConnectDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            ) {
                                Text("Connect App", style = TextStyle(color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                // Google Drive Connector Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (isGoogleDriveConnected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) else ElegantBorderColor,
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isGoogleDriveConnected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = "Drive Connect",
                                    tint = if (isGoogleDriveConnected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Google Drive",
                                style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            )
                        }

                        if (isGoogleDriveConnected) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Connected",
                                    style = TextStyle(color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                )
                                Text(
                                    text = driveUserEmail,
                                    style = TextStyle(color = TextSecondaryDark, fontSize = 11.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${driveFiles.size} drive files synced",
                                    style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp)
                                )
                            }
                            Button(
                                onClick = { viewModel.disconnectGoogleDrive() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            ) {
                                Text("Disconnect", style = TextStyle(color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Disconnected",
                                    style = TextStyle(color = TextSecondaryDark, fontSize = 12.sp)
                                )
                                Text(
                                    text = "Sync Google Drive folder structures",
                                    style = TextStyle(color = TextSecondaryDark.copy(alpha = 0.6f), fontSize = 10.sp),
                                    lineHeight = 14.sp
                                )
                            }
                            Button(
                                onClick = { showDriveConnectDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            ) {
                                Text("Connect Drive", style = TextStyle(color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // Expanded integrations data views
        if (isEmailConnected || isGoogleDriveConnected) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElegantBorderColor, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "LIVE SYNCHRONIZATION DATA",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.5.sp
                            )
                        )

                        if (isEmailConnected) {
                            Text(
                                text = "Recent Emails (IMAP Workspace)",
                                style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            )
                            syncedEmails.forEach { msg ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (msg.isRead) Icons.Default.Drafts else Icons.Default.Mail,
                                        contentDescription = null,
                                        tint = if (msg.isRead) TextSecondaryDark else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = msg.sender, style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                        Text(text = msg.subject, style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Text(text = msg.date, style = TextStyle(color = TextSecondaryDark, fontSize = 9.sp))
                                }
                            }
                        }

                        if (isGoogleDriveConnected) {
                            if (isEmailConnected) {
                                Divider(color = ElegantBorderColor.copy(alpha = 0.5f), thickness = 1.dp)
                            }
                            Text(
                                text = "Recent Drive Files (OAuth Cloud)",
                                style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            )
                            driveFiles.forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (file.mimeType) {
                                            "Document" -> Icons.Default.Description
                                            "Spreadsheet" -> Icons.Default.TableChart
                                            "PDF" -> Icons.Default.PictureAsPdf
                                            else -> Icons.Default.FolderOpen
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = file.name, style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 11.sp))
                                        Text(text = "${file.mimeType} • ${file.size}", style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp))
                                    }
                                    Text(text = file.lastModified, style = TextStyle(color = TextSecondaryDark, fontSize = 9.sp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // GEMINI AI INTEGRATION Section
        item {
            Text(
                text = "GEMINI AI AUTOMATION INTELLIGENCE",
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini Spark",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Playwright E2E Workload Analyst",
                                    style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                )
                                Text(
                                    text = "Powered by Gemini 3.5 Flash",
                                    style = TextStyle(color = TextSecondaryDark, fontSize = 11.sp)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.generatePlaywrightSummaryWithGemini() },
                            enabled = !isGeneratingSummary && playwrightJobs.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            if (isGeneratingSummary) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.QueryStats, contentDescription = "Run analysis", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyze", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (playwrightSummary.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                                .border(1.dp, ElegantBorderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = playwrightSummary,
                                    style = TextStyle(
                                        color = TextPrimaryDark,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.01f), RoundedCornerShape(16.dp))
                                .border(1.dp, ElegantBorderColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = TextSecondaryDark.copy(alpha = 0.4f), modifier = Modifier.size(32.dp))
                                Text(
                                    text = if (playwrightJobs.isEmpty()) "No active Playwright jobs configured." else "Click 'Analyze' to run Gemini priority analysis.",
                                    style = TextStyle(color = TextSecondaryDark, fontSize = 12.sp, textAlign = TextAlign.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Connect Email Dialog
    if (showEmailConnectDialog) {
        AlertDialog(
            onDismissRequest = { showEmailConnectDialog = false },
            title = { Text("Connect Workplace Email", style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Provide your enterprise email to grant the DabelsTech platform automated SMTP/IMAP credentials.", style = TextStyle(color = TextSecondaryDark, fontSize = 13.sp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.connectEmails(emailInput)
                        showEmailConnectDialog = false
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailConnectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Connect Drive Dialog
    if (showDriveConnectDialog) {
        AlertDialog(
            onDismissRequest = { showDriveConnectDialog = false },
            title = { Text("Sync Google Drive Workspace", style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Grant secure Google Drive API read-access to pull template logs and schema specifications automatically.", style = TextStyle(color = TextSecondaryDark, fontSize = 13.sp))
                    OutlinedTextField(
                        value = driveEmailInput,
                        onValueChange = { driveEmailInput = it },
                        label = { Text("Google Account Email") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.connectGoogleDrive(driveEmailInput)
                        showDriveConnectDialog = false
                    }
                ) {
                    Text("Connect")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDriveConnectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// TAB 0: AUTH0 USER PORTAL & AUDIT LOGS
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Auth0TabScreen(viewModel: TaskViewModel) {
    val context = LocalContext.current
    val clipManager = LocalClipboardManager.current

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val isLoggingIn by viewModel.isLoggingIn.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val accessToken by viewModel.accessToken.collectAsStateWithLifecycle()
    val idToken by viewModel.idToken.collectAsStateWithLifecycle()
    val refreshToken by viewModel.refreshToken.collectAsStateWithLifecycle()
    val securityLogs by viewModel.securityLogs.collectAsStateWithLifecycle()
    val auth0Url by viewModel.auth0Url.collectAsStateWithLifecycle()

    var showWebAuthDialog by remember { mutableStateOf(false) }
    var customDomain by remember { mutableStateOf("dabelstech.us.auth0.com") }
    var customClientId by remember { mutableStateOf("t9XzR3p8H2K9a4B7m2L1f6Z8q5Xw0D1s") }
    var selectedConnection by remember { mutableStateOf("google-oauth2") } // google-oauth2, database, github
    var customAuth0Url by remember { mutableStateOf(auth0Url) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Core Connection Status Indicator
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isLoggedIn) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isLoggedIn) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        RoundedCornerShape(24.dp)
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isLoggedIn) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isLoggedIn) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                            contentDescription = "Sync State Icon",
                            tint = if (isLoggedIn) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isLoggedIn) "Auth0 Active Session" else "Auth0 Session Off",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = if (isLoggedIn) "Bearer credentials injected reactively" else "Configure login below",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }

        if (!isLoggedIn) {
            // Universal Login Control Screen
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElegantBorderColor, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Universal Login",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Auth0 Universal Login",
                                style = TextStyle(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }

                        Text(
                            text = "Log in using secure DabelsTech custom OAuth/OIDC Client configuration.",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        )

                        Divider(color = ElegantBorderColor.copy(alpha = 0.5f), thickness = 1.dp)

                        OutlinedTextField(
                            value = customDomain,
                            onValueChange = { customDomain = it },
                            label = { Text("Auth0 Domain") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = customClientId,
                            onValueChange = { customClientId = it },
                            label = { Text("Client ID") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val workspaceLogin by viewModel.workspaceLogin.collectAsStateWithLifecycle()
                        var customWorkspaceLogin by remember { mutableStateOf(workspaceLogin) }

                        OutlinedTextField(
                            value = customWorkspaceLogin,
                            onValueChange = { 
                                customWorkspaceLogin = it
                                viewModel.workspaceLogin.value = it
                            },
                            label = { Text("Workspace Organization / Tenant ID") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth0_workspace_input")
                        )

                        // Connections Row
                        Text(
                            text = "Identity Provider Connection Profile",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "google-oauth2" to "Google",
                                "database" to "Database",
                                "github" to "GitHub"
                            ).forEach { (connId, connLabel) ->
                                val isSelected = selectedConnection == connId
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else ElegantBorderColor, RoundedCornerShape(8.dp))
                                        .clickable { selectedConnection = connId }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (connId) {
                                                "google-oauth2" -> Icons.Default.Language
                                                "github" -> Icons.Default.Code
                                                else -> Icons.Default.Storage
                                            },
                                            contentDescription = connLabel,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondaryDark,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = connLabel,
                                            style = TextStyle(
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else TextSecondaryDark,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Custom Auth0 Universal State Link Text Field
                        OutlinedTextField(
                            value = customAuth0Url,
                            onValueChange = { customAuth0Url = it },
                            label = { Text("Auth0 Universal OIDC State Link") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                showWebAuthDialog = true
                            },
                            enabled = !isLoggingIn,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_auth0_login")
                        ) {
                            if (isLoggingIn) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting Auth0 Universal...")
                            } else {
                                Icon(Icons.Default.Security, contentDescription = "Secure login")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Authenticate via Auth0 Web Connection", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Logged In profile and credentials inspector
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElegantBorderColor, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(LowPriorityColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auth0 Profile",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                )
                            }
                            TextButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.testTag("btn_auth0_logout")
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = "Log out")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Logout Session", color = HighPriorityColor)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile picture placeholder",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = userName,
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onBackground,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = userEmail,
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "OIDC sub: auth0|62a392b8d...",
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }

                        Divider(color = ElegantBorderColor.copy(alpha = 0.5f), thickness = 1.dp)

                        // Token visualizers
                        TokenRow(label = "ID Token", token = idToken, onCopy = {
                            clipManager.setText(AnnotatedString(idToken))
                            Toast.makeText(context, "ID Token copied to clipboard", Toast.LENGTH_SHORT).show()
                        })

                        TokenRow(label = "Access Token", token = accessToken, onCopy = {
                            clipManager.setText(AnnotatedString(accessToken))
                            Toast.makeText(context, "Access Token copied to clipboard", Toast.LENGTH_SHORT).show()
                        })

                        TokenRow(label = "Refresh Token", token = refreshToken, onCopy = {
                            clipManager.setText(AnnotatedString(refreshToken))
                            Toast.makeText(context, "Refresh Token copied to clipboard", Toast.LENGTH_SHORT).show()
                        })

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { viewModel.triggerTokenRefresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(1.dp, ElegantBorderColor, RoundedCornerShape(20.dp)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh OIDC Token")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Trigger Silent Token Refresh Flow", style = TextStyle(color = MaterialTheme.colorScheme.onSurface))
                        }
                    }
                }
            }
        }

        // Security Audit Logs Console (Always visible)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SECURITY AUDIT LOGS (LOCAL ROOM STATE)",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    TextButton(onClick = { viewModel.clearSecurityLogs() }) {
                        Text("Clear Logs", color = HighPriorityColor, fontSize = 11.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.dp, ElegantBorderColor, RoundedCornerShape(16.dp))
                ) {
                    if (securityLogs.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Terminal empty. Awaiting telemetry...",
                                style = TextStyle(
                                    color = Color(0xFF475569),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(securityLogs) { log ->
                                SecurityLogLine(log)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWebAuthDialog) {
        Dialog(
            onDismissRequest = { showWebAuthDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F0F15)),
                color = Color(0xFF0F0F15)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Browser Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161622))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showWebAuthDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Browser",
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Address Bar style container
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F0F15))
                                .border(1.dp, ElegantBorderColor, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Secure Connection",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (customAuth0Url.length > 8) customAuth0Url.substringBefore("?").replace("https://", "") else customAuth0Url,
                                style = TextStyle(color = TextSecondaryDark, fontSize = 12.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Status badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OIDC ACTIVE",
                                style = TextStyle(color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    
                    // Live interactive WebView
                    Box(modifier = Modifier.weight(1f)) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        useWideViewPort = true
                                        loadWithOverviewMode = true
                                        userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/100.0.0.0 Mobile Safari/537.36"
                                    }
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                        }
                                    }
                                    loadUrl(customAuth0Url)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // Bottom Controls Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF161622))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                viewModel.loginWithUrl(customAuth0Url)
                                showWebAuthDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Accept Callback")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulate OIDC Callback Success", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        OutlinedButton(
                            onClick = { showWebAuthDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimaryDark),
                            border = BorderStroke(1.dp, ElegantBorderColor),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TokenRow(label: String, token: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, ElegantBorderColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = TextStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
            Text(
                text = if (token.isEmpty()) "Not Generated" else token,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    color = TextPrimaryDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )
        }
        if (token.isNotEmpty()) {
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SecurityLogLine(log: SecurityLog) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.US) }
    val formattedTime = timeFormat.format(Date(log.timestamp))

    val badgeColor = when (log.eventType) {
        "AUTH0_LOGIN" -> LowPriorityColor
        "AUTH0_LOGOUT" -> HighPriorityColor
        "TOKEN_REFRESH" -> WarmAmber
        "API_INVOCATION" -> MaterialTheme.colorScheme.primary
        "PLAYWRIGHT_EXEC" -> Color(0xFFD0BCFF)
        "EMAIL_DISPATCH" -> Color(0xFF38BDF8)
        else -> Color(0xFF64748B)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "[$formattedTime]",
            style = TextStyle(
                color = Color(0xFF64748B),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(end = 6.dp)
        )
        Box(
            modifier = Modifier
                .padding(end = 6.dp, top = 2.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .border(0.5.dp, badgeColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = log.eventType,
                style = TextStyle(
                    color = badgeColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                )
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.message,
                style = TextStyle(
                    color = Color(0xFFE2E8F0),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            )
            if (log.details.isNotEmpty()) {
                Text(
                    text = "  -> ${log.details}",
                    style = TextStyle(
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

// ==========================================
// TAB 1: API CLIENT SANDBOX
// ==========================================
@Composable
fun ApiClientTabScreen(viewModel: TaskViewModel) {
    val clipManager = LocalClipboardManager.current
    val context = LocalContext.current

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val apiMethod by viewModel.apiMethod.collectAsStateWithLifecycle()
    val apiEndpoint by viewModel.apiEndpoint.collectAsStateWithLifecycle()
    val apiHeaders by viewModel.apiHeaders.collectAsStateWithLifecycle()
    val apiRequestBody by viewModel.apiRequestBody.collectAsStateWithLifecycle()
    val isApiCalling by viewModel.isApiCalling.collectAsStateWithLifecycle()
    val responseCode by viewModel.apiResponseCode.collectAsStateWithLifecycle()
    val responseTime by viewModel.apiResponseTimeMs.collectAsStateWithLifecycle()
    val responseBody by viewModel.apiResponseBody.collectAsStateWithLifecycle()

    var showEndpointDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Explanatory Subheader
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElegantBorderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.IntegrationInstructions,
                        contentDescription = "API Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Query secure DabelsTech endpoints. If logged in, Auth0 OIDC ID Token is automatically passed as an Authorization Bearer header.",
                        style = TextStyle(
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

        // Request Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElegantBorderColor, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "REST Client Request Details",
                        style = TextStyle(
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )

                    // Method & Endpoint Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Method Selector
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, ElegantBorderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.apiMethod.value = if (apiMethod == "GET") "POST" else "GET"
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = apiMethod,
                                style = TextStyle(
                                    color = if (apiMethod == "POST") CoralAccent else CyanPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                        }

                        // Endpoint preloaded options
                        Box(
                            modifier = Modifier
                                .weight(3.dp.value)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, ElegantBorderColor, RoundedCornerShape(12.dp))
                                .clickable { showEndpointDropdown = !showEndpointDropdown }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = apiEndpoint.substringAfter("api.dabelstech.com/"),
                                    style = TextStyle(color = TextPrimaryDark, fontSize = 13.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Endpoint Dropdown",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showEndpointDropdown,
                                onDismissRequest = { showEndpointDropdown = false },
                                modifier = Modifier.background(CardBg)
                            ) {
                                listOf(
                                    "https://api.dabelstech.com/v1/automation/status",
                                    "https://api.dabelstech.com/v1/automation/jobs",
                                    "https://api.dabelstech.com/v1/automation/email-logs"
                                ).forEach { endpoint ->
                                    DropdownMenuItem(
                                        text = { Text(endpoint.substringAfter("api.dabelstech.com/"), color = TextPrimaryDark) },
                                        onClick = {
                                            viewModel.apiEndpoint.value = endpoint
                                            showEndpointDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Headers Field
                    OutlinedTextField(
                        value = apiHeaders,
                        onValueChange = { viewModel.apiHeaders.value = it },
                        label = { Text("Request Headers") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Request Body (shown if POST)
                    if (apiMethod == "POST") {
                        OutlinedTextField(
                            value = apiRequestBody,
                            onValueChange = { viewModel.apiRequestBody.value = it },
                            label = { Text("Request Body (JSON)") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { viewModel.executeApiCall() },
                        enabled = !isApiCalling,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_api_send")
                    ) {
                        if (isApiCalling) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sending Request Header Payload...")
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Send request")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send HTTP Trigger", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Response Section (Only visible if responseCode is not null)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "RESPONSE LOGGER",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .border(1.dp, ElegantBorderColor, RoundedCornerShape(20.dp))
                ) {
                    if (responseCode == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Awaiting request activation...",
                                style = TextStyle(color = Color(0xFF475569), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Code & Time header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (responseCode == 200) LowPriorityColor.copy(alpha = 0.15f) else HighPriorityColor.copy(alpha = 0.15f))
                                            .border(0.5.dp, if (responseCode == 200) LowPriorityColor else HighPriorityColor, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$responseCode ${if (responseCode == 200) "OK" else "UNAUTHORIZED"}",
                                            style = TextStyle(
                                                color = if (responseCode == 200) LowPriorityColor else HighPriorityColor,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "${responseTime} ms",
                                        style = TextStyle(
                                            color = TextSecondaryDark,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp
                                        )
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        clipManager.setText(AnnotatedString(responseBody))
                                        Toast.makeText(context, "Response payload copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy response payload",
                                        tint = TextSecondaryDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Divider(color = ElegantBorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)

                            // JSON Text
                            Text(
                                text = responseBody,
                                style = TextStyle(
                                    color = if (responseCode == 200) Color(0xFF38BDF8) else Color(0xFFFDA4AF),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: EMAIL AUTOMATION WORKSPACE
// ==========================================
@Composable
fun EmailAutomationTabScreen(viewModel: TaskViewModel) {
    val emailCampaigns by viewModel.emailCampaigns.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    // Forms state
    var formTemplateName by remember { mutableStateOf("") }
    var formSubject by remember { mutableStateOf("") }
    var formBody by remember { mutableStateOf("") }
    var formRecipientGroup by remember { mutableStateOf("Active Users") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("btn_add_email_campaign")
                    .padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Campaign")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // Stats Panel
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EmailStatCard(
                        label = "Delivered",
                        value = "6,340",
                        icon = Icons.Default.MarkEmailRead,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    EmailStatCard(
                        label = "Avg Open Rate",
                        value = "63.2%",
                        icon = Icons.Default.BarChart,
                        color = Color(0xFF34D399),
                        modifier = Modifier.weight(1f)
                    )
                    EmailStatCard(
                        label = "Avg CTR",
                        value = "35.6%",
                        icon = Icons.Default.Mouse,
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Campaigns Header
            item {
                Text(
                    text = "ACTIVE AUTOMATED CAMPAIGNS",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Campaigns List
            if (emailCampaigns.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No campaigns designed. Tap + to configure.",
                            style = TextStyle(color = TextSecondaryDark, fontSize = 13.sp)
                        )
                    }
                }
            } else {
                items(emailCampaigns) { campaign ->
                    EmailCampaignItem(
                        campaign = campaign,
                        onRun = { viewModel.runEmailCampaign(campaign) },
                        onDelete = { viewModel.deleteCampaign(campaign) }
                    )
                }
            }
        }
    }

    // Modal Dialog sheet
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    "Design Email Campaign Template",
                    style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = formTemplateName,
                        onValueChange = { formTemplateName = it },
                        label = { Text("Template Name") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formSubject,
                        onValueChange = { formSubject = it },
                        label = { Text("Email Subject Line") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Recipient group selector dropdown placeholder simple selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Active Users", "Trial Signups", "Leads").forEach { audience ->
                            val isSel = formRecipientGroup == audience
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else ElegantBorderColor, RoundedCornerShape(8.dp))
                                    .clickable { formRecipientGroup = audience }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    audience,
                                    style = TextStyle(color = if (isSel) MaterialTheme.colorScheme.primary else TextSecondaryDark, fontSize = 11.sp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formBody,
                        onValueChange = { formBody = it },
                        label = { Text("Email Body Content") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formTemplateName.isNotBlank() && formSubject.isNotBlank()) {
                            viewModel.createEmailCampaign(
                                formTemplateName,
                                formSubject,
                                formBody,
                                formRecipientGroup
                            )
                            showAddDialog = false
                            // Reset form
                            formTemplateName = ""
                            formSubject = ""
                            formBody = ""
                            formRecipientGroup = "Active Users"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Schedule Template", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = CardBg
        )
    }
}

@Composable
fun EmailStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.border(1.dp, ElegantBorderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            Text(text = label, style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp), maxLines = 1)
            Text(text = value, style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 16.sp))
        }
    }
}

@Composable
fun EmailCampaignItem(campaign: EmailCampaign, onRun: () -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ElegantBorderColor, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = campaign.templateName,
                            style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = "Audience", tint = TextSecondaryDark, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = campaign.recipientGroup, style = TextStyle(color = TextSecondaryDark, fontSize = 11.sp))
                        }
                    }
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (campaign.status) {
                                "Sent" -> LowPriorityColor.copy(alpha = 0.15f)
                                "Sending" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else -> WarmAmber.copy(alpha = 0.15f)
                            }
                        )
                        .border(
                            0.5.dp,
                            when (campaign.status) {
                                "Sent" -> LowPriorityColor
                                "Sending" -> MaterialTheme.colorScheme.primary
                                else -> WarmAmber
                            },
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = campaign.status,
                        style = TextStyle(
                            color = when (campaign.status) {
                                "Sent" -> LowPriorityColor
                                "Sending" -> MaterialTheme.colorScheme.primary
                                else -> WarmAmber
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Subject: ${campaign.subject}",
                style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Medium, fontSize = 13.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = campaign.body,
                style = TextStyle(color = TextSecondaryDark, fontSize = 12.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Live campaign telemetry (delivered count etc)
            if (campaign.deliveredCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = ElegantBorderColor.copy(alpha = 0.3f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Delivered", style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp))
                        Text("${campaign.deliveredCount}", style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                    }
                    Column {
                        Text("Opened", style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp))
                        Text("${campaign.openedCount}", style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                    }
                    Column {
                        Text("Clicked", style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp))
                        Text("${campaign.clickedCount}", style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Template", tint = HighPriorityColor.copy(alpha = 0.8f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (campaign.status != "Sent" && campaign.status != "Sending") {
                    Button(
                        onClick = onRun,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run Campaign", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dispatch Campaign", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: PLAYWRIGHT JOB RUNNER & LOGS
// ==========================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaywrightTabScreen(viewModel: TaskViewModel) {
    val playwrightJobs by viewModel.playwrightJobs.collectAsStateWithLifecycle()
    val runningJobId by viewModel.runningJobId.collectAsStateWithLifecycle()
    val activeJobLogs by viewModel.activeJobLogs.collectAsStateWithLifecycle()

    var showAddJobDialog by remember { mutableStateOf(false) }

    // Forms
    var formJobName by remember { mutableStateOf("") }
    var formTargetUrl by remember { mutableStateOf("https://") }
    var formScriptType by remember { mutableStateOf("E2E Login Flow") }
    var formCronSchedule by remember { mutableStateOf("Manual") }
    var formIsHighPriority by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddJobDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .testTag("btn_add_playwright_job")
                    .padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Playwright Job")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // Live logs terminal panel (Always visible, sticky showing either active logs or last run details)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PLAYWRIGHT CONTAINER CONSOLE STREAM",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp
                        ),
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .border(1.dp, ElegantBorderColor, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        val currentLogDisplay = if (runningJobId != null) activeJobLogs else {
                            val runningJob = playwrightJobs.find { it.status == "Running" }
                            runningJob?.logOutput ?: "Console Idle. Activate a Playwright browser automation container below."
                        }

                        val isConsoleEmpty = currentLogDisplay.startsWith("Console Idle")

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            item {
                                Text(
                                    text = currentLogDisplay,
                                    style = TextStyle(
                                        color = if (isConsoleEmpty) Color(0xFF475569) else Color(0xFF34D399),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Jobs Header
            item {
                Text(
                    text = "CONFIGURED BROWSER JOBS",
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Jobs List
            if (playwrightJobs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No E2E jobs configured. Tap + to setup.",
                            style = TextStyle(color = TextSecondaryDark, fontSize = 13.sp)
                        )
                    }
                }
            } else {
                items(playwrightJobs) { job ->
                    PlaywrightJobItem(
                        job = job,
                        isGlobalRunnerBusy = runningJobId != null,
                        onRun = { viewModel.runPlaywrightJob(job) },
                        onRunFail = { viewModel.runPlaywrightJob(job, forceFail = true) },
                        onDelete = { viewModel.deletePlaywrightJob(job) }
                    )
                }
            }
        }
    }

    if (showAddJobDialog) {
        AlertDialog(
            onDismissRequest = { showAddJobDialog = false },
            title = {
                Text(
                    "Provision Playwright Automation Job",
                    style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = formJobName,
                        onValueChange = { formJobName = it },
                        label = { Text("Job Description Name") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = formTargetUrl,
                        onValueChange = { formTargetUrl = it },
                        label = { Text("Target URI to Crawl") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Script Type Selectors
                    Text(
                        text = "Playwright Browser Action Profile",
                        style = TextStyle(color = TextSecondaryDark, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("E2E Login Flow", "SEO Audit", "Lead Generation Scraper").forEach { scriptType ->
                            val isSel = formScriptType == scriptType
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else ElegantBorderColor, RoundedCornerShape(8.dp))
                                    .clickable { formScriptType = scriptType }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    scriptType,
                                    style = TextStyle(color = if (isSel) MaterialTheme.colorScheme.primary else TextSecondaryDark, fontSize = 11.sp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formCronSchedule,
                        onValueChange = { formCronSchedule = it },
                        label = { Text("Trigger Schedule (Cron or Manual)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = TextPrimaryDark,
                            unfocusedTextColor = TextPrimaryDark
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .clickable { formIsHighPriority = !formIsHighPriority }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "High Priority Monitoring",
                                style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            )
                            Text(
                                "Triggers instant background email alert upon failure.",
                                style = TextStyle(color = TextSecondaryDark, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = formIsHighPriority,
                            onCheckedChange = { formIsHighPriority = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (formJobName.isNotBlank() && formTargetUrl.isNotBlank()) {
                            viewModel.createPlaywrightJob(
                                formJobName,
                                formTargetUrl,
                                formScriptType,
                                formCronSchedule,
                                formIsHighPriority
                            )
                            showAddJobDialog = false
                            // Reset
                            formJobName = ""
                            formTargetUrl = "https://"
                            formScriptType = "E2E Login Flow"
                            formCronSchedule = "Manual"
                            formIsHighPriority = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Provision Runner Job", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddJobDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            },
            containerColor = CardBg
        )
    }
}

@Composable
fun PlaywrightJobItem(
    job: PlaywrightJob,
    isGlobalRunnerBusy: Boolean,
    onRun: () -> Unit,
    onRunFail: () -> Unit,
    onDelete: () -> Unit
) {
    val isRunningThisJob = job.status == "Running"

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ElegantBorderColor, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (job.status) {
                                    "Running" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    "Failed" -> HighPriorityColor.copy(alpha = 0.15f)
                                    else -> LowPriorityColor.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = null,
                            tint = when (job.status) {
                                "Running" -> MaterialTheme.colorScheme.primary
                                "Failed" -> HighPriorityColor
                                else -> LowPriorityColor
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = job.name,
                            style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = "URL", tint = TextSecondaryDark, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = job.targetUrl,
                                style = TextStyle(color = TextSecondaryDark, fontSize = 11.sp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Status & Priority Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (job.isHighPriority) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(HighPriorityColor.copy(alpha = 0.15f))
                                .border(0.5.dp, HighPriorityColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HIGH PRIORITY",
                                style = TextStyle(
                                    color = HighPriorityColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (job.status) {
                                    "Running" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    "Failed" -> HighPriorityColor.copy(alpha = 0.15f)
                                    else -> LowPriorityColor.copy(alpha = 0.15f)
                                }
                            )
                            .border(
                                0.5.dp,
                                when (job.status) {
                                    "Running" -> MaterialTheme.colorScheme.primary
                                    "Failed" -> HighPriorityColor
                                    else -> LowPriorityColor
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = job.status,
                            style = TextStyle(
                                color = when (job.status) {
                                    "Running" -> MaterialTheme.colorScheme.primary
                                    "Failed" -> HighPriorityColor
                                    else -> LowPriorityColor
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text("Script Type", style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp))
                    Text(job.scriptType, style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Medium, fontSize = 12.sp))
                }
                Column {
                    Text("Schedule Interval", style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp))
                    Text(job.cronSchedule, style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Medium, fontSize = 12.sp))
                }
                if (job.durationMs > 0) {
                    Column {
                        Text("Duration", style = TextStyle(color = TextSecondaryDark, fontSize = 10.sp))
                        Text("${job.durationMs}ms", style = TextStyle(color = TextPrimaryDark, fontWeight = FontWeight.Medium, fontSize = 12.sp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, enabled = !isRunningThisJob) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Job",
                        tint = if (isRunningThisJob) TextSecondaryDark else HighPriorityColor.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (!isRunningThisJob) {
                    OutlinedButton(
                        onClick = onRunFail,
                        enabled = !isGlobalRunnerBusy,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HighPriorityColor),
                        border = BorderStroke(1.dp, HighPriorityColor.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp).testTag("btn_fail_job_${job.id}")
                    ) {
                        Icon(imageVector = Icons.Default.BugReport, contentDescription = "Fail Suite", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Failure", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = onRun,
                    enabled = !isGlobalRunnerBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp).testTag("btn_run_job_${job.id}")
                ) {
                    if (isRunningThisJob) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 1.5.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Executing...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run Playwright Job", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Run Suite", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
