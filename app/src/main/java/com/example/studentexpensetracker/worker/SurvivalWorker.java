package com.example.studentexpensetracker.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository;
import com.example.studentexpensetracker.data.repository.TransactionRepository;
import com.example.studentexpensetracker.domain.SurvivalCalculator;
import com.example.studentexpensetracker.domain.SurvivalStats;

import java.util.Calendar;

public class SurvivalWorker extends Worker {

    public SurvivalWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        UserPreferencesRepository prefs = UserPreferencesRepository.getInstance(context);
        TransactionRepository txRepo = TransactionRepository.getInstance(context);

        if (!prefs.isNotificationsEnabled()) {
            return Result.success();
        }

        long lastNotified = prefs.getLastSurvivalNotificationTime();
        long oneDayMillis = 24L * 60 * 60 * 1000;
        if (System.currentTimeMillis() - lastNotified < oneDayMillis) {
            return Result.success();
        }

        int allowanceDay = prefs.getAllowanceDay();
        Double totalIncomeObj = txRepo.getTotalIncomeSync();
        Double totalExpenseObj = txRepo.getTotalExpenseSync();
        double totalIncome = totalIncomeObj != null ? totalIncomeObj : 0.0;
        double totalExpense = totalExpenseObj != null ? totalExpenseObj : 0.0;
        double currentBalance = totalIncome - totalExpense;

        int analysisDays = prefs.getSurvivalAnalysisDays();
        long timeLimitMillis = System.currentTimeMillis() - (analysisDays * 24L * 60 * 60 * 1000);
        Double recentExpObj = txRepo.getExpenseSumSinceSync(timeLimitMillis);
        double recentExpenses = recentExpObj != null ? recentExpObj : 0.0;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Double todaySpentObj = txRepo.getExpenseSumSinceSync(cal.getTimeInMillis());
        double todaySpent = todaySpentObj != null ? todaySpentObj : 0.0;

        SurvivalStats stats = SurvivalCalculator.calculate(
                currentBalance,
                recentExpenses,
                analysisDays,
                allowanceDay,
                todaySpent,
                System.currentTimeMillis()
        );

        boolean notificationSent = false;
        if (stats.getCurrentBalance() <= 0) {
            String msg = stats.getDaysUntilNextIncome() == 0
                    ? "Your balance is zero. Today is your allowance day!"
                    : "Your balance is zero or negative. Allowance arrives in " + stats.getDaysUntilNextIncome() + " days.";
            sendDangerNotification("Out of Money Alert! 🚨", msg);
            notificationSent = true;
        } else if (stats.getDaysLeft() < stats.getDaysUntilNextIncome() && stats.hasSufficientData()) {
            String msg = "You can only last " + stats.getDaysLeft() + " more days at your current rate, but next income is in "
                    + stats.getDaysUntilNextIncome() + " days. Try spending ≤ ₹" + ((int) stats.getSafeDailyLimit()) + "/day.";
            sendDangerNotification("Budget Running Out Early! 🚨", msg);
            notificationSent = true;
        }

        if (notificationSent) {
            prefs.setLastSurvivalNotificationTime(System.currentTimeMillis());
        }

        return Result.success();
    }

    private void sendDangerNotification(String title, String message) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "survival_alerts";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Budget Survival Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Alerts when you are spending too fast");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(1001, notification.build());
        }
    }
}
