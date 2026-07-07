package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

data class PlaywrightDayStat(
    val dayLabel: String,
    val successCount: Int,
    val failureCount: Int
)

// --- Entities ---

@Entity(tableName = "security_logs")
data class SecurityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // LOGIN, LOGOUT, TOKEN_REFRESH, API_INVOCATION, PLAYWRIGHT_EXEC, EMAIL_DISPATCH
    val message: String,
    val details: String
)

@Entity(tableName = "email_campaigns")
data class EmailCampaign(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val templateName: String,
    val subject: String,
    val body: String,
    val recipientGroup: String, // Active Users, Inactive Leads, Trial Signups
    val scheduledTime: Long,
    val status: String, // Draft, Scheduled, Sending, Sent
    val deliveredCount: Int = 0,
    val openedCount: Int = 0,
    val clickedCount: Int = 0
)

@Entity(tableName = "email_templates")
data class EmailTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val subject: String,
    val body: String, // variables: {{job_name}}, {{target_url}}, {{failure_time}}, {{log_snippet}}
    val isSystem: Boolean = false
)

@Entity(tableName = "playwright_jobs")
data class PlaywrightJob(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetUrl: String,
    val scriptType: String, // E2E Login Flow, SEO Audit, Performance Benchmarking, Page Scraper
    val status: String, // Idle, Running, Success, Failed
    val lastRunTime: Long = 0L,
    val cronSchedule: String = "Manual",
    val durationMs: Long = 0L,
    val logOutput: String = "",
    val isHighPriority: Boolean = false,
    val failureEmailTemplateId: Int? = null
)

// --- DAO ---

@Dao
interface AuthAppDao {
    // Security Logs
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllSecurityLogs(): Flow<List<SecurityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityLog(log: SecurityLog): Long

    @Query("DELETE FROM security_logs")
    suspend fun clearSecurityLogs()

    // Email Campaigns
    @Query("SELECT * FROM email_campaigns ORDER BY scheduledTime DESC")
    fun getAllEmailCampaigns(): Flow<List<EmailCampaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmailCampaign(campaign: EmailCampaign): Long

    @Update
    suspend fun updateEmailCampaign(campaign: EmailCampaign)

    @Delete
    suspend fun deleteEmailCampaign(campaign: EmailCampaign)

    @Query("DELETE FROM email_campaigns WHERE id = :id")
    suspend fun deleteEmailCampaignById(id: Int)

    // Playwright Jobs
    @Query("SELECT * FROM playwright_jobs ORDER BY id DESC")
    fun getAllPlaywrightJobs(): Flow<List<PlaywrightJob>>

    @Query("SELECT * FROM playwright_jobs WHERE id = :id")
    suspend fun getPlaywrightJobById(id: Int): PlaywrightJob?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaywrightJob(job: PlaywrightJob): Long

    @Update
    suspend fun updatePlaywrightJob(job: PlaywrightJob)

    @Delete
    suspend fun deletePlaywrightJob(job: PlaywrightJob)

    // Email Templates
    @Query("SELECT * FROM email_templates ORDER BY id DESC")
    fun getAllEmailTemplates(): Flow<List<EmailTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmailTemplate(template: EmailTemplate): Long

    @Update
    suspend fun updateEmailTemplate(template: EmailTemplate)

    @Delete
    suspend fun deleteEmailTemplate(template: EmailTemplate)

    @Query("DELETE FROM email_templates WHERE id = :id")
    suspend fun deleteEmailTemplateById(id: Int)

    @Query("SELECT * FROM email_templates WHERE id = :id")
    suspend fun getEmailTemplateById(id: Int): EmailTemplate?
}

// --- Database ---

@Database(
    entities = [SecurityLog::class, EmailCampaign::class, PlaywrightJob::class, EmailTemplate::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authAppDao(): AuthAppDao

    // Compatibility method to prevent compile errors during intermediate transitions
    fun taskDao(): AuthAppDao = authAppDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "auth_app_database"
                )
                .fallbackToDestructiveMigration() // safe for local prototype development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repository ---

class TaskRepository(private val authAppDao: AuthAppDao) {
    val allSecurityLogs: Flow<List<SecurityLog>> = authAppDao.getAllSecurityLogs()
    val allEmailCampaigns: Flow<List<EmailCampaign>> = authAppDao.getAllEmailCampaigns()
    val allPlaywrightJobs: Flow<List<PlaywrightJob>> = authAppDao.getAllPlaywrightJobs()
    val allEmailTemplates: Flow<List<EmailTemplate>> = authAppDao.getAllEmailTemplates()

    suspend fun insertLog(log: SecurityLog) = authAppDao.insertSecurityLog(log)
    suspend fun clearLogs() = authAppDao.clearSecurityLogs()

    suspend fun insertEmailCampaign(campaign: EmailCampaign) = authAppDao.insertEmailCampaign(campaign)
    suspend fun updateEmailCampaign(campaign: EmailCampaign) = authAppDao.updateEmailCampaign(campaign)
    suspend fun deleteEmailCampaign(campaign: EmailCampaign) = authAppDao.deleteEmailCampaign(campaign)
    suspend fun deleteEmailCampaignById(id: Int) = authAppDao.deleteEmailCampaignById(id)

    suspend fun getPlaywrightJobById(id: Int): PlaywrightJob? = authAppDao.getPlaywrightJobById(id)
    suspend fun insertPlaywrightJob(job: PlaywrightJob) = authAppDao.insertPlaywrightJob(job)
    suspend fun updatePlaywrightJob(job: PlaywrightJob) = authAppDao.updatePlaywrightJob(job)
    suspend fun deletePlaywrightJob(job: PlaywrightJob) = authAppDao.deletePlaywrightJob(job)

    suspend fun insertEmailTemplate(template: EmailTemplate) = authAppDao.insertEmailTemplate(template)
    suspend fun updateEmailTemplate(template: EmailTemplate) = authAppDao.updateEmailTemplate(template)
    suspend fun deleteEmailTemplate(template: EmailTemplate) = authAppDao.deleteEmailTemplate(template)
    suspend fun deleteEmailTemplateById(id: Int) = authAppDao.deleteEmailTemplateById(id)
    suspend fun getEmailTemplateById(id: Int): EmailTemplate? = authAppDao.getEmailTemplateById(id)
}
