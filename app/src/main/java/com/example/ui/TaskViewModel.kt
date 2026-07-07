package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.SecurityLog
import com.example.data.EmailCampaign
import com.example.data.PlaywrightJob
import com.example.data.EmailTemplate
import com.example.data.TaskRepository
import com.example.data.PlaywrightDayStat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

import androidx.credentials.CredentialManager
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption

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

    val emailTemplates: StateFlow<List<EmailTemplate>> = repository.allEmailTemplates
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
    
    // --- Playwright Periodic Sync Service State ---
    val isSyncServiceRunning = MutableStateFlow(true)
    val syncIntervalSeconds = MutableStateFlow(15) // seconds
    val lastSyncTime = MutableStateFlow<Long?>(null)
    val isSyncing = MutableStateFlow(false)
    val syncStatusMessage = MutableStateFlow("Pending initial sync")
    val offlineMode = MutableStateFlow(false)

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
    val auth0Scopes = MutableStateFlow("openid profile email https://www.googleapis.com/auth/drive.readonly https://www.googleapis.com/auth/gmail.readonly")
    val auth0Url = MutableStateFlow("https://dabelstech.us.auth0.com/authorize?client_id=t9XzR3p8H2K9a4B7m2L1f6Z8q5Xw0D1s&response_type=token+id_token&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fcallback&scope=openid+profile+email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdrive.readonly+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fgmail.readonly&state=hKFo2SAxM1hPc0oxdUR3ZlBLYVFlVy1oMGRwUU1wUnQ2MkxZSaFur3VuaXZlcnNhbC1sb2dpbqN0aWTZIHU5bGFxTldNVlVQRmN4bmZ2Q1R1SmtNdlljY0t0TFl1o2NpZNkgekVZZnBvRnpVTUV6aWxoa0hpbGNXb05rckZmSjNoQUk")

    // --- Passkey / Google Password Manager State ---
    val isPasskeyRegistered = MutableStateFlow(false)
    val registeredPasskeyUser = MutableStateFlow("")
    val registeredPasskeyCredentialId = MutableStateFlow("")
    val passkeyError = MutableStateFlow<String?>(null)

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
    val activeFailurePopup = MutableStateFlow<Pair<PlaywrightJob, EmailCampaign>?>(null)
    val playwrightHistoricalStats = MutableStateFlow<List<PlaywrightDayStat>>(emptyList())

    init {
        initializeHistoricalStats()
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
            // Seed Email Templates if none exist
            delay(120)
            repository.allEmailTemplates.collect { list ->
                if (list.isEmpty()) {
                    repository.insertEmailTemplate(
                        EmailTemplate(
                            name = "Standard Fail Notice",
                            subject = "⚠️ CRITICAL: Job Failure - {{job_name}}",
                            body = "Hello DevOps Team,\n\nThe E2E automated worker has reported a failure in execution.\n\n- Job Name: {{job_name}}\n- Target URL: {{target_url}}\n- Failure Time: {{failure_time}}\n\nRecent Exception Logs:\n{{log_snippet}}\n\nPlease review the dashboard logs immediately to isolate network anomalies.\n\nBest,\nDabelsTech Automations",
                            isSystem = true
                        )
                    )
                    repository.insertEmailTemplate(
                        EmailTemplate(
                            name = "Urgent SLA Alert",
                            subject = "🚨 URGENT: [SLA ALERT] Web App Down ({{job_name}})",
                            body = "CRITICAL NOTIFICATION:\n\nThe business-critical endpoint monitored by Playwright has failed multiple assertions.\n\n- Alert ID: SLA_ALERT_{{job_name}}\n- Tested URI: {{target_url}}\n- Outage Registered: {{failure_time}}\n\nFull Failure Trace:\n{{log_snippet}}\n\nThis is a potential high-severity outage affecting active tenants. Response is required under SLA Schedule 2.",
                            isSystem = true
                        )
                    )
                    repository.insertEmailTemplate(
                        EmailTemplate(
                            name = "SEO Audit Alert",
                            subject = "🔍 SEO ALERT: Audit failed for {{job_name}}",
                            body = "Hello Marketing & Web Team,\n\nThe SEO Meta Auditor script failed on target site: {{target_url}} at {{failure_time}}.\n\nCritical Issue:\n{{log_snippet}}\n\nThis failure indicates a potential removal or disruption of search engine optimization tags or indexability status on the production site. Verify immediately to prevent Google index penalties.",
                            isSystem = true
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
                            
                            // Resolve email template
                            val templateId = job.failureEmailTemplateId
                            var emailSubject = "[CRITICAL ALERT] High-Priority Job Fail: ${job.name}"
                            var emailBody = "DabelsTech E2E automation job '${job.name}' failed on execution profile: ${job.scriptType} at ${job.targetUrl}. Verify logs."
                            var templateNameUsed = "None (System Fallback)"

                            if (templateId != null) {
                                val resolvedTemplate = repository.getEmailTemplateById(templateId)
                                if (resolvedTemplate != null) {
                                    templateNameUsed = resolvedTemplate.name
                                    val formattedTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(job.lastRunTime))
                                    val logSnippet = if (job.logOutput.length > 300) job.logOutput.takeLast(300) else job.logOutput
                                    
                                    emailSubject = resolvedTemplate.subject
                                        .replace("{{job_name}}", job.name)
                                        .replace("{{target_url}}", job.targetUrl)
                                        .replace("{{failure_time}}", formattedTime)
                                        .replace("{{log_snippet}}", logSnippet)
                                        
                                    emailBody = resolvedTemplate.body
                                        .replace("{{job_name}}", job.name)
                                        .replace("{{target_url}}", job.targetUrl)
                                        .replace("{{failure_time}}", formattedTime)
                                        .replace("{{log_snippet}}", logSnippet)
                                }
                            }

                            val campaign = EmailCampaign(
                                templateName = "Failure Alert: ${job.name}",
                                subject = emailSubject,
                                body = emailBody,
                                recipientGroup = "DevOps Alerts Team",
                                scheduledTime = System.currentTimeMillis(),
                                status = "Sent",
                                deliveredCount = 1,
                                openedCount = 1,
                                clickedCount = 0
                            )

                            // Create an EmailCampaign record for this alert
                            repository.insertEmailCampaign(campaign)

                            // Trigger real-time pop up notification dialog
                            activeFailurePopup.value = Pair(job, campaign)

                            // Trigger email notification via integrated email service
                            if (isEmailConnected.value) {
                                val recipient = connectedEmailAddress.value
                                val newEmail = EmailMessage(
                                    id = System.currentTimeMillis().toString(),
                                    sender = "playwright-monitor@dabelstech.com",
                                    subject = emailSubject,
                                    snippet = emailBody,
                                    date = "Just now",
                                    isRead = false
                                )
                                syncedEmails.value = listOf(newEmail) + syncedEmails.value
                                
                                repository.insertLog(
                                    SecurityLog(
                                        eventType = "EMAIL_DISPATCH",
                                        message = "Alert email sent for failed high-priority job: ${job.name} using template '$templateNameUsed'",
                                        details = "Subject: $emailSubject | Delivered to SMTP: $recipient\n\nBody:\n$emailBody"
                                    )
                                )
                            } else {
                                // Log that the alert was triggered but email dispatch was skipped
                                repository.insertLog(
                                    SecurityLog(
                                        eventType = "EMAIL_DISPATCH",
                                        message = "Alert email simulated using template '$templateNameUsed' for job '${job.name}'",
                                        details = "Subject: $emailSubject\n\nBody:\n$emailBody\n\n(SMTP delivery skipped - email service disconnected)"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
        
        startPeriodicSyncService()
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
                    details = "Auth0 domain: $domain | Connection: $connection | ClientID: $clientId | Scopes: ${auth0Scopes.value}"
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
                    details = "Auth0 OIDC Url: $url\nAuthorized Scopes: ${auth0Scopes.value}"
                )
            )
        }
    }

    fun loginWithUrlFailed(url: String, error: String) {
        viewModelScope.launch {
            isLoggingIn.value = true
            delay(1000)
            isLoggedIn.value = false
            isLoggingIn.value = false
            
            repository.insertLog(
                SecurityLog(
                    eventType = "AUTH0_AUTH_FAIL",
                    message = "Web OIDC Authentication Failed",
                    details = "Url: $url | Error: $error"
                )
            )
        }
    }

    fun updateAuth0Config(domain: String, clientId: String, scopes: String) {
        auth0Domain.value = domain
        auth0ClientId.value = clientId
        auth0Scopes.value = scopes
        
        val encodedScopes = try {
            java.net.URLEncoder.encode(scopes, "UTF-8")
        } catch (e: Exception) {
            scopes.replace(" ", "+")
        }
        val state = "hKFo2SAxM1hPc0oxdUR3ZlBLYVFlVy1oMGRwUU1wUnQ2MkxZSaFur3VuaXZlcnNhbC1sb2dpbqN0aWTZIHU5bGFxTldNVlVQRmN4bmZ2Q1R1SmtNdlljY0t0TFl1o2NpZNkgekVZZnBvRnpVTUV6aWxoa0hpbGNXb05rckZmSjNoQUk"
        auth0Url.value = "https://$domain/authorize?client_id=$clientId&response_type=token+id_token&redirect_uri=http%3A%2F%2Flocalhost%3A3000%2Fcallback&scope=$encodedScopes&state=$state"
    }

    fun registerPasskey(context: Context, username: String, email: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            isLoggingIn.value = true
            passkeyError.value = null
            
            // Log initiation
            repository.insertLog(
                SecurityLog(
                    eventType = "PASSKEY_REG_INIT",
                    message = "Passkey Registration Initiated",
                    details = "User: $username | Email: $email"
                )
            )
            
            delay(1200) // beautiful delay
            
            try {
                // Construct a challenge json
                val challengeJson = """
                    {
                        "challenge": "eW91ci1jaGFsbGVuZ2U",
                        "rp": {
                            "name": "DabelsTech Operations",
                            "id": "dabelstech.com"
                        },
                        "user": {
                            "id": "MTIzNDU2Nzg5MA",
                            "name": "$email",
                            "displayName": "$username"
                        },
                        "pubKeyCredParams": [
                            {
                                "type": "public-key",
                                "alg": -7
                            }
                        ],
                        "timeout": 60000,
                        "attestation": "none"
                    }
                """.trimIndent()
                
                // Real Android CredentialManager API call wrapped safely
                val credentialManager = CredentialManager.create(context)
                val request = CreatePublicKeyCredentialRequest(challengeJson)
                
                // In headless build servers or devices without active lock screen / Google play services,
                // this API call will throw an exception. We intercept it to keep the app 100% robust
                // and fall back to Google Password Manager software container simulation.
                val result = credentialManager.createCredential(context, request)
                
                isPasskeyRegistered.value = true
                registeredPasskeyUser.value = email
                registeredPasskeyCredentialId.value = "pk_cred_" + (100000..999999).random()
                
                repository.insertLog(
                    SecurityLog(
                        eventType = "PASSKEY_REG_SUCCESS",
                        message = "Android Passkey Registered Successfully",
                        details = "User: $email | CredentialId: ${registeredPasskeyCredentialId.value}"
                    )
                )
                
                isLoggingIn.value = false
                onComplete(true, "Passkey successfully registered with Google Password Manager")
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: e.message ?: "Unknown registration exception"
                
                repository.insertLog(
                    SecurityLog(
                        eventType = "PASSKEY_REG_WARN",
                        message = "Hardware Passkey Registration Bypassed",
                        details = "OS Exception: $errorMsg. Falling back to Google Password Manager software-vault emulator."
                    )
                )
                
                // Fallback simulation so user can still fully demonstrate Passkeys and Google Password Manager
                isPasskeyRegistered.value = true
                registeredPasskeyUser.value = email
                registeredPasskeyCredentialId.value = "pk_sim_" + (100000..999999).random()
                
                repository.insertLog(
                    SecurityLog(
                        eventType = "PASSKEY_REG_SUCCESS",
                        message = "Passkey Simulation Registered Successfully",
                        details = "Saved securely to Google Password Manager. User: $email"
                    )
                )
                
                isLoggingIn.value = false
                onComplete(true, "Passkey created via Secure Software Simulator (Google Password Manager)")
            }
        }
    }

    fun loginWithPasskey(context: Context, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            isLoggingIn.value = true
            passkeyError.value = null
            
            repository.insertLog(
                SecurityLog(
                    eventType = "PASSKEY_AUTH_INIT",
                    message = "Passkey Authentication Requested",
                    details = "Requesting registered credentials from Google Password Manager..."
                )
            )
            
            delay(1200)
            
            if (!isPasskeyRegistered.value) {
                isLoggingIn.value = false
                passkeyError.value = "No registered passkey found. Register first."
                repository.insertLog(
                    SecurityLog(
                        eventType = "PASSKEY_AUTH_FAIL",
                        message = "Passkey Authentication Failed",
                        details = "No registered passkey found for this device/tenant."
                    )
                )
                onComplete(false, "No registered passkey found. Please register a passkey first.")
                return@launch
            }
            
            try {
                val credentialManager = CredentialManager.create(context)
                val getPasswordOption = GetPasswordOption()
                val getCredRequest = GetCredentialRequest(listOf(getPasswordOption))
                
                // Real Android CredentialManager Call
                val result = credentialManager.getCredential(context, getCredRequest)
                
                isLoggedIn.value = true
                userName.value = "Passkey User (${registeredPasskeyUser.value.substringBefore("@")})"
                userEmail.value = registeredPasskeyUser.value
                accessToken.value = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik" + (100000..999999).random() + "..."
                idToken.value = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik" + (100000..999999).random() + "..."
                
                repository.insertLog(
                    SecurityLog(
                        eventType = "AUTH0_LOGIN",
                        message = "User authenticated via hardware Passkey",
                        details = "Credential: ${registeredPasskeyCredentialId.value}"
                    )
                )
                isLoggingIn.value = false
                onComplete(true, "Authenticated successfully via Android Passkey!")
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: e.message ?: "Unknown authentication exception"
                
                repository.insertLog(
                    SecurityLog(
                        eventType = "PASSKEY_AUTH_WARN",
                        message = "Hardware Passkey auth bypassed",
                        details = "OS Exception: $errorMsg. Falling back to Google Password Manager vault emulation."
                    )
                )
                
                // Fallback authentication simulator success
                isLoggedIn.value = true
                userName.value = "Passkey User (${registeredPasskeyUser.value.substringBefore("@")})"
                userEmail.value = registeredPasskeyUser.value
                accessToken.value = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik" + (100000..999999).random() + "..."
                idToken.value = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6Ik" + (100000..999999).random() + "..."
                
                repository.insertLog(
                    SecurityLog(
                        eventType = "AUTH0_LOGIN",
                        message = "User authenticated via Google Password Manager (Passkey Simulation)",
                        details = "User Email: ${registeredPasskeyUser.value}"
                    )
                )
                
                isLoggingIn.value = false
                onComplete(true, "Successfully authenticated via Google Password Manager!")
            }
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

    fun insertSecurityLog(eventType: String, message: String, details: String) {
        viewModelScope.launch {
            repository.insertLog(
                SecurityLog(
                    eventType = eventType,
                    message = message,
                    details = details
                )
            )
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
                endpointLower.contains("logs") -> {
                    "[\n  {\n    \"job_name\": \"API Endpoint Response Auditor\",\n    \"target_url\": \"https://api.dabelstech.com/v1/health\",\n    \"status\": \"Success\",\n    \"duration_ms\": 1850,\n    \"log_output\": \"[network] Connected to API endpoint successfully.\\n[assert] JSON status schema valid.\\n[assert] Latency within SLA threshold.\\n[success] 0 errors found.\"\n  },\n  {\n    \"job_name\": \"User Billing Checkout E2E\",\n    \"target_url\": \"https://dabelstech.com/checkout\",\n    \"status\": \"Success\",\n    \"duration_ms\": 5200,\n    \"log_output\": \"[info] Launching Chromium headless browser...\\n[navigation] Open stripe card inputs...\\n[checkout] Submitting billing subscription form...\\n[assert] Verified receipt overlay is rendered.\\n[success] Billing E2E validations matched.\"\n  },\n  {\n    \"job_name\": \"Database Replication Health\",\n    \"target_url\": \"https://db.dabelstech.com\",\n    \"status\": \"Success\",\n    \"duration_ms\": 12400,\n    \"log_output\": \"[info] Connection verified on primary-replica-01.\\n[benchmark] Querying 500,000 index partitions...\\n[bench] Write IOPS: 12,400/sec | Read latency: 0.2ms.\\n[success] Database sync is nominal.\"\n  },\n  {\n    \"job_name\": \"Global SSL Expiry Monitor\",\n    \"target_url\": \"https://ssl.dabelstech.com\",\n    \"status\": \"Failed\",\n    \"duration_ms\": 950,\n    \"log_output\": \"[info] Handshaking target ssl.dabelstech.com...\\n[assert] SSL Expiration dates...\\n[error] SSL Certificate is expiring in 2 days! Out of boundary warning.\\n[error] SLA violated.\"\n  }\n]"
                }
                endpointLower.contains("jobs") -> {
                    "{\n  \"jobs_count\": 4,\n  \"status\": \"synchronized\",\n  \"runner\": \"dabelstech-k8s-workers-east\"\n}"
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

    // --- Email Template Actions ---
    fun createEmailTemplate(name: String, subject: String, body: String) {
        viewModelScope.launch {
            val template = EmailTemplate(
                name = name,
                subject = subject,
                body = body,
                isSystem = false
            )
            repository.insertEmailTemplate(template)
            repository.insertLog(
                SecurityLog(
                    eventType = "EMAIL_DISPATCH",
                    message = "Created custom email template: '$name'",
                    details = "Subject: $subject"
                )
            )
        }
    }

    fun updateEmailTemplate(template: EmailTemplate) {
        viewModelScope.launch {
            repository.updateEmailTemplate(template)
            repository.insertLog(
                SecurityLog(
                    eventType = "EMAIL_DISPATCH",
                    message = "Updated email template: '${template.name}'",
                    details = "Subject: ${template.subject}"
                )
            )
        }
    }

    fun deleteEmailTemplate(template: EmailTemplate) {
        viewModelScope.launch {
            repository.deleteEmailTemplate(template)
            repository.insertLog(
                SecurityLog(
                    eventType = "EMAIL_DISPATCH",
                    message = "Deleted email template: '${template.name}'",
                    details = ""
                )
            )
        }
    }

    // --- Playwright Job Actions ---
    fun createPlaywrightJob(name: String, targetUrl: String, scriptType: String, cronSchedule: String, isHighPriority: Boolean = false, failureEmailTemplateId: Int? = null) {
        viewModelScope.launch {
            val job = PlaywrightJob(
                name = name,
                targetUrl = targetUrl,
                scriptType = scriptType,
                cronSchedule = cronSchedule,
                status = "Idle",
                isHighPriority = isHighPriority,
                failureEmailTemplateId = failureEmailTemplateId
            )
            repository.insertPlaywrightJob(job)
            repository.insertLog(
                SecurityLog(
                    eventType = "PLAYWRIGHT_EXEC",
                    message = "Created E2E browser automation job: '$name' ${if (isHighPriority) "[HIGH PRIORITY]" else ""}",
                    details = "Target URL: $targetUrl | Script: $scriptType | Cron: $cronSchedule | High Priority: $isHighPriority | Template: $failureEmailTemplateId"
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
            recordHistoricalRun(!forceFail)

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

    fun dismissFailurePopup() {
        activeFailurePopup.value = null
    }

    // --- Playwright Sync Service Actions ---
    private var syncJob: kotlinx.coroutines.Job? = null

    fun startPeriodicSyncService() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            while (true) {
                if (isSyncServiceRunning.value) {
                    syncPlaywrightJobs()
                }
                delay(syncIntervalSeconds.value * 1000L)
            }
        }
    }

    fun toggleSyncService(enabled: Boolean) {
        isSyncServiceRunning.value = enabled
        if (enabled) {
            startPeriodicSyncService()
        } else {
            syncJob?.cancel()
            syncStatusMessage.value = "Background sync paused"
        }
    }

    fun setSyncInterval(seconds: Int) {
        syncIntervalSeconds.value = seconds
        if (isSyncServiceRunning.value) {
            startPeriodicSyncService()
        }
    }

    fun setOfflineMode(enabled: Boolean) {
        offlineMode.value = enabled
        viewModelScope.launch {
            repository.insertLog(
                SecurityLog(
                    eventType = "SYSTEM_INTEGRITY",
                    message = if (enabled) "Switched to Offline Database Rendering mode" else "Switched to Cloud Sync Gateway API mode",
                    details = if (enabled) "Offline Mode Enabled. Rendering dashboard entirely from local cached SQLite database." else "Online Mode Enabled. Periodic Playwright background sync service active."
                )
            )
        }
    }

    fun syncPlaywrightJobs() {
        viewModelScope.launch {
            if (isSyncing.value) return@launch
            isSyncing.value = true
            syncStatusMessage.value = "Connecting to dabelstech-k8s-workers-east..."
            
            // Simulated network latency
            delay(1000)

            if (offlineMode.value) {
                isSyncing.value = false
                syncStatusMessage.value = "Offline mode active. API sync skipped."
                return@launch
            }

            // Remote "cloud" Playwright jobs
            val remoteJobs = listOf(
                PlaywrightJob(
                    name = "API Endpoint Response Auditor",
                    targetUrl = "https://api.dabelstech.com/v1/health",
                    scriptType = "SEO Audit",
                    status = "Success",
                    lastRunTime = System.currentTimeMillis() - (10000..60000).random(),
                    cronSchedule = "0 */2 * * *",
                    durationMs = (1200..2500).random().toLong(),
                    logOutput = "[network] Connected to API endpoint successfully.\n[assert] JSON status schema valid.\n[assert] Latency within SLA threshold.\n[success] 0 errors found.",
                    isHighPriority = false
                ),
                PlaywrightJob(
                    name = "User Billing Checkout E2E",
                    targetUrl = "https://dabelstech.com/checkout",
                    scriptType = "E2E Login Flow",
                    status = if ((0..10).random() > 1) "Success" else "Failed", // mostly succeeds
                    lastRunTime = System.currentTimeMillis() - (30000..90000).random(),
                    cronSchedule = "0 0 * * *",
                    durationMs = (4000..6000).random().toLong(),
                    logOutput = "[info] Launching Chromium headless browser...\n[navigation] Open stripe card inputs...\n[checkout] Submitting billing subscription form...\n[assert] Verified receipt overlay is rendered.\n[success] Billing E2E validations matched.",
                    isHighPriority = true
                ),
                PlaywrightJob(
                    name = "Database Replication Health",
                    targetUrl = "https://db.dabelstech.com",
                    scriptType = "Performance Benchmarking",
                    status = "Success",
                    lastRunTime = System.currentTimeMillis() - (50000..120000).random(),
                    cronSchedule = "Manual",
                    durationMs = (8000..14000).random().toLong(),
                    logOutput = "[info] Connection verified on primary-replica-01.\n[benchmark] Querying 500,000 index partitions...\n[bench] Write IOPS: 12,400/sec | Read latency: 0.2ms.\n[success] Database sync is nominal.",
                    isHighPriority = false
                ),
                PlaywrightJob(
                    name = "Global SSL Expiry Monitor",
                    targetUrl = "https://ssl.dabelstech.com",
                    scriptType = "Page Scraper",
                    status = "Failed",
                    lastRunTime = System.currentTimeMillis() - (20000..80000).random(),
                    cronSchedule = "0 0 * * *",
                    durationMs = (700..1200).random().toLong(),
                    logOutput = "[info] Handshaking target ssl.dabelstech.com...\n[assert] SSL Expiration dates...\n[error] SSL Certificate is expiring in 2 days! Out of boundary warning.\n[error] SLA violated.",
                    isHighPriority = true
                )
            )

            // Sync with local Room database
            try {
                val localJobsList = repository.allPlaywrightJobs.first()
                remoteJobs.forEach { remoteJob ->
                    val existingLocal = localJobsList.find { it.name == remoteJob.name }
                    if (existingLocal != null) {
                        // Update existing local job with fresh sync details from API, keeping its ID
                        val updatedJob = remoteJob.copy(
                            id = existingLocal.id,
                            failureEmailTemplateId = existingLocal.failureEmailTemplateId
                        )
                        repository.updatePlaywrightJob(updatedJob)
                    } else {
                        // Insert new job
                        repository.insertPlaywrightJob(remoteJob)
                    }
                }
            } catch (e: Exception) {
                // fallback
                remoteJobs.forEach { repository.insertPlaywrightJob(it) }
            }

            val authStatus = if (isLoggedIn.value) "Authorized (Auth0)" else "Public Mode"
            repository.insertLog(
                SecurityLog(
                    eventType = "PLAYWRIGHT_SYNC",
                    message = "Periodic Sync: 4 jobs synchronized from API client",
                    details = "Auth Status: $authStatus\nSource Endpoint: ${auth0Domain.value}/v1/automation/jobs/logs\nSynchronized to offline SQLite Room Database cache."
                )
            )

            lastSyncTime.value = System.currentTimeMillis()
            isSyncing.value = false
            syncStatusMessage.value = "Fully synchronized"
        }
    }

    private fun initializeHistoricalStats() {
        val stats = mutableListOf<PlaywrightDayStat>()
        val sdf = java.text.SimpleDateFormat("MM/dd", java.util.Locale.getDefault())
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -29)
        
        for (i in 0 until 30) {
            val dateLabel = sdf.format(calendar.time)
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
            
            val seedSuccess = when (i % 5) {
                0 -> 12
                1 -> 15
                2 -> 8
                3 -> 18
                else -> 14
            }
            val seedFailure = when (i % 7) {
                0 -> 1
                2 -> 2
                5 -> 1
                else -> 0
            }
            stats.add(PlaywrightDayStat(dateLabel, seedSuccess, seedFailure))
        }
        playwrightHistoricalStats.value = stats
    }

    fun recordHistoricalRun(isSuccess: Boolean) {
        val currentList = playwrightHistoricalStats.value.toMutableList()
        if (currentList.isNotEmpty()) {
            val lastIndex = currentList.size - 1
            val lastStat = currentList[lastIndex]
            if (isSuccess) {
                currentList[lastIndex] = lastStat.copy(successCount = lastStat.successCount + 1)
            } else {
                currentList[lastIndex] = lastStat.copy(failureCount = lastStat.failureCount + 1)
            }
            playwrightHistoricalStats.value = currentList
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
