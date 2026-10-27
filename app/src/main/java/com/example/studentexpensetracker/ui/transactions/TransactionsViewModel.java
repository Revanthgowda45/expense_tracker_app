package com.example.studentexpensetracker.ui.transactions;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import com.example.studentexpensetracker.data.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionsViewModel extends AndroidViewModel {

    private final TransactionRepository repository;
    private final LiveData<List<TransactionEntity>> allTransactions;

    private final MutableLiveData<String> filterType = new MutableLiveData<>("ALL");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<TransactionEntity>> filteredTransactions = new MediatorLiveData<>();

    public TransactionsViewModel(@NonNull Application application) {
        super(application);
        repository = TransactionRepository.getInstance(application);
        allTransactions = repository.getAllTransactions();

        filteredTransactions.addSource(allTransactions, txs -> applyFilters());
        filteredTransactions.addSource(filterType, type -> applyFilters());
        filteredTransactions.addSource(searchQuery, query -> applyFilters());
    }

    public void setFilterType(String type) {
        filterType.setValue(type);
    }

    public String getFilterType() {
        return filterType.getValue() != null ? filterType.getValue() : "ALL";
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<List<TransactionEntity>> getFilteredTransactions() {
        return filteredTransactions;
    }

    public void deleteTransaction(TransactionEntity transaction) {
        repository.deleteTransaction(transaction);
    }

    private void applyFilters() {
        List<TransactionEntity> source = allTransactions.getValue();
        if (source == null) {
            filteredTransactions.setValue(new ArrayList<>());
            return;
        }

        String type = filterType.getValue() != null ? filterType.getValue() : "ALL";
        String query = searchQuery.getValue() != null ? searchQuery.getValue().trim().toLowerCase(Locale.ROOT) : "";

        List<TransactionEntity> result = new ArrayList<>();
        for (TransactionEntity tx : source) {
            if (!"ALL".equals(type) && !type.equalsIgnoreCase(tx.getType())) {
                continue;
            }
            if (!query.isEmpty()) {
                String note = tx.getNote() != null ? tx.getNote().toLowerCase(Locale.ROOT) : "";
                String payment = tx.getPaymentMethod() != null ? tx.getPaymentMethod().toLowerCase(Locale.ROOT) : "";
                String amountStr = String.valueOf(tx.getAmount());
                if (!note.contains(query) && !payment.contains(query) && !amountStr.contains(query)) {
                    continue;
                }
            }
            result.add(tx);
        }
        filteredTransactions.setValue(result);
    }
}
