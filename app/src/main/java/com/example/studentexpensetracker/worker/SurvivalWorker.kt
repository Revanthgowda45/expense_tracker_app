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
        val allowanceDay = preferencesRepository.allowanceDay.first()
        val totalIncome = transactionRepository.getTotalIncome().first() ?: 0.0
        val totalExpense = transactionRepository.getTotalExpense().first() ?: 0.0
        val currentBalance = totalIncome - totalExpense

        val analysisDays = 14
        val timeLimitMillis = System.currentTimeMillis() - (analysisDays * 24L * 60 * 60 * 1000)
        val recentExpenses = transactionRepository.getExpenseSumSince(timeLimitMillis).first() ?: 0.0

        val stats = SurvivalCalculator.calculate(
            currentBalance = currentBalance,
            recentExpensesSum = recentExpenses,
            daysAnalyzed = analysisDays,
            allowanceDayOfMonth = allowanceDay
        )

        if (stats.status == SurvivalStatus.DANGER && stats.hasSufficientData) {
            sendDangerNotification(stats.daysLeft, stats.daysUntilNextIncome)
        }

        return Result.success()
    }

    private fun sendDangerNotification(daysLeft: Int, daysUntilIncome: Int) {
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
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Fallback icon
            .setContentTitle("Budget Warning! \uD83D\uDEA8")
            .setContentText("You only have enough for $daysLeft days, but allowance is in $daysUntilIncome days.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
