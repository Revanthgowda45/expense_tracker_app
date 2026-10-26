package com.example.studentexpensetracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.studentexpensetracker.R
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository
import com.example.studentexpensetracker.data.repository.TransactionRepository
import com.example.studentexpensetracker.domain.SurvivalCalculator
import com.example.studentexpensetracker.domain.SurvivalStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SurvivalWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepository: TransactionRepository,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val notificationsEnabled = preferencesRepository.notificationsEnabled.first()
        if (!notificationsEnabled) {
            return Result.success()
        }

        // Rate-limit check: at most once per 24 hours
        val lastNotifiedTime = preferencesRepository.lastSurvivalNotificationTime.first()
        val oneDayMillis = 24L * 60 * 60 * 1000
        if (System.currentTimeMillis() - lastNotifiedTime < oneDayMillis) {
            return Result.success()
        }

        val allowanceDay = preferencesRepository.allowanceDay.first()
        val totalIncome = transactionRepository.getTotalIncome().first() ?: 0.0
        val totalExpense = transactionRepository.getTotalExpense().first() ?: 0.0
        val currentBalance = totalIncome - totalExpense

        val analysisDays = preferencesRepository.survivalAnalysisDays.first()
        val timeLimitMillis = System.currentTimeMillis() - (analysisDays * 24L * 60 * 60 * 1000)
        val recentExpenses = transactionRepository.getExpenseSumSince(timeLimitMillis).first() ?: 0.0

        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val todaySpent = transactionRepository.getExpenseSumSince(cal.timeInMillis).first() ?: 0.0

        val stats = SurvivalCalculator.calculate(
            currentBalance = currentBalance,
            recentExpensesSum = recentExpenses,
            daysAnalyzed = analysisDays,
            allowanceDayOfMonth = allowanceDay,
            todaySpent = todaySpent
        )

        var notificationSent = false

        if (stats.currentBalance <= 0) {
            sendDangerNotification(
                title = "Out of Money Alert! \uD83D\uDEA8",
                message = if (stats.daysUntilNextIncome == 0) "Your balance is zero. Today is your allowance day!" else "Your balance is zero or negative. Allowance arrives in ${stats.daysUntilNextIncome} days."
            )
            notificationSent = true
        } else if (stats.daysLeft < stats.daysUntilNextIncome && stats.hasSufficientData) {
            // Running out early: daysLeft < daysUntilNextIncome
            sendDangerNotification(
                title = "Budget Running Out Early! \uD83D\uDEA8",
                message = "You can only last ${stats.daysLeft} more days at your current rate, but next income is in ${stats.daysUntilNextIncome} days. Try spending ≤ ₹${stats.safeDailyLimit.toInt()}/day."
            )
            notificationSent = true
        }

        if (notificationSent) {
            preferencesRepository.setLastSurvivalNotificationTime(System.currentTimeMillis())
        }

        return Result.success()
    }

    private fun sendDangerNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "survival_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Budget Survival Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you are spending too fast"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}

