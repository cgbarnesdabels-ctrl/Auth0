package com.example.data

import android.util.Log
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Data model for transactional email requests.
 */
data class EmailRequest(
    val to: String,
    val subject: String,
    val body: String,
    val templateId: String? = null
)

/**
 * API interface for the email provider (e.g., SendGrid, Mailgun, or custom backend).
 */
interface EmailApi {
    @POST("v1/emails/send")
    suspend fun sendEmail(@Body request: EmailRequest): Response<Unit>
}

/**
 * EmailService handles the business logic for sending automated communications.
 */
class EmailService(private val authProvider: Auth0AuthProvider) {

    private val api: EmailApi by lazy {
        ApiClient.createService(EmailApi::class.java, authProvider)
    }

    /**
     * Sends a welcome email to a newly authenticated user.
     */
    suspend fun sendWelcomeEmail(userEmail: String, userName: String) {
        val request = EmailRequest(
            to = userEmail,
            subject = "Welcome to the Platform!",
            body = "Hello $userName,\n\nThank you for authenticating. Your secure workspace is now ready.",
            templateId = "welcome_modern_v1"
        )

        try {
            Log.d("EmailService", "Attempting to send welcome email to: $userEmail")
            val response = api.sendEmail(request)
            
            if (response.isSuccessful) {
                Log.i("EmailService", "Welcome email successfully queued for $userEmail")
            } else {
                Log.e("EmailService", "Failed to send email. Provider returned: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("EmailService", "Network error while sending email: ${e.message}")
        }
    }

    /**
     * Sends a security alert for sensitive actions.
     */
    suspend fun sendSecurityAlert(userEmail: String, action: String) {
        val request = EmailRequest(
            to = userEmail,
            subject = "Security Alert: New Action Detected",
            body = "A new sensitive action ($action) was detected on your account. If this wasn't you, please reset your password.",
            templateId = "security_alert_v1"
        )
        
        try {
            api.sendEmail(request)
        } catch (e: Exception) {
            Log.e("EmailService", "Failed to send security alert", e)
        }
    }
}
