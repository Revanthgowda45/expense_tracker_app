package com.example.studentexpensetracker.ui.home;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import com.example.studentexpensetracker.data.preferences.UserPreferencesRepository;
import com.example.studentexpensetracker.data.repository.TransactionRepository;
import com.example.studentexpensetracker.domain.SurvivalCalculator;
import com.example.studentexpensetracker.domain.SurvivalStats;
import com.example.studentexpensetracker.ui.reports.mood.MoodInsightsGenerator;
import com.example.studentexpensetracker.ui.reports.mood.WeeklyMoodSummary;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepo;
    private final UserPreferencesRepository prefsRepo;

    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpense;
    private final MediatorLiveData<Double> totalBalance = new MediatorLiveData<>();
    private final MediatorLiveData<List<TransactionEntity>> recentTransactions = new MediatorLiveData<>();
    private final MediatorLiveData<SurvivalStats> survivalStats = new MediatorLiveData<>();
    private final MediatorLiveData<WeeklyMoodSummary> weeklyMoodSummary = new MediatorLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        transactionRepo = TransactionRepository.getInstance(application);
        prefsRepo = UserPreferencesRepository.getInstance(application);

        totalIncome = transactionRepo.getTotalIncome();
        totalExpense = transactionRepo.getTotalExpense();

        totalBalance.addSource(totalIncome, inc -> updateTotalBalance());
        totalBalance.addSource(totalExpense, exp -> updateTotalBalance());

        LiveData<List<TransactionEntity>> allTxs = transactionRepo.getAllTransactions();
        recentTransactions.addSource(allTxs, txList -> {
            if (txList == null || txList.isEmpty()) {
                recentTransactions.setValue(new ArrayList<>());
            } else {
                int limit = Math.min(txList.size(), 5);
                recentTransactions.setValue(new ArrayList<>(txList.subList(0, limit)));
            }
        });

        weeklyMoodSummary.addSource(allTxs, txs -> {
            weeklyMoodSummary.setValue(MoodInsightsGenerator.generateWeeklySummary(txs));
        });

        survivalStats.addSource(totalIncome, inc -> updateSurvivalStats());
        survivalStats.addSource(totalExpense, exp -> updateSurvivalStats());
        survivalStats.addSource(allTxs, txs -> updateSurvivalStats());
        survivalStats.addSource(prefsRepo.getAllowanceDayLiveData(), day -> updateSurvivalStats());
        survivalStats.addSource(prefsRepo.getSurvivalAnalysisDaysLiveData(), days -> updateSurvivalStats());
    }

    private void updateTotalBalance() {
        double inc = totalIncome.getValue() != null ? totalIncome.getValue() : 0.0;
        double exp = totalExpense.getValue() != null ? totalExpense.getValue() : 0.0;
        totalBalance.setValue(inc - exp);
    }

    private void updateSurvivalStats() {
        double inc = totalIncome.getValue() != null ? totalIncome.getValue() : 0.0;
        double exp = totalExpense.getValue() != null ? totalExpense.getValue() : 0.0;
        double balance = inc - exp;

        int allowanceDay = prefsRepo.getAllowanceDay();
        int analysisDays = prefsRepo.getSurvivalAnalysisDays();

        List<TransactionEntity> allTxs = transactionRepo.getAllTransactions().getValue();
        double recentExpenses = 0.0;
        double todaySpent = 0.0;

        long now = System.currentTimeMillis();
        long windowTime = now - (analysisDays * 24L * 60 * 60 * 1000);

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        long todayStart = todayCal.getTimeInMillis();

        if (allTxs != null) {
            for (TransactionEntity tx : allTxs) {
                if ("EXPENSE".equals(tx.getType())) {
                    if (tx.getDateTime() >= windowTime) {
                        recentExpenses += tx.getAmount();
                    }
                    if (tx.getDateTime() >= todayStart) {
                        todaySpent += tx.getAmount();
                    }
                }
            }
        }

        SurvivalStats stats = SurvivalCalculator.calculate(
                balance,
                recentExpenses,
                analysisDays,
                allowanceDay,
                todaySpent,
                now
        );
        survivalStats.setValue(stats);
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<Double> getTotalBalance() {
        return totalBalance;
    }

    public LiveData<List<TransactionEntity>> getRecentTransactions() {
        return recentTransactions;
    }

    public LiveData<SurvivalStats> getSurvivalStats() {
        return survivalStats;
    }

    public LiveData<WeeklyMoodSummary> getWeeklyMoodSummary() {
        return weeklyMoodSummary;
    }

    public LiveData<String> getUserName() {
        return prefsRepo.getUserNameLiveData();
    }

    public LiveData<Double> getMonthlyBudget() {
        return prefsRepo.getMonthlyBudgetLiveData();
    }

    public boolean isCustomBudget() {
        return prefsRepo.getCustomMonthlyBudget() != null && prefsRepo.getCustomMonthlyBudget() > 0;
    }

    public double getEffectiveMonthlyBudget() {
        Double custom = prefsRepo.getCustomMonthlyBudget();
        if (custom != null && custom > 0) return custom;
        Double inc = totalIncome.getValue();
        if (inc != null && inc > 0) return inc;
        return 10000.0;
    }

    public void setMonthlyBudget(double amount) {
        prefsRepo.setMonthlyBudget(amount);
    }

    public void resetMonthlyBudget() {
        prefsRepo.clearMonthlyBudget();
    }
}
