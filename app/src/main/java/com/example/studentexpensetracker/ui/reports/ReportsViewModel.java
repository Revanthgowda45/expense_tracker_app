package com.example.studentexpensetracker.ui.reports;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import com.example.studentexpensetracker.data.repository.TransactionRepository;
import com.example.studentexpensetracker.ui.reports.mood.MoodAnalytics;
import com.example.studentexpensetracker.ui.reports.mood.MoodInsight;
import com.example.studentexpensetracker.ui.reports.mood.MoodInsightsGenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsViewModel extends AndroidViewModel {

    private final TransactionRepository repository;

    private final LiveData<Double> totalIncome;
    private final LiveData<Double> totalExpense;
    private final MediatorLiveData<Map<String, Double>> expensesByCategory = new MediatorLiveData<>();
    private final MediatorLiveData<MoodAnalytics> moodAnalytics = new MediatorLiveData<>();
    private final MediatorLiveData<List<MoodInsight>> moodInsights = new MediatorLiveData<>();

    public ReportsViewModel(@NonNull Application application) {
        super(application);
        repository = TransactionRepository.getInstance(application);

        totalIncome = repository.getTotalIncome();
        totalExpense = repository.getTotalExpense();

        LiveData<List<TransactionEntity>> allTxs = repository.getAllTransactions();

        expensesByCategory.addSource(allTxs, txs -> {
            if (txs == null || txs.isEmpty()) {
                expensesByCategory.setValue(Collections.emptyMap());
                return;
            }
            Map<String, Double> map = new HashMap<>();
            for (TransactionEntity tx : txs) {
                if ("EXPENSE".equals(tx.getType())) {
                    String cat = extractCategory(tx.getNote());
                    map.put(cat, map.getOrDefault(cat, 0.0) + tx.getAmount());
                }
            }
            expensesByCategory.setValue(map);
        });

        moodAnalytics.addSource(allTxs, txs -> {
            if (txs == null) {
                moodAnalytics.setValue(MoodAnalytics.EMPTY);
            } else {
                moodAnalytics.setValue(MoodInsightsGenerator.analyze(txs));
            }
        });

        moodInsights.addSource(allTxs, txs -> {
            if (txs == null) {
                moodInsights.setValue(Collections.emptyList());
            } else {
                moodInsights.setValue(MoodInsightsGenerator.generate(txs));
            }
        });
    }

    private String extractCategory(String note) {
        if (note == null || note.trim().isEmpty()) return "Other Expense";
        int colonIdx = note.indexOf(':');
        if (colonIdx > 0) {
            String cat = note.substring(0, colonIdx).trim();
            if (!cat.isEmpty()) return cat;
        }
        return note.trim();
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<Map<String, Double>> getExpensesByCategory() {
        return expensesByCategory;
    }

    public LiveData<MoodAnalytics> getMoodAnalytics() {
        return moodAnalytics;
    }

    public LiveData<List<MoodInsight>> getMoodInsights() {
        return moodInsights;
    }
}
