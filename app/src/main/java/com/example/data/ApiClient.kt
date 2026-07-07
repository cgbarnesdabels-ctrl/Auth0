package com.example.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException

/**
 * ApiClient provides a centralized, reusable networking utility.
 * It includes built-in error handling, logging, and an AuthInterceptor
 * to reactively inject Bearer tokens from the Auth0AuthProvider.
 */
object ApiClient {

    private const val BASE_URL = "https://api.example.com/" // Placeholder base URL

    /**
     * Custom Interceptor to add Authorization Bearer token to all requests.
     */
    class AuthInterceptor(private val authProvider: Auth0AuthProvider) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val token = authProvider.accessToken.value
            val originalRequest = chain.request()
            
            val authenticatedRequest = if (token.isNotEmpty()) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .header("Accept", "application/json")
                    .build()
            } else {
                originalRequest
            }

            val response = chain.proceed(authenticatedRequest)

            // Handle common authentication errors globally
            if (response.code == 401) {
                Log.e("ApiClient", "Unauthorized request (401). Session may be expired.")
                // In a real app, you might trigger a logout or token refresh here
            }

            return response
        }
    }

    /**
     * Global Error Handling Interceptor for common HTTP issues.
     */
    class ErrorInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = try {
                chain.proceed(chain.request())
            } catch (e: IOException) {
                Log.e("ApiClient", "Network failure: ${e.message}")
                throw e
            }

            if (!response.isSuccessful) {
                val errorCode = response.code
                val errorMessage = response.message
                Log.e("ApiClient", "API Error [$errorCode]: $errorMessage")
                
                // You can add logic here to throw custom exceptions based on error codes
            }

            return response
        }
    }

    /**
     * Create a pre-configured OkHttpClient.
     */
    fun createOkHttpClient(authProvider: Auth0AuthProvider): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authProvider))
            .addInterceptor(ErrorInterceptor())
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Create a Retrofit instance for a specific service interface.
     */
    fun <T> createService(serviceClass: Class<T>, authProvider: Auth0AuthProvider): T {
        val client = createOkHttpClient(authProvider)
        
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(serviceClass)
    }
}

/**
 * Example Service Interface to demonstrate usage.
 */
interface ExampleApiService {
    @retrofit2.http.GET("user/profile")
    suspend fun getUserProfile(): retrofit2.Response<UserProfileResponse>
}

data class UserProfileResponse(
    val id: String,
    val email: String,
    val name: String,
    val picture: String? = null
)
