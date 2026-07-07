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
                            logOutput = "[info] Launching Chromium headless...\n[navigation] Goto https://auth.dabelstech.com/login\n[action] Fill email & password\n[action] Click 'Continue'\n[status] Dashboard loaded. JWT generated. Run completed successfully."
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
                            logOutput = "[info] Launching WebKit...\n[navigation] Goto https://dabelstech.com\n[assert] H1 tag present\n[assert] Meta description present\n[status] SEO criteria satisfied."
                        )
                    )
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
    fun createPlaywrightJob(name: String, targetUrl: String, scriptType: String, cronSchedule: String) {
        viewModelScope.launch {
            val job = PlaywrightJob(
                name = name,
                targetUrl = targetUrl,
                scriptType = scriptType,
                cronSchedule = cronSchedule,
                status = "Idle"
            )
            repository.insertPlaywrightJob(job)
            repository.insertLog(
                SecurityLog(
                    eventType = "PLAYWRIGHT_EXEC",
                    message = "Created E2E browser automation job: '$name'",
                    details = "Target URL: $targetUrl | Script: $scriptType | Cron: $cronSchedule"
                )
            )
        }
    }

    fun runPlaywrightJob(job: PlaywrightJob) {
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

            val logSteps = listOf(
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

            val startTime = System.currentTimeMillis()
            
            for (step in logSteps) {
                activeJobLogs.value += "[${System.currentTimeMillis() % 100000}] $step\n"
                delay(400) // smooth visual stream of logs
            }

            val duration = System.currentTimeMillis() - startTime
            val finalStatus = "Success" // Always succeed for demonstration

            val finalLogs = activeJobLogs.value + "[success] Job finished with 0 errors in ${duration}ms."
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
                    message = "E2E Playwright run succeeded for: ${job.name}",
                    details = "Duration: ${duration}ms | Status: SUCCESS"
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
