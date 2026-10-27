package com.example.studentexpensetracker;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository;
import com.example.studentexpensetracker.worker.SurvivalWorker;

import java.util.concurrent.TimeUnit;

public class ExpenseTrackerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UserPreferencesRepository prefs = UserPreferencesRepository.getInstance(this);
        Boolean isDark = prefs.isDarkMode();
        if (isDark == null) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        } else if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        PeriodicWorkRequest dailyWorkRequest = new PeriodicWorkRequest.Builder(
                SurvivalWorker.class,
                1,
                TimeUnit.DAYS
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "SurvivalDailyCheck",
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
        );
    }
}
