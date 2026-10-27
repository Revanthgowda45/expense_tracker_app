package com.example.studentexpensetracker.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UserPreferencesRepository {

    private static final String PREF_NAME = "user_preferences";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_ALLOWANCE_DAY = "allowance_day";
    private static final String KEY_MONTHLY_BUDGET = "monthly_budget";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_SURVIVAL_ANALYSIS_DAYS = "survival_analysis_days";
    private static final String KEY_LAST_SURVIVAL_NOTIFICATION_TIME = "last_survival_notification_time";

    private static volatile UserPreferencesRepository sInstance;
    private final SharedPreferences prefs;

    private final MutableLiveData<Boolean> darkModeLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userNameLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> allowanceDayLiveData = new MutableLiveData<>();
    private final MutableLiveData<Double> monthlyBudgetLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> notificationsEnabledLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> survivalAnalysisDaysLiveData = new MutableLiveData<>();

    private final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
        if (key == null) return;
        switch (key) {
            case KEY_DARK_MODE:
                darkModeLiveData.postValue(sharedPreferences.contains(KEY_DARK_MODE) ? sharedPreferences.getBoolean(KEY_DARK_MODE, false) : null);
                break;
            case KEY_USER_NAME:
                userNameLiveData.postValue(sharedPreferences.getString(KEY_USER_NAME, "Student"));
                break;
            case KEY_ALLOWANCE_DAY:
                allowanceDayLiveData.postValue(sharedPreferences.getInt(KEY_ALLOWANCE_DAY, 1));
                break;
            case KEY_MONTHLY_BUDGET:
                monthlyBudgetLiveData.postValue(getCustomMonthlyBudget());
                break;
            case KEY_NOTIFICATIONS_ENABLED:
                notificationsEnabledLiveData.postValue(sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true));
                break;
            case KEY_SURVIVAL_ANALYSIS_DAYS:
                survivalAnalysisDaysLiveData.postValue(sharedPreferences.getInt(KEY_SURVIVAL_ANALYSIS_DAYS, 14));
                break;
        }
    };

    private UserPreferencesRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(listener);

        darkModeLiveData.setValue(prefs.contains(KEY_DARK_MODE) ? prefs.getBoolean(KEY_DARK_MODE, false) : null);
        userNameLiveData.setValue(prefs.getString(KEY_USER_NAME, "Student"));
        allowanceDayLiveData.setValue(prefs.getInt(KEY_ALLOWANCE_DAY, 1));
        monthlyBudgetLiveData.setValue(getCustomMonthlyBudget());
        notificationsEnabledLiveData.setValue(prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true));
        survivalAnalysisDaysLiveData.setValue(prefs.getInt(KEY_SURVIVAL_ANALYSIS_DAYS, 14));
    }

    public static UserPreferencesRepository getInstance(Context context) {
        if (sInstance == null) {
            synchronized (UserPreferencesRepository.class) {
                if (sInstance == null) {
                    sInstance = new UserPreferencesRepository(context);
                }
            }
        }
        return sInstance;
    }

    public LiveData<Boolean> getDarkModeLiveData() {
        return darkModeLiveData;
    }

    public Boolean isDarkMode() {
        return prefs.contains(KEY_DARK_MODE) ? prefs.getBoolean(KEY_DARK_MODE, false) : null;
    }

    public void setDarkMode(Boolean enabled) {
        if (enabled == null) {
            prefs.edit().remove(KEY_DARK_MODE).apply();
        } else {
            prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
        }
    }

    public LiveData<String> getUserNameLiveData() {
        return userNameLiveData;
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Student");
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public LiveData<Integer> getAllowanceDayLiveData() {
        return allowanceDayLiveData;
    }

    public int getAllowanceDay() {
        return prefs.getInt(KEY_ALLOWANCE_DAY, 1);
    }

    public void setAllowanceDay(int day) {
        int clamped = Math.max(1, Math.min(31, day));
        prefs.edit().putInt(KEY_ALLOWANCE_DAY, clamped).apply();
    }

    public LiveData<Double> getMonthlyBudgetLiveData() {
        return monthlyBudgetLiveData;
    }

    public Double getCustomMonthlyBudget() {
        if (!prefs.contains(KEY_MONTHLY_BUDGET)) return null;
        return (double) prefs.getFloat(KEY_MONTHLY_BUDGET, 0f);
    }

    public double getMonthlyBudget() {
        Double custom = getCustomMonthlyBudget();
        return custom != null && custom > 0 ? custom : 10000.0;
    }

    public void setMonthlyBudget(double budget) {
        if (budget > 0) {
            prefs.edit().putFloat(KEY_MONTHLY_BUDGET, (float) budget).apply();
        } else {
            clearMonthlyBudget();
        }
    }

    public void clearMonthlyBudget() {
        prefs.edit().remove(KEY_MONTHLY_BUDGET).apply();
    }

    public LiveData<Boolean> getNotificationsEnabledLiveData() {
        return notificationsEnabledLiveData;
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public LiveData<Integer> getSurvivalAnalysisDaysLiveData() {
        return survivalAnalysisDaysLiveData;
    }

    public int getSurvivalAnalysisDays() {
        return prefs.getInt(KEY_SURVIVAL_ANALYSIS_DAYS, 14);
    }

    public void setSurvivalAnalysisDays(int days) {
        int val = (days == 7) ? 7 : 14;
        prefs.edit().putInt(KEY_SURVIVAL_ANALYSIS_DAYS, val).apply();
    }

    public long getLastSurvivalNotificationTime() {
        return prefs.getLong(KEY_LAST_SURVIVAL_NOTIFICATION_TIME, 0L);
    }

    public void setLastSurvivalNotificationTime(long time) {
        prefs.edit().putLong(KEY_LAST_SURVIVAL_NOTIFICATION_TIME, time).apply();
    }
}
