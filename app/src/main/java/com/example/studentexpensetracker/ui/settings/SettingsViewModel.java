package com.example.studentexpensetracker.ui.settings;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository;
import com.example.studentexpensetracker.data.repository.TransactionRepository;

public class SettingsViewModel extends AndroidViewModel {

    private final UserPreferencesRepository userPreferencesRepository;
    private final TransactionRepository transactionRepository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        userPreferencesRepository = UserPreferencesRepository.getInstance(application);
        transactionRepository = TransactionRepository.getInstance(application);
    }

    public LiveData<Boolean> getDarkMode() {
        return userPreferencesRepository.getDarkModeLiveData();
    }

    public void setDarkMode(Boolean enabled) {
        userPreferencesRepository.setDarkMode(enabled);
    }

    public LiveData<String> getUserName() {
        return userPreferencesRepository.getUserNameLiveData();
    }

    public void setUserName(String name) {
        userPreferencesRepository.setUserName(name);
    }

    public LiveData<Integer> getAllowanceDay() {
        return userPreferencesRepository.getAllowanceDayLiveData();
    }

    public void setAllowanceDay(int day) {
        userPreferencesRepository.setAllowanceDay(day);
    }

    public LiveData<Double> getMonthlyBudget() {
        return userPreferencesRepository.getMonthlyBudgetLiveData();
    }

    public boolean isCustomBudget() {
        return userPreferencesRepository.getCustomMonthlyBudget() != null && userPreferencesRepository.getCustomMonthlyBudget() > 0;
    }

    public void setMonthlyBudget(double amount) {
        userPreferencesRepository.setMonthlyBudget(amount);
    }

    public void resetMonthlyBudget() {
        userPreferencesRepository.clearMonthlyBudget();
    }

    public LiveData<Boolean> getNotificationsEnabled() {
        return userPreferencesRepository.getNotificationsEnabledLiveData();
    }

    public void setNotificationsEnabled(boolean enabled) {
        userPreferencesRepository.setNotificationsEnabled(enabled);
    }

    public LiveData<Integer> getSurvivalAnalysisDays() {
        return userPreferencesRepository.getSurvivalAnalysisDaysLiveData();
    }

    public void setSurvivalAnalysisDays(int days) {
        userPreferencesRepository.setSurvivalAnalysisDays(days);
    }

    public void clearAllData(Runnable onComplete) {
        transactionRepository.clearAllTransactions(onComplete);
    }
}
