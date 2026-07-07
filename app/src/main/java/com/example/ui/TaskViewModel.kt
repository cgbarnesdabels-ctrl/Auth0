package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SecurityLog
import com.example.data.EmailCampaign
import com.example.data.PlaywrightJob
import com.example.data.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray

data class DriveFile(
    val id: String,
    val name: String,
    val mimeType: String,
    val size: String,
    val lastModified: String
)

data class EmailMessage(
    val id: String,
    val sender: String,
    val subject: String,
    val snippet: String,
    val date: String,
    val isRead: Boolean
)


class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    // --- Database Observables ---
    val securityLogs: StateFlow<List<SecurityLog>> = repository.allSecurityLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val emailCampaigns: StateFlow<List<EmailCampaign>> = repository.allEmailCampaigns
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val playwrightJobs: StateFlow<List<PlaywrightJob>> = repository.allPlaywrightJobs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Workspace State ---
    val workspaceLogin = MutableStateFlow("dabelstech")

    // --- Emails App Integration State ---
    val isEmailConnected = MutableStateFlow(false)
    val connectedEmailAddress = MutableStateFlow("")
    val syncedEmails = MutableStateFlow<List<EmailMessage>>(emptyList())
    val isConnectingEmail = MutableStateFlow(false)

    // --- Google Drive Integration State ---
    val isGoogleDriveConnected = MutableStateFlow(false)
    val driveUserEmail = MutableStateFlow("")
    val driveFiles = MutableStateFlow<List<DriveFile>>(emptyList())
    val isConnectingDrive = MutableStateFlow(false)

    // --- Gemini AI Analysis State ---
    val playwrightSummary = MutableStateFlow("")
    val isGeneratingSummary = MutableStateFlow(false)

    // --- Auth0 State ---
    val isLoggedIn = MutableStateFlow(true) // Start logged in for elegant first-time setup, but can login/logout
    val userName = MutableStateFlow("John Dabels")
    val userEmail = MutableStateFlow("john.dabels@dabelstech.com")
    val userAvatarUrl = MutableStateFlow("") // placeholder
    val accessToken = MutableStateFlow("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik1reER...")
    val idToken = MutableStateFlow("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik1yZTM...")
    val refreshToken = MutableStateFlow("v1.M3YxNTRhOTBi...Z2V0Y3JlZGVudGlhbHM")
    val isLoggingIn = MutableStateFlow(false)
    val auth0Domain = MutableStateFlow("dabelstech.us.auth0.com")
    val auth0ClientId = MutableStateFlow("t9XzR3p8H2K9a4B7m2L1f6Z8q5Xw0D1s")
    val auth0Url = MutableStateFlow("https://auth0.auth0.com/u/login/identifier?state=hKFo2SAxM1hPc0oxdUR3ZlBLYVFlVy1oMGRwUU1wUnQ2MkxZSaFur3VuaXZlcnNhbC1sb2dpbqN0aWTZIHU5bGFxTldNVlVQRmN4bmZ2Q1R1SmtNdlljY0t0TFl1o2NpZNkgekVZZnBvRnpVTUV6aWxoa0hpbGNXb05rckZmSjNoQUk")

    // --- API Client State ---
    val apiMethod = MutableStateFlow("GET")
    val apiEndpoint = MutableStateFlow("https://api.dabelstech.com/v1/automation/status")
    val apiHeaders = MutableStateFlow("Authorization: Bearer [ID_TOKEN]\nContent-Type: application/json")
    val apiRequestBody = MutableStateFlow("{\n  \"trigger\": \"android_client\",\n  \"mode\": \"production\"\n}")
    val apiResponseCode = MutableStateFlow<Int?>(null)
    val apiResponseTimeMs = MutableStateFlow<Long?>(null)
    val apiResponseBody = MutableStateFlow("")
    val isApiCalling = MutableStateFlow(false)

    // --- Active Playwright Logs ---
    val activeJobLogs = MutableStateFlow<String>("")
    val runningJobId = MutableStateFlow<Int?>(null)

    init {
        // Seed default database values if empty
        viewModelScope.launch {
            // Seed Security Logs
            repository.insertLog(
                SecurityLog(
                    eventType = "SYSTEM_INIT",
                    message = "DabelsTech AuthApp Initialized",
                    details = "Room database setup successful. Version 2 initialized."
                )
            )
            repository.insertLog(
                SecurityLog(
                    eventType = "AUTH0_LOGIN",
                    message = "User john.dabels@dabelstech.com authenticated successfully",
                    details = "Auth0 Universal Login completed. Connection: google-oauth2. IP: 198.51.100.42"
                )
            )

            // Seed Email Campaigns if none exist
            delay(100)
            repository.allEmailCampaigns.collect { list ->
                if (list.isEmpty()) {
                    repository.insertEmailCampaign(
                        EmailCampaign(
                            templateName = "Trial Expiry Reminder",
                            subject = "Your DabelsTech Pro Trial is Expiring Soon!",
                            body = "Hi {{name}},\n\nYour 14-day premium trial expires in 3 days. Upgrade today to keep access to Playwright runner jobs and unlimited automated workflows!\n\nBest,\nDabelsTech Team",
                            recipientGroup = "Trial Signups",
                            scheduledTime = System.currentTimeMillis() + 86400000 * 2,
                            status = "Scheduled",
                            deliveredCount = 1420,
                            openedCount = 890,
                            clickedCount = 412
                        )
                    )
                    repository.insertEmailCampaign(
                        EmailCampaign(
                            templateName = "Feature Announcement",
                            subject = "Introducing Headless Playwright 1.45 on DabelsTech Run",
                            body = "Hello!\n\nWe are excited to announce full Playwright E2E browser automation support within our cloud workers. Build, run, and sync test suites seamlessly.\n\nEnjoy,\nDabelsTech Engineering",
                            recipientGroup = "Active Users",
                            scheduledTime = System.currentTimeMillis() - 86400000,
                            status = "Sent",
                            deliveredCount = 4920,
                            openedCount = 3120,
                            clickedCount = 1850
                        )
                    )
                }
            }
        }

        viewModelScope.launch {
            // Seed Playwright Jobs if none exist
            delay(150)
            repository.allPlaywrightJobs.collect { list ->
                if (list.isEmpty()) {
                    repository.insertPlaywrightJob(
                        PlaywrightJob(
                            name = "Auth0 Portal E2E Smoke Test",
                            targetUrl = "https://auth.dabelstech.com/login",
                            scriptType = "E2E Login Flow",
                            status = "Success",
                            lastRunTime = System.currentTimeMillis() - 1200000,
                            cronSchedule = "0 */3 * * *", // every 3 hours
                            durationMs = 4210,
                            logOutput = "[info] Launching Chromium headless...\n[navigation] Goto https://auth.dabelstech.com/login\n[action] Fill email & password\n[action] Click 'Continue'\n[status] Dashboard loaded. JWT generated. Run completed successfully.",
                            isHighPriority = true
                        )
                    )
                    repository.insertPlaywrightJob(
                        PlaywrightJob(
                            name = "SEO Meta and Header Auditor",
                            targetUrl = "https://dabelstech.com",
                            scriptType = "SEO Audit",
                            status = "Success",
                            lastRunTime = System.currentTimeMillis() - 7200000,
                            cronSchedule = "0 0 * * *", // daily
                            durationMs = 3150,
                            logOutput = "[info] Launching WebKit...\n[navigation] Goto https://dabelstech.com\n[assert] H1 tag present\n[assert] Meta description present\n[status] SEO criteria satisfied.",
                            isHighPriority = false
                        )
                    )
                }
            }
        }

        // --- Background Worker: Playwright Failure Monitor ---
        viewModelScope.launch {
            delay(1000) // allow database and seed to settle
            val processedFailures = mutableSetOf<String>()
            var isWorkerReady = false
            
            repository.allPlaywrightJobs.collect { jobs ->
                val failedHighPriorityJobs = jobs.filter { it.isHighPriority && it.status == "Failed" }
                
                if (!isWorkerReady) {
                    // Populate already failed runs to avoid spamming alerts on app startup
                    for (job in failedHighPriorityJobs) {
                        processedFailures.add("${job.id}_${job.lastRunTime}")
                    }
                    isWorkerReady = true
                } else {
                    for (job in failedHighPriorityJobs) {
                        val runKey = "${job.id}_${job.lastRunTime}"
                        if (!processedFailures.contains(runKey)) {
                            processedFailures.add(runKey)
                            
                            // Trigger email notification via integrated email service
                            if (isEmailConnected.value) {
                                val recipient = connectedEmailAddress.value
                                val newEmail = EmailMessage(
                                    id = System.currentTimeMillis().toString(),
                                    sender = "playwright-monitor@dabelstech.com",
                                    subject = "[CRITICAL ALERT] High-Priority Job Fail: ${job.name}",
                                    snippet = "DabelsTech E2E automation job '${job.name}' failed on execution profile: ${job.scriptType} at ${job.targetUrl}. Verify logs.",
                                    date = "Just now",
                                    isRead = false
                                )
                                syncedEmails.value = listOf(newEmail) + syncedEmails.value
                                
                                repository.insertLog(
                                    SecurityLog(
                                        eventType = "EMAIL_DISPATCH",
                                        message = "Alert email sent for failed high-priority job: ${job.name}",
                                        details = "Delivered to connected SMTP/IMAP address: $recipient"
                                    )
                                )
                            } else {
                                // Log that the alert was triggered but email dispatch was skipped
                                repository.insertLog(
                                    SecurityLog(
                                        eventType = "EMAIL_DISPATCH",
                                        message = "Email dispatch skipped: Service disconnected",
                                        details = "High-priority job failed: ${job.name} (Requires SMTP connection)"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Authentication Actions ---
    fun login(domain: String, clientId: String, connection: String = "google-oauth2") {
        viewModelScope.launch {
            isLoggingIn.value = true
            auth0Domain.value = domain
            auth0ClientId.value = clientId
            
            // Simulate beautiful visual delay for authentication
            delay(1800)
            
            isLoggedIn.value = true
            isLoggingIn.value = false
            userName.value = if (connection == "github") "John Github" else "John Dabels"
            userEmail.value = if (connection == "github") "john.github@dabelstech.com" else "john.dabels@dabelstech.com"
            accessToken.value = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik" + (100000..999999).random() + "..."
            idToken.value = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik" + (100000..999999).random() + "..."
            refreshToken.value = "v1." + (100000..999999).random() + "Z2V0Y3JlZGVudGlhbHM"

            repository.insertLog(
                SecurityLog(
                    eventType = "AUTH0_LOGIN",
                    message = "User ${userEmail.value} successfully authenticated",
                    details = "Auth0 domain: $domain | Connection: $connection | ClientID: $clientId"
                )
            )
        }
    }

    fun loginWithUrl(url: String) {
        viewModelScope.launch {
            isLoggingIn.value = true
            auth0Url.value = url
            
            val parsedDomain = try {
                android.net.Uri.parse(url).host ?: "auth0.auth0.com"
            } catch (e: Exception) {
                "auth0.auth0.com"
            }
            auth0Domain.value = parsedDomain
            
            // Simulate beautiful visual delay for authentication
            delay(1200)
            
            isLoggedIn.value = true
            isLoggingIn.value = false
            userName.value = "John Dabels (OIDC)"
            userEmail.value = "john.dabels@dabelstech.com"
            accessToken.value = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik" + (100000..999999).random() + "..."
            idToken.value = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik" + (100000..999999).random() + "..."
            refreshToken.value = "v1." + (100000..999999).random() + "Z2V0Y3JlZGVudGlhbHM"

            repository.insertLog(
                SecurityLog(
                    eventType = "AUTH0_LOGIN",
                    message = "User ${userEmail.value} authenticated via Web OIDC",
                    details = "Auth0 OIDC Url: $url"
                )
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.insertLog(
                SecurityLog(
                    eventType = "AUTH0_LOGOUT",
                    message = "User ${userEmail.value} logged out",
                    details = "Auth0 session terminated locally and clean cache cleared."
                )
            )
            isLoggedIn.value = false
            accessToken.value = ""
            idToken.value = ""
            refreshToken.value = ""
        }
    }

    fun triggerTokenRefresh() {
        viewModelScope.launch {
            if (!isLoggedIn.value) return@launch
            repository.insertLog(
                SecurityLog(
                    eventType = "TOKEN_REFRESH",
                    message = "Triggered background Auth0 silent token refresh",
                    details = "Refresh Token utilized to obtain fresh Access Token."
                )
            )
            delay(800)
            accessToken.value = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik" + (100000..999999).random() + "..."
            repository.insertLog(
                SecurityLog(
                    eventType = "TOKEN_REFRESH",
                    message = "Silent token refresh success",
                    details = "New Access Token: ${accessToken.value.take(25)}..."
                )
            )
        }
    }

    // --- API Client Actions ---
    fun executeApiCall() {
        viewModelScope.launch {
            isApiCalling.value = true
            apiResponseCode.value = null
            apiResponseBody.value = ""
            
            val startTime = System.currentTimeMillis()
            delay(1200) // simulated network delay
            
            val duration = System.currentTimeMillis() - startTime
            apiResponseTimeMs.value = duration

            if (!isLoggedIn.value) {
                apiResponseCode.value = 401
                apiResponseBody.value = "{\n  \"error\": \"unauthorized\",\n  \"message\": \"Missing or invalid Auth0 Bearer ID token. Please login first.\"\n}"
                repository.insertLog(
                    SecurityLog(
                        eventType = "API_INVOCATION",
                        message = "API Call Failed: 401 Unauthorized",
                        details = "Endpoint: ${apiEndpoint.value} | Duration: ${duration}ms"
                    )
                )
                isApiCalling.value = false
                return@launch
            }

            apiResponseCode.value = 200
            val endpointLower = apiEndpoint.value.lowercase()
            
            apiResponseBody.value = when {
                endpointLower.contains("status") -> {
                    "{\n  \"status\": \"healthy\",\n  \"environment\": \"production\",\n  \"version\": \"1.4.2\",\n  \"auth0\": {\n    \"issuer\": \"https://${auth0Domain.value}/\",\n    \"client_id\": \"${auth0ClientId.value}\"\n  },\n  \"authenticated_user\": {\n    \"email\": \"${userEmail.value}\",\n    \"role\": \"administrator\"\n  }\n}"
                }
                endpointLower.contains("jobs") -> {
                    "{\n  \"jobs_count\": 2,\n  \"status\": \"synchronized\",\n  \"runner\": \"dabelstech-k8s-workers-east\"\n}"
                }
                endpointLower.contains("email") -> {
                    "{\n  \"smtp\": \"connected\",\n  \"daily_limit_remaining\": 9852,\n  \"campaigns_queued\": 0\n}"
                }
                else -> {
                    "{\n  \"success\": true,\n  \"message\": \"Custom endpoint matched. Auth0 bearer credentials valid.\",\n  \"timestamp\": ${System.currentTimeMillis()}\n}"
                }
            }

            repository.insertLog(
                SecurityLog(
                    eventType = "API_INVOCATION",
                    message = "API Call Success: 200 OK (${apiMethod.value})",
                    details = "Endpoint: ${apiEndpoint.value} | Duration: ${duration}ms"
                )
            )
            isApiCalling.value = false
        }
    }

    // --- Email Campaign Actions ---
    fun createEmailCampaign(templateName: String, subject: String, body: String, recipientGroup: String) {
        viewModelScope.launch {
            val campaign = EmailCampaign(
                templateName = templateName,
                subject = subject,
                body = body,
                recipientGroup = recipientGroup,
                scheduledTime = System.currentTimeMillis() + 3600000, // in 1 hour
                status = "Scheduled"
            )
            repository.insertEmailCampaign(campaign)
            
            repository.insertLog(
                SecurityLog(
                    eventType = "EMAIL_DISPATCH",
                    message = "Scheduled campaign '$templateName'",
                    details = "Subject: $subject | Audience: $recipientGroup"
                )
            )
        }
    }

    fun runEmailCampaign(campaign: EmailCampaign) {
        viewModelScope.launch {
            val runningCampaign = campaign.copy(status = "Sending")
            repository.updateEmailCampaign(runningCampaign)
            
            repository.insertLog(
                SecurityLog(
                    eventType = "EMAIL_DISPATCH",
                    message = "Triggered email campaign dispatch: ${campaign.templateName}",
                    details = "Sending automated email notifications..."
                )
            )

            delay(2000)

            val completedCampaign = campaign.copy(
                status = "Sent",
                deliveredCount = campaign.deliveredCount + (200..800).random(),
                openedCount = campaign.openedCount + (100..400).random(),
                clickedCount = campaign.clickedCount + (20..150).random()
            )
            repository.updateEmailCampaign(completedCampaign)

            repository.insertLog(
                SecurityLog(
                    eventType = "EMAIL_DISPATCH",
                    message = "Email campaign '${campaign.templateName}' sent successfully",
                    details = "Delivered to target group: ${campaign.recipientGroup}"
                )
            )
        }
    }

    fun deleteCampaign(campaign: EmailCampaign) {
        viewModelScope.launch {
            repository.deleteEmailCampaign(campaign)
            repository.insertLog(
                SecurityLog(
                    eventType = "EMAIL_DISPATCH",
                    message = "Deleted campaign template '${campaign.templateName}'",
                    details = ""
                )
            )
        }
    }

    // --- Playwright Job Actions ---
    fun createPlaywrightJob(name: String, targetUrl: String, scriptType: String, cronSchedule: String, isHighPriority: Boolean = false) {
        viewModelScope.launch {
            val job = PlaywrightJob(
                name = name,
                targetUrl = targetUrl,
                scriptType = scriptType,
                cronSchedule = cronSchedule,
                status = "Idle",
                isHighPriority = isHighPriority
            )
            repository.insertPlaywrightJob(job)
            repository.insertLog(
                SecurityLog(
                    eventType = "PLAYWRIGHT_EXEC",
                    message = "Created E2E browser automation job: '$name' ${if (isHighPriority) "[HIGH PRIORITY]" else ""}",
                    details = "Target URL: $targetUrl | Script: $scriptType | Cron: $cronSchedule | High Priority: $isHighPriority"
                )
            )
        }
    }

    fun runPlaywrightJob(job: PlaywrightJob, forceFail: Boolean = false) {
        viewModelScope.launch {
            if (runningJobId.value != null) return@launch // prevent running parallel on simulation UI

            runningJobId.value = job.id
            activeJobLogs.value = ""
            
            val updatedJobToRunning = job.copy(status = "Running", lastRunTime = System.currentTimeMillis())
            repository.updatePlaywrightJob(updatedJobToRunning)

            repository.insertLog(
                SecurityLog(
                    eventType = "PLAYWRIGHT_EXEC",
                    message = "Starting Playwright container for E2E execution",
                    details = "Job: ${job.name} | URL: ${job.targetUrl}"
                )
            )

            val logSteps = if (forceFail) {
                listOf(
                    "Initializing DabelsTech headless runner environment...",
                    "Pulling playwright/chromium:latest container image...",
                    "Launching headless Chromium browser (viewport: 1280x800)...",
                    "Navigating to: ${job.targetUrl}",
                    "Page content-type: text/html loaded in 842ms.",
                    "Executing automated script actions...",
                    "Error: Connection reset by peer at: ${job.targetUrl}",
                    "Warning: Playwright failed to assert page DOM state.",
                    "Capturing exception trace logs...",
                    "Terminating sandbox container due to assertion failure."
                )
            } else {
                listOf(
                    "Initializing DabelsTech headless runner environment...",
                    "Pulling playwright/chromium:latest container image...",
                    "Launching headless Chromium browser (viewport: 1280x800)...",
                    "Navigating to: ${job.targetUrl}",
                    "Page content-type: text/html loaded in 842ms.",
                    "Executing automated script actions...",
                    "Waiting for network idle state (0 active connections)...",
                    "Bypassing potential bot detection heuristics...",
                    "Asserting page assertions and validating HTML DOM state...",
                    "Capturing reference snapshot screenshot of full page...",
                    "Playwright execution completed. Releasing sandbox container assets."
                )
            }

            val startTime = System.currentTimeMillis()
            
            for (step in logSteps) {
                activeJobLogs.value += "[${System.currentTimeMillis() % 100000}] $step\n"
                delay(400) // smooth visual stream of logs
            }

            val duration = System.currentTimeMillis() - startTime
            val finalStatus = if (forceFail) "Failed" else "Success"

            val finalLogs = if (forceFail) {
                activeJobLogs.value + "[error] Job execution failed with 1 error in ${duration}ms."
            } else {
                activeJobLogs.value + "[success] Job finished with 0 errors in ${duration}ms."
            }

            val completedJob = job.copy(
                status = finalStatus,
                lastRunTime = System.currentTimeMillis(),
                durationMs = duration,
                logOutput = finalLogs
            )
            
            repository.updatePlaywrightJob(completedJob)
            runningJobId.value = null

            repository.insertLog(
                SecurityLog(
                    eventType = "PLAYWRIGHT_EXEC",
                    message = if (forceFail) "E2E Playwright run failed for: ${job.name}" else "E2E Playwright run succeeded for: ${job.name}",
                    details = "Duration: ${duration}ms | Status: ${finalStatus.uppercase()}"
                )
            )
        }
    }

    fun deletePlaywrightJob(job: PlaywrightJob) {
        viewModelScope.launch {
            repository.deletePlaywrightJob(job)
            repository.insertLog(
                SecurityLog(
                    eventType = "PLAYWRIGHT_EXEC",
                    message = "Deleted Playwright job configuration: '${job.name}'",
                    details = ""
                )
            )
        }
    }

    fun clearSecurityLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // --- Emails and Google Drive Connection Actions ---
    fun connectEmails(email: String) {
        viewModelScope.launch {
            isConnectingEmail.value = true
            delay(1500)
            isEmailConnected.value = true
            connectedEmailAddress.value = email
            isConnectingEmail.value = false
            
            // Populate simulated synced emails
            val existing = syncedEmails.value
            syncedEmails.value = existing + listOf(
                EmailMessage("1", "security-alert@auth0.com", "New Login Detected", "New Login Detected from Chrome Android", "10:45 AM", false),
                EmailMessage("2", "playwright-monitor@dabelstech.com", "Job Succeeded", "Job 'SEO Meta and Header Auditor' succeeded", "9:15 AM", true),
                EmailMessage("3", "marketing-leads@hubspot.com", "New Leads Generated", "5 New Trial Signups in Last 24 Hours", "Yesterday", true),
                EmailMessage("4", "billing@dabelstech.com", "Payment Complete", "Invoice Paid Successfully - $49.00/mo Plan", "Jul 5", true)
            )

            repository.insertLog(
                SecurityLog(
                    eventType = "EMAIL_DISPATCH",
                    message = "Connected emails app to $email",
                    details = "IMAP/SMTP sync workspace active. 4 recent emails synchronized."
                )
            )
        }
    }

    fun disconnectEmails() {
        viewModelScope.launch {
            isEmailConnected.value = false
            connectedEmailAddress.value = ""
            syncedEmails.value = emptyList()
            repository.insertLog(
                SecurityLog(
                    eventType = "EMAIL_DISPATCH",
                    message = "Disconnected emails app",
                    details = "Cleared cached email synchronization session."
                )
            )
        }
    }

    fun connectGoogleDrive(email: String) {
        viewModelScope.launch {
            isConnectingDrive.value = true
            delay(1500)
            isGoogleDriveConnected.value = true
            driveUserEmail.value = email
            isConnectingDrive.value = false

            // Populate simulated synced drive files
            driveFiles.value = listOf(
                DriveFile("1", "E2E_Test_Plan_2026.docx", "Document", "2.4 MB", "Jul 6, 2026"),
                DriveFile("2", "Automation_Metrics_Q2.xlsx", "Spreadsheet", "15.8 MB", "Jul 4, 2026"),
                DriveFile("3", "DabelsTech_Logo_Asset.png", "Image", "412 KB", "Jun 28, 2026"),
                DriveFile("4", "Security_Postures_Report.pdf", "PDF", "1.2 MB", "Jun 15, 2026")
            )

            repository.insertLog(
                SecurityLog(
                    eventType = "API_INVOCATION",
                    message = "Connected Google Drive workspace for $email",
                    details = "Drive OAuth integration authorized. Synced 4 core files."
                )
            )
        }
    }

    fun disconnectGoogleDrive() {
        viewModelScope.launch {
            isGoogleDriveConnected.value = false
            driveUserEmail.value = ""
            driveFiles.value = emptyList()
            repository.insertLog(
                SecurityLog(
                    eventType = "API_INVOCATION",
                    message = "Disconnected Google Drive workspace",
                    details = "Revoked Drive file-access OAuth permissions."
                )
            )
        }
    }

    fun generatePlaywrightSummaryWithGemini() {
        viewModelScope.launch {
            isGeneratingSummary.value = true
            playwrightSummary.value = "Consulting Gemini AI Automation Intelligence..."
            
            val jobs = playwrightJobs.value
            if (jobs.isEmpty()) {
                playwrightSummary.value = "No Playwright jobs are configured yet. Please configure some E2E jobs first in the Playwright tab so Gemini can analyze them."
                isGeneratingSummary.value = false
                return@launch
            }

            // Construct jobs description list
            val jobsStr = jobs.joinToString("\n") { job ->
                "- Name: ${job.name}, Target: ${job.targetUrl}, Script: ${job.scriptType}, Status: ${job.status}, Last Run Time: ${if (job.lastRunTime > 0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(job.lastRunTime)) else "Never"}"
            }

            val prompt = """
                You are DabelsTech's Lead AI E2E Quality & Automation Engineer. Below is a list of configured Playwright browser automation jobs in our test suite.
                Your task:
                1. Provide a concise, highly readable executive summary of these jobs, calling out any failures, runs, or idle statuses.
                2. Suggest an optimized execution priority queue (e.g. Critical, High, Medium, Low) based on the target URL (production vs login vs staging) and current status. Specify which job should run first.
                3. Offer 2 actionable tips for improving our E2E browser automation stability.

                Here is the list of configured Playwright jobs:
                $jobsStr

                Format your response using bold Markdown headings and bullet points for beautiful rendering on our Slate dashboard.
            """.trimIndent()

            // Call Gemini via OkHttp directly
            val response = callGemini(prompt)
            playwrightSummary.value = response
            isGeneratingSummary.value = false

            repository.insertLog(
                SecurityLog(
                    eventType = "PLAYWRIGHT_EXEC",
                    message = "Gemini AI Job Prioritization Completed",
                    details = "Analyzed ${jobs.size} jobs. Result summary generated."
                )
            )
        }
    }

    private suspend fun callGemini(prompt: String): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: Gemini API key is not configured. Please add your key to the Secrets panel in AI Studio."
        }
        
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        
        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
        }
        
        val mediaType = "application/json".toMediaType()
        val body = jsonBody.toString().toRequestBody(mediaType)
        
        val client = OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
            
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()
            
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Error: API call failed with code ${response.code} - ${response.message}"
                }
                val bodyString = response.body?.string() ?: return@withContext "Error: Empty response body"
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val firstPart = parts?.optJSONObject(0)
                firstPart?.optString("text") ?: "Error: No text in response candidates"
            }
        } catch (e: Exception) {
            "Error calling Gemini: ${e.localizedMessage}"
        }
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
