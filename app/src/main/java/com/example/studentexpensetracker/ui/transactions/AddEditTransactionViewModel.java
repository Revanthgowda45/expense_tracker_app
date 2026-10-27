package com.example.studentexpensetracker.ui.transactions;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.studentexpensetracker.data.local.AppExecutors;
import com.example.studentexpensetracker.data.local.entity.TransactionEntity;
import com.example.studentexpensetracker.data.repository.TransactionRepository;

public class AddEditTransactionViewModel extends AndroidViewModel {

    private final TransactionRepository repository;
    private final MutableLiveData<TransactionEntity> currentTransaction = new MutableLiveData<>();

    public AddEditTransactionViewModel(@NonNull Application application) {
        super(application);
        repository = TransactionRepository.getInstance(application);
    }

    public LiveData<TransactionEntity> getCurrentTransaction() {
        return currentTransaction;
    }

    public void loadTransaction(long id) {
        if (id <= 0) return;
        AppExecutors.getInstance().diskIO().execute(() -> {
            TransactionEntity entity = repository.getTransactionByIdSync(id);
            if (entity != null) {
                currentTransaction.postValue(entity);
            }
        });
    }

    public boolean saveTransaction(
            String amount,
            String type,
            String category,
            long dateMillis,
            String note,
            String mood,
            String paymentMethod,
            String receiptImagePath,
            Runnable onSuccess) {
        if (amount == null || amount.trim().isEmpty()) return false;
        double amountDouble;
        try {
            amountDouble = Double.parseDouble(amount.trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (amountDouble <= 0) return false;

        String selectedCat = (category != null && !category.trim().isEmpty())
                ? category.trim()
                : ("INCOME".equals(type) ? "Other Income" : "Other Expense");

        String finalNote = (note != null && !note.trim().isEmpty())
                ? selectedCat + ": " + note.trim()
                : selectedCat;

        long time = dateMillis > 0 ? dateMillis : System.currentTimeMillis();

        TransactionEntity entity = new TransactionEntity(
                amountDouble,
                type != null ? type : "EXPENSE",
                1L,
                finalNote,
                paymentMethod != null ? paymentMethod : "CASH",
                time,
                false,
                null,
                System.currentTimeMillis(),
                mood,
                receiptImagePath
        );

        repository.insertTransaction(entity, onSuccess);
        return true;
    }

    public boolean updateTransaction(
            long transactionId,
            String amount,
            String type,
            String category,
            long dateMillis,
            String note,
            String mood,
            String paymentMethod,
            String receiptImagePath,
            Runnable onSuccess) {
        if (amount == null || amount.trim().isEmpty()) return false;
        double amountDouble;
        try {
            amountDouble = Double.parseDouble(amount.trim());
        } catch (NumberFormatException e) {
            return false;
        }
        if (amountDouble <= 0) return false;

        String selectedCat = (category != null && !category.trim().isEmpty())
                ? category.trim()
                : ("INCOME".equals(type) ? "Other Income" : "Other Expense");

        String finalNote = (note != null && !note.trim().isEmpty())
                ? selectedCat + ": " + note.trim()
                : selectedCat;

        long time = dateMillis > 0 ? dateMillis : System.currentTimeMillis();

        TransactionEntity entity = new TransactionEntity(
                transactionId,
                amountDouble,
                type != null ? type : "EXPENSE",
                1L,
                finalNote,
                paymentMethod != null ? paymentMethod : "CASH",
                time,
                false,
                null,
                System.currentTimeMillis(),
                mood,
                receiptImagePath
        );

        repository.updateTransaction(entity, onSuccess);
        return true;
    }

    public void deleteTransaction(TransactionEntity transaction, Runnable onSuccess) {
        repository.deleteTransaction(transaction, onSuccess);
    }
}
